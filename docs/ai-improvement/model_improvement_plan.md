# Cinnamon Model Classes - Code Quality Improvement Plan

**Date:** 2026-04-28  
**Scope:** Analysis of all model classes in `src/main/java/com/dewarim/cinnamon/model/`  
**Key Constraint:** ID fields are unique and enforced by database constraints.

---

## Review status (2026-07-12)

This plan was reviewed against the current code and partially applied. **Read this before
acting on the categories below.**

**Applied (targeted subset):**
- **Category 2/3** — `MetasetType.unique` converted to primitive `boolean` (with the
  companion removal of the `getUnique() == null` check in `CreateMetasetTypeRequest`);
  redundant null-checks removed from `Lifecycle.getLifecycleStates()` and
  `AclGroup.getPermissionIds()`; `ObjectSystemData.metas` eagerly initialized.
- **Category 1 — only the two clearly-problematic cases**, keeping business-key identity:
  `ConfigEntry` no longer compares/hashes the large `config` XML blob; `Session` reduced
  to its stable `id` (was hashing mutable `ticket`/`expires`).

**Reviewed and deferred / not recommended — do not re-litigate without new evidence:**
- **Category 1 blanket "id-only equals for ~16 models" — NOT adopted.** No concrete bug
  was demonstrated; `equals` is relied on by tests (`assertEquals(folder, …)`,
  `assertEquals(meta, …)`) and by a DB-loaded `Set<Group>`; and a blanket switch
  introduces the null-id trap (transient/unsaved objects all compare equal). The stable
  business-key `equals` on the other models is intentional and stays.
- **Category 2 `Deletion`** — `getId()` already exists; the `long`→`Long` change is moot.
- **Category 4 (boolean getter naming)** — the section itself concludes "no change
  needed." Skip.
- **Category 5 (setId symmetry, toString field-name fixes, per-field JavaDoc)** —
  medium/docs, deferred; revisit only if it becomes a real need.

---

## Executive Summary

This document outlines systematic improvements to enhance code quality, consistency, and maintainability across all model classes. The analysis identified **5 critical categories** affecting 20+ models:

1. **equals/hashCode Contract Violations** (CRITICAL)
2. **Type Inconsistencies** (HIGH)
3. **Collection Initialization Patterns** (HIGH)
4. **Boolean Getters Naming** (MEDIUM)
5. **Missing API Symmetry & Documentation** (MEDIUM)

---

## Category 1: equals/hashCode Contract Violations (CRITICAL)

### Problem
Since IDs are unique and enforced by database constraints, all `Identifiable` models should use only the `id` field in `equals()` and `hashCode()` implementations. Currently, many models use business logic fields (like `name`), creating inconsistencies.

### Impact
- **Data Structure Bugs**: Objects with the same `id` but different business data are not equal
- **Set/Map Behavior**: Duplicate entries in collections when expected to deduplicate by ID
- **Persistence Issues**: Confusion between entity identity (database ID) and business equality

### Affected Models

#### `Acl.java` (Lines 47-58)
**Current (WRONG):**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Acl acl = (Acl) o;
    return Objects.equals(name, acl.name);  // ❌ Uses name, not id
}

@Override
public int hashCode() {
    return Objects.hash(name);  // ❌ Uses name, not id
}
```

**Recommended:**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Acl acl = (Acl) o;
    return Objects.equals(id, acl.id);  // ✅ Uses id for entity identity
}

@Override
public int hashCode() {
    return Objects.hash(id);  // ✅ Uses id for consistency
}
```

#### Other Models with Same Issue
| Model | Current Implementation | Issue |
|-------|------------------------|-------|
| `Language.java` | Uses `isoCode` | should use `id` |
| `UiLanguage.java` | Uses `isoCode` | should use `id` |
| `ObjectType.java` | Uses `name` | should use `id` |
| `FolderType.java` | Uses `name` | should use `id` |
| `Format.java` | Uses `contentType`, `extension`, `name` | should use `id` |
| `MetasetType.java` | Uses `name` | should use `id` |
| `Group.java` | Uses `id`, `name`, `parentId` | should use only `id` |
| `AclGroup.java` | Uses `aclId`, `groupId` | should use `id` |
| `ConfigEntry.java` | Uses `name`, `config`, `publicVisibility` | should use `id` |

