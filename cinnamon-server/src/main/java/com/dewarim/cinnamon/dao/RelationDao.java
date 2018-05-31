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
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("leftIds", leftIds);
        params.put("rightIds", rightIds);
        params.put("names", names);
        params.put("includeMetadata", includeMetadata);
        return sqlSession.selectList("com.dewarim.cinnamon.RelationMapper.getRelationsWithCriteria", params);
    }


}
