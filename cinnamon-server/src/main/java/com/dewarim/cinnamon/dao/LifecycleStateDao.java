package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LifecycleStateDao {

    public Optional<LifecycleState> getLifecycleStateById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        LifecycleState lifecycle = sqlSession.selectOne("com.dewarim.cinnamon.LifecycleStateMapper.getLifecycleStateById", id);
        return Optional.ofNullable(lifecycle);
    }

    public List<LifecycleState> getLifecycleStatesByLifecycleId(long id){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.LifecycleStateMapper.getLifecycleStatesByLifecycleId",id);
    }

}
