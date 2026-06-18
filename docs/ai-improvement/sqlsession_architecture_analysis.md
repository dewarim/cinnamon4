# SqlSession Architecture Analysis: Servlets, Background Threads & Logging

**Date:** 2026-04-28  
**Scope:** All SQL session usage patterns across servlets, background services, and logging

---

## Executive Summary

The codebase has **three distinct session usage patterns** that evolved independently, creating inconsistency and potential for session interlocking:

| Context | Session Source | Lifecycle | Commit Strategy |
|---------|---------------|-----------|-----------------|
| **Servlets** (via filters) | `ThreadLocalSqlSession` | Per-request (filter opens/closes) | Filter commits/rolls back |
| **Background threads** (Index, Tika, Deletion) | `CinnamonServer.getSqlSession()` | Per-operation (try-with-resources) | Manual commit per operation |
| **Logging** (AccessLog) | `CinnamonServer.getAccessLogSession()` | Per-operation (try-with-resources) | Manual commit per insert |

**Key Problems:**
1. Background threads use a different session factory path than servlets
2. DAOs have dual-mode `getSqlSession()` (ThreadLocal fallback vs injected), creating confusion
3. `DeletionTask` runs in a background thread but uses `ThreadLocalSqlSession` directly
4. No clear separation between "servlet DAOs" and "background DAOs"

---

## Current Architecture

### Pattern 1: Servlets (DbSessionFilter)

```
Request → DbSessionFilter
            ├─ ThreadLocalSqlSession.refreshSession()  // Opens fresh SqlSession
            ├─ chain.doFilter() → Servlet executes
            │   └─ DAOs call getSqlSession() → ThreadLocalSqlSession.getSqlSession()
            ├─ Check TransactionStatus
            ├─ Commit or Rollback
            └─ Cleanup (RequestScope.clearThreadLocal())
```

**Session Factory:** `ThreadLocalSqlSession.dbSessionFactory` (main DB)  
**Lifecycle:** Filter-managed  
**Commit:** Filter decides  

### Pattern 2: Background Services (IndexService, TikaService)

```
IndexService.run() loop:
    ├─ try (SqlSession s = CinnamonServer.getSqlSession())  // Opens new session directly
    │   ├─ new IndexJobDao(sqlSession)  // DAO gets explicit session
    │   ├─ Read index jobs
    │   └─ (session auto-closes via try-with-resources)
    ├─ Process index jobs (in thread pool!)
    ├─ try (SqlSession s = CinnamonServer.getSqlSession())  // Another new session
    │   ├─ Update job status
    │   ├─ sqlSession.commit()  // Manual commit
    │   └─ (session auto-closes)
    └─ Sleep
```

**Session Factory:** `CinnamonServer.getSqlSession()` → `dbSessionFactory.getSession()`  
**Lifecycle:** Try-with-resources  
**Commit:** Manual per block  

### Pattern 3: DeletionTask (Intentional Post-Commit Design, but Session Handling Needs Fix)

**Design Intent:** Files must only be deleted from disk AFTER the database transaction has confirmed the OSD deletion. The `Deletion` tracking rows act as a safety net:
1. Servlet creates `Deletion` rows (OSD id + content path)
2. `DbSessionFilter` commits the transaction (OSD deleted, Deletion rows created)
3. After commit, filter reads pending deletions and submits `DeletionTask`
4. `DeletionTask` deletes files from disk
5. On success: removes `Deletion` tracking rows from DB
6. On failure: marks `Deletion` as `deleteFailed` (files preserved, can retry later)

This design correctly prevents file loss on DB rollback — if the DB commit in step 2 fails, no files are deleted.

**The actual flow in DbSessionFilter:**
```
try (SqlSession sqlSession = ThreadLocalSqlSession.refreshSession()) {
    chain.doFilter(request, response);        // Servlet creates Deletion rows
    sqlSession.commit();                       // ✅ DB commit - OSDs confirmed deleted
    
    // Post-commit: safe to delete files now
    List<Deletion> deletions = deletionDao.listPendingDeletions();   // ⚠️ uses filter's session
    executorService.submit(new DeletionTask(deletions, ...)).get();  // ⚠️ runs on POOL thread
    
    sqlSession.close();
}
```

**Session problem:**
```
DeletionTask.run() (submitted via executorService → runs on POOL thread):
    ├─ new DeletionDao()  // Uses ThreadLocal ⚠️
    │   └─ getSqlSession() → ThreadLocal of the POOL thread (not the filter thread!)
    │       → Pool thread has its own ThreadLocal → gets a session from initialValue()
    │       → This is a DIFFERENT session than the filter's
    ├─ Delete files from disk
    ├─ deletionDao.update(...) or deletionDao.delete(...)  // Uses pool thread's session
    ├─ ThreadLocalSqlSession.getSqlSession().commit()      // Commits pool thread's session
    └─ Session never closed → connection leak on pool thread
```

