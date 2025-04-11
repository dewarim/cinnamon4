package com.dewarim.cinnamon.application.service;

import com.beust.jcommander.Strings;
import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.IdAndRootId;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.Deletion;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkResolver;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DeleteOsdService {

    private static final Logger log = LogManager.getLogger(DeleteOsdService.class);

    private final  AuthorizationService authorizationService = new AuthorizationService();
    private static Pattern              MAIN_BRANCH          = Pattern.compile("\\d+");

    public void verifyAndDelete(List<ObjectSystemData> osds, boolean deleteDescendants, boolean deleteAllVersions, UserAccount user) {
        if (osds.isEmpty()) {
            return;
        }
        OsdDao osdDao = new OsdDao();
        Set<Long> osdIds = osds.stream().map(osd -> {
                    if (deleteAllVersions) {
                        // TODO: should the DB just return osd.id if osd.rootId is null?
                        return Objects.requireNonNullElse(osd.getRootId(), osd.getId());
                    }
                    else {
                        return osd.getId();
                    }
                }
        ).collect(Collectors.toSet());

        Set<Long>           descendants = new HashSet<>();
        List<CinnamonError> errors      = new ArrayList<>();

        /*
         * Add all descendants to list of OSDs we want to delete.
         * Verify that previously unknown descendants are only added if deleteDescendants is true.
         * It's okay for the user to manually specify an osd and all its descendants for deletion
         * in one request without setting deleteDescendants to true.
         */
        osdIds.forEach(id -> {
                    Set<Long> osdAndDescendants = osdDao.getOsdIdByIdWithDescendants(id);
                    if (deleteDescendants) {
                        osdAndDescendants.remove(id);
                        descendants.addAll(osdAndDescendants);
                    }
                    else if (!osdIds.containsAll(osdAndDescendants)) {
                        CinnamonError error = new CinnamonError(ErrorCode.OBJECT_HAS_DESCENDANTS.getCode(), id);
                        errors.add(error);
                    }
                }
        );
        osds.addAll(osdDao.getObjectsById(new ArrayList<>(descendants), false));
        osdIds.addAll(descendants);

        /*
         * Check for protected relations.
         * It's okay to delete an object with a protected relation if the protected target objects are
         * also included in the list of osdIds.
         * Challenge: we cannot call getProtectedRelations in batch mode since then id from batch1 may
         * be related to an id from batch2.
         * Solution: fetch all relations for those ids (in batch mode) and check manually.
         */

        List<Relation>          protectedRelations = new RelationDao().getProtectedRelations(new ArrayList<>(osdIds));
        Map<Long, RelationType> relationTypes      = new RelationTypeDao().getRelationTypeMap(protectedRelations.stream().map(Relation::getTypeId).collect(Collectors.toSet()));
        if (protectedRelations.size() > 0) {
            protectedRelations.forEach(relation -> {
                var leftId       = relation.getLeftId();
                var rightId      = relation.getRightId();
                var relationType = relationTypes.get(relation.getTypeId());
                if (
                    // if both related  objects will be deleted, ignore their protections vs each other
                        (osdIds.contains(leftId) && osdIds.contains(rightId)) ||
                                // if only left is protected and will be deleted, that's okay too.
                                (osdIds.contains(leftId) && !relationType.isLeftObjectProtected()) ||
                                // and finally, if only right is protected and will be deleted, continue.
                                (osdIds.contains(rightId) && !relationType.isRightObjectProtected())
                ) {
                    return;
                }
                // maybe: add serialized relation for client side error handling?
                CinnamonError error = new CinnamonError(ErrorCode.OBJECT_HAS_PROTECTED_RELATIONS.getCode(), relation.toString());
                errors.add(error);
            });
        }

        errors.addAll(delete(osds, user));

        if (errors.size() > 0) {
            throw new FailedRequestException(ErrorCode.CANNOT_DELETE_DUE_TO_ERRORS, errors);
        }
        List<Long> osdIdsToToDelete = new ArrayList<>(osdIds).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        log.debug("delete {}", Strings.join(",", osdIdsToToDelete.stream().map(String::valueOf).collect(Collectors.toList())));
        var relationDao = new RelationDao();
        relationDao.deleteAllUnprotectedRelationsOfObjects(osdIdsToToDelete);
        relationDao.delete(protectedRelations.stream().map(Relation::getId).collect(Collectors.toList()));
        // TODO: in case of more than 32K ids, split osdIds into sub-lists for deletion
        LinkDao linkDao = new LinkDao();
        Set<IdAndRootId> idAndRootIds = osdDao.getIdAndRootsById(osdIdsToToDelete);
        List<Long> linkIdsToDelete = new ArrayList<>();
        List<Link> linksWeMayWantToDelete = linkDao.getLinksWeMayWantToDelete(osdIdsToToDelete);
        linksWeMayWantToDelete.forEach(link -> {
            if(link.getResolver()== LinkResolver.FIXED) {
                linkIdsToDelete.add(link.getId());
            }
            else {
                Optional<IdAndRootId> idAndRootIdOpt = idAndRootIds.stream().filter(i -> i.getId().equals(link.getObjectId())).findFirst();
                if(idAndRootIdOpt.isPresent()) {
                    IdAndRootId idAndRootId = idAndRootIdOpt.get();
                    if(idAndRootId.getId().equals(idAndRootId.getRootId())) {
                        // the link resolves to the root id, which we will delete: delete the link, too
                        linkIdsToDelete.add(link.getId());
                    }
                    else{
                        // the link currently resolves to this id, so we delete the OSD and update the link
                        // to point to the root id, which should be safe.
                        link.setObjectId(idAndRootId.getRootId());
                        linkDao.updateLink(link);
                    }
                }

            }
        });
        linkDao.delete(linkIdsToDelete);

        new OsdMetaDao().deleteByOsdIds(osdIdsToToDelete);
        osdDao.deleteOsds(osdIdsToToDelete);

        Set<Long>              predecessorIds = osds.stream().map(ObjectSystemData::getPredecessorId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<ObjectSystemData> predecessors   = osdDao.getObjectsById(predecessorIds.stream().toList(), false).stream().toList();
        for (ObjectSystemData predecessor : predecessors) {
            Set<Long> predDescendants = osdDao.getOsdIdByIdWithDescendants(predecessor.getId());
            if (MAIN_BRANCH.matcher(predecessor.getCmnVersion()).matches()){
                // ensure there are no descendants:
                if(predDescendants.isEmpty()) {
                    predecessor.setLatestHead(true);
                }
            }
            else{
                if(predDescendants.isEmpty()) {
                    predecessor.setLatestBranch(true);
                }
            }
            osdDao.updateOsd(predecessor, false);
        }

        List<Deletion> deletions = osds.stream().filter(osd -> Objects.nonNull(osd.getContentPath()))
                .map(osd -> new Deletion(osd.getId(), osd.getContentPath(), false))
                .collect(Collectors.toList());
        new DeletionDao().create(deletions);
    }

    private List<CinnamonError> delete(List<ObjectSystemData> osds, UserAccount user) {
        List<CinnamonError> errors = new ArrayList<>();
        for (ObjectSystemData osd : osds) {
            Long osdId = osd.getId();
            // - check permission for delete
            boolean deleteAllowed = authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.DELETE, user);
            if (!deleteAllowed) {
                CinnamonError error = new CinnamonError(ErrorCode.NO_DELETE_PERMISSION.getCode(), osdId);
                errors.add(error);
            }
            // - check for locked objects; superusers may delete locked objects:
            if (osd.getLockerId() != null && (!user.getId().equals(osd.getLockerId()) && !authorizationService.currentUserIsSuperuser())) {
                CinnamonError error = new CinnamonError(ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.getCode(), osdId);
                errors.add(error);
            }
        }
        return errors;
    }

}
