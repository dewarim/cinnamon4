package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectSystemData;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

public class OsdDao {

    /**
     * Max number of ids in "in clause" is 32768 for Postgresql.
     */
    private static final int BATCH_SIZE = 10000;

    public List<ObjectSystemData> getObjectsById(List<Long> ids, boolean includeSummary) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
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
            params.put("idList", partialList);
            results.addAll(sqlSession.selectList("com.dewarim.cinnamon.ObjectSystemDataMapper.getOsdsById", params));
            rowCount += BATCH_SIZE;
        }
        return results;
    }

    public ObjectSystemData getLatestHead(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.ObjectSystemDataMapper.getLatestHead", id);
    }

    public List<ObjectSystemData> getObjectsByFolderId(long folderId, boolean includeSummary) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("includeSummary", includeSummary);
        params.put("folderId", folderId);
        return new ArrayList<>(sqlSession.selectList("com.dewarim.cinnamon.ObjectSystemDataMapper.getOsdsByFolderId", params));
    }

    public Optional<ObjectSystemData> getObjectById(long id) {

        List<ObjectSystemData> objectsById = getObjectsById(Collections.singletonList(id), false);
        if(objectsById.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(objectsById.get(0));
    }
    
    public void updateOsd(ObjectSystemData osd){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        osd.setModified(new Date());
        osd.setModifierId(ThreadLocalSqlSession.getCurrentUser().getId());
        sqlSession.update("com.dewarim.cinnamon.ObjectSystemDataMapper.updateOsd", osd);
    }
}
