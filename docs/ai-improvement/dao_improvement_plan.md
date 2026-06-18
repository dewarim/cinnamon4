# Cinnamon DAO Classes - Code Quality Improvement Plan

**Date:** 2026-04-28  
**Scope:** Analysis of all DAO classes in `src/main/java/com/dewarim/cinnamon/dao/`  
**Count:** 31 DAO files identified (29 concrete DAOs + `CrudDao` interface + `MetaDao` interface)

---

## Executive Summary

This document outlines systematic improvements to enhance code quality, testability, consistency, and safety across all DAO classes. The analysis identified **7 categories** of issues ranging from a confirmed bug in the core interface to systemic architectural patterns that hinder testing and coupling:

1. **Internal DAO Instantiation / Hidden Dependencies** (HIGH) — dependencies between DAOs are invisible at the class level; value is code clarity and explicit coupling, not unit testability (which is covered by integration tests)
2. **String-Based Mapper Namespace Anti-Pattern** (HIGH)
3. **Cross-DAO Cache Coupling** (HIGH)
4. **SqlSession Injection for Background Threads** (HIGH) — `IndexService` legitimately needs explicit `SqlSession` injection because it runs outside the servlet/ThreadLocal lifecycle. However, the current pattern stores the injected session in a mutable field (stale-session risk), the `IndexService` constructor fragily uses ThreadLocal (undefined in background thread context), `findJobs()` holds one session open for an entire batch load (DB lock contention), and injection styles are inconsistent across the 7 affected DAOs.
5. **Copy-Paste Bug in Error Message** (MEDIUM)
6. **Missing Documentation and Interface Contracts** (MEDIUM)
7. **Utility Method Misplacement in Interface** (LOW)

---

## Category 1: Internal DAO Instantiation / Hidden Dependencies (HIGH)

### Problem

Several DAOs instantiate other DAOs directly in their method bodies. The collaborating DAOs are never declared as fields or constructor parameters; they are created silently at the call site.

> **Note on testability:** Most DAO logic lives in SQL, not Java. Mocking the `SqlSession` in a unit test exercises Java wiring but skips the SQL entirely — which is where bugs actually live. The existing integration tests against a real test database are the correct and sufficient testing strategy for DAOs. The value of the changes below is therefore **code clarity and explicit coupling**, not unit testability.

### Impact
- **Hidden Coupling**: Callers and readers cannot tell which secondary DAOs are consulted by a method
- **Harder Reasoning**: A change to `GroupUserDao` may unexpectedly affect `UserAccountDao` callers; the dependency is invisible
- **DIP Violation**: High-level DAOs depend on concrete low-level DAO implementations, making future structural changes harder

### Affected DAOs

#### `UserAccountDao.java` (Line 44)
**Current (WRONG):**
```java
private void addGroupInfo(UserAccount userAccount) {
    var groupUserDao = new GroupUserDao();  // ❌ Hidden instantiation inside private method
    List<GroupUser> groupUsers = groupUserDao.listGroupsOfUser(userAccount.getId());
    userAccount.getGroupIds().addAll(groupUsers.stream().map(GroupUser::getGroupId).toList());
}
```

**Recommended:**
```java
public class UserAccountDao implements CrudDao<UserAccount> {

    private final GroupUserDao groupUserDao;  // ✅ Declared dependency, injectable

    public UserAccountDao() {
        this.groupUserDao = new GroupUserDao();  // ✅ Default wiring visible in constructor
    }

    public UserAccountDao(GroupUserDao groupUserDao) {  // ✅ Constructor for testing
        this.groupUserDao = groupUserDao;
    }

    private void addGroupInfo(UserAccount userAccount) {
        List<GroupUser> groupUsers = groupUserDao.listGroupsOfUser(userAccount.getId());  // ✅ Uses field
        userAccount.getGroupIds().addAll(groupUsers.stream().map(GroupUser::getGroupId).toList());
    }
}
```

#### `UserAccountDao.java` (Line 95) – Static Method Creates Instance
**Current (WRONG):**
```java
public static boolean currentUserIsSuperuser() {
    UserAccountDao accountDao = new UserAccountDao();  // ❌ Static method creates a fresh, uninjectable instance
    return accountDao.isSuperuser(RequestScope.getCurrentUser());
}
```

**Recommended:**
```java
// ✅ Remove the static helper entirely; callers should use an injected instance or 
// call isSuperuser() on an existing DAO reference:
userAccountDao.isSuperuser(RequestScope.getCurrentUser());
```
If the static convenience method must stay, it should at minimum avoid instantiating a DAO with hidden dependencies. The cleanest solution is to make `isSuperuser` accept a `SqlSession` parameter and keep the static method purely functional.

#### `AclGroupDao.java` (Line 33)
**Current (WRONG):**
```java
public void loadPermissionsIntoAclGroups(List<AclGroup> aclGroups) {
    AclGroupPermissionDao agpDao = new AclGroupPermissionDao();  // ❌ Hidden collaborator
    Map<Long, List<Long>> aclGroupToPermissionIds = agpDao.listPermissionsOfAclGroups(...);
    ...
}
```

