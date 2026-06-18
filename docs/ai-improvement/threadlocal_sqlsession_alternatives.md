# ThreadLocalSqlSession Pattern - Alternatives to DAO Instantiation Anti-Pattern

**Date:** 2026-04-28

## Problem Summary

Currently, DAOs are instantiated fresh every time just to trigger `ThreadLocalSqlSession.refreshSession()` in the servlet constructor:

```java
// In OsdServlet constructor:
public OsdServlet() {
    super();
    ThreadLocalSqlSession.refreshSession();  // ❌ Session is already being refreshed by DbSessionFilter!
}

// In servlet method:
AclDao aclDao = new AclDao();  // ❌ REDUNDANT - created just to trigger refreshSession() above
```

**This is REDUNDANT** because `DbSessionFilter` already calls `refreshSession()` for every request! The DAO instantiation is masking/duplicating existing functionality.

---

## The Actual Architecture

### Already in Place: DbSessionFilter

**DbSessionFilter.java** (existing code at `/src/main/java/com/dewarim/cinnamon/filter/DbSessionFilter.java`):
```java
public class DbSessionFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException {
        try (SqlSession sqlSession = ThreadLocalSqlSession.refreshSession()) {  // ✅ ALREADY HAPPENING!
            log.debug("DbSessionFilter: before");
            chain.doFilter(request, response);  // Servlet executes here
            log.debug("DbSessionFilter: after");
            
            // Transaction decision based on request outcome
            if (ThreadLocalSqlSession.getTransactionStatus() == TransactionStatus.OK) {
                sqlSession.commit();  // ✅ Commit if successful
                // Handle deletions and indexing
                List<Deletion> deletions = deletionDao.listPendingDeletions();
                // ... process deletions and index jobs
            } else {
                sqlSession.rollback();  // ✅ Rollback if error occurred
            }
        } catch (Exception e) {
            log.warn("Caught unexpected exception -> rollback:", e);
            ThreadLocalSqlSession.getSqlSession().rollback();  // ✅ Rollback on exception
            // Generate error response
        }
        finally {
            RequestScope.clearThreadLocal();  // ✅ Cleanup
        }
    }
}
```

### Key Facts

1. **Filter already calls refreshSession()**: Every request gets a fresh `SqlSession` via `DbSessionFilter`
2. **Transaction management in place**: Filter decides commit/rollback based on `TransactionStatus`
3. **Exception handling**: Filter catches exceptions and rolls back
4. **Post-commit cleanup**: Handles deletions and index jobs after successful commit
5. **Resource cleanup**: Uses try-with-resources and clears ThreadLocal

### The Redundancy

```
Current Flow:
───────────────

Request arrives
    ↓
DbSessionFilter.doFilter() called
    ├─ Calls ThreadLocalSqlSession.refreshSession()  ✅ Fresh session opens
    ├─ chain.doFilter(request, response)
    │   ↓
    │   Servlet.doPost() called
    │     ├─ Servlet constructor: new AclDao() → calls refreshSession() ❌ REDUNDANT!
    │     ├─ Servlet method: new AclDao() → calls refreshSession() ❌ REDUNDANT!
    │     └─ Uses DAOs which call ThreadLocalSqlSession.getSqlSession() ✅ Gets filter's session
    │   ↓
    │ chain returns
    ├─ Checks TransactionStatus
    ├─ Commits or rollbacks  ✅
    └─ Cleanup
```

**The Problem:** The filter already opened a fresh session AND will manage commit/rollback. The DAO instantiation in servlet constructor is completely redundant!


---

## The Solution: Remove Redundant DAO Instantiation

Since `DbSessionFilter` already handles session refresh and transaction management, the solution is simple:

**Remove the redundant `ThreadLocalSqlSession.refreshSession()` calls from servlet constructors** and replace DAO instantiation patterns with **cached, static DAO instances**.

### Approach 1: Static Cached DAOs (SIMPLEST & RECOMMENDED)

**Before:**
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {
    
    public AclServlet() {
        super();
        ThreadLocalSqlSession.refreshSession();  // ❌ Redundant - filter already does this
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclDao aclDao = new AclDao();  // ❌ Unnecessary allocation per request
        // ...
    }
}
```

**After (using static cached DAO):**
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {
    
    // ✅ Cached instance - DbSessionFilter handles session refresh
    private static final AclDao aclDao = new AclDao();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ✅ Session already refreshed by DbSessionFilter
        // ✅ Use cached DAO instance
        list(convertListRequest(cinnamonRequest, ListAclRequest.class), aclDao, cinnamonResponse);
    }
}
```

