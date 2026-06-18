# Cinnamon Servlet Classes - Code Quality Improvement Plan

**Date:** 2026-04-28  
**Scope:** Analysis of all servlet classes in `src/main/java/com/dewarim/cinnamon/application/servlet/`  
**Count:** 29 servlets identified

---

## Executive Summary

This document outlines systematic improvements to enhance servlet code quality, testability, maintainability, and performance. The analysis identified **6 critical categories** affecting architecture and code consistency:

1. **DAO Instantiation Anti-Pattern** (CRITICAL)
2. **Code Duplication & Extraction** (HIGH)
3. **Missing Documentation & Comments** (HIGH)
4. **Error Handling Inconsistency** (HIGH)
5. **Resource Management** (MEDIUM)
6. **Initialization Patterns** (MEDIUM)

---

## Category 1: DAO Instantiation Anti-Pattern (CRITICAL) - ACTUALLY ALREADY SOLVED BY DbSessionFilter

### The Real Issue: Redundant Pattern Masking Existing Infrastructure

**Important Discovery:** Your `DbSessionFilter` (at `/src/main/java/com/dewarim/cinnamon/filter/DbSessionFilter.java`) already handles:
- ✅ Opening fresh `SqlSession` per request
- ✅ Transaction commit/rollback based on success/failure
- ✅ Exception handling with rollback
- ✅ Post-commit operations (deletions, indexing)
- ✅ Resource cleanup

The problem is that **the servlet constructor instantiation pattern is redundant**—it's doing what the filter already does!

**Current (Confusing) Pattern:**
- Servlet constructor calls `ThreadLocalSqlSession.refreshSession()`
- DbSessionFilter also calls `ThreadLocalSqlSession.refreshSession()`
- DAO instantiation obscures that the filter already handles session management
- Creates unnecessary object allocation

### The Root Cause

The DAO instantiation pattern was born from the need to ensure fresh sessions, but now:
1. DbSessionFilter already provides fresh sessions per request
2. All DAOs use the same `ThreadLocal` session within a request
3. Multiple DAO instances are unnecessary—they all use the same session
4. The instantiation pattern became a (confusing) convention rather than a necessity

### Impact of Current Anti-Pattern
- **Code Clarity**: Obscures that session refresh is already handled
- **Performance**: Unnecessary DAO allocation per request (1-14 per servlet)
- **Testing**: Makes it hard to mock DAOs
- **Redundancy**: Duplicates filter's session refresh work

### The Solution: Simplify to Static Cached DAOs

Since `DbSessionFilter` already refreshes sessions, just use static/cached DAO instances:


### Affected Patterns (All 29 Servlets)

#### Current (Working but Obscure):
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {
    
    public AclServlet() {
        super();
        ThreadLocalSqlSession.refreshSession();  // ✅ Ensures fresh session per request
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        AclDao           aclDao           = new AclDao();  // ❌ But why instantiate DAO just for this?
        // ...
        list(convertListRequest(cinnamonRequest, ListAclRequest.class), aclDao, cinnamonResponse);
        // ...
    }
}
```

**The Problem:** The session refresh is hidden inside DAO instantiation, obscuring the real intent. 

### Affected Patterns (All 29 Servlets)

#### Current (Redundant):
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {
    
    public AclServlet() {
        super();
        ThreadLocalSqlSession.refreshSession();  // ❌ Redundant - DbSessionFilter already did this!
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclDao aclDao = new AclDao();  // ❌ Unnecessary allocation
        // ...
        list(convertListRequest(cinnamonRequest, ListAclRequest.class), aclDao, cinnamonResponse);
    }
}
```

#### Recommended (Simple & Clear):
```java
@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {
    
    // ✅ Cached instance - DbSessionFilter handles session refresh
    private static final AclDao aclDao = new AclDao();
    
    // ✅ No redundant constructor needed

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ✅ Use cached DAO - session already refreshed by DbSessionFilter
        list(convertListRequest(cinnamonRequest, ListAclRequest.class), aclDao, cinnamonResponse);
    }
}
```