**Recommended:**
```java
public class AclGroupDao implements CrudDao<AclGroup> {

    private final AclGroupPermissionDao aclGroupPermissionDao;  // ✅ Visible dependency

    public AclGroupDao() {
        this.aclGroupPermissionDao = new AclGroupPermissionDao();  // ✅ Default wiring
    }

    public AclGroupDao(AclGroupPermissionDao aclGroupPermissionDao) {  // ✅ Testable constructor
        this.aclGroupPermissionDao = aclGroupPermissionDao;
    }

    public void loadPermissionsIntoAclGroups(List<AclGroup> aclGroups) {
        Map<Long, List<Long>> aclGroupToPermissionIds =
            aclGroupPermissionDao.listPermissionsOfAclGroups(...);  // ✅ Uses field
        ...
    }
}
```

#### `GroupUserDao.java` (Line 33)
**Current (WRONG):**
```java
public void addUserToGroups(Long userId, List<Long> ids) {
    create(new GroupDao().getObjectsById(ids).stream()  // ❌ Anonymous collaborator
        .map(group -> new GroupUser(userId, group.getId()))
        .collect(Collectors.toList())
    );
    invalidateUserCaches(userId);
}
```

**Recommended:** Declare `GroupDao` as a constructor-injected field, following the same pattern as `AclGroupDao` above.

> **Practical scope:** The goal here is **explicit, readable dependency declaration** — not enabling unit tests that mock the database away. No-arg constructors remain, so all existing call sites continue to work unchanged. Integration tests already cover this code correctly.

---

## Category 2: String-Based Mapper Namespace Anti-Pattern (HIGH)

### Problem

`CrudDao.getTypeClassName()` returns a raw `String` value that is concatenated with a suffix to produce MyBatis mapper statement IDs (e.g., `"com.dewarim.cinnamon.model.Acl.insert"`). There is no compile-time validation of these strings. A typo in `getTypeClassName()`, a class rename, or a mapper XML mismatch will only fail at runtime.

### Impact
- **No Compile-Time Safety**: Refactoring a model class name silently breaks all its mapper calls
- **Typo Risk**: `getTypeClassName()` returning a wrong class name produces `PersistenceException` only at runtime
- **Hard to Refactor**: IDEs cannot follow string-based references to mapper XML files
- **Confirmed Bug**: The string anti-pattern already produced a copy-paste fault (see Category 5)

### Affected DAOs

All 29 concrete DAOs must implement `getTypeClassName()`. Any incorrect return value causes a silent runtime failure.

**Current pattern in `LifecycleDao.java` (Line 23) – better variant:**
```java
@Override
public String getTypeClassName() {
    return Lifecycle.class.getName();  // ✅ Uses .class.getName() – but still runtime-only
}
```

**Current pattern in `OsdMetaDao.java` (Line 65) – worse variant:**
```java
@Override
public String getTypeClassName() {
    return "com.dewarim.cinnamon.model.OsdMeta";  // ❌ Hardcoded string; class rename = silent breakage
}
```

**Recommended – enforce Class literal:**

Eliminate raw-string overrides by changing the interface contract to use `Class<T>` instead:
```java
// In CrudDao<T>:
Class<T> getTypeClass();  // ✅ Compile-time safe; rename tools update all references

default String getMapperNamespace(SqlAction action) {
    return getTypeClass().getName() + action.getSuffix();  // ✅ Same logic, type-safe input
}
```

**Implementation in each DAO:**
```java
// In LifecycleDao:
@Override
public Class<Lifecycle> getTypeClass() {
    return Lifecycle.class;  // ✅ Compiler catches wrong type; rename refactors update this
}
```

This change has no runtime behavior difference but removes the entire class of string-mismatch bugs. The old `getTypeClassName()` should be deprecated and removed after migration.

---

## Category 3: Cross-DAO Cache Coupling (HIGH)

### Problem

`GroupUserDao` directly reaches into `UserAccountDao`'s static cache by calling `UserAccountDao.clearSuperuserCache()` and independently calls `AccessFilter.reload()`. There is no central cache manager. `ChangeTriggerDao` implements its own double-checked-locking cache in a completely different style. `UserAccountDao` holds `superuserCache` as a static `ConcurrentHashMap` while `ChangeTriggerDao` uses `static volatile` fields with a lock object.

### Impact
- **Tight Coupling**: `GroupUserDao` imports and depends on `UserAccountDao`'s internal static state
- **Non-Extensible**: Adding a new cache requires editing every DAO that might need to invalidate it
- **Inconsistent Strategies**: `ChangeTriggerDao` uses opt-in `invalidateChangeTriggerCache()` while `UserAccountDao` exposes `clearSuperuserCache()` — two incompatible patterns
- **Missed Invalidations**: Future developers may add a new cache without knowing all callers that should invalidate it

