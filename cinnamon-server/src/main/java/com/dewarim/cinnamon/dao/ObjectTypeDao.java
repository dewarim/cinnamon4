package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class ObjectTypeDao {
    
    public List<ObjectType> listObjectTypes(){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.ObjectTypeMapper.list");
    }
    
}