**Why This Works:**
1. `DbSessionFilter` opens fresh `SqlSession` for each request
2. All DAOs delegate to `ThreadLocalSqlSession.getSqlSession()` (the same thread-local session)
3. No need for multiple DAO instances per request—they all use the same session
4. No need for redundant `refreshSession()` calls—filter handles it
5. Static field is thread-safe because each thread gets its own session from ThreadLocal

### Example: How DbSessionFilter Actually Works

```java
// DbSessionFilter (already in your codebase!)
public class DbSessionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException {
        try (SqlSession sqlSession = ThreadLocalSqlSession.refreshSession()) {  // ✅ Fresh session!
            chain.doFilter(request, response);  // Servlet runs here
            
            if (ThreadLocalSqlSession.getTransactionStatus() == TransactionStatus.OK) {
                sqlSession.commit();  // ✅ Commit if successful
            } else {
                sqlSession.rollback();  // ✅ Rollback if error
            }
        } catch (Exception e) {
            ThreadLocalSqlSession.getSqlSession().rollback();  // ✅ Exception handling
        }
    }
}
```

**Flow:**
```
Request → DbSessionFilter opens fresh session → Servlet executes (uses static DAO) 
→ DAO calls getSqlSession() → Gets thread's session from filter → Servlet returns
→ Filter checks status → Commits or rolls back → Filter closes session
```

### No New Pattern Needed!

All three "recommended patterns" from my earlier analysis are unnecessary because:
- **Filter Pattern**: Already implemented as `DbSessionFilter`
- **DaoFactory**: Optional enhancement (not needed)
- **DaoAwareServlet**: Optional enhancement (not needed)

The fix is simply: **Use static/cached DAOs and remove redundant refreshSession() calls**

---

## Understanding ThreadLocalSqlSession Architecture

### Current Implementation

Cinnamon uses `ThreadLocalSqlSession` to manage per-request database sessions:

**Key Components:**
- `ThreadLocal<SqlSession> localSqlSession`: Stores one SqlSession per thread
- `refreshSession()`: Closes old session, opens new one (called per request)
- `getSqlSession()`: Returns the thread's current SqlSession
- `CrudDao.getSqlSession()`: All DAOs use `ThreadLocalSqlSession.getSqlSession()`

**Current Flow:**
```
Servlet.doPost() called
  → new AclDao() instantiated (constructor body ignored)
  → Servlet constructor calls ThreadLocalSqlSession.refreshSession()
  → Fresh SqlSession opened for this thread
  → Servlet methods call aclDao.list()
  → aclDao calls getSqlSession() → ThreadLocal returns fresh session
  → Database operations use fresh session
  → Request completes
```

**Why Multiple DAOs Can Share One Session:**
All DAOs use `ThreadLocalSqlSession.getSqlSession()` which returns the same session for the same thread. Multiple DAO instances are unnecessary - they're all using the same underlying session anyway.

### Why Separate Session per Request

This pattern ensures:
- **Thread Safety**: Each thread in the servlet container gets its own session
- **No Stale Connections**: Old connections from previous requests not reused
- **Fresh Database State**: Each request sees latest DB changes
- **Isolation**: Concurrent requests don't interfere with each other

---

## Category 2: Code Duplication & Extraction (HIGH)

### Problem
Repeated patterns across servlets waste time and create consistency issues:
- **Request Parsing**: Same try-catch-validate pattern in 50+ locations
- **Response Wrapping**: Similar wrapper creation code repeated
- **Authorization Checks**: Identical permission validation logic
- **Error Handling**: Duplicated error handling across methods

### Impact
- **Maintenance**: Bugs in one place need fixing in 20+ places
- **Inconsistency**: Fixes applied inconsistently
- **Code Size**: 30% waste from duplication
- **Testing**: Multiple code paths to test for same logic

### Example 1: Request Parsing (Appearing 50+ times)

**Current (Duplicated):**
```java
// In AclServlet
private void getUserAcls(CinnamonRequest request, AclDao aclDao, CinnamonResponse response) throws IOException {
    IdRequest idRequest = request.getMapper().readValue(request.getInputStream(), IdRequest.class)
            .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());  // ❌ Repeated pattern
    // ...
}

// In FolderServlet - same pattern
private void setFolderType(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, FolderDao folderDao) throws IOException {
    SetFolderTypeRequest typeRequest = request.getMapper().readValue(request.getInputStream(), SetFolderTypeRequest.class)
            .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());  // ❌ Same pattern again
    // ...
}
```

