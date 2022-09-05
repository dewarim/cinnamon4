package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderMetaDao implements CrudDao<Meta>, MetaDao{

    public List<Meta> listByFolderId(long id) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.listByFolderId", id);
    }

    public Meta getFolderMetaById(Long metaId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectOne("com.dewarim.cinnamon.model.FolderMeta.getMetasetById", metaId);
    }

    public int deleteById(Long metaId) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.delete("com.dewarim.cinnamon.model.FolderMeta.deleteById",metaId);
    }

    public List<Meta> listByFolderIds(List<Long> folderIds) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.listByFolderIds", folderIds);
    }

    public void deleteByFolderIds(List<Long> folderIdsToDelete) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<List<Long>> partitions  = CrudDao.partitionLongList(folderIdsToDelete);
        partitions.forEach(partition -> {
            try {
                sqlSession.delete("com.dewarim.cinnamon.model.FolderMeta.deleteByFolderIds", partition);
            }
            catch (PersistenceException e){
                throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
            }
        });
    }

    @Override
    public String getTypeClassName() {
        return "com.dewarim.cinnamon.model.FolderMeta";
    }
    public List<Meta> getMetaByTypeIdsAndOsd(List<Long> typeIds, long id) {
        SqlSession          sqlSession = ThreadLocalSqlSession.getSqlSession();
        Map<String, Object> params     = new HashMap<>();
        params.put("id", id);
        params.put("typeIds", typeIds);
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.getMetaByTypeIdsAndFolder", params);
    }

    public List<Long> getUniqueMetaTypeIdsOfObject(Long folderId){
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.getUniqueMetaTypeIdsOfFolder", folderId);
    }
}
