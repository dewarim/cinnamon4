package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.IndexItem;
import org.apache.ibatis.session.SqlSession;

public class IndexItemDao implements CrudDao<IndexItem>{

    private SqlSession sqlSession;

    @Override
    public String getTypeClassName() {
        return IndexItem.class.getName();
    }

    public IndexItemDao setSqlSession(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        return this;
    }

    @Override
    public SqlSession getSqlSession(){
        if(sqlSession != null){
            return sqlSession;
        }
        return ThreadLocalSqlSession.getSqlSession();
    }
}
