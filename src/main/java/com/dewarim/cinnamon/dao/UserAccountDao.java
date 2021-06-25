package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.GroupUser;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.UserInfo;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class UserAccountDao {

    public Optional<UserAccount> getUserAccountByName(String username) {
        SqlSession            sqlSession  = ThreadLocalSqlSession.getSqlSession();
        Optional<UserAccount> userAccount = Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountByName", username));
        userAccount.ifPresent(this::addGroupInfo);
        return userAccount;
    }

    public Optional<UserAccount> getUserAccountById(Long id) {
        SqlSession            sqlSession  = ThreadLocalSqlSession.getSqlSession();
        Optional<UserAccount> userAccount = Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.UserAccountMapper.getUserAccountById", id));
        userAccount.ifPresent(this::addGroupInfo);
        return userAccount;
    }

    private void addGroupInfo(UserAccount userAccount) {
        var groupUserDao = new GroupUserDao();
        List<GroupUser> groupUsers = groupUserDao.listGroupsOfUser(userAccount.getId());
        userAccount.getGroupIds().addAll(groupUsers.stream().map(GroupUser::getGroupId).collect(Collectors.toList()));
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
        Map<Long,List<Long>> groupsOfUser = new HashMap<>();
        new GroupUserDao().list().forEach(groupUser -> {
            List<Long> groupIds = groupsOfUser.getOrDefault(groupUser.getUserId(), new ArrayList<>());
            groupIds.add(groupUser.getGroupId());
            groupsOfUser.put(groupUser.getUserId(),groupIds);
        });

        return accounts.stream()
                .map(user -> new UserInfo(user.getId(), user.getName(), user.getLoginType(),
                        user.isActivated(), user.isLocked(), user.getUiLanguageId(), user.getEmail(), user.getFullname(), user.isChangeTracking(),
                        user.isPasswordExpired(), groupsOfUser.getOrDefault(user.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    public void updateUser(UserAccount user) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.UserAccountMapper.updateUser", user);
    }
}