#### Special Cases

**`Folder.java` (Line 146-153)**  
Uses business fields (`name`, `aclId`, `ownerId`, `parentId`). Since folders can have the same name but be different objects:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Folder folder = (Folder) o;
    return Objects.equals(id, folder.id);  // ✅ Use id only
}
```

**`ObjectSystemData.java` (Lines 477-525)**  
Complex equals with many fields - simplify to use `id`:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ObjectSystemData that = (ObjectSystemData) o;
    return Objects.equals(id, that.id);  // ✅ Simpler and more correct
}

@Override
public int hashCode() {
    return Objects.hash(id);  // ✅ Consistent with equals
}
```

**`Session.java` (Line 72)**  
Hashes all fields including `ticket` and `expires` - should hash only `id`:
```java
@Override
public int hashCode() {
    return Objects.hash(id);  // ✅ Session is identified by id
}
```

**`Deletion.java` (Lines 5-68)**  
Missing `equals()` and `hashCode()` entirely - add them:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Deletion that = (Deletion) o;
    return Objects.equals(osdId, that.osdId);  // Note: osdId IS the id in this model
}

@Override
public int hashCode() {
    return Objects.hash(osdId);
}
```

**`UserAccount.java` (Lines 195-208)**  
Compares too many fields - simplify:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserAccount that = (UserAccount) o;
    return Objects.equals(id, that.id);  // ✅ Id is unique identifier
}
```

---

## Category 2: Type Inconsistencies (HIGH)

### Problem
Models inconsistently use `Boolean` (wrapper) vs `boolean` (primitive) and `long` vs `Long`, causing null-safety issues and inconsistent patterns.

### Impact
- **NullPointerException Risk**: `Boolean` fields can be null; unboxing causes NPE
- **API Inconsistency**: Some methods take primitives, others take wrappers
- **Maintenance Burden**: Developers must remember which fields use which type

### Affected Models

#### `MetasetType.java` (Line 11)
**Current (INCONSISTENT):**
```java
private Boolean unique = false;  // ❌ Wrapper type with default

public Boolean getUnique() {
    return unique;
}

public void setUnique(Boolean unique) {
    this.unique = unique;
}
```

**Recommended:**
```java
private boolean unique = false;  // ✅ Primitive with clear default

public boolean isUnique() {
    return unique;
}

public void setUnique(boolean unique) {
    this.unique = unique;
}
```

#### `Deletion.java` (Line 7)
**Current (INCONSISTENT):**
```java
private long osdId;  // ❌ Primitive long instead of Long

public long getOsdId() {
    return osdId;
}

public void setOsdId(long osdId) {
    this.osdId = osdId;
}
```

**Recommended:**
```java
private Long osdId;  // ✅ Long for consistency with other models

public Long getOsdId() {
    return osdId;
}

public void setOsdId(Long osdId) {
    this.osdId = osdId;
}
```

Also add missing `getId()` implementation:
```java
@Override
public Long getId() {
    return osdId;  // ✅ Explicitly map osdId to contract
}
```

#### `UserAccount.java` (Lines 47-48)
**Current:**
```java
public UserAccount(String name, String password, String fullname, String email, Long uiLanguageId, 
                   String loginType,
                   Boolean changeTracking, Boolean activated, Boolean activateTriggers) {  // ❌ Boolean parameters
    this.changeTracking   = changeTracking;
    this.activated        = activated;
    this.activateTriggers = activateTriggers;
}
```

**Recommended:**
```java
public UserAccount(String name, String password, String fullname, String email, Long uiLanguageId, 
                   String loginType,
                   boolean changeTracking, boolean activated, boolean activateTriggers) {  // ✅ Primitive parameters
    this.changeTracking   = changeTracking;
    this.activated        = activated;
    this.activateTriggers = activateTriggers;
}
```

---

## Category 3: Collection Initialization Patterns (HIGH)

### Problem
Models use inconsistent patterns for initializing collections: some eagerly initialize at declaration, others lazily in getters.

### Impact
- **Null Reference Risk**: Forgetting to null-check or initialize before adding items
- **Inconsistent Behavior**: `getMetasets()` might return null vs empty list depending on model
- **Defensive Code**: Clients must check for null collections

