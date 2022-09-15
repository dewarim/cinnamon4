package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.GroupUser;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class UserAccountDao implements CrudDao<UserAccount> {

    public Optional<UserAccount> getUserAccountByName(String username) {
        SqlSession            sqlSession  = ThreadLocalSqlSession.getSqlSession();
        Optional<UserAccount> userAccount = Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.UserAccount.getUserAccountByName", username));
        userAccount.ifPresent(this::addGroupInfo);
        return userAccount;
    }

    public Optional<UserAccount> getUserAccountById(Long id) {
        SqlSession            sqlSession  = ThreadLocalSqlSession.getSqlSession();
        Optional<UserAccount> userAccount = Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.UserAccount.getUserAccountById", id));
        userAccount.ifPresent(this::addGroupInfo);
        return userAccount;
    }

    private void addGroupInfo(UserAccount userAccount) {
        var             groupUserDao = new GroupUserDao();
        List<GroupUser> groupUsers   = groupUserDao.listGroupsOfUser(userAccount.getId());
        userAccount.getGroupIds().addAll(groupUsers.stream().map(GroupUser::getGroupId).toList());
    }

    public void changeUserActivationStatus(UserAccount user) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.UserAccount.changeUserActivationStatus", user);
    }

    public boolean isSuperuser(UserAccount user) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = Map.of("superuserGroupName", Constants.GROUP_SUPERUSERS, "userId", user.getId());
        return sqlSession.selectOne("com.dewarim.cinnamon.model.UserAccount.getSuperuserStatus",
                params) != null;
    }

    public static boolean currentUserIsSuperuser() {
        UserAccountDao accountDao = new UserAccountDao();
        return accountDao.isSuperuser(ThreadLocalSqlSession.getCurrentUser());
    }

    public List<UserAccount> listActiveUserAccounts() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.UserAccount.listActiveUserAccounts");
    }

    public List<UserAccount> listUserAccounts() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.UserAccount.list");
    }

    public void updateUser(UserAccount user) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.UserAccount.update", user);
    }

    @Override
    public String getTypeClassName() {
        return UserAccount.class.getName();
    }
}
