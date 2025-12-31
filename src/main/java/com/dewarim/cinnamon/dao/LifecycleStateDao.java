package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.LifecycleState;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LifecycleStateDao implements CrudDao<LifecycleState> {

    public Optional<LifecycleState> getLifecycleStateById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        SqlSession     sqlSession = getSqlSession();
        LifecycleState lifecycleState  = sqlSession.selectOne("com.dewarim.cinnamon.model.LifecycleState.getLifecycleStateById", id);
        return Optional.ofNullable(lifecycleState);
    }

    public List<LifecycleState> getLifecycleStatesByLifecycleId(long id) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.LifecycleState.getLifecycleStatesByLifecycleId", id);
    }

    public List<LifecycleState> getLifecycleStatesByNameList(List<String> names) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.LifecycleState.getLifecycleStatesByNameList", names);
    }

    @Override
    public String getTypeClassName() {
        return LifecycleState.class.getName();
    }
}
