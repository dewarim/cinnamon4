package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.UserInfo;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 */
public class UserAccountDao {

    public Optional<UserAccount> getUserAccountByName(String username) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountByName", username));
    }

    public Optional<UserAccount> getUserAccountById(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountById", id));
    }

    public void changeUserActivationStatus(UserAccount user) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.UserAccountMapper.changeUserActivationStatus", user);
    }

    public boolean isSuperuser(UserAccount user) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("superuserGroupName", Constants.GROUP_SUPERUSERS);
        params.put("userId", user.getId());
        return sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getSuperuserStatus",
                params) != null;
    }

    public static boolean currentUserIsSuperuser() {
        UserAccountDao accountDao = new UserAccountDao();
        return accountDao.isSuperuser(ThreadLocalSqlSession.getCurrentUser());
    }

    public List<UserAccount> listActiveUserAccounts() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.UserAccountMapper.listActiveUserAccounts");
    }

    public List<UserAccount> listUserAccounts() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.UserAccountMapper.listUserAccounts");
    }

    public List<UserInfo> listUserAccountsAsUserInfo() {
        List<UserAccount> accounts = listUserAccounts();
        return accounts.stream()
                .map(user -> new UserInfo(user.getId(), user.getName(), user.getLoginType(),
                        user.isActivated(), user.isLocked(), user.getUiLanguageId()))
                .collect(Collectors.toList());
    }

    public void updateUser(UserAccount user){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.UserAccountMapper.updateUser", user);
    }
}