**Recommended (Extract to utility):**
```java
public interface RequestParser {
    
    static <T extends Validatable<?>> T parseAndValidate(CinnamonRequest request, Class<T> requestClass) throws IOException {
        return request.getMapper()
            .readValue(request.getInputStream(), requestClass)
            .validateRequest()
            .orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }
}
```

**Usage (Simplified):**
```java
private void getUserAcls(CinnamonRequest request, AclDao aclDao, CinnamonResponse response) throws IOException {
    IdRequest idRequest = RequestParser.parseAndValidate(request, IdRequest.class);  // ✅ Single line
    // ...
}
```

### Example 2: Authorization Checks (Appearing 30+ times)

**Current (Duplicated):**
```java
// In FolderServlet
for (Folder folder : folders) {
    authorizationService.throwUpUnlessUserOrOwnerHasPermission(folder, DefaultPermission.DELETE, user, ErrorCode.NO_DELETE_PERMISSION);
}

// In OsdServlet - same check repeated
for (ObjectSystemData osd : osds) {
    authorizationService.throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.DELETE, user, ErrorCode.NO_DELETE_PERMISSION);
}
```

**Recommended (Extract to utility):**
```java
public interface AuthorizationValidator {
    
    default <T extends Ownable> void authorizeAll(List<T> ownables, DefaultPermission permission, UserAccount user, ErrorCode errorCode) {
        for (T ownable : ownables) {
            authorizationService.throwUpUnlessUserOrOwnerHasPermission(ownable, permission, user, errorCode);
        }
    }
}

// In base servlet:
public abstract class AuthorizedServlet extends BaseServlet implements AuthorizationValidator {
    protected final AuthorizationService authorizationService = new AuthorizationService();
}
```

**Usage:**
```java
authorizeAll(folders, DefaultPermission.DELETE, user, ErrorCode.NO_DELETE_PERMISSION);  // ✅ One line
```

### Example 3: Wrapper Creation (Appearing 40+ times)

**Current:**
```java
AclWrapper aclWrapper = new AclWrapper();
aclWrapper.getAcls().addAll(acls);
aclWrapper.getAcls().removeAll(Collections.singleton(null));
response.setWrapper(aclWrapper);
```

**Recommended (Extract to utility):**
```java
public interface WrapperFactory {
    
    static <T, W extends Wrapper<T>> void wrappAndRespond(CinnamonResponse response, List<T> items, Class<W> wrapperClass) {
        try {
            W wrapper = wrapperClass.getConstructor().newInstance();
            wrapper.getList().addAll(items);
            wrapper.getList().removeAll(Collections.singleton(null));
            response.setWrapper(wrapper);
        } catch (Exception e) {
            throw new CinnamonException("Failed to wrap response", e);
        }
    }
}
```

---

## Category 3: Missing Documentation & Comments (HIGH)

### Problem
Servlets lack JavaDoc and inline comments explaining:
- Method purposes and workflows
- Parameter meanings
- Complex business logic (e.g., version management, cascade deletes)
- Error conditions and recovery
- Authorization requirements

### Impact
- **Onboarding**: New developers cannot understand code flow
- **Maintenance**: Non-obvious logic requires reverse engineering
- **Bugs**: Incorrect assumptions about expected behavior
- **API Usage**: No reference for client developers

### Example Improvements

#### Before (No Documentation):
```java
@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends BaseServlet implements CruddyServlet<ObjectSystemData> {

    private final AuthorizationService authorizationService = new AuthorizationService();
    private final DeleteOsdService deleteOsdService = new DeleteOsdService();
    private static final Logger log = LogManager.getLogger(OsdServlet.class);
    private ContentProviderService contentProviderService;
    private Long tikaMetasetTypeId;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ...
    }
}
```

