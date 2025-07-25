package com.dewarim.cinnamon.security.authorization;


import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Accessible;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A utility class to filter objects if the user does not have the required permissions.
 */
public class AccessFilter {

    static Logger log = LogManager.getLogger(AccessFilter.class);

    private static List<Acl>  acls;
    private static Permission browsePermission;
    private static Group      ownerGroup;

    private final        Set<Long>                   objectAclsWithBrowsePermissions;
    private final        Set<Long>                   ownerAclsWithBrowsePermissions;
    private final        Map<AclPermission, Boolean> checkedPermissions                      = new ConcurrentHashMap<>();
    private final        UserAccount                 user;
    private final        boolean                     superuser;
    private static final Map<Long, Set<Long>>        userAclsWithBrowsePermissionCache       = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>>        ownerAclsWithBrowsePermissionCache      = new ConcurrentHashMap<>();
    private static final Map<String, Permission>     nameToPermissionMapping                 = new ConcurrentHashMap<>();
    private static final Map<Long, Acl>              idToAclMapping                          = new ConcurrentHashMap<>();

    private static final Object  INITIALIZING = new Object();
    private static       Boolean initialized  = false;

    private AccessFilter(UserAccount user) {
        this.user = user;
        objectAclsWithBrowsePermissions = getUserAclsWithBrowsePermissions(user);
        ownerAclsWithBrowsePermissions = getOwnerAclsWithBrowsePermissions(user);
        superuser = new UserAccountDao().isSuperuser(user);
    }

    public static synchronized AccessFilter getInstance(UserAccount user) {
        synchronized (INITIALIZING) {
            if (!initialized) {
                initialize();
            }
        }
        return new AccessFilter(user);
    }

    public boolean hasUserBrowsePermission(Long aclId) {
        return objectAclsWithBrowsePermissions.contains(aclId);
    }

    public boolean hasOwnerBrowsePermission(Long aclId) {
        return ownerAclsWithBrowsePermissions.contains(aclId);
    }

    /**
     * Check if the user has browse permission for a given thing, either through the object's acl or as owner.
     */
    public boolean hasBrowsePermissionForOwnable(Ownable ownable) {
        if (superuser) {
            return true;
        }
        long aclId = ownable.getAclId();
        return hasUserBrowsePermission(aclId) || (ownable.getOwnerId().equals(user.getId()) && hasOwnerBrowsePermission(aclId));
    }

    public boolean hasPermission(long aclId, DefaultPermission permission) {
        if (superuser) {
            return true;
        }
        return hasPermission(aclId, permission, false);
    }

    public boolean hasPermission(long aclId, DefaultPermission defaultPermission, boolean checkOwnerPermission) {
        if (superuser) {
            return true;
        }
        Permission permission = nameToPermissionMapping.get(defaultPermission.getName());
        if (permission == null) {
            String message = String.format("unknown permission name '%s' was used with aclId %d", defaultPermission.getName(), aclId);
            throw new IllegalStateException(message);
        }
        Acl acl = idToAclMapping.get(aclId);
        if (acl == null) {
            throw new IllegalStateException(String.format("unknown acl id %d was used.", aclId));
        }

        AclPermission aclPermission = new AclPermission(aclId, permission.getId(), checkOwnerPermission);
        if (checkedPermissions.containsKey(aclPermission)) {
            return checkedPermissions.get(aclPermission);
        }

        if (checkOwnerPermission) {
            // generate all AclPermissions available for this permission.
            Set<Long> ownerAclIdSet = generateOwnerAclIdSet(permission, user);
            ownerAclIdSet.forEach(id -> {
                        AclPermission aclPerm = new AclPermission(aclId, permission.getId(), true);
                        checkedPermissions.put(aclPerm, true);
                    }
            );
            acls.forEach(myAcl -> {
                if (!ownerAclIdSet.contains(myAcl.getId())) {
                    AclPermission aclPerm = new AclPermission(myAcl.getId(), permission.getId(), true);
                    checkedPermissions.put(aclPerm, false);
                }
            });
            return checkedPermissions.get(new AclPermission(aclId, permission.getId(), true));
        }

        boolean checkResult = checkAclGroups(acl, permission, user);
        checkedPermissions.put(aclPermission, checkResult);
        return checkResult;
    }

    public static void reload() {
        synchronized (INITIALIZING) {
            initialized = false;
        }
    }

    public static void reloadUser(Long userId) {
        userAclsWithBrowsePermissionCache.remove(userId);
        ownerAclsWithBrowsePermissionCache.remove(userId);
    }

    private static void initialize() {
        synchronized (INITIALIZING) {
            PermissionDao permissionDao = new PermissionDao();
            browsePermission = permissionDao.getPermissionByName(DefaultPermission.BROWSE.getName());
            AclDao aclDao = new AclDao();
            acls = aclDao.list();
            acls.forEach(acl -> idToAclMapping.put(acl.getId(), acl));
            ownerGroup = new GroupDao().getOwnerGroup();
            List<Permission> permissions = permissionDao.list();
            permissions.forEach(permission -> nameToPermissionMapping.put(permission.getName(), permission));
            initialized = true;
        }
    }

