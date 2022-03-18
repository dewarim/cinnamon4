package com.dewarim.cinnamon.application;

import com.beust.jcommander.Strings;
import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.OsdMetaDao;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DeleteOsdService {

    private static final Logger               log                  = LogManager.getLogger(DeleteOsdService.class);
    private final        AuthorizationService authorizationService = new AuthorizationService();

    public void verifyAndDelete(List<ObjectSystemData> osds, boolean deleteDescendants, boolean deleteAllVersions, UserAccount user) {
        OsdDao osdDao = new OsdDao();
        Set<Long> osdIds = osds.stream().map(osd -> {
                    if (deleteAllVersions) {
                        // TODO: should the DB just return osd.id if osd.rootId is null?
                        return Objects.requireNonNullElse(osd.getRootId(), osd.getId());
                    } else {
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
                    } else if (!osdIds.containsAll(osdAndDescendants)) {
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
        List<Long> osdIdsToToDelete = new ArrayList<>(osdIds);
        log.debug("delete " + Strings.join(",", osdIdsToToDelete.stream().map(String::valueOf).collect(Collectors.toList())));
        var relationDao = new RelationDao();
        relationDao.deleteAllUnprotectedRelationsOfObjects(osdIdsToToDelete);
        relationDao.delete(protectedRelations.stream().map(Relation::getId).collect(Collectors.toList()));
        new LinkDao().deleteAllLinksToObjects(osdIdsToToDelete);
        new OsdMetaDao().deleteByOsdIds(osdIdsToToDelete);
        osdDao.deleteOsds(osdIdsToToDelete);
    }

    private List<CinnamonError> delete(List<ObjectSystemData> osds, UserAccount user) {
        List<CinnamonError> errors = new ArrayList<>();
        for (ObjectSystemData osd : osds) {
            Long osdId = osd.getId();
            // - check permission for delete
            boolean deleteAllowed = authorizationService.userHasPermission(osd.getAclId(), DefaultPermission.DELETE_OBJECT, user);
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
