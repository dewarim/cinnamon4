package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.MetasetType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class MetasetTypeDao {

    public List<MetasetType> listMetasetTypes() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.MetasetTypeMapper.list");
    }

    public Optional<MetasetType> getMetasetTypeById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        MetasetType metasetType = sqlSession.selectOne("com.dewarim.cinnamon.MetasetTypeMapper.getMetasetTypeById", id);
        return Optional.ofNullable(metasetType);
    }


}
