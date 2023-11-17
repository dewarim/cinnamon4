package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.relations.Relation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationDao implements CrudDao<Relation> {

    public List<Relation> getRelations(Collection<Long> leftIds, Collection<Long> rightIds, Collection<Long> relationTypeIds, boolean includeMetadata) {
        // cannot use Map.of here since values may be null & SQL code has conditions for non-null
        Map<String, Object> params = new HashMap<>();
        params.put("leftIds", leftIds);
        params.put("rightIds", rightIds);
        params.put("typeIds", relationTypeIds);
        params.put("includeMetadata", includeMetadata);

        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsWithCriteria", params);
    }

    public List<Relation> getRelationsOrMode(Collection<Long> leftIds, Collection<Long> rightIds, Collection<Long> relationTypeIds, boolean includeMetadata) {
        Map<String, Object> params = new HashMap<>();
        if (CollectionUtils.isNotEmpty(leftIds)) {
            params.put("leftIds", leftIds);
        }
        if (CollectionUtils.isNotEmpty(rightIds)) {
            params.put("rightIds", rightIds);
        }
        if (CollectionUtils.isNotEmpty(relationTypeIds)) {
            params.put("typeIds", relationTypeIds);
        }
        params.put("includeMetadata", includeMetadata);

        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsWithCriteriaOr", params);
    }

    public List<Relation> getProtectedRelations(List<Long> osdIds) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getProtectedRelations", osdIds);
    }

    public void deleteAllUnprotectedRelationsOfObjects(List<Long> osdIds) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.relations.Relation.deleteAllUnprotectedRelationsOfObjects", osdIds);
    }

    @Override
    public String getTypeClassName() {
        return Relation.class.getName();
    }

    public List<Relation> getRelationsToCopy(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsToCopy", id);
    }

    public List<Relation> getRelationsToCopyOnVersion(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.relations.Relation.getRelationsToCopyOnVersion", id);
    }
}
