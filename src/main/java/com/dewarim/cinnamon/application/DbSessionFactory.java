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

    /**
     * System property that overrides MyBatis' autoMappingUnknownColumnBehavior. The test run sets
     * it to FAILING so that a statement selecting a column no property can receive fails the build;
     * production leaves it unset, which means NONE.
     */
    public static final String UNKNOWN_COLUMN_BEHAVIOUR_PROPERTY = "cinnamon.autoMappingUnknownColumnBehavior";

    private final SqlSessionFactory sqlSessionFactory;
    
    public DbSessionFactory(String propertiesFilename, String resourcePath) {
        try {
            InputStream mybatisConfigStream = Resources.getResourceAsStream(resourcePath);
            Properties properties = new Properties();
            // seeded first so that a properties file written before this setting existed - the test
            // configuration, for one - still resolves the placeholder in mybatis-config.xml
            properties.put("poolMaximumActiveConnections",
                    String.valueOf(DatabaseConfig.DEFAULT_POOL_MAXIMUM_ACTIVE_CONNECTIONS));
            // NONE unless overridden: see the comment on the setting in mybatis-config.xml.
            properties.put("autoMappingUnknownColumnBehavior",
                    System.getProperty(UNKNOWN_COLUMN_BEHAVIOUR_PROPERTY, "NONE"));
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