### Affected Models

#### `Lifecycle.java` (Lines 55-67)
**Current (LAZY INITIALIZATION):**
```java
private List<LifecycleState> lifecycleStates = new ArrayList<>();

public List<LifecycleState> getLifecycleStates() {
    if(lifecycleStates == null){  // ❌ Defensive check not needed if eager initialized
        lifecycleStates = new ArrayList<>();
    }
    return lifecycleStates;
}

public void setLifecycleStates(List<LifecycleState> lifecycleStates) {
    if(lifecycleStates == null) {  // ❌ Silently ignoring null is confusing
        return;
    }
    this.lifecycleStates = lifecycleStates;
}
```

**Recommended (EAGER INITIALIZATION):**
```java
private List<LifecycleState> lifecycleStates = new ArrayList<>();  // ✅ Already eager

public List<LifecycleState> getLifecycleStates() {
    return lifecycleStates;  // ✅ Never null, cleaner getter
}

public void setLifecycleStates(List<LifecycleState> lifecycleStates) {
    this.lifecycleStates = Objects.requireNonNullElse(lifecycleStates, new ArrayList<>());  // ✅ Explicit nullhandling
}
```

#### `ObjectSystemData.java` (Lines 465-474)
**Current (LAZY):**
```java
private List<Meta> metas;  // ❌ Can be null

public List<Meta> getMetas() {
    if (metas == null) {
        metas = new ArrayList<>();
    }
    return metas;
}
```

**Recommended (EAGER):**
```java
private List<Meta> metas = new ArrayList<>();  // ✅ Never null

public List<Meta> getMetas() {
    return metas;  // ✅ Guarantee: never null
}
```

#### `AclGroup.java` (Lines 61-70)
**Current (MIXED PATTERN):**
```java
private List<Long> permissionIds = new ArrayList<>();  // ✅ Eager declared

public List<Long> getPermissionIds() {
    if (permissionIds == null) {  // ❌ Unnecessary check
        permissionIds = new ArrayList<>();
    }
    return permissionIds;
}
```

**Recommended:**
```java
private List<Long> permissionIds = new ArrayList<>();  // ✅ Eager init

public List<Long> getPermissionIds() {
    return permissionIds;  // ✅ Simple, no null check needed
}
```

---

## Category 4: Boolean Getters Naming Convention (MEDIUM)

### Problem
Some boolean fields use `get` prefix instead of standard JavaBean convention `is` prefix, reducing readability and IDE support.

### Impact
- **IDE Suggestions**: IDEs may not recognize non-standard boolean getters
- **JSON Serialization**: Some frameworks expect `isXxx` naming
- **Code Readability**: `isActivated()` is more idiomatic than `getActivated()`

### Affected Models

#### `Folder.java` (Line 121-122)
**Current:**
```java
public boolean isHasSubfolders() {  // ✅ Correct per JavaBean convention
    return hasSubfolders;
}
```

**Note:** This getter name is awkward-sounding but actually correct. The JavaBean convention dictates that a boolean field `hasSubfolders` should have getter `isHasSubfolders()`. The apparent redundancy ("is" + "has") is a consequence of the semantic field naming choice. 

**Alternative approaches:**
1. **Keep as-is** (Recommended): Field name `hasSubfolders` clearly documents intent, getter `isHasSubfolders()` follows the pattern correctly despite sounding redundant.
2. **Rename field to `subfolders`**: Would allow cleaner getter `hasSubfolders()`, but loses semantic clarity about it being a boolean flag.

The current implementation is acceptable; no change needed.

#### General Pattern
For boolean fields, follow JavaBean convention `is<PropertyName>()`:

| Field Name | Correct Getter | Note |
|-----------|---|---|
| `activated` | `isActivated()` | ✅ Follows convention |
| `hasSubfolders` | `isHasSubfolders()` | ✅ Follows convention (redundant-sounding but correct) |
| `metadataChanged` | `isMetadataChanged()` | ✅ Follows convention |

---

## Category 5: Missing API Symmetry & Documentation (MEDIUM)

### Problem
Some models lack `setId()` methods, missing setter/getter pairs, incomplete JavaDoc.

