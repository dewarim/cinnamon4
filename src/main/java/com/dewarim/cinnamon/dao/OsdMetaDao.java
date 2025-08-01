package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OsdMetaDao implements CrudDao<Meta>, MetaDao {

    private SqlSession sqlSession;

    public OsdMetaDao() {
    }

    public OsdMetaDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<Meta> listByOsd(long id) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listByOsd", id);
    }

    /**
     * When we want to update the content anyway, we do not need to load the potentially 100+ MByte of content.
     */
    public List<Meta> listWithoutContentByOsd(long id) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listWithoutContentByOsd", id);
    }

    public Optional<Meta> getMetaById(Long metaId) {
        SqlSession sqlSession = getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.OsdMeta.getMetasetById", metaId));
    }

    @Override
    public List<Meta> listMetaByObjectIds(List<Long> ids) {
        if(ids.isEmpty()) {
            return new ArrayList<>();
        }
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
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

}
