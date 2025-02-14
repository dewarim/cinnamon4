package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.service.debug.DebugLogService;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Group;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AclDao implements CrudDao<Acl> {

    public Optional<Acl> getAclByName(String name) {
        SqlSession sqlSession = getSqlSession();
        Acl        selected   = sqlSession.selectOne("com.dewarim.cinnamon.model.Acl.getAclByName", name);
        DebugLogService.log("aclByName:",selected);
        return Optional.ofNullable(selected);
    }

    public List<Acl> getUserAcls(Long userId) {
        SqlSession sqlSession = getSqlSession();
        GroupDao   groupDao   = new GroupDao();
        Set<Group> groups     = groupDao.getGroupsWithAncestorsOfUserById(userId);
        List<Long> groupIds   = groups.stream().map(Group::getId).collect(Collectors.toList());
        List<Acl>  acls       = sqlSession.selectList("com.dewarim.cinnamon.model.Acl.getUserAcls", groupIds);
        List<Acl>  distinctAcls    = acls.stream().distinct().collect(Collectors.toList());
        DebugLogService.log("getUserAcls:",distinctAcls);
        return distinctAcls;
    }

    @Override
    public String getTypeClassName() {
        return Acl.class.getName();
    }
}
