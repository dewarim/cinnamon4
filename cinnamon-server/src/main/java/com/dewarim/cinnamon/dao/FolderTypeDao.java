package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.FolderType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class FolderTypeDao implements CrudDao<FolderType>{

    @Override
    public String getTypeClassName() {
        return FolderType.class.getName();
    }

    public List<FolderType> listFolderTypes() {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return sqlSession.selectList("com.dewarim.cinnamon.FolderTypeMapper.list");
    }

    public Optional<FolderType> getFolderTypeById(long id) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        FolderType folderType = sqlSession.selectOne("com.dewarim.cinnamon.model.FolderType.getFolderTypeById", id);
        return Optional.ofNullable(folderType);
    }

    public Optional<FolderType> getFolderTypeByName(String name) {
        SqlSession   sqlSession   = ThreadLocalSqlSession.getSqlSession();
        FolderType folderType = sqlSession.selectOne("com.dewarim.cinnamon.model.FolderType.getFolderTypeByName", name);
        return Optional.ofNullable(folderType);
    }



}
