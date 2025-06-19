package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.FolderType;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class FolderTypeDao implements CrudDao<FolderType>{

    @Override
    public String getTypeClassName() {
        return FolderType.class.getName();
    }

    public Optional<FolderType> getFolderTypeById(long id) {
        SqlSession   sqlSession   = getSqlSession();
        FolderType folderType = sqlSession.selectOne("com.dewarim.cinnamon.model.FolderType.getFolderTypeById", id);
        return Optional.ofNullable(folderType);
    }

    public Optional<FolderType> getFolderTypeByName(String name) {
        SqlSession   sqlSession   = getSqlSession();
        FolderType folderType = sqlSession.selectOne("com.dewarim.cinnamon.model.FolderType.getFolderTypeByName", name);
        return Optional.ofNullable(folderType);
    }



}
