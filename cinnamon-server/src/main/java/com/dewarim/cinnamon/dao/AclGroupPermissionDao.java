package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.AclGroupPermission;
import com.dewarim.cinnamon.model.Permission;
import org.apache.ibatis.session.SqlSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AclGroupPermissionDao {

    public boolean checkIfAnyAclGroupHasThisPermission(List<AclGroup> aclGroups, Permission permission) {
        if(aclGroups.isEmpty()){
            return false;
        }
         return fetchAepCombinationOfAclGroupsAndThisPermission(aclGroups,permission).size() > 0;
    }
    
    public List<AclGroup> filterAclGroupsWithoutThisPermission(List<AclGroup> aclGroups, Permission permission){
        if(aclGroups.isEmpty()){
            return Collections.emptyList();
        }
        List<AclGroupPermission> aclGroupPermissions = fetchAepCombinationOfAclGroupsAndThisPermission(aclGroups, permission);
        Set<Long> allowedAclGroupIds = aclGroupPermissions.stream().map(AclGroupPermission::getAclGroupId).collect(Collectors.toSet());
        return aclGroups.stream().filter(aclGroup -> allowedAclGroupIds.contains(aclGroup.getId())).collect(Collectors.toList());
    }
    
    private List<AclGroupPermission> fetchAepCombinationOfAclGroupsAndThisPermission(List<AclGroup> aclGroups, Permission permission){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("aclGroups", aclGroups);
        params.put("permissionId", permission.getId());
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclGroupPermissionMapper.getAclGroupPermissionForAclGroupsAndPermission", params);
    }
}
