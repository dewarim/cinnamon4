package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.Format;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Optional;

public class ConfigEntryDao {

    public Optional<ConfigEntry> getConfigEntryByName(String name) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        return Optional.ofNullable(sqlSession.selectOne("com.dewarim.cinnamon.ConfigEntryMapper.getConfigEntryByName", name));
    }
    
    public void insertConfigEntry(ConfigEntry configEntry) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.ConfigEntryMapper.insertConfigEntry", configEntry);
    }
    
    public void updateConfigEntry(ConfigEntry configEntry) {
        SqlSession sqlSession = ThreadLocalSqlSession.getSqlSession();
        sqlSession.insert("com.dewarim.cinnamon.ConfigEntryMapper.updateConfigEntry", configEntry);
    }
    
    

}
