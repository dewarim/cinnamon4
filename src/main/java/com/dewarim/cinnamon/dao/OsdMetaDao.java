package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsdMetaDao implements CrudDao<Meta>{

    public List<Meta> listByOsd(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.listByOsd", id);
    }

    public List<Meta> getMetaByNamesAndOsd(List<String> names, long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("id", id);
        params.put("typeNames", names);
        return sqlSession.selectList("com.dewarim.cinnamon.model.OsdMeta.getMetasetsByNameAndOsd", params);
    }

    // TODO: the DAO should receive ready-to-persist Meta objects, not metaRequest.
    public Meta createMeta(CreateMetaRequest metaRequest, MetasetType metaType) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Meta       osdMeta    = new Meta(metaRequest.getId(), metaType.getId(), metaRequest.getContent());
        int        resultRows = sqlSession.insert("com.dewarim.cinnamon.model.OsdMeta.insert", osdMeta);
        if (resultRows != 1) {
            // TODO: should not throw RuntimeException - see CrudDao for better exceptions
            throw new RuntimeException("Create OsdMeta failed.");
        }
        return osdMeta;
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