### Affected DAOs

- `GroupUserDao.java` (Lines 50–51): directly calls `UserAccountDao.clearSuperuserCache()` and `AccessFilter.reload()`
- `UserAccountDao.java` (Lines 27, 89): owns `superuserCache` as a static `ConcurrentHashMap`
- `ChangeTriggerDao.java` (Lines 14–16, 56–62): owns its own double-checked-locking cache

#### `GroupUserDao.java` (Lines 46–52) – Direct Cross-DAO Coupling
**Current (WRONG):**
```java
public void deleteByGroupIds(List<Long> ids) {
    SqlSession sqlSession = getSqlSession();
    sqlSession.delete("com.dewarim.cinnamon.model.GroupUser.deleteByGroupIds", ids);
    UserAccountDao.clearSuperuserCache();  // ❌ GroupUserDao knows about UserAccountDao's internals
    AccessFilter.reload();                 // ❌ Crosses DAO boundary into security layer
}
```

**Recommended – introduce a `CacheInvalidationService`:**
```java
// New: CacheInvalidationService.java
public class CacheInvalidationService {

    /** Called when a bulk group membership change occurs (e.g., group deleted). */
    public void onGroupMembershipBulkChange() {  // ✅ Named intent
        UserAccountDao.clearSuperuserCache();
        AccessFilter.reload();
    }

    /** Called when a single user's group membership changes. */
    public void onGroupMembershipChange(Long userId) {
        UserAccountDao.invalidateSuperuserCache(userId);
        AccessFilter.reloadUser(userId);
    }
}

// In GroupUserDao (with injected service):
public class GroupUserDao implements CrudDao<GroupUser> {

    private final CacheInvalidationService cacheService;  // ✅ Named collaborator

    public GroupUserDao() { this.cacheService = new CacheInvalidationService(); }
    public GroupUserDao(CacheInvalidationService cacheService) { this.cacheService = cacheService; }

    public void deleteByGroupIds(List<Long> ids) {
        getSqlSession().delete("com.dewarim.cinnamon.model.GroupUser.deleteByGroupIds", ids);
        cacheService.onGroupMembershipBulkChange();  // ✅ No import of UserAccountDao needed
    }
}
```

---

## Category 4: SqlSession Injection for Background Threads (HIGH)

### Background: Why the Injection Pattern Exists

`IndexService` runs as a dedicated background thread (`implements Runnable`), operating entirely **outside** the servlet request lifecycle. The `DbSessionFilter` that manages per-request `ThreadLocalSqlSession` sessions never runs for the IndexService thread. The IndexService therefore opens its own `SqlSession` instances explicitly via `CinnamonServer.getSqlSession()` and passes them directly into DAOs via constructor injection:

```java
// IndexService.findJobs() — correct explicit-session pattern:
try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
    OsdDao        osdDao        = new OsdDao(sqlSession);         // ✅ Explicit injection
    FolderDao     folderDao     = new FolderDao(sqlSession);
    OsdMetaDao    osdMetaDao    = new OsdMetaDao(sqlSession);
    FolderMetaDao folderMetaDao = new FolderMetaDao(sqlSession);
    // ... load all data for this batch
}
```

The injection-capable DAOs (`OsdMetaDao`, `FolderMetaDao`, `IndexItemDao`, `IndexJobDao`, `IndexEventDao`, `OsdDao`, `FolderDao`, `RelationDao`, `FormatDao`) are precisely those needed by the IndexService. This is a legitimate architectural requirement, not an accident.

### Problem 1: Lock Contention from Long-Held Sessions

`findJobs()` opens **one session** and holds it open for the entire duration of loading the full index batch: all OSDs, folder paths, relations, formats, and metadata for potentially hundreds of objects. If any of these queries is slow (e.g., loading large metadata blobs), the session—and the underlying database connection and any read locks—is held for the full duration, blocking or delaying concurrent servlet request threads that write to the same tables.

**Current (RISK):**
```java
// IndexService.findJobs() — session held for entire batch load:
try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {  // ⚠️ Session opened here
    OsdDao     osdDao     = new OsdDao(sqlSession);
    OsdMetaDao osdMetaDao = new OsdMetaDao(sqlSession);

    // ⚠️ Session held while loading potentially 100s of OSDs...
    Map<Long, ObjectSystemData> osds = osdDao.getObjectsById(osdsToLoad, true)...;
    // ⚠️ ...then all their relations...
    List<Relation> relations = relationDao.getRelationsOrMode(...);
    // ⚠️ ...then all metadata (potentially large blobs)...
    List<Meta> metas = osdMetaDao.listMetaByObjectIds(...);
    // ⚠️ Session finally released only when all loading is complete
}
```