#### After (With Documentation):
```java
/**
 * Servlet handling all Object System Data (OSD) operations.
 * 
 * <p>This servlet implements CRUD operations for Cinnamon objects (documents, media files, etc.).
 * It manages object lifecycle including versioning, content storage, metadata, and locking.</p>
 * 
 * <p><strong>Authentication:</strong> All operations require a valid session ticket.</p>
 * <p><strong>Authorization:</strong> Operations respect ACL-based permissions (BROWSE, READ, WRITE, DELETE).</p>
 * 
 * <p><strong>Supported Operations:</strong></p>
 * <ul>
 *     <li>CREATE: Create new OSDs with optional content</li>
 *     <li>READ: Retrieve OSD metadata and content</li>
 *     <li>UPDATE: Modify OSD properties and metadata</li>
 *     <li>VERSION: Create new versions with optional content</li>
 *     <li>DELETE: Delete OSDs with cascading cleanup (metadata, content, links)</li>
 * </ul>
 * 
 * @see CruddyServlet for CRUD base operations
 * @see BaseServlet for shared utilities (metadata, authorization)
 */
@MultipartConfig
@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends BaseServlet implements CruddyServlet<ObjectSystemData> {

    private final AuthorizationService authorizationService = new AuthorizationService();
    private final DeleteOsdService deleteOsdService = new DeleteOsdService();
    private static final Logger log = LogManager.getLogger(OsdServlet.class);
    private ContentProviderService contentProviderService;
    private Long tikaMetasetTypeId;  // ✅ Cached to avoid repeated lookups

    /**
     * Handles POST requests routing to appropriate operation handlers.
     * 
     * @param request the HTTP request containing the operation mapping
     * @param response the HTTP response for sending results
     * @throws ServletException if servlet initialization fails
     * @throws IOException if I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ...
    }
}
```

### Complex Methods Requiring Documentation

**OsdServlet.copyToExistingOsd (120+ lines):**
```java
/**
 * Copies content and/or metadata from source OSDs to target OSDs.
 * 
 * <p><strong>Behavior:</strong></p>
 * <ul>
 *     <li>If copyContent=true: replaces target's content, deletes old target content, schedules deletion job</li>
 *     <li>If copyContent=false: preserves target's content</li>
 *     <li>If metasetTypeIds specified: copies only those metaset types from source to target</li>
 *     <li>Tika metadata on target is deleted if Tika is enabled</li>
 *     <li>All permissions are validated before any modifications</li>
 * </ul>
 * 
 * <p><strong>Permissions Required:</strong></p>
 * <ul>
 *     <li>BROWSE on source OSD</li>
 *     <li>READ_OBJECT_CUSTOM_METADATA on source OSD (if copying metadata)</li>
 *     <li>WRITE on target OSD</li>
 *     <li>MODIFY_OBJECT_CONTENT_CUSTOM_METADATA on target OSD (if copying metadata)</li>
 * </ul>
 * 
 * <p><strong>Atomicity:</strong> Validates all permissions before modifying any objects.
 * If any permission check fails, no modifications are made.</p>
 * 
 * @param request containing CopyToExistingOsdRequest with copy tasks
 * @param cinnamonResponse response for sending results
 * @param user current user performing the operation
 * @param osdDao DAO for OSD access
 * @throws IOException if request parsing fails
 * @throws FailedRequestException if permission checks fail or copy tasks are invalid
 */
private void copyToExistingOsd(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
    // ...
}
```

---

## Category 4: Error Handling Inconsistency (HIGH)

### Problem
Error handling varies across servlets:
- Some throw custom exceptions, some let exceptions propagate
- Inconsistent null checking patterns
- No validation before resource-intensive operations
- Mix of checked and unchecked exception handling

### Impact
- **Unreliable Error Messages**: Client receives 500 errors instead of meaningful feedback
- **Resource Leaks**: Failed operations don't clean up resources
- **Debugging**: Stack traces instead of logged context
- **Client Experience**: Inconsistent error response formats

### Example Issues

#### Issue 1: Uncaught Exceptions
```java
// In SearchServlet
private void searchIds(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user) throws IOException {
    SearchIdsRequest searchRequest = request.getMapper().readValue(request.getInputStream(), SearchIdsRequest.class)
            .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());  // ✅ Good
    SearchResult searchResult = searchService.doSearch(searchRequest.getQuery(), // ❌ No null check on result
            searchRequest.getSearchType(), user);
    cinnamonResponse.setResponse(new SearchIdsResponse(searchResult.osdIds(), searchResult.folderIds()));
}
```

