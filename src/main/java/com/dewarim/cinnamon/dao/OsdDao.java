package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

public class OsdDao implements CrudDao<ObjectSystemData> {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    private SqlSession sqlSession;

    public List<ObjectSystemData> getObjectsById(List<Long> ids, boolean includeSummary) {
        if(ids == null || ids.isEmpty()){
            return List.of();
        }
        SqlSession sqlSession = getSqlSession();
        List<ObjectSystemData> results = new ArrayList<>(ids.size());
        int requestSize = ids.size();
        int rowCount = 0;
        Map<String, Object> params = new HashMap<>();
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
        return results;
    }

    public ObjectSystemData getLatestHead(long id) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.ObjectSystemData.getLatestHead", id);
    }

    public List<ObjectSystemData> getObjectsByFolderId(long folderId, boolean includeSummary, VersionPredicate versionPredicate) {
        SqlSession sqlSession = getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("includeSummary", includeSummary);
        params.put("folderId", folderId);

        switch (versionPredicate) {
            case ALL -> params.put("versionPredicate", "");
            case HEAD -> params.put("versionPredicate", " AND latest_head=true ");
            case BRANCH -> params.put("versionPredicate", " AND latest_branch=true ");
        }
        return new ArrayList<>(sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdsByFolderId", params));
    }

    public Optional<ObjectSystemData> getObjectById(long id) {

        List<ObjectSystemData> objectsById = getObjectsById(Collections.singletonList(id), false);
        if (objectsById.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(objectsById.get(0));
    }

    public void updateOsd(ObjectSystemData osd, boolean updateModifier) {
        SqlSession sqlSession = getSqlSession();
        if (updateModifier) {
            UserAccount currentUser = ThreadLocalSqlSession.getCurrentUser();
            if (currentUser.isChangeTracking()) {
                osd.setModified(new Date());
                osd.setModifierId(currentUser.getId());
            }
        }
        sqlSession.update("com.dewarim.cinnamon.model.ObjectSystemData.updateOsd", osd);
        new IndexJobDao().insertIndexJob(new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.UPDATE,false ));
    }

    public ObjectSystemData saveOsd(ObjectSystemData osd) {
        SqlSession sqlSession = getSqlSession();
        int resultRows = sqlSession.insert("com.dewarim.cinnamon.model.ObjectSystemData.insertOsd", osd);
        if (resultRows != 1) {
            ErrorCode.DB_INSERT_FAILED.throwUp();
        }
        if (osd.getRootId() == null) {
            osd.setRootId(osd.getId());
            updateOsd(osd, false);
        }
        IndexJob indexJob = new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.CREATE, false );
        new IndexJobDao().insertIndexJob(indexJob);
        return osd;
    }

    public Optional<String> findLastDescendantVersion(long predecessorId) {
        List<ObjectSystemData> objects = findObjectsWithSamePredecessor(predecessorId);
        if (objects.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(objects.get(0).getCmnVersion());
    }

    public List<ObjectSystemData> findObjectsWithSamePredecessor(long predecessorId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.findLastDescendantVersion", predecessorId);
    }

    public void deleteOsds(List<Long> osdIdsToToDelete) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.ObjectSystemData.deleteOsds", osdIdsToToDelete);
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

    public SqlSession getSqlSession() {
        if (sqlSession != null) {
            return sqlSession;
        }
        return ThreadLocalSqlSession.getSqlSession();
    }

    public void updateOwnershipAndModifierAndCreatorAndLocker(Long userId, Long assetReceiverId, Long adminUserId) {
        List<ObjectSystemData> osds = getOsdByModifierOrCreatorOrOwnerOrLocker(userId);
        Date currentDate = new Date();
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
}