**Recommended — split the batch load into short, focused sessions:**
```java
// ✅ Separate session per logical fetch unit — shorter lock windows
private Map<Long, ObjectSystemData> loadOsds(List<Long> ids) {
    try (SqlSession session = CinnamonServer.getSqlSession()) {
        return new OsdDao(session).getObjectsById(ids, true)
            .stream().collect(Collectors.toMap(ObjectSystemData::getId, Function.identity()));
    }
}

private Map<Long, List<Meta>> loadOsdMetas(Set<Long> osdIds) {
    try (SqlSession session = CinnamonServer.getSqlSession()) {
        Map<Long, List<Meta>> result = new HashMap<>();
        for (Meta meta : new OsdMetaDao(session).listMetaByObjectIds(osdIds.stream().toList())) {
            result.computeIfAbsent(meta.getObjectId(), k -> new ArrayList<>()).add(meta);
        }
        return result;
    }
}
// ... one focused method per entity type
```

This reduces each lock window to only the time needed for one specific query type, allowing servlet threads to interleave between the IndexService's fetch rounds.

### Problem 2: IndexService Constructor Uses ThreadLocal — Fragile in Background Thread Context

`IndexService` constructor (line 76) calls `new IndexItemDao().list()`, which falls back to `ThreadLocalSqlSession.getSqlSession()`. At construction time, the IndexService may be initialised on a servlet thread (where a ThreadLocal session might exist by coincidence), but this is an undocumented and fragile dependency. If the initialisation ever moves to a dedicated background thread, it will silently receive `null` or throw.

**Current (FRAGILE):**
```java
public IndexService(LuceneConfig config, ContentProviderService contentProviderService) {
    // ...
    indexItems = new IndexItemDao().list();  // ❌ Uses ThreadLocal — undefined in background thread context
}
```

**Recommended:**
```java
public IndexService(LuceneConfig config, ContentProviderService contentProviderService) {
    // ...
    try (SqlSession session = CinnamonServer.getSqlSession()) {  // ✅ Explicit session — safe anywhere
        indexItems = new IndexItemDao(session).list();
    }
}
```

### Problem 3: Inconsistent Injection Style Across Background DAOs

Of the DAOs that support explicit injection, the injection mechanism is inconsistent:

| DAO | Constructor injection | Setter injection | Lazy fallback to ThreadLocal |
|---|---|---|---|
| `OsdMetaDao` | ✅ | ❌ | ✅ |
| `FolderMetaDao` | ✅ | ❌ | ✅ |
| `IndexEventDao` | ✅ (mandatory) | ❌ | ✅ (fallback) |
| `IndexJobDao` | ✅ | ✅ | ✅ |
| `IndexItemDao` | ❌ | ✅ | ✅ |

`IndexJobDao` and `IndexItemDao` use setter injection (the same field, stored mutably), which allows the injected session to be silently overwritten. `IndexEventDao` only offers constructor injection — the cleanest form but not used by the others.

Additionally, in all five DAOs the lazy-init caching pattern (`if (sqlSession == null) { sqlSession = ThreadLocal... }`) **stores the session in the instance field** as a side-effect of the first call. This means a DAO instance injected with session A will permanently remember session A even after that session is closed, causing failures if that instance is ever reused.

**Current (FRAGILE CACHING):**
```java
// Same pattern in OsdMetaDao, FolderMetaDao, IndexJobDao, IndexItemDao:
private SqlSession sqlSession;

@Override
public SqlSession getSqlSession() {
    if (sqlSession == null) {
        sqlSession = ThreadLocalSqlSession.getSqlSession();  // ❌ Caches the session permanently in the field
    }
    return sqlSession;  // ❌ May return a closed session on second call
}
```

**Recommended — `SessionAwareDao` base class with no caching:**
```java
// New: SessionAwareDao.java
/**
 * Base class for DAOs that need to operate both on request-scoped ThreadLocal sessions
 * (in servlet threads managed by DbSessionFilter) and on explicitly provided sessions
 * (in background threads such as IndexService that manage their own session lifecycle).
 *
 * <p>Always use constructor injection when calling from a background thread. The no-arg
 * constructor falls back to the ThreadLocal session provided by DbSessionFilter.</p>
 */
public abstract class SessionAwareDao {

    private final SqlSession explicitSession;  // ✅ Final — immutable, cannot be overwritten

    /** Use in servlet threads where DbSessionFilter manages the session via ThreadLocal. */
    protected SessionAwareDao() {
        this.explicitSession = null;
    }

    /** Use in background threads (e.g. IndexService) that manage their own SqlSession. */
    protected SessionAwareDao(SqlSession sqlSession) {
        this.explicitSession = Objects.requireNonNull(sqlSession, "sqlSession must not be null");
    }

    /**
     * Returns the session to use for this DAO operation.
     * Never caches the result — each call resolves the correct session at the moment of the call.
     */
    @Override
    public SqlSession getSqlSession() {
        return explicitSession != null ? explicitSession : ThreadLocalSqlSession.getSqlSession();
        // ✅ No caching: if called from servlet thread, always returns the current request's session
        // ✅ If called from background thread with explicit session, always returns that session
    }
}
```

