package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ObjectType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class ObjectTypeDao {

    public List<ObjectType> listObjectTypes() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.ObjectTypeMapper.list");
    }

    public Optional<ObjectType> getObjectTypeById(Long typeId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        ObjectType type       = sqlSession.selectOne("com.dewarim.cinnamon.ObjectTypeMapper.getObjectTypeById", typeId);
        return Optional.ofNullable(type);
    }
}
