package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ConfigEntry;
import org.apache.ibatis.session.SqlSession;

import java.util.Optional;

public class ConfigEntryDao implements CrudDao<ConfigEntry>{

    public Optional<ConfigEntry> getConfigEntryByName(String name) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.model.ConfigEntry.getConfigEntryByName", name));
    }
    
    public void insertConfigEntry(ConfigEntry configEntry) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.model.ConfigEntry.insertConfigEntry", configEntry);
    }
    
    public void updateConfigEntry(ConfigEntry configEntry) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.model.ConfigEntry.updateConfigEntry", configEntry);
    }

    @Override
    public String getTypeClassName() {
        return ConfigEntry.class.getName();
    }
}
