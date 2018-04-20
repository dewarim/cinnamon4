package com.dewarim.cinnamon.security.authorization;


import com.dewarim.cinnamon.DefaultPermissions;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A utility class to filter OSDs from search results for whom the user does not have browse permissions.
 */
public class BrowseAcls {

    static Logger log = LogManager.getLogger(BrowseAcls.class);

    private static List<Acl> acls;
    private static Permission browsePermission;
    private static Permission folderBrowsePermission;
    private static CmnGroup ownerGroup;

    private Set<Long> objectAclsWithBrowsePermissions;
    private Set<Long> ownerAclsWithBrowsePermissions;
    private Set<Long> folderAclsWithBrowsePermissions;
    private Map<AclPermission, Boolean> checkedPermissions = new ConcurrentHashMap<>();
    private UserAccount user;
    private static final Map<Long, Set<Long>> userAclsWithBrowsePermissionCache = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> userAclsWithFolderBrowsePermissionCache = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> ownerAclsWithBrowsePermissionCache = new ConcurrentHashMap<>();
    private static final Map<String, Permission> nameToPermissionMapping = new ConcurrentHashMap<>();
    private static Map<Long,Acl> idToAclMapping = new ConcurrentHashMap<>();
    
    private static Object INITIALIZING = new Object();
    private static Boolean initialized = false;

    private BrowseAcls(UserAccount user) {
        this.user = user;
        objectAclsWithBrowsePermissions = getUserAclsWithBrowsePermissions(user);
        ownerAclsWithBrowsePermissions = getOwnerAclsWithBrowsePermissions(user);
        folderAclsWithBrowsePermissions = getFolderAclsWithBrowsePermissions(user);
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
    
    public boolean hasFolderBrowsePermission(Long aclId){
        return folderAclsWithBrowsePermissions.contains(aclId);
    }
    
    public boolean hasBrowsePermissionForOsd(ObjectSystemData osd) {
        long aclId = osd.getAclId();
        return hasUserBrowsePermission(aclId) || (osd.getOwnerId().equals(user.getId()) && hasOwnerBrowsePermission(aclId));
    }    
    
    public boolean hasBrowsePermissionForLink(Link link) {
        long aclId = link.getAclId();
        return hasUserBrowsePermission(aclId) || (link.getOwnerId().equals(user.getId()) && hasOwnerBrowsePermission(aclId));
    }

    public boolean hasPermission(long aclId, String permissionName){
        Permission permission = nameToPermissionMapping.get(permissionName);
        if(permission== null){
            throw new IllegalStateException("unknown permission name was used.");
        }
        Acl acl = idToAclMapping.get(aclId);
        if(acl == null){
            throw new IllegalStateException("unknown acl id was used.");
        }
        AclPermission aclPermission = new AclPermission(aclId,permission.getId());
        if(checkedPermissions.containsKey(aclPermission)){
            return checkedPermissions.get(aclPermission);
        }
        boolean checkResult = checkAclEntries(acl, permission, user);
        checkedPermissions.put(aclPermission, checkResult);
        return checkResult;
    }
    
    public static void reload() {
        synchronized (INITIALIZING) {
            initialized = false;
        }
    }

    public static void reloadUser(UserAccount user) {
        userAclsWithBrowsePermissionCache.remove(user.getId());
        ownerAclsWithBrowsePermissionCache.remove(user.getId());
        userAclsWithFolderBrowsePermissionCache.remove(user.getId());
    }

    private static void initialize() {
        synchronized (INITIALIZING) {
            PermissionDao permissionDao = new PermissionDao();
            browsePermission = permissionDao.getPermissionByName(DefaultPermissions.BROWSE_OBJECT.getName());
            folderBrowsePermission = permissionDao.getPermissionByName(DefaultPermissions.BROWSE_FOLDER.getName());
            AclDao aclDao = new AclDao();
            acls = aclDao.list();
            acls.forEach(acl -> idToAclMapping.put(acl.getId(), acl));
            ownerGroup = new CmnGroupDao().getOwnerGroup();
            List<Permission> permissions = permissionDao.listPermissions();
            permissions.forEach(permission -> nameToPermissionMapping.put(permission.getName(),permission));
        }

    }

    private static Set<Long> getUserAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!userAclsWithBrowsePermissionCache.containsKey(userId)) {
            long startTime = System.currentTimeMillis();
            log.info("Generating object acls list with browse permissions for user " + user);
            userAclsWithBrowsePermissionCache.put(userId, generateObjectAclSet(browsePermission, user));
            long endTime = System.currentTimeMillis();
            log.info("object acl list generated in " + (endTime - startTime) + " ms");
        }
        return userAclsWithBrowsePermissionCache.get(userId);
    }

    private static Set<Long> getFolderAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!userAclsWithFolderBrowsePermissionCache.containsKey(userId)) {
            long startTime = System.currentTimeMillis();
            log.info("Generating object acls list with folder browse permissions for user " + user);
            userAclsWithFolderBrowsePermissionCache.put(userId, generateObjectAclSet(folderBrowsePermission, user));
            long endTime = System.currentTimeMillis();
            log.info("folder acl list generated in " + (endTime - startTime) + " ms");
        }
        return userAclsWithFolderBrowsePermissionCache.get(userId);
    }

    private static Set<Long> getOwnerAclsWithBrowsePermissions(UserAccount user) {
        Long userId = user.getId();
        if (!ownerAclsWithBrowsePermissionCache.containsKey(userId)) {
            long startTime = System.currentTimeMillis();
            log.info("Generating owner acls list with browse permissions for user " + user);
            ownerAclsWithBrowsePermissionCache.put(userId, generateOwnerAclIdSet(browsePermission, user));
            long endTime = System.currentTimeMillis();
            log.info("owner acl list generated in " + (endTime - startTime) + " ms");

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
            boolean checkAclResult = checkAclEntries(acl, permission, user);
            if (checkAclResult) {
                aclIds.add(acl.getId());
            }

        }
        return aclIds;
    }

    private static boolean checkAclEntries(Acl acl, Permission permission, UserAccount user) {
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

        List<AclEntry> ownerAclEntries = new AclEntryDao().getAclEntriesByGroup(ownerGroup);
        return new AclEntryPermissionDao()
                .filterAclEntriesWithoutThisPermission(ownerAclEntries, permission)
                .stream()
                .map(AclEntry::getAclId)
                .collect(Collectors.toSet());
    }

}
