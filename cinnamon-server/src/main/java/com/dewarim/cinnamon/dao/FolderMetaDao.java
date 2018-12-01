package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderMetaDao {

    public List<Meta> listByFolderId(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.FolderMetaMapper.listByFolderId", id);
    }

    public List<Meta> getMetaByNamesAndFolderId(List<String> names, long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("id", id);
        params.put("typeNames", names);
        return sqlSession.selectList("com.dewarim.cinnamon.FolderMetaMapper.getMetasetsByNameAndFolderId", params);
    }

    public Meta createMeta(CreateMetaRequest metaRequest, MetasetType metaType) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Meta folderMeta = new Meta(metaRequest.getId(), metaType.getId(), metaRequest.getContent());
        int resultRows = sqlSession.insert("com.dewarim.cinnamon.FolderMetaMapper.insertMeta", folderMeta);
        if(resultRows != 1){
            throw new RuntimeException("Create FolderMeta failed.");
        }
        return folderMeta;
    }

    public Meta getFolderMetaById(Long metaId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.FolderMetaMapper.getMetasetById", metaId);
    }

    public int deleteById(Long metaId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.FolderMetaMapper.deleteById",metaId);
    }
}
