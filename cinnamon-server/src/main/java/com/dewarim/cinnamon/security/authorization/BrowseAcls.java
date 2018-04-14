package com.dewarim.cinnamon.security.authorization;


import com.dewarim.cinnamon.DefaultPermissions;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class to filter OSDs from search results for whom the user does not have browse permissions.
 */
public class BrowseAcls {

    static Logger log = LogManager.getLogger(BrowseAcls.class);

    private static List<Acl> acls;
    private static Permission browsePermission;
    private static CmnGroup ownerGroup;
    
    private Set<Long> objectAclsWithBrowsePermissions;
    private Set<Long> ownerAclsWithBrowsePermissions;
    private long userId;
    private static final Map<Long, Set<Long>> userAclsWithBrowsePermissionCache = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> ownerAclsWithBrowsePermissionCache = new ConcurrentHashMap<>();

    private static Object INITIALIZING = new Object();
    private static Boolean initialized = false;

    private BrowseAcls(UserAccount user) {
        userId = user.getId();
        objectAclsWithBrowsePermissions = getUserAclsWithBrowsePermissions(user);
        ownerAclsWithBrowsePermissions = getOwnerAclsWithBrowsePermissions(user);
    }

    public static BrowseAcls getInstance(UserAccount user) {
        if (!initialized) {
            synchronized (INITIALIZING) {
                if (!initialized) {
                    initialize();
                }
            }
        }
        return new BrowseAcls(user);
    }

    public boolean hasUserBrowsePermission(Long aclId) {
        return objectAclsWithBrowsePermissions.contains(aclId);
    }

    public boolean hasOwnerBrowsePermission(Long aclId) {
        return ownerAclsWithBrowsePermissions.contains(aclId);
    }

    public boolean hasBrowsePermissionForOsd(ObjectSystemData osd) {
        long aclId = osd.getAclId();
        return hasUserBrowsePermission(aclId) || (osd.getOwnerId().equals(userId) && hasOwnerBrowsePermission(aclId));
    }

    public static void reload() {
        synchronized (INITIALIZING) {
            initialized = false;
        }
    }

    public static void reloadUser(UserAccount user) {
        synchronized (userAclsWithBrowsePermissionCache) {
            userAclsWithBrowsePermissionCache.remove(user.getId());
        }
        synchronized (ownerAclsWithBrowsePermissionCache) {
            ownerAclsWithBrowsePermissionCache.remove(user.getId());
        }
    }

    private static void initialize() {
        synchronized (INITIALIZING) {
            PermissionDao permissionDao = new PermissionDao();
            browsePermission = permissionDao.getPermissionByName(DefaultPermissions.BROWSE_OBJECT.getName());
            AclDao aclDao = new AclDao();
            acls = aclDao.list();
            ownerGroup = new CmnGroupDao().getOwnerGroup();
        }

    }

    private static Set<Long> getUserAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!userAclsWithBrowsePermissionCache.containsKey(userId)) {
            synchronized (userAclsWithBrowsePermissionCache) {
                long startTime = System.currentTimeMillis();
                log.info("Generating object acls list with browse permissions for user " + user);
                userAclsWithBrowsePermissionCache.put(userId, generateObjectAclSet(user));
                long endTime = System.currentTimeMillis();
                log.info("object acl list generated in " + (endTime - startTime) + " ms");
            }
        }
        return userAclsWithBrowsePermissionCache.get(userId);
    }

    private static Set<Long> getOwnerAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!ownerAclsWithBrowsePermissionCache.containsKey(userId)) {
            synchronized (ownerAclsWithBrowsePermissionCache) {
                long startTime = System.currentTimeMillis();
                log.info("Generating owner acls list with browse permissions for user " + user);
                ownerAclsWithBrowsePermissionCache.put(userId, generateOwnerAclIdSet(browsePermission, user));
                long endTime = System.currentTimeMillis();
                log.info("owner acl list generated in " + (endTime - startTime) + " ms");

            }
        }
        return ownerAclsWithBrowsePermissionCache.get(userId);
    }

    private static Set<Long> generateObjectAclSet(UserAccount user) {
        UserAccountDao userDao = new UserAccountDao();
        if (userDao.isSuperuser(user)) {
            // Superusers are exempt from permission checking, so they automatically have BrowsePermission on all objects.
            return acls.stream().map(Acl::getId).collect(Collectors.toSet());
        }
        Set<Long> aclIds = new HashSet<>();

        for (Acl acl : acls) {
            // compute browse permissions for acls
            boolean checkAclResult = checkObjectAclEntries(acl, browsePermission, user);
            if (checkAclResult) {
                aclIds.add(acl.getId());
            }

        }
        return aclIds;
    }

    private static boolean checkObjectAclEntries(Acl acl, Permission permission, UserAccount user) {
        // create Union of Sets: user.groups and acl.groups => iterate over each group for permitlevel.

        Set<CmnGroup> userGroups = new CmnGroupDao().getGroupsWithAncestorsOfUserById(user.getId());
        List<Long> groupIds = userGroups.stream().map(CmnGroup::getId).collect(Collectors.toList());
        AclEntryDao aclEntryDao = new AclEntryDao();
        List<AclEntry> aclEntries = aclEntryDao.getAclEntriesByGroupIdsAndAcl(groupIds, acl.getId());

        Optional<AclEntry> everyoneAclEntry = aclEntryDao.getAclEntryForEveryoneGroup(acl.getId());
        everyoneAclEntry.ifPresent(aclEntries::add);

        AclEntryPermissionDao aepDao = new AclEntryPermissionDao();
        return aepDao.checkIfAnyAclEntryHasThisPermission(aclEntries, permission);
    }

    private static Set<Long> generateOwnerAclIdSet(Permission permission, UserAccount user) {
        UserAccountDao userDao = new UserAccountDao();
        if (userDao.isSuperuser(user)) {
            // Superusers are exempt from permission checking, so they automatically have BrowsePermission on all objects.
            return acls.stream().map(Acl::getId).collect(Collectors.toSet());
        }

        List<AclEntry> ownerAclEntries =  new AclEntryDao().getAclEntriesByGroup(ownerGroup);
        return new AclEntryPermissionDao()
                .filterAclEntriesWithoutThisPermission(ownerAclEntries, permission)
                .stream()
                .map(AclEntry::getAclId)
                .collect(Collectors.toSet());
    }

}