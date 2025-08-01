package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.IdAndRootId;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.api.RootAndLatestHead;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.service.debug.DebugLogService;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.stream.Collectors;

/*
 * note: calling default update() implementation on OSD will not update changeTracking.
 */
public class OsdDao implements CrudDao<ObjectSystemData> {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    private SqlSession sqlSession;

    public OsdDao() {
    }

    public OsdDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<ObjectSystemData> getObjectsById(List<Long> ids, boolean includeSummary) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        SqlSession             sqlSession  = getSqlSession();
        List<ObjectSystemData> results     = new ArrayList<>(ids.size());
        int                    requestSize = ids.size();
        int                    rowCount    = 0;
        Map<String, Object>    params      = new HashMap<>();
        params.put("includeSummary", includeSummary);
        while (rowCount < requestSize) {
            int lastIndex = rowCount + BATCH_SIZE;
            if (lastIndex > requestSize) {
                lastIndex = requestSize;
            }
            List<Long> partialList = ids.subList(rowCount, lastIndex);
            params.put("ids", partialList);
            results.addAll(sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdsById", params));
            rowCount += BATCH_SIZE;
        }
        DebugLogService.log("getOsdsById", results);
        return results;
    }

    public Set<IdAndRootId> getIdAndRootsById(List<Long> ids) {
        SqlSession        sqlSession = getSqlSession();
        List<IdAndRootId> idAndRoots = sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getIdAndRoot", ids);
        return new HashSet<>(idAndRoots);
    }

    public Map<Long, ObjectSystemData> getLatestHeads(List<ObjectSystemData> osds) {
        if (osds == null || osds.isEmpty()) {
            return Map.of();
        }
        SqlSession                  sqlSession         = getSqlSession();
        Set<Long>                   rootIds            = osds.stream().map(ObjectSystemData::getRootId).collect(Collectors.toSet());
        List<RootAndLatestHead>     rootAndLatestHeads = sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getLatestHeads", rootIds.stream().toList());
        Set<Long>                   headIds            = rootAndLatestHeads.stream().map(RootAndLatestHead::getHeadId).collect(Collectors.toSet());
        List<ObjectSystemData>      heads              = getObjectsById(headIds.stream().toList(), false);
        Map<Long, ObjectSystemData> latestHeadMappings = new HashMap<>();
        for (ObjectSystemData osd : osds) {
            Long rootId = osd.getRootId();
            rootAndLatestHeads.stream()
                    .filter(rootHead -> rootHead.getRootId().equals(rootId))
                    .findFirst()
                    .ifPresent(rootHead -> {
                        Optional<ObjectSystemData> headOsd = heads.stream().filter(head -> head.getId().equals(rootHead.getHeadId())).findFirst();
                        headOsd.ifPresent(objectSystemData -> latestHeadMappings.put(osd.getId(), objectSystemData));
                    });
        }
        return latestHeadMappings;
    }

    public List<ObjectSystemData> getObjectsByFolderId(long folderId, boolean includeSummary, VersionPredicate versionPredicate) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("includeSummary", includeSummary);
        params.put("folderId", folderId);

