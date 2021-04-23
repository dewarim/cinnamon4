package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.CmnGroup;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AclDao {

    public Optional<Acl> getAclById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Acl acl = sqlSession.selectOne("com.dewarim.cinnamon.AclMapper.getAclById", id);
        return Optional.ofNullable(acl);
    }

    public Acl getAclByName(String name) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.AclMapper.getAclByName", name);
    }

    public Acl save(Acl acl) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.AclMapper.insertAcl", acl);
        return acl;
    }

    public int changeAclName(Acl acl) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.update("com.dewarim.cinnamon.AclMapper.changeAclName", acl);
    }

    public int deleteAcl(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.AclMapper.deleteAcl", id);
    }

    public List<Acl> list() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.AclMapper.listAcls");
    }

    public List<Acl> getUserAcls(Long userId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        CmnGroupDao groupDao = new CmnGroupDao();
        Set<CmnGroup> groups = groupDao.getGroupsWithAncestorsOfUserById(userId);
        List<Long> groupIds = groups.stream().map(CmnGroup::getId).collect(Collectors.toList());
        List<Acl> acls = sqlSession.selectList("com.dewarim.cinnamon.AclMapper.getUserAcls", groupIds);
        return acls.stream().distinct().collect(Collectors.toList());
    }

}