The post-commit design is correct and should be preserved. Only the session management within `DeletionTask` needs fixing.

### Pattern 4: Logging (AccessLogDao)

```
AccessLogDao.insert():
    ├─ try (SqlSession s = CinnamonServer.getAccessLogSession())  // Separate factory
    │   ├─ Insert log entry
    │   ├─ sqlSession.commit()
    │   └─ (auto-close)
```

**Session Factory:** `dbLoggingSessionFactory` (separate DB config)  
**Lifecycle:** Try-with-resources  
**Commit:** Manual per operation  

---

## Identified Problems

### Problem 1: DeletionTask Session on Pool Thread ⚠️ HIGH

`DeletionTask` is submitted via `CinnamonServer.executorService` (a `ThreadPoolExecutor`) and runs on a **pool thread**. The filter thread blocks via `.get()` until the task completes. The design intent is correct: delete files only after DB commit. But the session handling is problematic:

```java
public class DeletionTask implements Runnable {
    @Override
    public void run() {
        DeletionDao deletionDao = new DeletionDao();  // ❌ Uses ThreadLocal of POOL thread
        // ... operations ...
        ThreadLocalSqlSession.getSqlSession().commit();  // ❌ Commits pool thread's session
    }
}
```

**What actually happens:**
- `ThreadLocalSqlSession` has an `initialValue()` that creates a new session via `dbSessionFactory.openSession()`
- So the pool thread gets its own fresh session (not the filter's session)
- This session is created lazily, committed manually, but **never closed**
- On the next reuse of this pool thread, the ThreadLocal still holds the old session

**Risks:**
- Session/connection leak: pool thread accumulates unclosed sessions
- On thread reuse, stale session from previous DeletionTask
- No rollback on exception in the catch block
- The `deletionDao` field on `DbSessionFilter` (line 32) also uses ThreadLocal — it works because it's called on the filter thread, not the pool thread

**Note on design intent:**  
The post-commit file deletion design is correct and important:
1. DB transaction commits (OSD rows deleted, Deletion tracking rows created)
2. Only THEN delete files from disk
3. If file deletion fails, Deletion rows remain as "retry" markers
4. This prevents file loss on DB rollback

Only the session management within `DeletionTask` needs fixing — not the overall flow.

### Problem 2: Dual-Mode DAOs Create Confusion

Several DAOs (OsdDao, IndexJobDao, FormatDao, OsdMetaDao) have two modes:

```java
public class OsdDao implements CrudDao<ObjectSystemData> {
    private SqlSession sqlSession;  // Optional injected session
    
    public OsdDao() { }                         // Mode 1: ThreadLocal
    public OsdDao(SqlSession sqlSession) {      // Mode 2: Explicit session
        this.sqlSession = sqlSession;
    }
    
    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();  // Fallback to ThreadLocal
        }
        return sqlSession;
    }
}
```

**Risks:**
- Same DAO class behaves differently depending on constructor used
- In background threads, using no-arg constructor falls back to ThreadLocal → wrong session or null
- Once `sqlSession` is set (either injected or from ThreadLocal), it's cached forever in the DAO instance
- If DAO is stored as a field (static or instance), it caches a potentially closed session

### Problem 3: IndexService Internal Thread Pool

`IndexService.executeJobs()` creates its own thread pool for parallel indexing:

```java
private void executeJobs(List<Runnable> jobs) {
    try (ExecutorService executorService = Executors.newFixedThreadPool(...)) {
        jobs.forEach(executorService::submit);
        executorService.shutdown();
        executorService.awaitTermination(...);
    }
}
```

These inner threads run indexing tasks that don't need SQL sessions (they work on in-memory Lucene documents). But the surrounding code on the IndexService thread uses explicit sessions correctly via try-with-resources. **This is currently safe** because the inner threads only modify the Lucene IndexWriter, not the database.

### Problem 4: SearchService Mixed Session Patterns

`SearchService.doSearch()` is called from servlet context (via DbSessionFilter) but creates plain DAOs without sessions:

```java
public SearchResult doSearch(...) {
    // ...
    if (config.isVerifySearchResults()) {
        OsdDao osdDao = new OsdDao();  // ❌ Uses ThreadLocal - works from servlet, 
                                        //    breaks from background thread
        IndexJobDao indexJobDao = new IndexJobDao();  // ❌ Same issue
    }
}
```

**Risk:** If `SearchService` is ever called from a non-servlet context (e.g., background job), these DAOs will fail or use wrong sessions.

### Problem 5: No Session Cleanup in Background Threads

Servlet sessions get cleaned up by `DbSessionFilter`:
```java
finally {
    RequestScope.clearThreadLocal();
}
```

Background threads (IndexService, TikaService) manage sessions via try-with-resources but:
- `DeletionTask` never cleans up
- Executor pool threads accumulate ThreadLocal state
- No guarantee of cleanup on thread reuse

---

## Recommended Architecture

### Principle: Explicit Session Management Everywhere

Replace the dual-mode pattern with clear, explicit session management:

**Rule 1:** Servlets use ThreadLocal sessions (already managed by DbSessionFilter)  
**Rule 2:** Background threads ALWAYS use explicit try-with-resources sessions  
**Rule 3:** DAOs that need to work in both contexts get session via parameter, never fallback  
**Rule 4:** Logging uses its own session factory (already correct)

### Step 1: Fix DeletionTask (HIGH)

The post-commit design is correct and must be preserved. Only the session handling needs fixing.

**Design to preserve:**
1. `DbSessionFilter` commits DB transaction (OSD deleted, Deletion rows created)
2. `DeletionTask` runs post-commit → deletes files from disk
3. On success: removes Deletion tracking rows
4. On failure: marks Deletion as failed (files preserved for retry)

**Before (session leak on pool thread):**
```java
public class DeletionTask implements Runnable {
    @Override
    public void run() {
        // ...
        DeletionDao deletionDao = new DeletionDao();        // ❌ ThreadLocal on pool thread
        // ... delete files ...
        deletionDao.delete(...);
        ThreadLocalSqlSession.getSqlSession().commit();      // ❌ Never closed
    }
}
```

**After (explicit session, properly closed):**
```java
public class DeletionTask implements Runnable {
    @Override
    public void run() {
        try {
            boolean hasLock = lock.tryLock();
            if (!hasLock) return;
            
            // ✅ Own session for post-commit cleanup, independent of filter's session
            try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
                DeletionDao    deletionDao    = new DeletionDao(sqlSession);
                ContentProvider contentProvider = contentProviderService.getContentProvider(
                    DefaultContentProvider.FILE_SYSTEM.name());
                int successfulDeletions = 0;
                
                for (Deletion deletion : deletions) {
                    ContentMetadataLight metadataLight = new ContentMetadataLight();
                    metadataLight.setContentPath(deletion.getContentPath());
                    if (contentProvider.deleteContent(metadataLight)) {
                        successfulDeletions++;
                        deletion.setDeleted(true);
                    } else {
                        deletion.setDeleteFailed(true);
                        deletionDao.update(List.of(deletion));  // ✅ Uses explicit session
                    }
                }
                
                deletionDao.delete(
                    deletions.stream()
                        .filter(Deletion::isDeleted)
                        .map(Deletion::getId)
                        .collect(Collectors.toList())
                );
                sqlSession.commit();  // ✅ Explicit commit on own session
                CinnamonServer.cinnamonStats.getDeletions().addAndGet(successfulDeletions);
            }  // ✅ Session auto-closed via try-with-resources
        } catch (Exception e) {
            log.warn("Failed to delete content: ", e);
            // Files NOT deleted → Deletion rows remain → can retry later ✅
        } finally {
            lock.unlock();
        }
    }
}
```

**Why this preserves the safety guarantee:**
- Files are only deleted after the main DB commit (enforced by DbSessionFilter flow)
- DeletionTask gets its own session for the cleanup bookkeeping
- If the cleanup session fails, files may be deleted but Deletion rows remain → 
  next attempt will see `deleteFailed=true` and can handle accordingly
- Session properly closed via try-with-resources → no connection leak

**Note:** `DeletionDao` needs to support the `DeletionDao(SqlSession)` constructor (like `OsdDao` already does). If it doesn't, add it.

### Step 2: Standardize DAO Session Pattern

**Current (Confusing dual-mode):**
```java
public class OsdDao implements CrudDao<ObjectSystemData> {
    private SqlSession sqlSession;
    
    public OsdDao() { }
    public OsdDao(SqlSession sqlSession) { this.sqlSession = sqlSession; }
    
    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();  // ❌ Fallback
        }
        return sqlSession;
    }
}
```

**Recommended Option A: Keep Dual-Mode but Document Clearly**
```java
public class OsdDao implements CrudDao<ObjectSystemData> {
    private SqlSession sqlSession;
    
    /**
     * Creates a DAO that uses the servlet request's ThreadLocal session.
     * ONLY use this from servlet context (where DbSessionFilter is active).
     */
    public OsdDao() { }
    
    /**
     * Creates a DAO with an explicit session.
     * Use this from background threads (IndexService, TikaService, DeletionTask).
     */
    public OsdDao(SqlSession sqlSession) { 
        this.sqlSession = sqlSession; 
    }
    
    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }
}
```

**Recommended Option B: Enforce Explicit Sessions (SAFER)**
```java
public class OsdDao implements CrudDao<ObjectSystemData> {
    private final SqlSession sqlSession;
    
    /**
     * Creates a DAO that uses the servlet request's ThreadLocal session.
     */
    public OsdDao() { 
        this.sqlSession = ThreadLocalSqlSession.getSqlSession();
    }
    
    /**
     * Creates a DAO with an explicit session for background threads.
     */
    public OsdDao(SqlSession sqlSession) { 
        this.sqlSession = Objects.requireNonNull(sqlSession, "sqlSession must not be null");
    }
    
    @Override
    public SqlSession getSqlSession() {
        return sqlSession;  // ✅ Always returns the session set at construction time
    }
}
```

**Note:** Option B makes the no-arg constructor capture the ThreadLocal session at construction time instead of lazily. This is clearer but means the DAO must be created AFTER DbSessionFilter has run (which is already the case in servlets).

### Step 3: Standardize Background Thread Session Pattern

All background services already mostly follow this pattern. Standardize it:

```java
// ✅ CORRECT pattern for background threads (already used by IndexService & TikaService):
try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
    OsdDao osdDao = new OsdDao(sqlSession);
    // ... use DAO ...
    sqlSession.commit();
}  // session auto-closed
```

**Apply this consistently to:**
- ✅ IndexService (already correct)
- ✅ TikaService (already correct)
- ❌ DeletionTask (needs fix - see Step 1)
- ❌ SearchService.verifySearchResults (needs fix)

### Step 4: Fix SearchService.verifySearchResults

**Before:**
```java
if (config.isVerifySearchResults()) {
    OsdDao osdDao = new OsdDao();  // ❌ ThreadLocal - only works from servlet
    IndexJobDao indexJobDao = new IndexJobDao();  // ❌ Same
}
```

**After:**
```java
if (config.isVerifySearchResults()) {
    // SearchService.doSearch is called from servlet context,
    // so ThreadLocal session is available via DbSessionFilter
    OsdDao osdDao = new OsdDao();  // ✅ OK in servlet context
    // ... BUT if this is ever called from background, it would break.
    // Consider: pass SqlSession as parameter to doSearch() for safety
}
```

**Or safer approach - pass session explicitly:**
```java
// In SearchServlet:
searchService.doSearch(query, searchType, user, ThreadLocalSqlSession.getSqlSession());

// In SearchService:
public SearchResult doSearch(String xmlQuery, SearchType searchType, UserAccount user, 
                             SqlSession sqlSession) {
    // ...
    if (config.isVerifySearchResults()) {
        OsdDao osdDao = new OsdDao(sqlSession);  // ✅ Explicit
    }
}
```

### Step 5: Add Session Context Documentation to CrudDao

```java
public interface CrudDao<T extends Identifiable> {
    
    /**
     * Returns the SqlSession for this DAO.
     * 
     * <p><strong>Session Sources:</strong></p>
     * <ul>
     *   <li><strong>Servlet context:</strong> Returns ThreadLocal session managed by DbSessionFilter</li>
     *   <li><strong>Background threads:</strong> Must be injected via constructor</li>
     * </ul>
     * 
     * <p><strong>Important:</strong> Background threads (IndexService, TikaService, DeletionTask)
     * MUST use explicit sessions via try-with-resources and manual commit/rollback.
     * Never rely on ThreadLocal sessions in background threads.</p>
     */
    default SqlSession getSqlSession() {
        return ThreadLocalSqlSession.getSqlSession();
    }
}
```

---

## Session Lifecycle Comparison

### Servlet Request Lifecycle (Correct ✅)

```
Thread: jetty-thread-42
───────────────────────────────────────────────────────

DbSessionFilter.doFilter()
  ├─ SqlSession opened via refreshSession()
  ├─ chain.doFilter()
  │   ├─ AuthenticationFilter: validates ticket
  │   ├─ RequestResponseFilter: wraps request/response
  │   ├─ ChangeTriggerFilter: handles triggers
  │   └─ Servlet.doPost()
  │       ├─ DAO calls use ThreadLocal session
  │       ├─ All operations share ONE session
  │       └─ Returns to filter chain
  ├─ TransactionStatus check
  ├─ Commit (success) or Rollback (failure)
  ├─ Post-commit: submit DeletionTask, wait for index
  └─ RequestScope.clearThreadLocal()

Session closed via try-with-resources ✅
```

### Background Thread Lifecycle (IndexService - Correct ✅)

```
Thread: Index-Service
─────────────────────────────────────────────────

IndexService.run() loop:
  ├─ try (SqlSession s1 = CinnamonServer.getSqlSession())
  │   ├─ Read index jobs
  │   └─ s1 auto-closes (no commit needed - read only)
  ├─ Process jobs (Lucene operations, no DB)
  ├─ try (SqlSession s2 = CinnamonServer.getSqlSession())
  │   ├─ Update job status
  │   ├─ s2.commit()
  │   └─ s2 auto-closes
  ├─ try (SqlSession s3 = CinnamonServer.getSqlSession())
  │   ├─ Delete completed jobs
  │   ├─ s3.commit()
  │   └─ s3 auto-closes
  └─ Thread.sleep(configuredDelay)

Each operation has its own session ✅
```

### DeletionTask Lifecycle (Session Leak ⚠️)

**Design Intent (Correct):**
- Runs AFTER DbSessionFilter has committed → files only deleted when DB deletion is confirmed
- Deletion tracking rows act as safety net against file loss

**Session Issue:**
```
Thread: pool-thread-N (from executorService)
───────────────────────────────────────────────

DeletionTask.run():
  ├─ DeletionDao dao = new DeletionDao()  // Uses ThreadLocal of POOL thread ⚠️
  │   └─ getSqlSession() → ThreadLocal.initialValue()
  │       → Creates new session (pool thread has no filter-managed session)
  ├─ Delete files from disk  ✅ (correct: DB commit already happened)
  ├─ dao.update/delete(...)  // Update/remove Deletion tracking rows
  ├─ ThreadLocalSqlSession.getSqlSession().commit()  // Commits pool thread's session
  └─ Session NEVER closed → connection leak on pool thread ⚠️

On next reuse of pool-thread-N: stale ThreadLocal session remains ⚠️
```

**Fix:** Use `CinnamonServer.getSqlSession()` with try-with-resources (see Step 1).

---

## Implementation Priority

### Priority 1: Fix DeletionTask (HIGH - Session Leak)
- Use `CinnamonServer.getSqlSession()` with try-with-resources
- Preserve post-commit design (only delete files after DB commit)
- Add `DeletionDao(SqlSession)` constructor if missing
- Add proper rollback on error
- Estimated effort: 30 minutes

### Priority 2: Document DAO Dual-Mode Pattern (HIGH)
- Add JavaDoc to `CrudDao.getSqlSession()`
- Document which DAOs support explicit sessions
- Document when to use which constructor
- Estimated effort: 2 hours

### Priority 3: Fix SearchService (MEDIUM)
- Pass explicit session or document servlet-only constraint
- Estimated effort: 1 hour

### Priority 4: Consider Removing ThreadLocal Fallback (LOW - Future)
- Make all DAOs require explicit session in constructor
- Servlet code passes `ThreadLocalSqlSession.getSqlSession()` explicitly
- Background code passes `CinnamonServer.getSqlSession()`
- This is a larger refactoring but creates maximum clarity
- Estimated effort: 8-16 hours

---

## Summary

| Component | Current State | Issue | Fix |
|-----------|---|---|---|
| **DbSessionFilter** | ✅ Correct | None | Keep as-is |
| **IndexService** | ✅ Correct | None | Keep as-is (uses try-with-resources + explicit sessions) |
| **TikaService** | ✅ Correct | None | Keep as-is (uses try-with-resources + explicit sessions) |
| **AccessLogDao** | ✅ Correct | None | Keep as-is (uses separate factory + try-with-resources) |
| **DeletionTask** | ⚠️ Session leak | Uses ThreadLocal on pool thread; design intent (post-commit file delete) is correct | Use explicit session via try-with-resources; preserve post-commit flow |
| **SearchService** | ⚠️ Fragile | Uses no-arg DAOs (ThreadLocal) | Document or pass explicit session |
| **Servlet DAOs** | ⚠️ Redundant | `new XxxDao()` per request | Use static cached DAOs |
| **DAO dual-mode** | ⚠️ Confusing | Silent fallback to ThreadLocal | Document or enforce explicit sessions |

**Most Important Fix:** `DeletionTask` — session leak on pool thread. The post-commit design (delete files only after DB commit) is correct and must be preserved; only the session management within the task needs fixing to use `CinnamonServer.getSqlSession()` with try-with-resources.


