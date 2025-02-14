package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.ObjectType;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class ObjectTypeDao implements CrudDao<ObjectType>{

    public Optional<ObjectType> getObjectTypeById(Long typeId) {
        SqlSession sqlSession = getSqlSession();
        ObjectType type       = sqlSession.selectOne("com.dewarim.cinnamon.model.ObjectType.getObjectTypeById", typeId);
        return Optional.ofNullable(type);
    }

    @Override
    public String getTypeClassName() {
        return ObjectType.class.getName();
    }
}