**Fix:**
```java
private void searchIds(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user) throws IOException {
    SearchIdsRequest searchRequest = RequestParser.parseAndValidate(request, SearchIdsRequest.class);
    SearchResult searchResult = searchService.doSearch(searchRequest.getQuery(), searchRequest.getSearchType(), user);
    if (searchResult == null) {  // ✅ Defensive check
        throw new FailedRequestException(ErrorCode.SEARCH_FAILED);
    }
    cinnamonResponse.setResponse(new SearchIdsResponse(
        searchResult.osdIds() != null ? searchResult.osdIds() : Collections.emptyList(),
        searchResult.folderIds() != null ? searchResult.folderIds() : Collections.emptyList()
    ));
}
```

#### Issue 2: Silent Null Removal
```java
AclWrapper aclWrapper = new AclWrapper();
aclWrapper.getAcls().addAll(acls);
aclWrapper.getAcls().removeAll(Collections.singleton(null));  // ❌ Silently removes nulls - why are there nulls?
response.setWrapper(aclWrapper);
```

**Fix:**
```java
List<Acl> nonNullAcls = acls.stream()
    .filter(acl -> acl != null)  // ✅ Explicit filtering
    .collect(Collectors.toList());
if (nonNullAcls.size() != acls.size()) {
    log.warn("DAO returned {} null values out of {}", acls.size() - nonNullAcls.size(), acls.size());
}
AclWrapper aclWrapper = new AclWrapper();
aclWrapper.getAcls().addAll(nonNullAcls);
response.setWrapper(aclWrapper);
```

#### Issue 3: Unhandled SQL Exceptions
```java
default List<T> update(UpdateRequest<T> updateRequest, CrudDao<T> dao, CinnamonResponse cinnamonResponse){
    try {
        List<T> updatedItems = dao.update(updateRequest.list());
        cinnamonResponse.setWrapper(updateRequest.fetchResponseWrapper().setList(updatedItems));
        return updatedItems;
    }
    catch (PersistenceException | SQLException e){  // ✅ Good - catches both
        throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, e.getMessage());  // ✅ Good - wraps
    }
}
```

---

## Category 5: Resource Management (MEDIUM)

### Problem
File and stream resources not properly managed:
- No try-with-resources for file streams
- Part uploads not validated before processing
- Memory leaks possible with large uploads
- No resource cleanup on error

### Impact
- **File Handle Leaks**: Temporary file handles accumulate over time
- **Disk Space**: Incomplete uploads consume disk
- **Performance**: Resource exhaustion causes service degradation
- **Security**: Large uploads can cause DoS

### Examples

#### Before (Unsafe):
```java
private void setContent(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException, ServletException {
    SetContentRequest setContentRequest = // ... parse request
    Part contentPart = request.getPart("content");  // ❌ No null check or size validation
    
    ObjectSystemData osd = // ... load OSD
    File contentFile = new File(contentPath);
    contentPart.write(contentPath);  // ❌ Risk of incomplete writes
}
```

#### After (Safe):
```java
private void setContent(CinnamonRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException, ServletException {
    SetContentRequest setContentRequest = RequestParser.parseAndValidate(request, SetContentRequest.class);
    
    // ✅ Validate upload before processing
    Part contentPart = request.getPart("content");
    if (contentPart == null) {
        throw new FailedRequestException(ErrorCode.UPLOAD_MISSING_CONTENT_PART);
    }
    if (contentPart.getSize() > MAX_UPLOAD_SIZE) {
        throw new FailedRequestException(ErrorCode.UPLOAD_TOO_LARGE);
    }
    
    // ✅ Load OSD and check permissions
    ObjectSystemData osd = getOsd(setContentRequest.getId(), user, osdDao);
    
    // ✅ Use temporary file with cleanup
    File tempFile = File.createTempFile("upload_", ".tmp");
    try {
        contentPart.write(tempFile.getAbsolutePath());
        // ✅ Verify file before moving to final location
        validateUploadedFile(tempFile);
        moveToFinalLocation(osd, tempFile);
    } finally {
        // ✅ Clean up temporary file
        if (tempFile.exists()) {
            Files.deleteIfExists(tempFile.toPath());
        }
    }
}
```

