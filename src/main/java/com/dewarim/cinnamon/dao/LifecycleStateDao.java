package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.LifecycleState;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LifecycleStateDao implements CrudDao<LifecycleState> {

    public Optional<LifecycleState> getLifecycleStateById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        SqlSession     sqlSession = ThreadLocalSqlSession.getSqlSession();
        LifecycleState lifecycleState  = sqlSession.selectOne("com.dewarim.cinnamon.model.LifecycleState.getLifecycleStateById", id);
        debugLog("lifecycleState:", lifecycleState);
        return Optional.ofNullable(lifecycleState);
    }

    public List<LifecycleState> getLifecycleStatesByLifecycleId(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.LifecycleState.getLifecycleStatesByLifecycleId", id);
    }

    public List<LifecycleState> getLifecycleStatesByNameList(List<String> names) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<LifecycleState> lifecycleStates = sqlSession.selectList("com.dewarim.cinnamon.model.LifecycleState.getLifecycleStatesByNameList", names);
        debugLog("lifecycleStates:", lifecycleStates);
        return lifecycleStates;
    }

    @Override
    public String getTypeClassName() {
        return LifecycleState.class.getName();
    }
}
