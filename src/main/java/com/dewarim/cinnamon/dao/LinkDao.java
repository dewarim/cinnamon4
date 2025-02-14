package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.links.IdsAndLinkType;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LinkDao implements CrudDao<Link> {

    @Override
    public String getTypeClassName() {
        return Link.class.getName();
    }

    public Optional<Link> getLinkById(long id) {
        SqlSession sqlSession = getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.links.Link.getLinkById", id));
    }

    public List<Link> getLinksByFolderId(Long folderId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.links.Link.getLinkByFolderId", folderId);
    }

    public List<Link> getLinksByFolderIdAndLinkType(List<Long> folderIds, LinkType linkType) {
        SqlSession     sqlSession     = getSqlSession();
        IdsAndLinkType idsAndLinkType = new IdsAndLinkType(folderIds, linkType);
        return sqlSession.selectList("com.dewarim.cinnamon.model.links.Link.getLinkByFolderIdAndLinkType", idsAndLinkType);
    }

    public int updateLink(Link link) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.update("com.dewarim.cinnamon.model.links.Link.updateLink", link);
    }

    public void deleteAllFixedLinksToObjects(List<Long> osdIds) {
        // TODO maybe: batch deleteAllFixedLinksToObjects - or fetch links, then delete via CRUD
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.links.Link.deleteAllFixedLinksToObjects", osdIds);
    }

    public List<Link> getLinksToOutsideStuff(List<Long> folderIds) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.links.Link.getLinksToOutsideStuff", folderIds);
    }

    public void deleteAllLinksToFolders(List<Long> folderIds) {
        // TODO maybe: batch deleteAllLinksToFolders - or fetch links, then delete via CRUD
        SqlSession sqlSession = getSqlSession();
        sqlSession.delete("com.dewarim.cinnamon.model.links.Link.deleteAllLinksToFolders", folderIds);

    }

    public List<Link> getLinksWeMayWantToDelete(List<Long> objectIds) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.links.Link.getLinksWeMayWantToDelete", objectIds);
    }
}
