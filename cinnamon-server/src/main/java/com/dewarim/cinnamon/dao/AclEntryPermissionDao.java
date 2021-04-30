package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.AclEntryPermission;
import com.dewarim.cinnamon.model.Permission;
import org.apache.ibatis.session.SqlSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AclEntryPermissionDao {

    public boolean checkIfAnyAclEntryHasThisPermission(List<AclEntry> aclEntries, Permission permission) {
        if(aclEntries.isEmpty()){
            return false;
        }
         return fetchAepCombinationOfAclEntriesAndThisPermission(aclEntries,permission).size() > 0;
    }
    
    public List<AclEntry> filterAclEntriesWithoutThisPermission(List<AclEntry> aclEntries, Permission permission){
        if(aclEntries.isEmpty()){
            return Collections.emptyList();
        }
        List<AclEntryPermission> aclEntryPermissions = fetchAepCombinationOfAclEntriesAndThisPermission(aclEntries, permission);
        Set<Long> allowedAclEntryIds = aclEntryPermissions.stream().map(AclEntryPermission::getAclEntryId).collect(Collectors.toSet());
        return aclEntries.stream().filter(aclEntry -> allowedAclEntryIds.contains(aclEntry.getId())).collect(Collectors.toList());
    }
    
    private List<AclEntryPermission> fetchAepCombinationOfAclEntriesAndThisPermission(List<AclEntry> aclEntries, Permission permission){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params = new HashMap<>();
        params.put("aclEntries", aclEntries);
        params.put("permissionId", permission.getId());
        return sqlSession.selectList("com.dewarim.cinnamon.model.AclEntryPermissionMapper.getAclEntryPermissionForAclEntriesAndPermission", params);
    }
}
