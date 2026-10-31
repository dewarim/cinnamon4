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

    private final SqlSessionFactory sqlSessionFactory;
    
    public DbSessionFactory(String propertiesFilename, String resourcePath) {
        try {
            InputStream mybatisConfigStream = Resources.getResourceAsStream(resourcePath);
            Properties properties = new Properties();
            // seeded first so that a properties file written before this setting existed - the test
            // configuration, for one - still resolves the placeholder in mybatis-config.xml
            properties.put("poolMaximumActiveConnections",
                    String.valueOf(DatabaseConfig.DEFAULT_POOL_MAXIMUM_ACTIVE_CONNECTIONS));
            if (propertiesFilename == null) {
                DatabaseConfig databaseConfig = CinnamonServer.config.getDatabaseConfig();
                properties.put("username",databaseConfig.getUser());
                properties.put("password",databaseConfig.getPassword());
                properties.put("driver",databaseConfig.getDriver());
                properties.put("url",databaseConfig.getDatabaseUrl());
                properties.put("poolMaximumActiveConnections",
                        String.valueOf(databaseConfig.getPoolMaximumActiveConnections()));
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
