package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Lifecycle;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class LifecycleDao implements CrudDao<Lifecycle>{

    public Optional<Lifecycle> getLifecycleById(Long id) {
        SqlSession   sqlSession   = getSqlSession();
        Lifecycle lifecycle = sqlSession.selectOne("com.dewarim.cinnamon.model.Lifecycle.getLifecycleById", id);
        debugLog("lifecycleById:", lifecycle);
        return Optional.ofNullable(lifecycle);
    }

    public Optional<Lifecycle> getLifecycleByName(String name) {
        SqlSession   sqlSession   = getSqlSession();
        Lifecycle lifecycle = sqlSession.selectOne("com.dewarim.cinnamon.model.Lifecycle.getLifecycleByName", name);
        return Optional.ofNullable(lifecycle);
    }

    @Override
    public String getTypeClassName() {
        return Lifecycle.class.getName();
    }
}
