package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Permission;
import org.apache.ibatis.session.SqlSession;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionDao {

    public List<Permission> getUserPermissionForAcl(long userId, long aclId) {
        GroupDao   groupDao = new GroupDao();
        Set<Group> groups      = groupDao.getGroupsWithAncestorsOfUserById(userId);
        List<Long>  groupIds    = groups.stream().map(Group::getId).collect(Collectors.toList());

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
    
    public Permission getPermissionByName(String name){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.PermissionMapper.getPermissionByName",name);
    }

}
