package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.SqlSession;


/**
 */
public class UserAccountDao {

    private DbSessionFactory dbSessionFactory = new DbSessionFactory();

    public UserAccount getUserAccountByName(String username) {
        try (SqlSession sqlSession = dbSessionFactory.getSession()) {
            UserAccount user = sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountByName", username);
            return user;
        }
    }
    
    public static void main(String[] args){
        UserAccount user = new UserAccountDao().getUserAccountByName("admin");
        System.out.println(user.getName());
    }

}
