package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclGroup;
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

        AclGroupDao aclGroupDao = new AclGroupDao();
        List<AclGroup> aclGroups = aclGroupDao.getAclGroupsByGroupIdsAndAcl(groupIds,aclId);
        if(aclGroups.isEmpty()){
            // if the user has no connection to the given acl, he won't have any permissions.
            return Collections.emptyList();
        }
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.PermissionMapper.getUserPermissionsForAclGroups", aclGroups);
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