        switch (versionPredicate) {
            case ALL -> params.put("versionPredicate", "");
            case HEAD -> params.put("versionPredicate", " AND latest_head=true ");
            case BRANCH -> params.put("versionPredicate", " AND latest_branch=true ");
        }
        List<ObjectSystemData> selected = sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdsByFolderId", params);
        DebugLogService.log("getOsdsByFolderId", selected);
        return selected;
    }

    public Optional<ObjectSystemData> getObjectById(long id) {

        List<ObjectSystemData> objectsById = getObjectsById(Collections.singletonList(id), false);
        if (objectsById.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(objectsById.getFirst());
    }

    public void updateOsd(ObjectSystemData osd, boolean updateModifier, boolean updateTikaMetaset) {
        SqlSession sqlSession = getSqlSession();
        if (updateModifier) {
            UserAccount currentUser = ThreadLocalSqlSession.getCurrentUser();
            if (currentUser.isChangeTracking()) {
                osd.setModified(new Date());
                osd.setModifierId(currentUser.getId());
            }
        }
        sqlSession.update("com.dewarim.cinnamon.model.ObjectSystemData.updateOsd", osd);
        new IndexJobDao(sqlSession).insertIndexJob(new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.UPDATE));
        DebugLogService.log("update:", osd);
    }

    public void updateOsd(ObjectSystemData osd, boolean updateModifier) {
        updateOsd(osd, updateModifier, false);
    }

    public ObjectSystemData saveOsd(ObjectSystemData osd) {
        SqlSession sqlSession = getSqlSession();
        int        resultRows = sqlSession.insert("com.dewarim.cinnamon.model.ObjectSystemData.insertOsd", osd);
        if (resultRows != 1) {
            throw ErrorCode.DB_INSERT_FAILED.exception();
        }
        if (osd.getRootId() == null) {
            osd.setRootId(osd.getId());
            updateOsd(osd, false);
        }
        IndexJob indexJob = new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.CREATE);
        new IndexJobDao(sqlSession).insertIndexJob(indexJob);
        DebugLogService.log("save:", osd);
        return osd;
    }

    public Optional<String> findLastDescendantVersion(long predecessorId) {
        List<ObjectSystemData> objects = findObjectsWithSamePredecessor(predecessorId);
        if (objects.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(objects.getFirst().getCmnVersion());
    }

    public List<ObjectSystemData> findObjectsWithSamePredecessor(long predecessorId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.findLastDescendantVersion", predecessorId);
    }

    public void deleteOsds(List<Long> osdIdsToToDelete) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.ObjectSystemData.deleteOsds", osdIdsToToDelete);
        IndexJobDao jobDao = new IndexJobDao(sqlSession);
        osdIdsToToDelete.forEach(id -> {
            IndexJob indexJob = new IndexJob(IndexJobType.OSD, id, IndexJobAction.DELETE);
            jobDao.insertIndexJob(indexJob);
        });
        DebugLogService.log("osds to delete:", osdIdsToToDelete);
    }

    public Set<Long> getOsdIdByIdWithDescendants(Long id) {
        SqlSession sqlSession = getSqlSession();
        return new HashSet<>(sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdIdByIdWithDescendants", id));
    }

    @Override
    public String getTypeClassName() {
        return ObjectSystemData.class.getName();
    }

    public OsdDao setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        return this;
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

    public void updateOwnershipAndModifierAndCreatorAndLocker(Long userId, Long assetReceiverId, Long adminUserId) {
        List<ObjectSystemData> osds        = getOsdByModifierOrCreatorOrOwnerOrLocker(userId);
        Date                   currentDate = new Date();
        osds.forEach(osd -> {
            if (osd.getOwnerId().equals(userId)) {
                osd.setOwnerId(assetReceiverId);
            }
            osd.setModifierId(adminUserId);
            osd.setModified(currentDate);
            if (osd.getCreatorId().equals(userId)) {
                osd.setCreatorId(assetReceiverId);
            }
            if (osd.getLockerId() != null && osd.getLockerId().equals(userId)) {
                osd.setLockerId(assetReceiverId);
            }
            updateOsd(osd, false);
        });
    }

    private List<ObjectSystemData> getOsdByModifierOrCreatorOrOwnerOrLocker(Long userId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdByModifierOrCreatorOrOwnerOrLocker", userId);

    }

    public List<Ownable> getOsdsAsOwnables(Set<Long> osdIds) {
        SqlSession sqlSession = getSqlSession();
        if (osdIds == null || osdIds.isEmpty()) {
            return List.of();
        }
        return sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdsAsOwnables", osdIds.stream().toList());
    }

    public List<ObjectSystemData> getRootOsdsWithLatestHeadLinks(List<Long> osdIdsToToDelete) {
        SqlSession session = getSqlSession();
        return session.selectList("com.dewarim.cinnamon.model.ObjectSystemData.rootIdForLatestHeadLinks", osdIdsToToDelete);
    }

    public List<ObjectSystemData> getOsdsMissingTikaMetaset(Long tikaMetasetTypeId, int limit) {
        SqlSession session = getSqlSession();
        Map<String,Object> params = Map.of("tikaMetasetTypeId", tikaMetasetTypeId, "limit", limit);
        return session.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdsMissingTikaMetaset", params);
    }

    public Set<Long> findKnownIds(List<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return Set.of();
        }

        List<List<Long>> partitions = CrudDao.partitionLongList(ids.stream().toList());
        SqlSession       sqlSession = getSqlSession();
        HashSet<Long>          results    = new HashSet<>(ids.size());
        partitions.forEach(partition -> {
            if (!partition.isEmpty()) {
                results.addAll(sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.findKnownIds", partition));
            }
        });

        return results;
    }
}