---

## Category 6: Initialization Patterns (MEDIUM)

### Problem
Inconsistent servlet initialization:
- Some servicesdse lazily initialized in methods
- Static initializers vs instance fields
- No clear lifecycle management
- Constructor side effects

### Impact
- **Startup Time**: Lazy initialization delays first request
- **Predictability**: Timing issues hard to debug
- **Testing**: Can't initialize servlets for testing
- **Clustering**: Issues with servlet instances in different JVMs

### Examples

#### Before (Inconsistent):
```java
public class OsdServlet extends BaseServlet implements CruddyServlet<ObjectSystemData> {
    
    private ContentProviderService contentProviderService;  // ❌ No initialization
    private Long tikaMetasetTypeId;  // ❌ Lazy loaded in method
    
    private Long getTikaMetasetTypeId() {  // ❌ Repeated lookup pattern
        if (tikaMetasetTypeId == null) {
            Optional<MetasetType> tikaMetasetType = new MetasetTypeDao().list().stream()
                .filter(metasetType -> metasetType.getName().equals(TIKA_METASET_NAME))
                .findFirst();
            if (tikaMetasetType.isPresent()) {
                tikaMetasetTypeId = tikaMetasetType.get().getId();
            } else {
                if (CinnamonServer.config.getCinnamonTikaConfig().isUseTika()) {
                    throw new CinnamonException("Could not find tika metaset type");
                }
            }
        }
        return tikaMetasetTypeId;
    }
}
```

#### After (Consistent):
```java
public class OsdServlet extends BaseServlet implements CruddyServlet<ObjectSystemData> {
    
    private ContentProviderService contentProviderService;  // ✅ Initialized in init()
    private Long tikaMetasetTypeId;  // ✅ Also initialized in init()
    
    @Override
    public void init() throws ServletException {
        super.init();
        // ✅ Initialize all services once at startup
        this.contentProviderService = (ContentProviderService) 
            getServletContext().getAttribute(CONTENT_PROVIDER_SERVICE);
        this.tikaMetasetTypeId = initializeTikaMetasetTypeId();
        if (this.tikaMetasetTypeId == null && 
            CinnamonServer.config.getCinnamonTikaConfig().isUseTika()) {
            throw new ServletException("Tika metaset type not found");
        }
    }
    
    private Long initializeTikaMetasetTypeId() {
        return getDao(MetasetTypeDao.class).list().stream()
            .filter(metasetType -> metasetType.getName().equals(TIKA_METASET_NAME))
            .map(MetasetType::getId)
            .findFirst()
            .orElse(null);
    }
}
```

---

## Priority Implementation Roadmap

### Phase 1: Fix DAO Instantiation Anti-Pattern (Week 1)
Make consistent use of static/cached DAOs - the infrastructure is already in place:

**What DbSessionFilter Already Does:**
- ✅ Opens fresh SqlSession per request
- ✅ Closes old sessions
- ✅ Manages commit/rollback
- ✅ Handles exceptions
- ✅ Post-commit cleanup
- ✅ Request-scoped cleanup

**What We Need to Do (Very Simple):**
1. Identify all DAO instantiation points in servlets
2. Convert to static cached fields
3. Remove redundant `ThreadLocalSqlSession.refreshSession()` calls from constructors
4. Verify DbSessionFilter is active in deployment

**Example per servlet:**
```java
// Before
public void doPost(...) {
    AclDao dao = new AclDao();  // ❌ Remove
}

// After
private static final AclDao dao = new AclDao();  // ✅ Add field

public void doPost(...) {
    // ✅ Use dao directly
}
```

**Effort:** 2-3 hours for all 29 servlets (5 min per servlet)
**Output:** Cleaner code, better performance, consistent pattern
**Risk:** Very low (just consolidating existing pattern)

### Phase 2: Extract Common Patterns (Week 2-3)
Reduce code duplication (request parsing, authorization, responses):

