package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsdMetaDao {

    public List<Meta> listByOsd(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.OsdMetaMapper.listByOsd", id);
    }

    public List<Meta> getMetaByNamesAndOsd(List<String> names, long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("id", id);
        params.put("typeNames", names);
        return sqlSession.selectList("com.dewarim.cinnamon.OsdMetaMapper.getMetasetsByNameAndOsd", params);
    }

    public Meta createMeta(CreateMetaRequest metaRequest, MetasetType metaType) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Meta       osdMeta    = new Meta(metaRequest.getId(), metaType.getId(), metaRequest.getContent());
        int        resultRows = sqlSession.insert("com.dewarim.cinnamon.OsdMetaMapper.insertMeta", osdMeta);
        if (resultRows != 1) {
            throw new RuntimeException("Create OsdMeta failed.");
        }
        return osdMeta;
    }

    public Meta getOsdMetaById(Long metaId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.OsdMetaMapper.getMetasetById", metaId);
    }

    public int deleteById(Long metaId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.OsdMetaMapper.deleteById",metaId);
    }
}
