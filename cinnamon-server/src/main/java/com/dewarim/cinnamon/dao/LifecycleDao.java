package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Lifecycle;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LifecycleDao {

    public List<Lifecycle> listLifecycles() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.LifecycleMapper.list");
    }

    public Optional<Lifecycle> getLifecycleById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        Lifecycle lifecycle = sqlSession.selectOne("com.dewarim.cinnamon.LifecycleMapper.getLifecycleById", id);
        return Optional.ofNullable(lifecycle);
    }


}
