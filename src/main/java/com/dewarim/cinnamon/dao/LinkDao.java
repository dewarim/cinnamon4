package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.links.Link;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LinkDao implements CrudDao<Link> {

    @Override
    public String getTypeClassName() {
        return Link.class.getName();
    }

    public Optional<Link> getLinkById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.links.Link.getLinkById", id));
    }

    public List<Link> getLinksByFolderId(Long folderId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.links.Link.getLinkByFolderId", folderId);
    }

    public int updateLink(Link link) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.update("com.dewarim.cinnamon.model.links.Link.updateLink", link);

    }

    public void deleteAllLinksToObjects(List<Long> osdIds) {
        // TODO maybe: batch deleteAllLinksToObjects - or fetch links, then delete via CRUD
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.links.Link.deleteAllLinksToObjects", osdIds);
    }

    public List<Link> getLinksToOutsideStuff(List<Long> folderIds) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.links.Link.getLinksToOutsideStuff", folderIds);
    }

    public void deleteAllLinksToFolders(List<Long> folderIds) {
        // TODO maybe: batch deleteAllLinksToFolders - or fetch links, then delete via CRUD
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.links.Link.deleteAllLinksToFolders", folderIds);

    }
}
