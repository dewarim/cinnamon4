package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.IndexItem;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class IndexItemDao {

    public List<IndexItem> listIndexItems() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.IndexItemMapper.list");
    }

    public Optional<IndexItem> getIndexItemById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        IndexItem indexItem = sqlSession.selectOne("com.dewarim.cinnamon.IndexItemMapper.getIndexItemById", id);
        return Optional.ofNullable(indexItem);
    }


}
