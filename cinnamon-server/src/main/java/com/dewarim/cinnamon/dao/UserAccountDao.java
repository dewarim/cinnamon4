package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;

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

    public boolean isSuperuser(UserAccount user){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String,Object> params = new HashMap<>();
        params.put("superuserGroupName",Constants.GROUP_SUPERUSERS);
        params.put("userId",user.getId());
        return sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getSuperuserStatus",
                params) != null;
    }
    
    public static boolean currentUserIsSuperuser(){
        UserAccountDao accountDao = new UserAccountDao();
        return accountDao.isSuperuser(ThreadLocalSqlSession.getCurrentUser());
    }
}
