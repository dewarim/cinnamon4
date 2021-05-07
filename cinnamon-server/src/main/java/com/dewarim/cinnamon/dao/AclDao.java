package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.CmnGroup;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AclDao implements CrudDao<Acl> {

    public Optional<Acl> getAclById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Acl        acl        = sqlSession.selectOne("com.dewarim.cinnamon.model.Acl.getAclById", id);
        return Optional.ofNullable(acl);
    }

    public Acl getAclByName(String name) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.Acl.getAclByName", name);
    }

    public List<Acl> getUserAcls(Long userId) {
        SqlSession    sqlSession = ThreadLocalSqlSession.getSqlSession();
        CmnGroupDao   groupDao   = new CmnGroupDao();
        Set<CmnGroup> groups     = groupDao.getGroupsWithAncestorsOfUserById(userId);
        List<Long>    groupIds   = groups.stream().map(CmnGroup::getId).collect(Collectors.toList());
        List<Acl>     acls       = sqlSession.selectList("com.dewarim.cinnamon.model.Acl.getUserAcls", groupIds);
        return acls.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public String getTypeClassName() {
        return Acl.class.getName();
    }
}
