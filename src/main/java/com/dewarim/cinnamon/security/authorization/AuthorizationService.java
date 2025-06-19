package com.dewarim.cinnamon.security.authorization;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.search.BrowsableAcls;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkResolver;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorizationService {

    public List<Folder> filterFoldersByBrowsePermission(List<Folder> folders, UserAccount user) {
        return folders.stream().filter(folder -> hasUserOrOwnerPermission(folder, DefaultPermission.BROWSE, user)).collect(Collectors.toList());
    }

    public List<ObjectSystemData> filterObjectsByBrowsePermission(List<ObjectSystemData> osds, UserAccount user) {
        return osds.stream().filter(osd -> hasUserOrOwnerPermission(osd, DefaultPermission.BROWSE, user)).collect(Collectors.toList());
    }

    public List<Link> filterLinksByBrowsePermission(List<Link> links, UserAccount user) {
        return links.stream()
                .filter(link -> hasUserOrOwnerPermission(link, DefaultPermission.BROWSE, user))
                .collect(Collectors.toList());
    }

    public List<Link> filterFolderLinksAndTargetsByBrowsePermission(List<Link> links, UserAccount user) {
        // first, filter links if they themselves are browsable:
        List<Link> filteredLinks = links.stream().filter(ownable -> hasUserOrOwnerPermission(ownable, DefaultPermission.BROWSE, user))
                .toList();

        // only return links if the target Folder is also browsable:
        List<Link>         results   = new ArrayList<>();
        Set<Long>          folderIds = filteredLinks.stream().map(Link::getFolderId).collect(Collectors.toSet());
        Map<Long, Ownable> ownables  = new FolderDao().getFoldersAsOwnables(folderIds).stream().collect(Collectors.toMap(Identifiable::getId, ownable -> ownable));
        for (Link link : filteredLinks) {
            Long ownableId = link.getFolderId();
            if (ownables.containsKey(ownableId)) {
                Ownable ownable = ownables.get(ownableId);
                if (hasUserOrOwnerPermission(ownable, DefaultPermission.BROWSE, user)) {
                    results.add(link);
                }
            }
        }
        return results;
    }

    public List<Link> filterOsdLinksAndTargetsByBrowsePermission(List<Link> links, UserAccount user) {
        // first, filter links if they themselves are browsable:
        List<Link> filteredLinks = links.stream().filter(ownable -> hasUserOrOwnerPermission(ownable, DefaultPermission.BROWSE, user))
                .toList();

        // resolve links pointing to latest head:
        List<Link>                  latestHeadLinks = filteredLinks.stream().filter(link -> link.getResolver() == LinkResolver.LATEST_HEAD).toList();
        OsdDao                      osdDao          = new OsdDao();
        Map<Long, ObjectSystemData> latestHeads     = osdDao.getLatestHeads(osdDao.getObjectsById(latestHeadLinks.stream().map(Link::getObjectId).toList()));
        for (Link latestHeadLink : latestHeadLinks) {
            latestHeadLink.setResolvedId(latestHeads.get(latestHeadLink.getObjectId()).getId());
        }
        Set<Long> fixedIds            = filteredLinks.stream().filter(link -> link.getResolver() == LinkResolver.FIXED).map(Link::getObjectId).collect(Collectors.toSet());
        Set<Long> latestHeadIds       = latestHeads.values().stream().map(ObjectSystemData::getId).collect(Collectors.toSet());
        Set<Long> fixedAndResolvedIds = new HashSet<>(fixedIds);
        fixedAndResolvedIds.addAll(latestHeadIds);

        // only return links if the target OSD is also browsable:
        List<Link>         results  = new ArrayList<>();
        Map<Long, Ownable> ownables = osdDao.getOsdsAsOwnables(fixedAndResolvedIds).stream().collect(Collectors.toMap(Identifiable::getId, ownable -> ownable));
        for (Link link : filteredLinks) {
            Long ownableId = link.getObjectId();
            if (link.getResolver() == LinkResolver.LATEST_HEAD) {
                ownableId = link.getResolvedId();
            }
            if (ownables.containsKey(ownableId)) {
                Ownable ownable = ownables.get(ownableId);
                if (hasUserOrOwnerPermission(ownable, DefaultPermission.BROWSE, user)) {
                    results.add(link);
                }
            }
        }
        return results;
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

    public boolean currentUserIsSuperuser() {
        return AccessFilter.getInstance(ThreadLocalSqlSession.getCurrentUser()).isSuperuser();
    }

    public boolean hasUserOrOwnerPermission(Ownable ownable, DefaultPermission permission, UserAccount user) {
        if (UserAccountDao.currentUserIsSuperuser()) {
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

    public BrowsableAcls getBrowsableAcls(UserAccount user) {
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        return new BrowsableAcls(accessFilter.getObjectAclsWithBrowsePermissions(), accessFilter.getOwnerAclsWithBrowsePermissions(), user.getId());
    }
}
