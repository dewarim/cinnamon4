package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Permission;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionDao implements CrudDao<Permission> {

    public List<Permission> getUserPermissionForAcl(long userId, long aclId) {
        GroupDao   groupDao = new GroupDao();
        Set<Group> groups   = groupDao.getGroupsWithAncestorsOfUserById(userId);
        List<Long> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());

        AclGroupDao    aclGroupDao = new AclGroupDao();
        List<AclGroup> aclGroups   = aclGroupDao.getAclGroupsByGroupIdsAndAcl(groupIds, aclId);
        if (aclGroups.isEmpty()) {
            // if the user has no connection to the given acl, he won't have any permissions.
            return Collections.emptyList();
        }
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.Permission.getUserPermissionsForAclGroups", aclGroups);
    }

    public Permission getPermissionByName(String name) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.Permission.getPermissionByName", name);
    }

    @Override
    public List<Permission> create(List<Permission> items) {
        throw new CinnamonException("Permissions can only be created by developers.");
    }

    @Override
    public int delete(List<Long> ids) {
        throw new CinnamonException("Permissions cannot be deleted.");
    }

    @Override
    public List<Permission> update(List<Permission> items) throws SQLException {
        throw new CinnamonException("Permissions can only be updated by developers.");
    }

    @Override
    public String getTypeClassName() {
        return Permission.class.getName();
    }
}
