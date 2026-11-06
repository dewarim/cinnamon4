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

    /**
     * There are only a handful of metaset types and CrudDao.list() is a single statement, so this
     * filters in memory rather than adding a by-name select.
     */
    public Optional<MetasetType> getMetasetTypeByName(String name) {
        return list().stream().filter(metasetType -> metasetType.getName().equals(name)).findFirst();
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
