package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class FolderService {

    private static final Logger log = LogManager.getLogger(FolderService.class);

    public List<Folder> getSubFolders(long parentId, UserAccount user) {
        FolderDao    folderDao    = new FolderDao();
        List<Folder> subFolders  = folderDao.getDirectSubFolders(parentId, false);
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return subFolders.stream()
                .filter(accessFilter::hasBrowsePermissionForOwnable)
                .collect(Collectors.toList());
    }

    public List<ObjectSystemData> getFolderContent(long folderId, boolean latestHead, UserAccount user) {
        OsdDao         osdDao    = new OsdDao();
        VersionPredicate predicate = latestHead ? VersionPredicate.HEAD : VersionPredicate.ALL;
        List<ObjectSystemData> osds = osdDao.getObjectsByFolderId(folderId, false, predicate);
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return osds.stream()
                .filter(accessFilter::hasBrowsePermissionForOwnable)
                .collect(Collectors.toList());
    }

    /**
     * Resolves a slash-separated path (e.g. "/home/alice/docs") to the target Folder.
     * Returns empty if the path does not exist or the user cannot browse it.
     */
    public Folder resolvePath(String folderPath, UserAccount user) {
        FolderDao    folderDao = new FolderDao();
        List<Folder> ancestors = folderDao.getFolderByPathWithAncestors(folderPath, false);
        if (ancestors.isEmpty()) {
            throw ErrorCode.FOLDER_NOT_FOUND.exception();
        }
        Folder       target       = ancestors.getLast();
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        if (!accessFilter.hasBrowsePermissionForOwnable(target)) {
            throw ErrorCode.NO_BROWSE_PERMISSION.exception();
        }
        return target;
    }

    public String homeFolderPath(UserAccount user) {
        return "/home/" + user.getName();
    }

    /**
     * Ensures that the user's home folder (/home/<username>) exists.
     * If the /home parent exists but the user subfolder does not, it is created
     * with the default ACL and default folder type, owned by the user.
     * Returns the home folder (existing or newly created).
     */
    public Folder ensureHomeFolderExists(UserAccount user) {
        String    homePath = homeFolderPath(user);
        FolderDao folderDao = new FolderDao();

        List<Folder> existing = folderDao.getFolderByPathWithAncestors(homePath, false);
        if (!existing.isEmpty()) {
            return existing.getLast();
        }

        // Resolve default ACL and folder type (needed for both /home and /home/<username>)
        Acl defaultAcl = new AclDao().getAclByName(Constants.ACL_DEFAULT)
                .orElseThrow(() -> new RuntimeException("Default ACL '" + Constants.ACL_DEFAULT + "' not found"));
        FolderType defaultType = new FolderTypeDao().getFolderTypeByName(Constants.FOLDER_TYPE_DEFAULT)
                .orElseThrow(() -> new RuntimeException("Default folder type '" + Constants.FOLDER_TYPE_DEFAULT + "' not found"));

        // Find or create the /home parent folder
        List<Folder> homeParentFolders = folderDao.getFolderByPathWithAncestors("/home", false);
        Folder homeParent;
        if (homeParentFolders.isEmpty()) {
            Folder root = folderDao.getRootFolder(false);
            Long adminId = new UserAccountDao().getUserAccountByName("admin")
                    .map(UserAccount::getId)
                    .orElse(root.getOwnerId());
            homeParent = folderDao.saveFolder(new Folder("home", defaultAcl.getId(), adminId, root.getId(), defaultType.getId(), null));
            log.info("Created missing /home folder beneath root");
        } else {
            homeParent = homeParentFolders.getLast();
        }

        Folder newFolder = new Folder(user.getName(), defaultAcl.getId(), user.getId(), homeParent.getId(), defaultType.getId(), null);
        Folder created = folderDao.saveFolder(newFolder);
        log.info("Created home folder for user '{}' at {}", user.getName(), homePath);
        return created;
    }
}