**Usage:**
```java
public class OsdMetaDao extends SessionAwareDao implements CrudDao<Meta>, MetaDao {
    public OsdMetaDao() { super(); }                                // ✅ Servlet thread
    public OsdMetaDao(SqlSession sqlSession) { super(sqlSession); } // ✅ Background thread
    // ✅ No getSqlSession() override needed — inherited from SessionAwareDao
}

public class IndexJobDao extends SessionAwareDao {
    public IndexJobDao() { super(); }
    public IndexJobDao(SqlSession sqlSession) { super(sqlSession); }
    // ✅ Remove the setSqlSession() setter — no mutable injection allowed
}
```

### Problem 4: No Upper Bound on Batch Duration

`executeJobs()` uses `awaitTermination(config.getThreadPoolWaitInMinutes(), ...)`. If any indexing worker stalls (e.g., reading a large binary file for content indexing), the entire batch and its associated database work is delayed. During this wait, any sessions opened before `executeJobs()` for setup purposes would already be closed (with the recommended split-session approach), but the risk remains that slow Tika processing or content reads create long pauses between DB operations.

**Recommended — log and alert on slow batches:**
```java
boolean terminatedOkay = executorService.awaitTermination(config.getThreadPoolWaitInMinutes(), TimeUnit.MINUTES);
if (!terminatedOkay) {
    log.error("IndexJobs timed out after {} minutes. Consider reducing batch size (currently {}) " +
              "or increasing thread pool wait time.", config.getThreadPoolWaitInMinutes(), jobs.size());
    // ✅ Force-terminate remaining tasks to unblock the IndexService loop
    executorService.shutdownNow();
}
```

---

## Category 5: Copy-Paste Bug in Error Message (MEDIUM)

### Problem

In `CrudDao.java` line 180, the `update()` method catches a `PersistenceException` and constructs an error message that reads `"delete failed with:\n"`. The word `"delete"` is a copy-paste error from the `delete()` method directly above it. The correct message is `"update failed with:\n"`.

### Impact
- **Misleading Logs**: Operators and developers diagnosing failures will see `"delete failed"` in the context of an update operation
- **Debugging Cost**: Incorrect message may send investigations down the wrong path
- **Trust Erosion**: Inaccurate error messages reduce confidence in the logging infrastructure

### Affected Code

**`CrudDao.java` (Line 180):**
```java
// Current (BUG):
CinnamonError error = new CinnamonError(ErrorCode.DB_UPDATE_FAILED.getCode(),
    "delete failed with:\n" + convertStackTrace(e) + ...);  // ❌ Says "delete" inside update()
```

**Recommended:**
```java
// Fixed:
CinnamonError error = new CinnamonError(ErrorCode.DB_UPDATE_FAILED.getCode(),
    "update failed with:\n" + convertStackTrace(e) + ...);  // ✅ Matches the operation
```

This is a one-word change with no behavioral impact but immediate diagnostic value.

---

## Category 6: Missing Documentation and Interface Contracts (MEDIUM)

### Problem

`CrudDao` — the core interface used by all 29 concrete DAOs — has no JavaDoc. The cache lifecycle hooks (`useCache()`, `addToCache()`, `getCachedVersion()`, `removeFromCache()`) are undocumented no-ops in the interface, making it unclear that they form an opt-in caching protocol. Most DAO classes also lack class-level JavaDoc.

### Impact
- **Onboarding Friction**: New developers cannot understand the cache hook protocol without reading `ChangeTriggerDao` as a reverse-engineered example
- **Incorrect Implementations**: Without documentation, a DAO implementor may not know to override all four hooks atomically
- **Hidden Contract**: `verifyExistence()` and `ignoreNopUpdates()` delegate to server config but this is not documented on the interface methods

### Affected Code

**`CrudDao.java` (Lines 229–243) – undocumented cache hooks:**
```java
default boolean useCache(){     // ❌ No JavaDoc – what does "cache" mean here?
    return false;
}

default void addToCache(T item){  // ❌ No contract – when is this called?
}

default T getCachedVersion(Long id){  // ❌ No contract – can this return null?
    return null;
}

default void removeFromCache(Long id){  // ❌ No contract – what should implementations do?
}
```

**Recommended – add JavaDoc to all four hooks:**
```java
/**
 * Returns {@code true} if this DAO participates in instance-level caching.
 * When {@code true}, {@link #addToCache}, {@link #getCachedVersion}, and
 * {@link #removeFromCache} must all be overridden consistently.
 * Default: {@code false} (no caching).
 */
default boolean useCache() { return false; }

/**
 * Adds the given item to the DAO's local cache after a successful read.
 * Only invoked when {@link #useCache()} returns {@code true}.
 *
 * @param item the item to cache; never {@code null}
 */
default void addToCache(T item) {}

/**
 * Returns the cached version of an item by ID, or {@code null} if not cached.
 * Only invoked when {@link #useCache()} returns {@code true}.
 *
 * @param id the item ID; never {@code null}
 * @return the cached item, or {@code null} if not present in cache
 */
default T getCachedVersion(Long id) { return null; }

/**
 * Removes the cached entry for the given ID, if present.
 * Only invoked when {@link #useCache()} returns {@code true}.
 *
 * @param id the item ID to evict from local cache
 */
default void removeFromCache(Long id) {}
```

