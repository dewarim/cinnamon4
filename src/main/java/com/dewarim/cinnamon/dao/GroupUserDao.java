package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.GroupUser;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupUserDao implements CrudDao<GroupUser> {

    @Override
    public String getTypeClassName() {
        return GroupUser.class.getName();
    }

    public void removeUserFromGroups(Long userId, List<Long> ids) {
        if(ids == null || ids.isEmpty()){
            return;
        }
        SqlSession sqlSession = getSqlSession();
        Map<String, Object> params = Map.of("userId", userId, "ids", ids);
        sqlSession.delete("com.dewarim.cinnamon.model.GroupUser.remove", params);
        // Invalidate caches since group membership changed
        invalidateUserCaches(userId);
    }

    public void addUserToGroups(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        create(new GroupDao().getObjectsById(ids).stream().map(group ->
                        new GroupUser(userId, group.getId())
                ).collect(Collectors.toList())
        );
        // Invalidate caches since group membership changed
        invalidateUserCaches(userId);
    }

    public List<GroupUser> listGroupsOfUser(Long userId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.GroupUser.listGroupsOfUser", userId);
    }

    public void deleteByGroupIds(List<Long> ids) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.GroupUser.deleteByGroupIds", ids);
        // Note: This affects potentially many users, so we clear all caches
        UserAccountDao.clearSuperuserCache();
        AccessFilter.reload();
    }

    public void deleteByUserId(Long userId) {
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.GroupUser.deleteByUserId", userId);
        // Invalidate caches since group membership changed
        invalidateUserCaches(userId);
    }

    /**
     * Invalidate all caches related to a user's permissions and superuser status.
     * @param userId the user ID whose caches should be invalidated
     */
    private void invalidateUserCaches(Long userId) {
        UserAccountDao.invalidateSuperuserCache(userId);
        AccessFilter.reloadUser(userId);
    }
}
