package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

public class OsdMetaDao implements CrudDao<Meta>, MetaDao{

    public List<Meta> listByOsd(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listByOsd", id);
    }
    @Override
    public List<Meta> listMetaByObjectIds(List<Long> ids) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listByOsds", ids);
    }

    public List<Meta> getMetaByTypeIdsAndOsd(List<Long> typeIds, long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = Map.of("id", id, "typeIds", typeIds);
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.getMetaByTypeIdsAndOsd", params);
    }

    public List<Long> getUniqueMetaTypeIdsOfObject(Long osdId){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.getUniqueMetaTypeIdsOfOsd", osdId);
    }

    @Override
    public String getTypeClassName() {
        return "com.dewarim.cinnamon.model.OsdMeta";
    }

    @Override
    public String getMapperNamespace(SqlAction action) {
        return CrudDao.super.getMapperNamespace(action);
    }

    public void deleteByOsdIds(List<Long> osdIdsToToDelete) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<List<Long>> partitions  = CrudDao.partitionLongList(osdIdsToToDelete);
        partitions.forEach(partition -> {
            try {
                sqlSession.delete("com.dewarim.cinnamon.model.OsdMeta.deleteByOsdIds", partition);
            }
            catch (PersistenceException e){
                throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
            }
        });
    }
}