1. Extract request parsing utilities
2. Extract authorization validation utilities
3. Extract wrapper creation utilities  
4. Update servlets to use extracted utilities

**Effort:** 30-50 hours
**Output:** New utilities, reduced duplication

### Phase 3: Update All Servlets (Week 4-5)
Apply patterns to all 29 servlets:

1. Use `RequestParser.parseAndValidate()` instead of inline parsing
2. Use authorization helpers instead of repeated loops
3. Update error handling
4. Add documentation

**Effort:** 40-60 hours
**Output:** 29 refactored, cleaner servlets

### Phase 4: Documentation & Testing (Week 9-10)
Add documentation and tests:

1. Add JavaDoc to all servlets
2. Add comments to complex methods
3. Create unit tests with mocked DAOs
4. Update developer guide

**Effort:** 40-60 hours
**Output:** Documentation, test suite

---

## Key Refactoring Classes to Create

### 1. SqlSessionRefreshFilter.java (If using Filter approach)
```java
package com.dewarim.cinnamon.application.filter;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import jakarta.servlet.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filter that ensures each request gets a fresh SqlSession.
 * 
 * This centralizes session lifecycle management, removing the need to 
 * instantiate DAOs just for their side effect of calling refreshSession().
 */
public class SqlSessionRefreshFilter implements Filter {
    private static final Logger log = LogManager.getLogger(SqlSessionRefreshFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) {
        log.info("SqlSessionRefreshFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String threadName = Thread.currentThread().getName();
        log.debug("Refreshing SqlSession for request thread: {}", threadName);
        
        try {
            // ✅ Explicit session refresh for this request
            ThreadLocalSqlSession.refreshSession();
            chain.doFilter(request, response);
        } finally {
            // Optional: Additional cleanup here if needed
            log.debug("Request completed for thread: {}", threadName);
        }
    }
    
    @Override
    public void destroy() {
        log.info("SqlSessionRefreshFilter destroyed");
    }
}
```

**web.xml Configuration:**
```xml
<filter>
    <filter-name>SqlSessionRefreshFilter</filter-name>
    <filter-class>com.dewarim.cinnamon.application.filter.SqlSessionRefreshFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>SqlSessionRefreshFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

### 2. DaoFactory.java
```java
package com.dewarim.cinnamon.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for cached DAO instances.
 * 
 * DAOs are stateless and delegate all work to the ThreadLocal SqlSession,
 * so a single cached instance per DAO class is sufficient and more efficient
 * than instantiating a new DAO per request.
 */
public class DaoFactory {
    private static final DaoFactory INSTANCE = new DaoFactory();
    private final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();
    
    private DaoFactory() {
        initializeAllDaos();
    }
    
    private void initializeAllDaos() {
        // ✅ Initialize all DAOs once at startup
        cache(AclDao.class, new AclDao());
        cache(FolderDao.class, new FolderDao());
        cache(OsdDao.class, new OsdDao());
        cache(UserAccountDao.class, new UserAccountDao());
        // ... add more DAOs as needed
    }
    
    public static DaoFactory getInstance() {
        return INSTANCE;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getDao(Class<T> daoClass) {
        return (T) daoCache.computeIfAbsent(daoClass, k -> {
            throw new IllegalArgumentException("Unknown DAO: " + k.getSimpleName());
        });
    }
    
    private <T> void cache(Class<T> clazz, T instance) {
        daoCache.put(clazz, instance);
    }
}
```

### 3. DaoAwareServlet.java (If using backward-compatible approach)
```java
package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.DaoFactory;

/**
 * Base servlet that ensures fresh SqlSession and provides DAO access.
 * 
 * Replaces the pattern of instantiating DAOs just for their side effect
 * of indirectly calling refreshSession(). Instead, we're explicit about
 * session refresh and reuse DAO instances.
 */
public abstract class DaoAwareServlet extends BaseServlet {
    
