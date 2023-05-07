package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OsdMetaDao implements CrudDao<Meta>, MetaDao {

    private SqlSession sqlSession;

    public OsdMetaDao() {
    }

    public OsdMetaDao(TransactionIsolationLevel level) {
        this.sqlSession = ThreadLocalSqlSession.getNewSession(level);
    }

    public List<Meta> listByOsd(long id) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listByOsd", id);
    }

    public Optional<Meta> getMetaById(Long metaId) {
        SqlSession sqlSession = getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.OsdMeta.getMetasetById", metaId));
    }

    @Override
    public List<Meta> listMetaByObjectIds(List<Long> ids) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listByOsds", ids);
    }

    public List<Meta> getMetaByTypeIdsAndOsd(List<Long> typeIds, long id) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("id", id, "typeIds", typeIds);
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.getMetaByTypeIdsAndOsd", params);
    }

    public List<Long> getUniqueMetaTypeIdsOfObject(Long osdId) {
        SqlSession sqlSession = getSqlSession();
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
        SqlSession       sqlSession = getSqlSession();
        List<List<Long>> partitions = CrudDao.partitionLongList(osdIdsToToDelete);
        partitions.forEach(partition -> {
            try {
                sqlSession.delete("com.dewarim.cinnamon.model.OsdMeta.deleteByOsdIds", partition);
            } catch (PersistenceException e) {
                throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
            }
        });
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession != null) {
            return sqlSession;
        } else {
            return ThreadLocalSqlSession.getSqlSession();
        }
    }

    /**
     * When indexing metasets, we create a new SqlSession for OsdMetaDao so that it's always fresh and has access to
     * the newest metas. After reading the metadata, we should close this one-of session as quickly as possible.
     */
    public void closePrivateSession(){
        if(sqlSession != null){
            sqlSession.close();
        }
    }
}
