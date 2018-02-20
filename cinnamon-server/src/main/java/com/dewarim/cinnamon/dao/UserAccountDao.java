package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.SqlSession;

/**
 */
public class UserAccountDao {

    public UserAccount getUserAccountByName(String username) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountByName", username);
    }
    
    public UserAccount getUserAccountById(Long id){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountById", id);
    }

    public void changeUserActivationStatus(UserAccount user){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.UserAccountMapper.changeUserActivationStatus", user);
    }
    
}
