package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.model.GroupUser;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class UserAccountDao implements CrudDao<UserAccount> {

    private static final Logger log = LogManager.getLogger(UserAccountDao.class);

    /**
     * Cache for superuser status. Key is user ID, value is superuser status.
     * This cache is invalidated when a user's group membership changes.
     */
    private static final Map<Long, Boolean> superuserCache = new ConcurrentHashMap<>();

    public Optional<UserAccount> getUserAccountByName(String username) {
        SqlSession            sqlSession  = getSqlSession();
        Optional<UserAccount> userAccount = Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.UserAccount.getUserAccountByName", username));
        userAccount.ifPresent(this::addGroupInfo);
        return userAccount;
    }

    public Optional<UserAccount> getUserAccountById(Long id) {
        SqlSession            sqlSession  = getSqlSession();
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
        SqlSession sqlSession = getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.UserAccount.changeUserActivationStatus", user);
    }

    public boolean isSuperuser(UserAccount user) {
        Long    userId = user.getId();
        Boolean cached = superuserCache.get(userId);
        if (cached != null) {
            return cached;
        }

        SqlSession          sqlSession  = getSqlSession();
        Map<String, Object> params      = Map.of("superuserGroupName", Constants.GROUP_SUPERUSERS, "userId", userId);
        boolean             isSuperuser = sqlSession.selectOne("com.dewarim.cinnamon.model.UserAccount.getSuperuserStatus", params) != null;

        superuserCache.put(userId, isSuperuser);
        log.debug("Cached superuser status for user {}: {}", userId, isSuperuser);
        return isSuperuser;
    }

    /**
     * Invalidate the superuser cache for a specific user.
     * This should be called when a user's group membership changes.
     *
     * @param userId the user ID to invalidate
     */
    public static void invalidateSuperuserCache(Long userId) {
        if (userId != null) {
            Boolean removed = superuserCache.remove(userId);
            if (removed != null) {
                log.info("Invalidated superuser cache for user {}", userId);
            }
        }
    }

    /**
     * Clear the entire superuser cache.
     * Use this when bulk changes are made to group memberships.
     */
    public static void clearSuperuserCache() {
        superuserCache.clear();
        log.info("Cleared entire superuser cache");
    }

    public static boolean currentUserIsSuperuser() {
        UserAccountDao accountDao = new UserAccountDao();
        return accountDao.isSuperuser(RequestScope.getCurrentUser());
    }

    public List<UserAccount> listActiveUserAccounts() {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.UserAccount.listActiveUserAccounts");
    }

    public List<UserAccount> listUserAccounts() {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.UserAccount.list");
    }

    public void updateUser(UserAccount user) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.update("com.dewarim.cinnamon.model.UserAccount.update", user);
    }

    @Override
    public String getTypeClassName() {
        return UserAccount.class.getName();
    }
}
