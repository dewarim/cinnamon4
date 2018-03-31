package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.configuration.DatabaseConfig;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 */
public class DbSessionFactory {

    private SqlSessionFactory sqlSessionFactory;
    
    public static final String DEFAULT_PROPERTIES_FILENAME = "sql/mybatis.properties.xml";
    
    public DbSessionFactory(String propertiesFilename) {
        String resource = "sql/mybatis-config.xml";
        try {
            InputStream mybatisConfigStream = Resources.getResourceAsStream(resource);
            Properties properties = new Properties();
            if (propertiesFilename == null) {
                DatabaseConfig databaseConfig = CinnamonServer.config.getDatabaseConfig();
                properties.put("username",databaseConfig.getUser());
                properties.put("password",databaseConfig.getPassword());
                properties.put("driver",databaseConfig.getDriver());
                properties.put("url",databaseConfig.getDatabaseUrl());
            }
            else {
                InputStream propertyStream = Resources.getResourceAsStream(propertiesFilename);
                properties.loadFromXML(propertyStream);
            }
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfigStream, properties);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
    
    public SqlSession getSession(){
        return sqlSessionFactory.openSession();
    }
}
