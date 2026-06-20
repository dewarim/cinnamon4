package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    public Folder createFolder(String name, Long parentId, Long aclId, Long typeId, String summary, Long ownerId, UserAccount user) {
        FolderDao folderDao    = new FolderDao();
        Folder    parentFolder = folderDao.getFolderById(parentId).orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND::exception);
        AccessFilter filter = AccessFilter.getInstance(user);
        if (!filter.hasPermissionOnOwnable(parentFolder, DefaultPermission.CREATE_FOLDER, parentFolder)) {
            throw ErrorCode.NO_CREATE_PERMISSION.exception();
        }
        folderDao.getFolderByParentAndName(parentFolder.getId(), name, false)
                .ifPresent(f -> ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN.throwUp());

        FolderTypeDao typeDao = new FolderTypeDao();
        Long resolvedTypeId = typeId == null
                ? typeDao.getFolderTypeByName(Constants.FOLDER_TYPE_DEFAULT).orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND::exception).getId()
                : typeDao.getFolderTypeById(typeId).orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND::exception).getId();

        Long resolvedAclId = aclId == null
                ? parentFolder.getAclId()
                : new AclDao().getObjectById(aclId).orElseThrow(ErrorCode.ACL_NOT_FOUND::exception).getId();

        Long resolvedOwnerId = ownerId != null ? ownerId : user.getId();
        Folder newFolder = new Folder(name, resolvedAclId, resolvedOwnerId, parentId, resolvedTypeId, summary);
        if (user.isChangeTracking()) {
            newFolder.setMetadataChanged(true);
        }
        return folderDao.saveFolder(newFolder);
    }

    public void updateFolder(Long folderId, String name, Long parentId, Long aclId, Long ownerId, Long typeId,
                             Boolean metadataChangedOverride, UserAccount user) {
        FolderDao    folderDao = new FolderDao();
        Folder       folder    = folderDao.getFolderById(folderId, true).orElseThrow(ErrorCode.FOLDER_NOT_FOUND::exception);
        AccessFilter filter    = AccessFilter.getInstance(user);

        boolean changed  = false;
        boolean reIndex  = false;

        if (name != null) {
            if (!filter.hasPermissionOnOwnable(folder, DefaultPermission.SET_NAME, folder)) {
                throw ErrorCode.NO_NAME_WRITE_PERMISSION.exception();
            }
            Folder parent = folder.getParentId() == null
                    ? folderDao.getRootFolder(false)
                    : folderDao.getFolderById(folder.getParentId()).orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND::exception);
            folderDao.getFolderByParentAndName(parent.getId(), name, false)
                    .ifPresent(f -> ErrorCode.DUPLICATE_FOLDER_NAME_FORBIDDEN.throwUp());
            folder.setName(name);
            changed = true;
            reIndex = true;
        }

        if (parentId != null) {
            if (parentId.equals(folderId)) {
                ErrorCode.CANNOT_MOVE_FOLDER_INTO_ITSELF.throwUp();
            }
            Folder newParent = folderDao.getFolderById(parentId).orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND::exception);
            if (!filter.hasPermissionOnOwnable(newParent, DefaultPermission.CREATE_FOLDER, newParent)) {
                throw ErrorCode.NO_CREATE_PERMISSION.exception();
            }
            if (!parentId.equals(folder.getParentId())) {
                if (!filter.hasPermissionOnOwnable(folder, DefaultPermission.SET_PARENT, folder)) {
                    throw ErrorCode.NO_SET_PARENT_PERMISSION.exception();
                }
                folder.setParentId(newParent.getId());
                changed = true;
                reIndex = true;
            }
        }

        if (aclId != null && !aclId.equals(folder.getAclId())) {
            if (!filter.hasPermissionOnOwnable(folder, DefaultPermission.SET_ACL, folder)) {
                throw ErrorCode.MISSING_SET_ACL_PERMISSION.exception();
            }
            folder.setAclId(new AclDao().getObjectById(aclId).orElseThrow(ErrorCode.ACL_NOT_FOUND::exception).getId());
            changed = true;
        }

        if (ownerId != null && !ownerId.equals(folder.getOwnerId())) {
            if (!filter.hasPermissionOnOwnable(folder, DefaultPermission.SET_OWNER, folder)) {
                throw ErrorCode.NO_SET_OWNER_PERMISSION.exception();
            }
            folder.setOwnerId(new UserAccountDao().getUserAccountById(ownerId)
                    .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND::exception).getId());
            changed = true;
        }

        if (typeId != null && !typeId.equals(folder.getTypeId())) {
            if (!filter.hasPermissionOnOwnable(folder, DefaultPermission.SET_TYPE, folder)) {
                throw ErrorCode.NO_TYPE_WRITE_PERMISSION.exception();
            }
            folder.setTypeId(new FolderTypeDao().getFolderTypeById(typeId)
                    .orElseThrow(ErrorCode.FOLDER_TYPE_NOT_FOUND::exception).getId());
            changed = true;
        }

        if (metadataChangedOverride != null) {
            if (user.isChangeTracking()) {
                throw ErrorCode.CHANGED_FLAG_ONLY_USABLE_BY_UNTRACKED_USERS.exception();
            }
            folder.setMetadataChanged(metadataChangedOverride);
            changed = true;
        } else if (changed && user.isChangeTracking()) {
            folder.setMetadataChanged(true);
        }

        if (changed) {
            folderDao.updateFolder(folder);
            if (reIndex) {
                IndexJobDao indexJobDao = new IndexJobDao();
                indexJobDao.reIndexFolderContent(folderId);
                List<Long> subFolderIds = folderDao.getRecursiveSubFolderIds(folderId);
                if (!subFolderIds.isEmpty()) {
                    indexJobDao.reindexFolders(subFolderIds);
                    for (Long subFolderId : subFolderIds) {
                        indexJobDao.insertIndexJob(new IndexJob(IndexJobType.FOLDER, subFolderId, IndexJobAction.UPDATE), true);
                        indexJobDao.reIndexFolderContent(subFolderId);
                    }
                }
            }
        }
    }

    public void deleteFolder(Set<Long> folderIds, boolean deleteRecursively, boolean deleteContent, UserAccount user) {
        FolderDao         folderDao         = new FolderDao();
        DeleteOsdService  deleteOsdService  = new DeleteOsdService();
        DeleteLinkService deleteLinkService = new DeleteLinkService();
        AuthorizationService auth           = new AuthorizationService();

        List<Folder> folders   = collectFolders(folderIds, deleteRecursively, deleteContent, folderDao);
        for (Folder f : folders) {
            auth.throwUpUnlessUserOrOwnerHasPermission(f, DefaultPermission.DELETE, user, ErrorCode.NO_DELETE_PERMISSION);
        }

        List<Long> allFolderIds = folders.stream().map(Folder::getId).collect(Collectors.toList());
        OsdDao     osdDao       = new OsdDao();
        if (deleteContent) {
            for (Folder f : folders) {
                List<ObjectSystemData> osds = osdDao.getObjectsByFolderId(f.getId(), false, VersionPredicate.ALL);
                deleteOsdService.verifyAndDelete(osds, true, true, user);
            }
        } else if (folderDao.hasContent(allFolderIds)) {
            throw ErrorCode.FOLDER_IS_NOT_EMPTY.exception();
        }

        if (folderDao.hasSubfolders(allFolderIds) && !deleteRecursively) {
            throw ErrorCode.FOLDER_HAS_SUBFOLDERS.exception();
        }

        LinkDao    linkDao = new LinkDao();
        List<Link> links   = linkDao.getLinksToOutsideStuff(allFolderIds);
        deleteLinkService.verifyAndDelete(links, user, linkDao);
        linkDao.deleteAllLinksToFolders(allFolderIds);

        new FolderMetaDao().deleteByFolderIds(allFolderIds);
        folderDao.delete(allFolderIds);
    }

    private List<Folder> collectFolders(Set<Long> idSet, boolean recursively, boolean deleteContent, FolderDao folderDao) {
        List<Long>   ids     = new ArrayList<>(idSet);
        List<Folder> folders = folderDao.getFoldersById(ids, false);
        if (folders.size() != ids.size()) {
            throw ErrorCode.FOLDER_NOT_FOUND.getException().get();
        }
        List<Folder> result = new ArrayList<>(folders);
        if (recursively) {
            for (Folder folder : folders) {
                List<Folder> sub = folderDao.getDirectSubFolders(folder.getId(), false);
                if (!sub.isEmpty()) {
                    result.addAll(collectFolders(sub.stream().map(Folder::getId).collect(Collectors.toSet()),
                            true, deleteContent, folderDao));
                }
            }
        }
        return result;
    }

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
