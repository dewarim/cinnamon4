package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.model.ConfigEntry;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigEntryDao implements CrudDao<ConfigEntry> {

    public List<ConfigEntry> getConfigEntriesByName(List<String> names) {
        SqlSession              sqlSession = ThreadLocalSqlSession.getSqlSession();
        List<ConfigEntry>       results    = new ArrayList<>();
        List<List<ConfigEntry>> partitions = partitionList(names.stream().map(name -> new ConfigEntry(name, null, true)).collect(Collectors.toList()));
        Map<String,Object> params = new HashMap<>();
        partitions.forEach(partition -> {
                    List<String> partNames = partition.stream().map(ConfigEntry::getName).collect(Collectors.toList());
                    results.addAll(sqlSession.selectList("com.dewarim.cinnamon.model.ConfigEntry.getConfigEntriesByName", partNames));
                }
        );
        return results;
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