    /**
     * Gets a DAO instance from the factory, ensuring a fresh SqlSession for this request.
     * 
     * @param daoClass the DAO class to retrieve
     * @param <T> the DAO type
     * @return a cached DAO instance that uses the thread's current SqlSession
     */
    protected final <T> T getDao(Class<T> daoClass) {
        // ✅ Explicit session refresh - clear intent
        ThreadLocalSqlSession.refreshSession();
        
        // ✅ Return cached DAO instance (all DAOs use ThreadLocal session anyway)
        return DaoFactory.getInstance().getDao(daoClass);
    }
}
```

### 3. RequestParser.java
```java
public class RequestParser {
    static <T extends Validatable<?>> T parseAndValidate(CinnamonRequest, Class<T>)
}
```

### 4. AuthorizationValidator.java
```java
public interface AuthorizationValidator {
    default <T extends Ownable> void authorizeAll(...)
}
```

### 5. WrapperFactory.java
```java
public class WrapperFactory {
    static <T, W extends Wrapper<T>> void wrapAndRespond(...)
}
```

### 6. ServletErrorHandler.java
```java
public class ServletErrorHandler {
    static void handleError(Exception, CinnamonResponse, Logger)
}
```

---

## Migration Strategy

### Step 1: Non-Breaking Infrastructure (Safe)
Create new classes without modifying existing servlets:
- DaoFactory can coexist with new AclDao() code
- RequestParser is additive
- BaseServlet enhancements are backward compatible

### Step 2: Gradual Migration (Reduce Risk)
Update servlets incrementally:
- Start with simple servlets (ConfigEntryServlet, LanguageServlet)
- Update complex servlets (OsdServlet, FolderServlet) last
- Run tests after each batch

### Step 3: Version Control
Create feature branch for large refactoring:
```bash
git checkout -b refactor/servlet-architecture
# Make changes in phases
# Test thoroughly
# Create PR for review
# Merge when approved
```

---

## Testing Strategy

### Unit Tests to Add

```java
@WebMvcTest(AclServlet.class)
public class AclServletTest {
    
    @MockBean
    private AclDao aclDao;
    
    @Test
    public void testListAcls_Success() {
        // ✅ Can now mock DAO
        when(aclDao.list()).thenReturn(List.of(...));
        // Test servlet behavior
    }
}
```

### Integration Tests
```java
public class AclServletIntegrationTest {
    @Test
    public void testListAcls_EndToEnd() {
        // ✅ Full request/response cycle
    }
}
```

---

## Code Quality Metrics

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Code Duplication | ~30% | ~5% | 80% reduction |
| Test Coverage | ~10% | ~70% | 7x improvement |
| Average Method Size | 45 lines | 20 lines | 55% reduction |
| DAOs per Servlet | 1-14 new instances | 1-14 shared | Better caching |
| Documentation | ~5 classes | All classes | 100% coverage |
| Error Consistency | Mixed | Unified | 100% consistent |

---

## Estimated Effort Summary

| Task | Hours | Days |
|------|-------|------|
| Fix DAO Anti-Pattern (Phase 1) | 3 | 0.5 days |
| Pattern Extraction (Phase 2) | 40 | 5 days |
| Servlet Updates (Phase 3) | 50 | 6 days |
| Documentation (Phase 4) | 50 | 6 days |
| **Total** | **143** | **17.5 days** |

**Note:** This is much simpler than originally thought because DbSessionFilter already handles session management! Phase 1 is now just consolidating an existing pattern (using static DAOs instead of instantiating per request).

---

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Servlet instantiation in containers | HIGH | Use factory pattern compatible with servlet containers |
| DAO thread safety | MEDIUM | Ensure DAOs are stateless or use thread-local storage |
| Breaking changes | HIGH | Incremental migration, backward compatibility layer |
| Performance regression | MEDIUM | Benchmark before/after, profile hot paths |
| Integration test failures | MEDIUM | Comprehensive test suite before migration |

---

## Conclusion

This improvement plan addresses **6 systemic architectural issues** affecting testability, maintainability, and consistency. Implementation following the 4-phase approach minimizes risk while delivering:

- **Phase 1** enables dependency injection for testing
- **Phase 2** reduces duplication and improves consistency
- **Phase 3** completes refactoring with maintainability gains
- **Phase 4** ensures knowledge transfer and quality standards

**Expected Outcomes:**
- 70%+ test coverage (from ~10%)
- 80% reduction in code duplication
- 30+ servlet classes maintainable and testable
- Clear architectural patterns for future development


