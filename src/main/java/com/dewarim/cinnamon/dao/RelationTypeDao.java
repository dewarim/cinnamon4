package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.relations.RelationType;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RelationTypeDao implements CrudDao<RelationType> {

    public Optional<RelationType> getRelationTypeById(long id) {
        SqlSession   sqlSession   = getSqlSession();
        RelationType relationType = sqlSession.selectOne("com.dewarim.cinnamon.model.relations.RelationType.getRelationTypeById", id);
        return Optional.ofNullable(relationType);
    }
    
    public Optional<RelationType> getRelationTypeByName(String name){
        SqlSession   sqlSession   = getSqlSession();
        RelationType relationType = sqlSession.selectOne("com.dewarim.cinnamon.model.relations.RelationType.getRelationTypeByName", name);
        return Optional.ofNullable(relationType);
    }

    @Override
    public String getTypeClassName() {
        return RelationType.class.getName();
    }

    public Map<Long, RelationType> getRelationTypeMap(Set<Long> ids) {
        List<RelationType> relationTypes = getObjectsById(new ArrayList<>(ids));
        return relationTypes.stream().collect(Collectors.toMap(RelationType::getId, Function.identity()));
    }

}
