package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.DefaultPermissions;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.UserAccount;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorizationService {

    public List<ObjectSystemData> filterObjectsByBrowsePermission(List<ObjectSystemData> osds, UserAccount user) {
        Set<Long> browsableOsds = getBrowsableAcls(user.getId());
        // TODO: filter by dynamic acls like owner/everyone acl
        return osds.stream().filter(osd -> browsableOsds.contains(osd.getAclId())).collect(Collectors.toList());
    }

    private Set<Long> getBrowsableAcls(long userId) {
        AclDao aclDao = new AclDao();
        // TODO: cache results of getUserAcls

        List<Acl> userAcls = aclDao.getUserAcls(userId);
        PermissionDao permissionDao = new PermissionDao();
        Permission browsePermission = permissionDao.getPermissionByName(DefaultPermissions.BROWSE_OBJECT.getName());
        Set<Long> aclIdsWithBrowsePermission = new HashSet<>();
        userAcls.stream().forEach(acl -> {
            List<Permission> permissions = permissionDao.getUserPermissionForAcl(userId, acl.getId());
            Optional<Permission> permission = permissions
                    .stream()
                    .filter(p -> p.getId().equals(browsePermission.getId()))
                    .findFirst();
            if(permission.isPresent()){
                aclIdsWithBrowsePermission.add(acl.getId());
            }
        });
        
        return aclIdsWithBrowsePermission;
    }
}