    private static Set<Long> getUserAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!userAclsWithBrowsePermissionCache.containsKey(userId)) {
            long startTime = System.currentTimeMillis();
            log.info("Generating object acls list with browse permissions for user {}", user);
            userAclsWithBrowsePermissionCache.put(userId, generateObjectAclSet(browsePermission, user));
            long endTime = System.currentTimeMillis();
            log.info("object acl list generated in {} ms", endTime - startTime);
        }
        return userAclsWithBrowsePermissionCache.get(userId);
    }



    private static Set<Long> getOwnerAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!ownerAclsWithBrowsePermissionCache.containsKey(userId)) {
            long startTime = System.currentTimeMillis();
            log.info("Generating owner acls list with browse permissions for user {}", user);
            ownerAclsWithBrowsePermissionCache.put(userId, generateOwnerAclIdSet(browsePermission, user));
            long endTime = System.currentTimeMillis();
            log.info("owner acl list generated in {} ms", endTime - startTime);
        }
        return ownerAclsWithBrowsePermissionCache.get(userId);
    }

    private static synchronized Set<Long> generateObjectAclSet(Permission permission, UserAccount user) {
        UserAccountDao userDao = new UserAccountDao();
        if (userDao.isSuperuser(user)) {
            // Superusers are exempt from permission checking, so they automatically have BrowsePermission on all objects.
            return acls.stream().map(Acl::getId).collect(Collectors.toSet());
        }
        Set<Long> aclIds = new HashSet<>();

        for (Acl acl : acls) {
            // compute browse permissions for acls
            boolean checkAclResult = checkAclGroups(acl, permission, user);
            if (checkAclResult) {
                aclIds.add(acl.getId());
            }

        }
        return aclIds;
    }

    private static boolean checkAclGroups(Acl acl, Permission permission, UserAccount user) {
        // create Union of Sets: user.groups and acl.groups => iterate over each group for permitlevel.

        Set<Group>     userGroups  = new GroupDao().getGroupsWithAncestorsOfUserById(user.getId());
        List<Long>     groupIds    = userGroups.stream().map(Group::getId).collect(Collectors.toList());
        AclGroupDao    aclGroupDao = new AclGroupDao();
        List<AclGroup> aclGroups   = aclGroupDao.getAclGroupsByGroupIdsAndAcl(groupIds, acl.getId());

        Optional<AclGroup> everyoneAclGroup = aclGroupDao.getAclGroupForEveryoneGroup(acl.getId());
        everyoneAclGroup.ifPresent(aclGroups::add);

        AclGroupPermissionDao aepDao = new AclGroupPermissionDao();
        return aepDao.checkIfAnyAclGroupHasThisPermission(aclGroups, permission);
    }

    private static Set<Long> generateOwnerAclIdSet(Permission permission, UserAccount user) {
        UserAccountDao userDao = new UserAccountDao();
        if (userDao.isSuperuser(user)) {
            // Superusers are exempt from permission checking, so they automatically have BrowsePermission on all objects.
            return acls.stream().map(Acl::getId).collect(Collectors.toSet());
        }

        List<AclGroup> ownerAclGroups = new AclGroupDao().getAclGroupsByGroup(ownerGroup);
        return new AclGroupPermissionDao()
                .filterAclGroupsWithoutThisPermission(ownerAclGroups, permission)
                .stream()
                .map(AclGroup::getAclId)
                .collect(Collectors.toSet());
    }

    public void verifyHasPermissionOnOwnable(Accessible accessible, DefaultPermission permission, Ownable ownable, ErrorCode errorCode) {
        if (!hasPermissionOnOwnable(accessible, permission, ownable)) {
            errorCode.throwUp();
        }
    }

    /**
     * Verify if user has permission to change / access an object.
     * Check both permissions of the owner (if the user is the owner)
     * and the generic acl permission.
     * @param accessible an object that gives us the ACL to use
     * @param permission the permission we want to check
     * @param ownable the ownable, which may be different from the accessible (for
     *                example, a link may have an ACL but point to an ownable OSD)
     * @return true if the user has permission
     * <p>
     * TODO: refactor, the use case of ownable != accessible seems no longer to be checked with this method,
     * so accessible=ownable in all existing use cases -> we can remove one parameter
     */
    public boolean hasPermissionOnOwnable(Accessible accessible, DefaultPermission permission, Ownable ownable) {
        if (superuser) {
            return true;
        }
        Long aclId = accessible.getAclId();
        if (aclId == null) {
            throw new IllegalArgumentException("Cannot check permissions without the accessible providing an aclId!");
        }
        if (ownable.getOwnerId() != null && user.getId().equals(ownable.getOwnerId())) {
            return hasPermission(aclId, permission) || hasPermission(aclId, permission, true);
        }
        return hasPermission(aclId, permission);
    }

    public boolean isSuperuser() {
        return superuser;
    }

    public Set<Long> getObjectAclsWithBrowsePermissions() {
        return objectAclsWithBrowsePermissions;
    }

    public Set<Long> getOwnerAclsWithBrowsePermissions() {
        return ownerAclsWithBrowsePermissions;
    }
}
