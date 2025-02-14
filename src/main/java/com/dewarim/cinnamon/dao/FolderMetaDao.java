package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.Meta;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FolderMetaDao implements CrudDao<Meta>, MetaDao {
    private SqlSession sqlSession;

    public FolderMetaDao() {
    }

    public FolderMetaDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<Meta> listByFolderId(long id) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.listByFolderId", id);
    }

    public Optional<Meta> getMetaById(Long metaId) {
        SqlSession sqlSession = getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.FolderMeta.getMetasetById", metaId));
    }

    public List<Meta> listMetaByObjectIds(List<Long> folderIds) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.listByFolderIds", folderIds);
    }

    public void deleteByFolderIds(List<Long> folderIdsToDelete) {
        SqlSession       sqlSession = getSqlSession();
        List<List<Long>> partitions = CrudDao.partitionLongList(folderIdsToDelete);
        partitions.forEach(partition -> {
            try {
                sqlSession.delete("com.dewarim.cinnamon.model.FolderMeta.deleteByFolderIds", partition);
            } catch (PersistenceException e) {
                throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
            }
        });
    }

    @Override
    public String getTypeClassName() {
        return "com.dewarim.cinnamon.model.FolderMeta";
    }

    public List<Meta> getMetaByTypeIdsAndOsd(List<Long> typeIds, long id) {
        SqlSession          sqlSession = getSqlSession();
        Map<String, Object> params     = Map.of("id", id, "typeIds", typeIds);
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.getMetaByTypeIdsAndFolder", params);
    }

    public List<Long> getUniqueMetaTypeIdsOfObject(Long folderId) {
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.model.FolderMeta.getUniqueMetaTypeIdsOfFolder", folderId);
    }


    @Override
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            sqlSession = ThreadLocalSqlSession.getSqlSession();
        }
        return sqlSession;
    }

}
