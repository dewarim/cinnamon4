package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.relations.RelationType;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RelationTypeDao implements CrudDao<RelationType> {

    public Optional<RelationType> getRelationTypeById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        RelationType relationType = sqlSession.selectOne("com.dewarim.cinnamon.model.relations.RelationType.getRelationTypeById", id);
        return Optional.ofNullable(relationType);
    }
    
    public Optional<RelationType> getRelationTypeByName(String name){
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        RelationType relationType = sqlSession.selectOne("com.dewarim.cinnamon.model.relations.RelationType.getRelationTypeByName", name);
        return Optional.ofNullable(relationType);
    }

    @Override
    public String getTypeClassName() {
        return RelationType.class.getName();
    }

    public Map<Long, RelationType> getRelationTypeMap(Set<Long> ids) {
        List<RelationType> relationTypes = getObjectsById(new ArrayList<>(ids));
        Map<Long,RelationType> relationTypeMap = new HashMap<>();
        relationTypes.stream().forEach(relationType -> relationTypeMap.put(relationType.getId(), relationType));
        return relationTypeMap;
    }

}
