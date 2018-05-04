package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.FolderType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class FolderTypeDao {

    public List<FolderType> listFolderTypes() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.FolderTypeMapper.list");
    }

    public Optional<FolderType> getFolderTypeById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        FolderType folderType = sqlSession.selectOne("com.dewarim.cinnamon.FolderTypeMapper.getFolderTypeById", id);
        return Optional.ofNullable(folderType);
    }


}
