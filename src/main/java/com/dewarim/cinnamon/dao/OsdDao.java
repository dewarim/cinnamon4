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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OsdDao implements CrudDao<ObjectSystemData> {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;
    private final IndexJobDao indexJobDao = new IndexJobDao();

    public List<ObjectSystemData> getObjectsById(List<Long> ids, boolean includeSummary) {
        SqlSession             sqlSession  = ThreadLocalSqlSession.getSqlSession();
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
        return results;
    }

    public ObjectSystemData getLatestHead(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.ObjectSystemData.getLatestHead", id);
    }

    public List<ObjectSystemData> getObjectsByFolderId(long folderId, boolean includeSummary, VersionPredicate versionPredicate) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
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
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        if (updateModifier) {
            UserAccount currentUser = ThreadLocalSqlSession.getCurrentUser();
            if (currentUser.isChangeTracking()) {
                osd.setModified(new Date());
                osd.setModifierId(currentUser.getId());
            }
        }
        sqlSession.update("com.dewarim.cinnamon.model.ObjectSystemData.updateOsd", osd);
    }

    public ObjectSystemData saveOsd(ObjectSystemData osd) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        int        resultRows = sqlSession.insert("com.dewarim.cinnamon.model.ObjectSystemData.insertOsd", osd);
        if (resultRows != 1) {
            ErrorCode.DB_INSERT_FAILED.throwUp();
        }
        IndexJob indexJob = new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.CREATE);
        indexJobDao.insertIndexJob(indexJob);
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
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.findLastDescendantVersion", predecessorId);
    }

    public void deleteOsds(List<Long> osdIdsToToDelete) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.ObjectSystemData.deleteOsds", osdIdsToToDelete);
    }

    public Set<Long> getOsdIdByIdWithDescendants(Long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        return new HashSet<>(sqlSession.selectList("com.dewarim.cinnamon.model.ObjectSystemData.getOsdIdByIdWithDescendants", id));
    }

    @Override
    public String getTypeClassName() {
        return ObjectSystemData.class.getName();
    }
}
