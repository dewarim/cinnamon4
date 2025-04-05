package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.index.IndexEvent;
import org.apache.ibatis.session.SqlSession;

public class IndexEventDao implements CrudDao<IndexEvent> {

    public void commit() {
        SqlSession sqlSession = getSqlSession();
        sqlSession.commit();
    }

    @Override
    public String getTypeClassName() {
        return IndexEvent.class.getName();
    }
}
