package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Format;
import org.apache.ibatis.session.SqlSession;

public class FormatDao implements CrudDao<Format> {

    private SqlSession sqlSession;

    public FormatDao() {
    }

    public FormatDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public String getTypeClassName() {
        return Format.class.getName();
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }
}
