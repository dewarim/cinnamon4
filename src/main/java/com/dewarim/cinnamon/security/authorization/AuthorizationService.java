package com.dewarim.cinnamon.security.authorization;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorizationService {

    public List<Folder> filterFoldersByBrowsePermission(List<Folder> folders, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return folders.stream().filter(accessFilter::hasBrowsePermissionForOwnable).collect(Collectors.toList());
    }

    public List<ObjectSystemData> filterObjectsByBrowsePermission(List<ObjectSystemData> osds, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return osds.stream().filter(accessFilter::hasBrowsePermissionForOwnable).collect(Collectors.toList());
    }

    public List<Link> filterLinksByBrowsePermission(List<Link> links, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return links.stream().filter(link ->
                {
                    switch (link.getType()) {
                        case FOLDER:
                            return accessFilter.hasFolderBrowsePermission(link.getAclId())
                                    || (link.getOwnerId().equals(user.getId()) && accessFilter.hasOwnerBrowsePermission(link.getAclId()));
                        case OBJECT:
                            return accessFilter.hasBrowsePermissionForOwnable(link)
                                    || (link.getOwnerId().equals(user.getId()) && accessFilter.hasPermission(link.getAclId(), DefaultPermission.BROWSE_FOLDER, true));
                        default:
                            throw new IllegalStateException("unknown link type");
                    }

                }
        ).collect(Collectors.toList());
    }

    public boolean userHasPermission(Long aclId, DefaultPermission permission, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return accessFilter.hasPermission(aclId, permission);
    }

    public boolean userHasOwnerPermission(Long aclId, DefaultPermission permission, UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return accessFilter.hasPermission(aclId, permission, true);
    }

    public void throwUpUnlessUserOrOwnerHasPermission(
            Ownable ownable, DefaultPermission permission, UserAccount user, ErrorCode errorCode) {
        if (!hasUserOrOwnerPermission(ownable, permission, user)) {
            errorCode.throwUp();
        }
    }

    public boolean currentUserIsSuperuser(){
        return AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser()).isSuperuser();
    }

    public boolean hasUserOrOwnerPermission(Ownable ownable, DefaultPermission permission, UserAccount user) {
        if(UserAccountDao.currentUserIsSuperuser()){
            // Superuser is allowed to do everything.
            return true;
        }
        Long aclId = ownable.getAclId();
        if (userHasPermission(aclId, permission, user)) {
            return true;
        }
        if (ownable.getOwnerId().equals(user.getId())) {
            return userHasOwnerPermission(aclId, permission, user);
        }
        return false;
    }
}
