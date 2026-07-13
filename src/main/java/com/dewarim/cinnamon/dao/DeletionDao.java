package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Deletion;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class DeletionDao implements CrudDao<Deletion>{

    private SqlSession sqlSession;

    public DeletionDao() {
    }

    public DeletionDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public String getTypeClassName() {
        return Deletion.class.getName();
    }

    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

    public List<Deletion> listPendingDeletions(){
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("listPendingDeletions");
    }
}