**Benefits:**
- Removes redundant `refreshSession()` calls
- Single DAO instance per servlet class (not per request)
- Eliminates unnecessary object allocation
- Relies on already-implemented `DbSessionFilter`
- No code changes needed outside servlets

### Approach 2: DaoFactory (For Future Flexibility)

Create a simple factory for centralized DAO management (if needed later):

```java
public class DaoFactory {
    private static final DaoFactory INSTANCE = new DaoFactory();
    private final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();
    
    private DaoFactory() {
        cache(AclDao.class, new AclDao());
        cache(FolderDao.class, new FolderDao());
        cache(OsdDao.class, new OsdDao());
        // ... etc - instantiate all DAOs once at startup
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getDao(Class<T> daoClass) {
        return (T) daoCache.get(daoClass);
    }
    
    public static DaoFactory getInstance() {
        return INSTANCE;
    }
    
    private <T> void cache(Class<T> clazz, T instance) {
        daoCache.put(clazz, instance);
    }
}
```

**Servlet using factory:**
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclDao aclDao = DaoFactory.getInstance().getDao(AclDao.class);  // ✅ Cached instance
        // ...
    }
}
```

**Benefits:**
- Single point for DAO management
- Enables future swapping of DAO implementations
- Still simple and straightforward
- No external dependencies

### Comparison with Current Anti-Pattern

| Aspect | Static DAO | DaoFactory | Current Pattern |
|--------|-----------|-----------|---|
| **Session Refresh** | Filter | Filter | Constructor (redundant) |
| **DAO Instances** | 1 per class | 1 per class | 1+ per request |
| **Code Clarity** | Clear | Clear | Confused intent |
| **Performance** | Optimal | Optimal | Poor |
| **Implementation** | Trivial | Simple | Current way |
| **Changes Needed** | Very minor | Minor | None (fix current) |

---

## Implementation: Remove the Anti-Pattern

### Step 1: Identify Affected Servlets
All 29 servlets have this pattern - check each for:
- DAO instantiation in methods
- Redundant `ThreadLocalSqlSession.refreshSession()` calls in constructors

### Step 2: Convert to Static Fields

**Pattern to apply to every servlet:**

```java
// Before
public class XxxServlet {
    protected void doPost(...) {
        XxxDao xxxDao = new XxxDao();
        // use xxxDao
    }
}

// After
public class XxxServlet {
    private static final XxxDao xxxDao = new XxxDao();  // ✅ Static, cached
    
    protected void doPost(...) {
        // ✅ Use cached instance - DbSessionFilter handles session
        // use xxxDao
    }
}
```

### Step 3: Remove Redundant Constructor Calls

```java
// Before
public OsdServlet() {
    super();
    ThreadLocalSqlSession.refreshSession();  // ❌ Redundant
}

// After
// ✅ Remove the redundant refreshSession() - DbSessionFilter has already done it
// Keep constructors that call super(), remove the refreshSession() call
```

### Step 4: Verify DbSessionFilter is Active

Confirm `DbSessionFilter` is registered in `web.xml` or via `@WebFilter` annotation:

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

---

## Alternative 1: Filter-Based Session Refresh (ALREADY IMPLEMENTED)

**This is not an alternative - it's already in your codebase!**

`DbSessionFilter` already:
- ✅ Calls `refreshSession()` for every request
- ✅ Manages transaction commit/rollback
- ✅ Handles exceptions properly
- ✅ Manages post-commit operations (deletions, indexing)
- ✅ Cleans up resources

The servlet constructor instantiation is redundant with what the filter already does.

---

## Alternative 2: Base Servlet Helper (BACKWARD COMPATIBLE)

### Concept
Create a base servlet that explicitly calls `refreshSession()` and provides cached DAO access. This allows gradual migration without breaking existing code.

### Implementation

**DaoAwareServlet.java:**
```java
public abstract class DaoAwareServlet extends BaseServlet {
    