---

## Category 7: Utility Method Misplacement in Interface (LOW)

### Problem

`CrudDao` declares `convertStackTrace(Exception e)` as a `static` method directly on the interface. This is a generic utility with no relationship to data access — it belongs in a dedicated utility class.

### Impact
- **Principle Violation**: Interfaces should define contracts, not utility implementations
- **Discoverability**: Developers searching for generic utilities won't look in a DAO interface
- **Namespace Pollution**: Infrastructure concerns dilute the interface's purpose

### Affected Code

**`CrudDao.java` (Lines 222–227):**
```java
static String convertStackTrace(Exception e) {  // ❌ Generic utility placed on a DAO interface
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
}
```

**Recommended – move to `DaoUtils.java`:**
```java
// New: DaoUtils.java
public final class DaoUtils {
    private DaoUtils() {}

    /**
     * Converts the stack trace of an exception to a String for inclusion in error messages.
     */
    public static String convertStackTrace(Exception e) {  // ✅ Utility in utility class
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
```

Update both call sites in `CrudDao` (`create()` and `delete()`) to `DaoUtils.convertStackTrace(e)`.

---

## Priority Implementation Roadmap

### Phase 1: Immediate Fixes (Week 1)
Low-risk, high-value changes with zero behavioral impact:

1. Fix the `"delete failed"` copy-paste bug in `CrudDao.update()` — one word change
2. Add JavaDoc to all four cache hooks in `CrudDao` — documentation only
3. Move `convertStackTrace` to `DaoUtils` and update the two call sites in `CrudDao`

**Effort:** ~2 hours  
**Risk:** Very low — no logic changes  
**Models affected:** `CrudDao` only

### Phase 2: Fix Background-Thread SqlSession Safety (Week 2)
Eliminate lock contention risk and fragile ThreadLocal usage in IndexService:

1. Create `SessionAwareDao` abstract base class: immutable `final` explicit-session field, no session caching, clean two-constructor pattern
2. Migrate all injection-supporting DAOs (`OsdMetaDao`, `FolderMetaDao`, `IndexItemDao`, `IndexJobDao`, `IndexEventDao`) to extend it — remove all `getSqlSession()` overrides and all `setSqlSession()` setters
3. Fix `IndexService` constructor (line 76): replace `new IndexItemDao().list()` with an explicit `CinnamonServer.getSqlSession()` + constructor injection
4. Refactor `IndexService.findJobs()`: split the single long-held session into one short `try (SqlSession...)` block per entity type (OSDs, metadata, relations, formats) to reduce DB lock-hold duration
5. Add `executorService.shutdownNow()` fallback after `awaitTermination` timeout in `executeJobs()`

**Effort:** ~8 hours  
**Risk:** Low — behavior is identical; only session lifecycle scope changes  
**Tests:** Run existing IndexService integration tests; verify no lock-timeout regressions; confirm IndexService still correctly indexes after refactoring

### Phase 3: Introduce CacheInvalidationService (Week 2–3)
Decouple cross-DAO cache calls behind a named abstraction:

1. Create `CacheInvalidationService` with `onGroupMembershipChange(Long)` and `onGroupMembershipBulkChange()`
2. Inject it into `GroupUserDao` replacing the direct `UserAccountDao.clearSuperuserCache()` and `AccessFilter.reload()` calls
3. Add class-level JavaDoc to `ChangeTriggerDao` documenting its double-checked-locking cache strategy

**Effort:** ~6 hours  
**Risk:** Low — observable behavior (cache cleared + AccessFilter reloaded) is fully preserved

### Phase 4: Surface Hidden DAO Dependencies (Week 3–4)
Make inter-DAO dependencies visible at the class level for **code clarity and maintainability**, not unit testability. SQL logic is tested via integration tests against the real test database — mocking `SqlSession` in unit tests would skip the SQL entirely and provide little value.

1. Add `GroupUserDao groupUserDao` field to `UserAccountDao` with default and injectable constructors
2. Add `AclGroupPermissionDao aclGroupPermissionDao` field to `AclGroupDao`
3. Add `GroupDao groupDao` field to `GroupUserDao`
4. All existing no-arg constructor call sites (servlets, etc.) remain unchanged

**Effort:** ~4 hours  
**Risk:** Very low — purely additive; no call sites change  
**Value:** Readers can immediately see what a DAO depends on; future structural changes become easier