### Impact
- **Incomplete API**: Can't set entity ID via standard setter
- **Serialization**: Frameworks may fail to deserialize objects
- **Documentation**: Unclear model contracts and field meanings

### Sub-Issues

#### Missing setId() Methods

**`Acl.java` (Line 26-31)**
```java
public Long getId() {
    return id;
}
// ❌ Missing setId() - cannot set id

public void setName(String name) {
    this.name = name;
}
```

**Recommended:**
```java
public Long getId() {
    return id;
}

public void setId(Long id) {  // ✅ Add symmetry
    this.id = id;
}
```

Also affects: `Language.java`, `UiLanguage.java`, `FolderType.java`, `ObjectType.java`

#### Missing Setters

**`Folder.java` - No setter for `folderPath` (Line 176-182)**
```java
public String getFolderPath() {
    return folderPath;
}

public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
}  // ✅ Already has setter - good
```

**Required: Verify all getters have corresponding setters.**

#### Naming Inconsistencies in toString()

**`ConfigEntry.java` (Line 87)**
```java
@Override
public String toString() {
    return "ConfigEntry{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", content='" + config + '\'' +  // ❌ Field is "config", toString says "content"
           ", publicVisibility=" + publicVisibility +
           '}';
}
```

**Recommended:**
```java
@Override
public String toString() {
    return "ConfigEntry{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", config='" + config + '\'' +  // ✅ Match field name
           ", publicVisibility=" + publicVisibility +
           '}';
}
```

#### Missing JavaDoc

**All models** lack class-level documentation explaining:
- Business purpose ("Core Cinnamon object holding system data...")
- ID uniqueness constraint
- Field domains and meanings
- Mutability characteristics

**Example Enhancement for `ObjectSystemData.java`:**
```java
/**
 * Core Cinnamon object representing system data of a document/resource.
 * 
 * <p>This entity represents the metadata and system information for an object within Cinnamon.
 * The {@code id} field is the unique identifier, enforced by a database constraint, and should
 * be used exclusively for object equality and hashing.</p>
 * 
 * <p><strong>Versioning:</strong> Objects support versioning through the {@code cmnVersion} field,
 * allowing multiple versions and branches to coexist in the repository.</p>
 * 
 * @see CinnamonObject for related interfaces
 */
@JacksonXmlRootElement(localName = "objectSystemData")
public class ObjectSystemData implements ContentMetadata, CinnamonObject, OwnableWithMetadata {
    // ...
}
```

**Example for problematic field in `Folder.java`:**
```java
/**
 * Indicates whether this folder contains subfolders.
 * Read-only - computed from database queries, not settable by clients.
 */
private boolean hasSubfolders = false;
```

---

## Priority Implementation Roadmap

### Phase 1: Critical (Week 1)
Fix **equals/hashCode violations** - highest impact on data consistency:

1. Update all `Identifiable` models to use only `id` in equals/hashCode
2. Add equals/hashCode to `Deletion.java`, `ChangeTrigger.java` (if missing)
3. Simplify complex implementations (e.g., `ObjectSystemData.java`)

**Models:** `Acl`, `Language`, `UiLanguage`, `ObjectType`, `FolderType`, `Format`, `MetasetType`, `Group`, `AclGroup`, `ConfigEntry`, `Folder`, `Session`, `UserAccount`, `ObjectSystemData`, `Deletion`

### Phase 2: High Priority (Week 2)
Fix **type inconsistencies** and **collection patterns**:

1. Replace `Boolean` wrapper with `boolean` primitives
2. Replace `long` with `Long` for consistency
3. Standardize on eager collection initialization
4. Update corresponding constructors/setters

**Models:** `MetasetType`, `Deletion`, `Lifecycle`, `ObjectSystemData`, `AclGroup`

### Phase 3: Medium Priority (Week 3)
Fix **naming conventions** and **API symmetry**:

1. Add missing `setId()` methods
2. Fix toString() field name mismatches
3. Verify all getters have setters

**Models:** `Acl`, `Language`, `UiLanguage`, `FolderType`, `ObjectType`, `ConfigEntry`

### Phase 4: Documentation (Week 4)
Add **JavaDoc** and comments:

