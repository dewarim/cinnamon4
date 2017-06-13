package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.SqlSession;

/**
 */
public class UserAccountDao {

    private DbSessionFactory dbSessionFactory;

    public UserAccountDao(DbSessionFactory dbSessionFactory) {
        this.dbSessionFactory = dbSessionFactory;
    }

    public UserAccount getUserAccountByName(String username) {
        try (SqlSession sqlSession = dbSessionFactory.getSession()) {
            return sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountByName", username);
        }
    }

    public static void main(String[] args) {
        UserAccount user = new UserAccountDao(new DbSessionFactory(null)).getUserAccountByName("admin");
        System.out.println(user.getName());
    }

}