### Phase 5: Migrate to Class-Literal Mapper Namespace (Week 4–5)
Replace the string-based `getTypeClassName()` with a type-safe `getTypeClass()`:

1. Add `Class<T> getTypeClass()` to `CrudDao` alongside the existing `getTypeClassName()`
2. Rewrite `getMapperNamespace` to use `getTypeClass().getName()` internally
3. Migrate all 29 DAOs from `getTypeClassName()` to `getTypeClass()` one by one
4. Eliminate hardcoded string overrides in `OsdMetaDao` and `FolderMetaDao`
5. Deprecate then remove `getTypeClassName()` after full migration

**Effort:** ~10 hours  
**Risk:** Medium — touches all 29 DAOs; mitigate by migrating in batches and running integration tests after each batch

### Phase 6: Documentation Completion (Week 6)
Add class-level JavaDoc to all DAO classes:

1. Document business purpose, caching strategy (if any), and unusual mapper patterns
2. Add method-level JavaDoc to all public non-CRUD methods
3. Add a `package-info.java` explaining the DAO layer architecture, `ThreadLocalSqlSession` dependency, and the opt-in cache protocol

**Effort:** ~8 hours  
**Risk:** None

---

## Testing Strategy

### Guiding Principle: Integration Tests Over DAO Unit Tests

DAOs are thin wrappers around SQL. The actual logic — correctness, performance, transaction boundaries, constraint violations — lives in the SQL and the database schema, not in the Java layer. Mocking the `SqlSession` in a unit test would verify Java wiring but skip the SQL entirely. Since bugs in this layer are almost always SQL bugs, not Java bugs, **the existing integration test suite against a real test database is the correct and sufficient strategy for DAO testing.**

DAO-level unit tests (with mocked sessions) are therefore **not recommended** for this codebase. The test investment should go into integration test coverage instead.

### Integration Tests to Add or Strengthen

The following gaps in integration test coverage are worth addressing as part of the refactoring phases:

**Phase 2 — Background Thread SqlSession Safety:**
```java
// ✅ Start IndexService on a fresh thread (no DbSessionFilter, no ThreadLocal session)
// and assert that construction and the first indexing cycle complete without error.
// This catches the ThreadLocal fragility in the IndexService constructor.
@Test
public void testIndexService_StartsAndIndexesOnFreshThread() throws Exception {
    Thread indexThread = new Thread(indexService);
    indexThread.start();
    indexThread.join(10_000);
    assertFalse(indexThread.isAlive(), "IndexService thread should complete a cycle within 10s");
    // Assert expected documents appeared in Lucene index
}
```

```java
// ✅ Concurrent load test: servlet requests writing to OSD/metadata tables
// while IndexService is indexing the same objects.
// Catches lock-contention regressions from long-held findJobs() sessions.
@Test
public void testIndexService_NoLockTimeoutUnderConcurrentServletLoad() {
    // Run N servlet-style DB writes on T threads in parallel with IndexService
    // Assert no PersistenceException / lock timeout within reasonable time (e.g. 30s)
}
```

**Phase 5 — Mapper Namespace Migration:**
```java
// ✅ After migrating getTypeClassName() → getTypeClass(), run the full suite.
// Each DAO's CRUD operations exercise the actual mapper XML lookup — a wrong namespace
// will fail here immediately.
@Test
public void testAllDaos_BasicCrudOperations_MapperNamespacesCorrect() {
    // For each DAO: create one entity, read it back, update it, delete it.
    // If any getTypeClass() returns a wrong class, MyBatis throws PersistenceException.
}
```

**Phase 1 — Error Message Bug:**
```java
// ✅ Force an update constraint violation and check the logged/thrown error message
// does not contain "delete failed".
@Test
public void testCrudDao_UpdateFailure_ErrorMessageSaysUpdate() {
    // Insert an entity, then try to update it with a value that violates a DB constraint
    try {
        dao.update(List.of(entityWithInvalidData));
        fail("Expected FailedRequestException");
    } catch (FailedRequestException e) {
        assertThat(e.getMessage()).doesNotContain("delete failed");
        assertThat(e.getMessage()).contains("update failed");
    }
}
```

### What Unit Tests Are Still Useful For

The one area where unit tests (without a real DB) add genuine value is the **cache invalidation logic** in `ChangeTriggerDao`, `UserAccountDao`, and the new `CacheInvalidationService` — because this logic is pure Java, not SQL:

```java
// ✅ Cache behavior is pure Java logic — worth testing without DB
@Test
public void testChangeTriggerDao_CacheInvalidatedOnCreate() {
    ChangeTriggerDao dao = new ChangeTriggerDao();
    dao.listCached();                           // populate cache
    dao.create(List.of(new ChangeTrigger()));   // should invalidate
    assertFalse(ChangeTriggerDao.isCacheInitialized());
}

@Test
public void testSessionAwareDao_InjectedSessionNeverBecomesStale() {
    // ✅ Verifies SessionAwareDao uses the explicit session immutably
    SqlSession session = mock(SqlSession.class);
    OsdMetaDao dao     = new OsdMetaDao(session);

    assertSame(session, dao.getSqlSession());
    assertSame(session, dao.getSqlSession());  // Second call must NOT fall back to ThreadLocal
}
```

