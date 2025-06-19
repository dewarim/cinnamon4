package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.MetasetType;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class MetasetTypeDao implements CrudDao<MetasetType>{

    private SqlSession sqlSession;

    public MetasetTypeDao() {
    }

    public MetasetTypeDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public Optional<MetasetType> getMetasetTypeById(long id) {
        SqlSession   sqlSession   = getSqlSession();
        MetasetType metasetType = sqlSession.selectOne("com.dewarim.cinnamon.model.MetasetType.getMetasetTypeById", id);
        return Optional.ofNullable(metasetType);
    }

    @Override
    public String getTypeClassName() {
        return MetasetType.class.getName();
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }
}
