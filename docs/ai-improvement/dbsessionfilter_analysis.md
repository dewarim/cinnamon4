# DbSessionFilter - The Key to Understanding the DAO Anti-Pattern

**Date:** 2026-04-28

## Quick Summary

**Your codebase already has the perfect solution in `DbSessionFilter`!**

The apparent DAO instantiation "anti-pattern" is actually just a redundant convention masking your existing filter infrastructure. Here's what you actually need to do:

### The Fix (Very Simple)

1. **Use static cached DAOs** instead of instantiating per request
2. **Remove redundant `refreshSession()` calls** from servlet constructors
3. **That's it**—DbSessionFilter already handles everything else

---

## How DbSessionFilter Works

Your filter (at `/src/main/java/com/dewarim/cinnamon/filter/DbSessionFilter.java`) does the Heavy Lifting:

```java
public class DbSessionFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException {
        
        // 1. ✅ OPENS FRESH SESSION for this thread
        try (SqlSession sqlSession = ThreadLocalSqlSession.refreshSession()) {
            
            log.debug("DbSessionFilter: before");
            
            // 2. ✅ SERVLET EXECUTES HERE
            chain.doFilter(request, response);
            
            log.debug("DbSessionFilter: after");
            
            // 3. ✅ DECIDES: Commit or Rollback?
            if (ThreadLocalSqlSession.getTransactionStatus() == TransactionStatus.OK) {
                sqlSession.commit();  // ✅ Commit successful requests
                
                // 4. ✅ POST-COMMIT OPERATIONS
                List<Deletion> deletions = deletionDao.listPendingDeletions();
                if (deletions.size() > 0) {
                    CinnamonServer.executorService.submit(
                        new DeletionTask(deletions, contentProviderService)
                    ).get(deletionWaitPeriod, TimeUnit.SECONDS);
                }
                
                // 5. ✅ HANDLE INDEX JOBS
                List<IndexJob> indexJobs = RequestScope.getIndexJobs();
                if (indexJobs.size() > 0) {
                    searchService.waitUntilIndexed(indexJobs);
                }
                
                sqlSession.close();
            } else {
                // ✅ ROLLBACK if error occurred
                sqlSession.rollback();
            }
            
        } catch (Exception e) {
            // ✅ EXCEPTION HANDLING - Rollback on unexpected errors
            log.warn("Caught unexpected exception -> rollback:", e);
            ThreadLocalSqlSession.getSqlSession().rollback();
            RequestScope.removeIndexJobs();
            ErrorResponseGenerator.generateErrorMessage(
                (HttpServletRequest) request, 
                (HttpServletResponse) response, 
                INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER, 
                e.getMessage()
            );
        }
        finally {
            // ✅ CLEANUP ThreadLocal
            RequestScope.clearThreadLocal();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialize services from servlet context
        contentProviderService = (ContentProviderService) 
            filterConfig.getServletContext().getAttribute(CONTENT_PROVIDER_SERVICE);
        searchService = (SearchService) 
            filterConfig.getServletContext().getAttribute(SEARCH_SERVICE);
    }
}
```

## What This Means

**DbSessionFilter is Already Doing:**
1. ✅ Opening fresh `SqlSession` per request thread
2. ✅ Committing changes if successful
3. ✅ Rolling back if errors occur
4. ✅ Handling post-commit operations (deletions, index jobs)
5. ✅ Cleanup and resource management
6. ✅ Exception handling

**What Servlets Currently Still Do (REDUNDANTLY):**
```java
public OsdServlet() {
    super();
    ThreadLocalSqlSession.refreshSession();  // ❌ Filter already did this!
}

protected void doPost(...) {
    AclDao aclDao = new AclDao();  // ❌ Creates new DAO instance per request
    aclDao.list();  // Uses filter's session via ThreadLocal
}
```

## Why This Pattern Exists

The DAO instantiation pattern probably emerged when:
1. Sessions weren't managed by a filter
2. Each DAO instantiation was the trigger for session refresh
3. It became a convention

Now DbSessionFilter handles it, but the pattern remained as a vestigial convention.

---

## The Actual Problem & Solution

### Problem

**Redundancy and confusion:**
```
DbSessionFilter calls refreshSession() ← OPENS FRESH SESSION
                                    ↓
        Servlet constructor calls refreshSession() ← REDUNDANT
                                    ↓
Servlet method instantiates new DAO ← CREATES UNNECESSARY OBJECT
                                    ↓
        DAO calls getSqlSession() ← GETS FILTER'S SESSION
```

