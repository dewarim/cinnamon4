package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.service.debug.DebugLogService;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Group;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AclDao implements CrudDao<Acl> {

    // TODO: maybe make cache configurable -> could use singleton CacheService to return cache of class X
    private static final Cache<Long, Acl> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

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

    @Override
    public boolean useCache() {
        return true;
    }

    @Override
    public void addToCache(Acl item) {
        CACHE.put(item.getId(), item);
    }

    @Override
    public Acl getCachedVersion(Long id) {
        return CACHE.getIfPresent(id);
    }
}