---

## Summary of DAO Classes & Recommended Actions

| DAO | Hidden Deps | String Mapper | Cache Coupling | Session Injection | Error Bug | No JavaDoc |
|-----|-------------|---------------|----------------|-------------------|-----------|------------|
| `CrudDao` (interface) | — | ❌ root cause | — | — | ❌ Line 180 | ❌ |
| `AclDao` | — | ❌ | — | — | — | ❌ |
| `AclGroupDao` | ❌ `AclGroupPermissionDao` | ❌ | — | — | — | ❌ |
| `AclGroupPermissionDao` | — | ❌ | — | — | — | ❌ |
| `ChangeTriggerDao` | — | ❌ | ❌ own strategy | — | — | ❌ |
| `DeletionDao` | — | ❌ | — | — | — | ❌ |
| `FolderDao` | — | ❌ | — | ❌ constructor (IndexService) | — | ❌ |
| `FolderMetaDao` | — | ❌ hardcoded string | — | ❌ constructor + stale cache | — | ❌ |
| `FolderTypeDao` | — | ❌ | — | — | — | ❌ |
| `FormatDao` | — | ❌ | — | ❌ constructor (IndexService) | — | ❌ |
| `GroupDao` | — | ❌ | — | — | — | ❌ |
| `GroupUserDao` | ❌ `GroupDao` | ❌ | ❌ calls `UserAccountDao` | — | — | ❌ |
| `IndexEventDao` | — | ❌ | — | ❌ constructor only | — | ❌ |
| `IndexItemDao` | — | ❌ | — | ❌ setter + stale cache | — | ❌ |
| `IndexJobDao` | ❌ `OsdDao` (line 118) | ❌ | — | ❌ constructor + setter + stale cache | — | ❌ |
| `LanguageDao` | — | ❌ | — | — | — | ❌ |
| `LifecycleDao` | — | ❌ | — | — | — | ✅ |
| `LifecycleStateDao` | — | ❌ | — | — | — | ❌ |
| `MetasetTypeDao` | — | ❌ | — | — | — | ❌ |
| `ObjectTypeDao` | — | ❌ | — | — | — | ❌ |
| `OsdDao` | — | ❌ | — | ❌ constructor (IndexService) | — | ❌ |
| `OsdMetaDao` | — | ❌ hardcoded string | — | ❌ constructor + stale cache | — | ❌ |
| `RelationDao` | — | ❌ | — | ❌ constructor (IndexService) | — | ❌ |
| `RelationTypeDao` | — | ❌ | — | — | — | ❌ |
| `SessionDao` | — | ❌ | — | — | — | ❌ |
| `UiLanguageDao` | — | ❌ | — | — | — | ❌ |
| `UserAccountDao` | ❌ `GroupUserDao` | ❌ | ❌ owns `superuserCache` | — | — | ❌ partial |
| `MetaDao` (interface) | — | — | — | — | — | ❌ |

**Session Injection column key:**
- `constructor (IndexService)` — has constructor injection but no stale-cache bug (session not stored in field beyond the call)
- `constructor + stale cache` — has constructor injection but the lazy-init pattern stores the session in a mutable field, risking stale-session reuse
- `constructor + setter + stale cache` — worst case: both injection styles with mutable field storage

---

## Conclusion

This improvement plan addresses **7 systemic code-quality categories** affecting architecture, testability, reliability, and maintainability. Implementation in the suggested 6-phase approach isolates risk while delivering immediate and cumulative value:

- **Phase 1** fixes a confirmed bug and removes misplaced code — zero risk, immediate gain
- **Phase 2** fixes background-thread SqlSession safety: eliminates the stale-session caching bug, the fragile ThreadLocal usage in the IndexService constructor, and the lock contention from long-held sessions in `findJobs()` — confined to IndexService and 7 affected DAOs
- **Phase 3** eliminates cross-DAO coupling behind a named abstraction — improves future extensibility
- **Phase 4** makes inter-DAO dependencies visible at the class level for code clarity; ~4h effort since unit-test scaffolding is not needed (integration tests cover DAO behaviour against the real database)
- **Phase 5** replaces the most pervasive anti-pattern with compile-time type safety
- **Phase 6** ensures knowledge transfer for all team members and future maintainers

Estimated effort: **35–45 developer hours** across all DAOs.

> **Test strategy note:** DAO correctness is verified by integration tests against a real test database — this is the right approach for SQL-heavy code. Unit tests with mocked sessions would skip the SQL and provide little value. The improvements here target code clarity, correctness (the lock bug, the error message bug), and architectural cleanliness, not unit-test coverage metrics.