    protected final <T> T getDao(Class<T> daoClass) {
        ThreadLocalSqlSession.refreshSession();  // ✅ Explicit refresh
        return DaoFactory.getInstance().getDao(daoClass);  // ✅ Cached instance
    }
}
```

**Servlet usage:**
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends DaoAwareServlet implements CruddyServlet<Acl> {  // ✅ Extends DaoAwareServlet

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ✅ getDao() refreshes session AND returns cached DAO
        AclDao aclDao = getDao(AclDao.class);
        // ...
        list(convertListRequest(cinnamonRequest, ListAclRequest.class), aclDao, cinnamonResponse);
    }
}
```

### Alternative 2: Base Servlet Helper (IF PREFERRED)

If you want a more explicit approach that centralizes DAO access, you could create a base servlet:

```java
public abstract class DaoAwareServlet extends BaseServlet {
    
    protected final <T> T getDao(Class<T> daoClass) {
        // DbSessionFilter already refreshed session, just get cached DAO
        return DaoFactory.getInstance().getDao(daoClass);
    }
}

@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends DaoAwareServlet implements CruddyServlet<Acl> {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclDao aclDao = getDao(AclDao.class);  // ✅ Explicit, cached
        // ...
    }
}
```

**Advantages:**
- Explicit DAO access pattern
- Centralized point for DAO management
- Single point to add caching logic if needed

**Disadvantages:**
- Extra layer of indirection
- Less performance benefit over static fields
- More code to maintain

---

## Alternative 3: Keep Static DAOs Simple (RECOMMENDED)

The simplest and most performant approach - just use static fields:

```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {
    
    private static final AclDao aclDao = new AclDao();  // ✅ Simple, cached, reused

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // All DAOs use the same ThreadLocal session set up by DbSessionFilter
        list(convertListRequest(cinnamonRequest, ListAclRequest.class), aclDao, cinnamonResponse);
    }
}
```

**Advantages:**
- Simplest to understand and maintain
- Best performance (no extra indirection)
- Leverages existing DbSessionFilter
- Minimal code changes

---

## Comparison Table

| Approach | Complexity | Performance | Changes | Benefits |
|----------|-----------|---|---|---|
| **Static DAOs** | Very Simple | Optimal | Minimal | Direct, clear, fast |
| **DaoFactory** | Simple | Optimal | Minor | Centralized access |
| **DaoAwareServlet** | Medium | Good | Moderate | Explicit DAO handling |
| **Current Pattern** | Complex | Poor | None | Already deployed |

---

## Final Recommendation: Static DAOs

**The simplest solution that already works with your infrastructure:**

1. Add `private static final XxxDao xxxDao = new XxxDao();` to each servlet
2. Remove DAO instantiation from methods
3. Remove redundant `ThreadLocalSqlSession.refreshSession()` calls from constructors
4. That's it! DbSessionFilter already handles the rest.

---

## Implementation: Static DAO Approach

### Changes Required Per Servlet

Find this in each servlet:
```java
public class XxxServlet {
    
    public XxxServlet() {
        super();
        ThreadLocalSqlSession.refreshSession();  // ❌ Remove this
    }
    
    protected void doPost(...) {
        XxxDao xxxDao = new XxxDao();  // ❌ Remove this
        // use xxxDao
    }
}
```

Replace with:
```java
public class XxxServlet {
    
    private static final XxxDao xxxDao = new XxxDao();  // ✅ Add this
    
    // ✅ Remove the constructor
    
    protected void doPost(...) {
        // ✅ Use xxxDao directly
    }
}
```

### Affected Servlets: All 29

Servlets using this pattern:
- AclServlet
- AclGroupServlet
- FolderServlet
- OsdServlet
- All other servlets (complete list in servlet_improvement_plan.md)

### Effort

- **Per servlet**: 5 minutes (minimal changes)
- **Total for all 29**: ~2-3 hours
- **Risk**: Very low (just consolidating existing pattern)
- **Benefit**: Better performance, clearer code, no redundant calls

---

## Summary

**Problem:** Redundant DAO instantiation masking your already-existing `DbSessionFilter` pattern

**Root Cause:** Constructor side effect pattern obscures that session refresh happens in the filter

**Solution:** Use static/cached DAOs, remove redundant instantiation

**Benefit:** Clearer code, better performance, explicit session management visible in filter chain

**Implementation Complexity:** Very low (static fields)

**Risk:** Very low (consolidating existing working pattern)


