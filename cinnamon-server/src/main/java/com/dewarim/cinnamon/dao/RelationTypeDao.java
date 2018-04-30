package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.RelationType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class RelationTypeDao {

    public List<RelationType> listRelationTypes() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.RelationTypeMapper.list");
    }

    public Optional<RelationType> getRelationTypeById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        RelationType relationType = sqlSession.selectOne("com.dewarim.cinnamon.RelationTypeMapper.getRelationTypeById", id);
        return Optional.ofNullable(relationType);
    }


}