1. Add class-level JavaDoc to all models
2. Document readonly fields
3. Explain non-obvious fields (versioning, state machines, etc.)
4. Add field-level comments for foreign keys

**All Models**

---

## Testing Strategy

### Unit Tests to Add
For each refactored model, ensure:

```java
@Test
public void testEqualsUsingId() {
    Acl acl1 = new Acl(1L, "Admin");
    Acl acl2 = new Acl(1L, "Different Name");
    assertEquals(acl1, acl2);  // ✅ Same id = equal
}

@Test
public void testEqualsWithDifferentId() {
    Acl acl1 = new Acl(1L, "Admin");
    Acl acl2 = new Acl(2L, "Admin");
    assertNotEquals(acl1, acl2);  // ✅ Different id = not equal
}

@Test
public void testHashCodeConsistency() {
    Acl acl = new Acl(1L, "Admin");
    int hash1 = acl.hashCode();
    int hash2 = acl.hashCode();
    assertEquals(hash1, hash2);  // ✅ hashCode is idempotent
}

@Test
public void testHashCodeWithSameId() {
    Acl acl1 = new Acl(1L, "Admin");
    Acl acl2 = new Acl(1L, "Different");
    assertEquals(acl1.hashCode(), acl2.hashCode());  // ✅ Same id = same hash
}

@Test
public void testCollectionsNeverNull() {
    Lifecycle lc = new Lifecycle();
    assertNotNull(lc.getLifecycleStates());  // ✅ No null check in client code
    assertEquals(0, lc.getLifecycleStates().size());
}
```

---

## Summary of Model Classes & Recommended Actions

| Model | equals Needs Fix | Type Issue | Collection Issue | Naming Issue | Doc Missing |
|-------|---|---|---|---|---|
| Acl | ✅ Use id | - | - | Add setId() | ✅ |
| AclGroup | ✅ Use id only | - | Fix getter | - | ✅ |
| AclGroupPermission | ? | ? | ? | ? | ✅ |
| ChangeTrigger | Missing | - | - | - | ✅ |
| ChangeTriggerType | ? | ? | ? | ? | ✅ |
| ConfigEntry | ✅ Use id | - | - | Fix toString() | ✅ |
| Deletion | ✅ Add/use id | long→Long | - | - | ✅ |
| Folder | ✅ Use id | - | - | - | ✅ |
| FolderPath | ? | ? | ? | ? | ✅ |
| FolderType | ✅ Use id | - | - | Add setId() | ✅ |
| Format | ✅ Use id | - | - | - | ✅ |
| Group | ✅ Use id | - | - | - | ✅ |
| GroupUser | ? | ? | ? | ? | ✅ |
| IndexItem | ? | ? | ? | ? | ✅ |
| IndexMode | ? | ? | ? | ? | ✅ |
| Language | ✅ Use id | - | - | Add setId() | ✅ |
| Lifecycle | ✅ Use id | - | Fix pattern | - | ✅ |
| LifecycleState | ? | ? | ? | ? | ✅ |
| LoginType | ? | ? | ? | ? | ✅ |
| Meta | ✅ Use id | - | - | - | ✅ |
| MetasetType | ✅ Use id | Boolean→boolean | - | - | ✅ |
| ObjectSystemData | ✅ Simplify | - | Fix collections | - | ✅ |
| ObjectType | ✅ Use id | - | - | Add setId() | ✅ |
| Permission | ? | ? | ? | ? | ✅ |
| ProviderClass | ? | ? | ? | ? | ✅ |
| ProviderType | ? | ? | ? | ? | ✅ |
| Session | ✅ Use id | - | - | - | ✅ |
| UiLanguage | ✅ Use id | - | - | Add setId() | ✅ |
| UrlMappingInfo | ? | ? | ? | ? | ✅ |
| UserAccount | ✅ Simplify | Boolean→boolean | - | - | ✅ |

---

## Conclusion

This improvement plan addresses **5 systemic code quality issues** affecting model consistency, data integrity, and maintainability. Implementation in the suggested 4-phase approach minimizes risk while delivering immediate value:

- **Phase 1** prevents data corruption bugs
- **Phase 2** improves API consistency
- **Phase 3** enhances developer experience
- **Phase 4** ensures knowledge transfer

Estimated effort: **40-60 developer hours** across all models.


