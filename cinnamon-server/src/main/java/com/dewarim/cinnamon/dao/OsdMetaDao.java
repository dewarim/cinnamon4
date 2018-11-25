package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.OsdMeta;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsdMetaDao {

    public List<OsdMeta> listByOsd(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.OsdMetaMapper.listByOsd", id);
    }

    public List<OsdMeta> getMetaByNamesAndOsd(List<String> names, long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("id", id);
        params.put("typeNames", names);
        return sqlSession.selectList("com.dewarim.cinnamon.OsdMetaMapper.getMetasetsByNameAndOsd", params);
    }

}
