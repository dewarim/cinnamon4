package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.relations.Relation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationDao implements CrudDao<Relation>{

    public List<Relation> getRelations(Collection<Long> leftIds, Collection<Long> rightIds, Collection<String> names, boolean includeMetadata) {
        Map<String, Object> params = new HashMap<>();
        params.put("leftIds", leftIds);
        params.put("rightIds", rightIds);
        params.put("names", names);
        params.put("includeMetadata", includeMetadata);

        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsWithCriteria", params);
    }

    public List<Relation> getRelationsOrMode(Collection<Long> leftIds, Collection<Long> rightIds, Collection<String> names, boolean includeMetadata) {
        Map<String, Object> params = new HashMap<>();
        if(CollectionUtils.isNotEmpty(leftIds)) {
            params.put("leftIds", leftIds);
        }
        if(CollectionUtils.isNotEmpty(rightIds)){
            params.put("rightIds", rightIds);
        }
        if(CollectionUtils.isNotEmpty(names)){
            params.put("names", names);
        }
        params.put("includeMetadata", includeMetadata);

        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsWithCriteriaOr", params);
    }

    public int deleteRelation(Long leftId, Long rightId, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("leftId", leftId);
        params.put("rightId", rightId);
        params.put("name", name);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.model.relations.Relation.deleteRelationByExample", params);
    }

    public List<Relation> getProtectedRelations(List<Long> osdIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", osdIds);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getProtectedRelations", params);
    }

    public List<Relation> getAllRelationsOfObjects(List<Long> osdIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", osdIds);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getAllRelationsOfObjects", params);
    }

    public void deleteAllUnprotectedRelationsOfObjects(List<Long> osdIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", osdIds);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.relations.Relation.deleteAllUnprotectedRelationsOfObjects", params);
    }

    @Override
    public String getTypeClassName() {
        return Relation.class.getName();
    }

    public List<Relation> getRelationsToCopy(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsToCopy", id);
    }
}
