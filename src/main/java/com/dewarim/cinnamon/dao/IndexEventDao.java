package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.index.IndexEvent;
import org.apache.ibatis.session.SqlSession;

public class IndexEventDao implements CrudDao<IndexEvent> {

    private SqlSession sqlSession;

    public IndexEventDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = CrudDao.super.getSqlSession();
        }
        return sqlSession;
    }

    public void commit() {
        SqlSession sqlSession = getSqlSession();
        sqlSession.commit();
    }

    @Override
    public String getTypeClassName() {
        return IndexEvent.class.getName();
    }
}
