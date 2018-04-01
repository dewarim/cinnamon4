package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.CmnGroup;
import com.dewarim.cinnamon.model.Permission;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionDao {

    public List<Permission> getUserPermissionForAcl(long userId, long aclId) {
        CmnGroupDao cmnGroupDao = new CmnGroupDao();
        Set<CmnGroup> groups = cmnGroupDao.getGroupsWithAncestorsOfUserById(userId);
        List<Long> groupIds = groups.stream().map(CmnGroup::getId).collect(Collectors.toList());

        AclEntryDao aclEntryDao = new AclEntryDao();
        List<AclEntry> aclEntries = aclEntryDao.getAclEntriesByGroupIdsAndAcl(groupIds,aclId);
        if(aclEntries.isEmpty()){
            // if the user has no connection to the given acl, he won't have any permissions.
            return Collections.emptyList();
        }
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.PermissionMapper.getUserPermissionsForAclEntries", aclEntries);
    }

    public List<Permission> listPermissions() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.PermissionMapper.listPermissions");
    }

}