### Solution

**Stop the redundancy—use static cached DAOs:**

```java
// Before (Redundant)
@WebServlet(...)
public class AclServlet extends HttpServlet {
    
    public AclServlet() {
        super();
        ThreadLocalSqlSession.refreshSession();  // ❌ Remove this
    }
    
    protected void doPost(...) {
        AclDao aclDao = new AclDao();  // ❌ Remove this
        aclDao.list();
    }
}

// After (Clean & Clear)
@WebServlet(...)
public class AclServlet extends HttpServlet {
    
    private static final AclDao aclDao = new AclDao();  // ✅ ADD THIS
    
    // ✅ Remove the constructor entirely
    
    protected void doPost(...) {
        aclDao.list();  // ✅ Use the static instance
    }
}
```

## Why Static DAOs Work Perfectly

1. **Thread Safety**: Each thread gets its own `SqlSession` from ThreadLocal, not from the DAO
2. **Shared Session**: Multiple DAOs in the same request use the same ThreadLocal session
3. **No Allocation Overhead**: One DAO instance per servlet class (not per request)
4. **Clear Logic**: Code shows what's actually happening (using a DAO instance)
5. **Still Testable**: Can mock the static field in tests if needed

---

## Implementation: The Actual Fix

### Per Servlet (5 minutes each)

**Step 1: Add static cached DAO field**
```java
private static final AclDao aclDao = new AclDao();  // ✅ Add this
```

**Step 2: Remove DAO instantiation in methods**
```java
// Remove lines like:
// AclDao aclDao = new AclDao();
```

**Step 3: Remove redundant constructor refresh**
```java
// Remove:
// public AclServlet() {
//     super();
//     ThreadLocalSqlSession.refreshSession();
// }
```

### Affected Servlets (All 29)

Apply the above 3 steps to:
- AclServlet
- AclGroupServlet
- FolderServlet
- OsdServlet
- UserAccountServlet
- ConfigEntryServlet
- FormatServlet
- LanguageServlet
- MetasetTypeServlet
- ObjectTypeServlet
- UiLanguageServlet
- FolderTypeServlet
- LifecycleServlet
- LifecycleStateServlet
- PermissionServlet
- IndexItemServlet
- IndexServlet
- SearchServlet
- RelationServlet
- RelationTypeServlet
- ChangeTriggerServlet
- GroupServlet
- LinkServlet
- StaticServlet
- TestServlet
- ProviderClass-related servlets (if any)

### Total Effort

- **Per servlet**: ~5 minutes
- **All 29 servlets**: ~2-3 hours
- **Risk**: Minimal (consolidating existing pattern)
- **Benefit**: Clearer code, better performance, no redundancy

---

## Verification

### 1. Verify DbSessionFilter is Wired Up

Check `web.xml` or servlet initialization:
```xml
<filter>
    <filter-name>DbSessionFilter</filter-name>
    <filter-class>com.dewarim.cinnamon.filter.DbSessionFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>DbSessionFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

### 2. Check ThreadLocalSqlSession

Should be configured with `DbSessionFactory`:
```java
// Somewhere in initialization:
ThreadLocalSqlSession.setDbSessionFactory(dbSessionFactory);
```

### 3. Test One Servlet

1. Convert one servlet (e.g., `AclServlet`)
2. Run its tests
3. Verify functionality
4. Roll out to remaining servlets

---

## Summary: Why This Works

| Aspect | Current Pattern | New Pattern |
|--------|---|---|
| **Session Refresh** | Constructor calls it (redundant) | Filter already did it ✅ |
| **DAO Instance** | New per request | Static cached instance ✅ |
| **Thread Safety** | Via ThreadLocal | Via ThreadLocal ✅ |
| **Shared Session** | All DAOs use ThreadLocal | All DAOs use ThreadLocal ✅ |
| **Code Clarity** | Confused intent | Clear intent ✅ |
| **Performance** | Poor (many allocations) | Good (no unnecessary allocations) ✅ |
| **Maintenance** | Scattered pattern | Consistent pattern ✅ |

---

## Conclusion

**You don't need a new pattern—you need to simplify to what DbSessionFilter already provides.**

The DAO instantiation anti-pattern is just a redundant convention left over from before you had a proper filter. The fix is trivial:
1. Add static cached DAO fields
2. Remove instanton in methods
3. Remove redundant constructor calls
4. Done!

**DbSessionFilter is your real session manager. Let it do its job.**


