package com.dewarim.cinnamon.application;

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
        if(propertiesFilename == null) {
            propertiesFilename = DEFAULT_PROPERTIES_FILENAME;
        }
        try {
            InputStream mybatisConfigStream = Resources.getResourceAsStream(resource);
            InputStream propertyStream = Resources.getResourceAsStream(propertiesFilename);
            Properties properties = new Properties();
            properties.loadFromXML(propertyStream);
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
