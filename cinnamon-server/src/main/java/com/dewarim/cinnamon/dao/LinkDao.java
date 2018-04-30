package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Link;
import com.dewarim.cinnamon.model.request.CreateLinkRequest;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class LinkDao {


    public Optional<Link> getLinkById(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.LinkMapper.getLinkById", id));
    }


    public List<Link> getLinksByFolderId(Long folderId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.LinkMapper.getLinkByFolderId", folderId);
    }

    public int deleteLink(Long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.LinkMapper.deleteLink", id);
    }

    public Link createLink(CreateLinkRequest linkRequest) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        Link link = new Link();
        link.setType(linkRequest.getLinkType());
        switch (link.getType()) {
            case FOLDER:
                link.setFolderId(linkRequest.getId());
                break;
            case OBJECT:
                link.setObjectId(linkRequest.getId());
                break;
        }
        link.setAclId(linkRequest.getAclId());
        link.setResolver(linkRequest.getLinkResolver());
        link.setOwnerId(linkRequest.getOwnerId());
        link.setParentId(linkRequest.getParentId());
        int resultRows = sqlSession.insert("com.dewarim.cinnamon.LinkMapper.insertLink", link);
        if(resultRows != 1){
            throw new RuntimeException("Create link failed.");
        }
        return link;
    }

    public int updateLink(Link link) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.update("com.dewarim.cinnamon.LinkMapper.updateLink", link);

    }
}
