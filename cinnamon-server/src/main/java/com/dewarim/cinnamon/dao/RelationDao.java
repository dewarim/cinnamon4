package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.relations.Relation;
import org.apache.ibatis.session.SqlSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationDao {

    public List<Relation> getRelations(Collection<Long> leftIds, Collection<Long> rightIds, Collection<String> names, boolean includeMetadata) {
        Map<String, Object> params = new HashMap<>();
        params.put("leftIds", leftIds);
        params.put("rightIds", rightIds);
        params.put("names", names);
        params.put("includeMetadata", includeMetadata);

        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.RelationMapper.getRelationsWithCriteria", params);
    }

    public void createRelation(Relation relation) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.RelationMapper.createRelation", relation);
    }

    public int deleteRelation(Long leftId, Long rightId, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("leftId", leftId);
        params.put("rightId", rightId);
        params.put("name", name);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.RelationMapper.deleteRelation", params);
    }

    public List<Relation> getProtectedRelations(List<Long> osdIds) {
        // TODO: allow deletions of over 32K Objects at once.
        Map<String, Object> params = new HashMap<>();
        params.put("ids", osdIds);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.RelationMapper.getProtectedRelations", params);
    }

    public List<Relation> getAllRelationsOfObjects(List<Long> osdIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", osdIds);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.RelationMapper.getAllRelationsOfObjects", params);
    }

    public void deleteAllUnprotectedRelationsOfObjects(List<Long> osdIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", osdIds);
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.RelationMapper.deleteAllUnprotectedRelationsOfObjects", params);
    }
}
