package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

public interface CrudDao<T> {

    default List<T> create(List<T> items){
        List<T> createdItems = new ArrayList<>();
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        items.forEach(item -> {
            String sqlAction = item.getClass().getName()+".insert" ;
            sqlSession.insert(sqlAction, item);
            createdItems.add(item);
        });
        return createdItems;
    }

}
