package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;

import static com.dewarim.cinnamon.Constants.ADMIN_USER_NAME;
import static com.dewarim.cinnamon.Constants.DAO_USER_ACCOUNT;

/**
 */
public class CinnamonIntegrationTest {
    
    static int cinnamonTestPort = 9999;
    static CinnamonServer cinnamonServer;
    
    @BeforeClass
    public static void setUpServer() throws Exception{
        if(cinnamonServer == null) {
            cinnamonServer = new CinnamonServer(cinnamonTestPort);

            DbSessionFactory dbSessionFactory = new DbSessionFactory("sql/mybatis.test.properties.xml");

            SqlSession session = dbSessionFactory.getSqlSessionFactory().openSession();
            Connection conn = session.getConnection();
            Reader reader = Resources.getResourceAsReader("sql/CreateTestDB.sql");
            ScriptRunner runner = new ScriptRunner(conn);
            PrintWriter errorPrintWriter = new PrintWriter(System.out);
            runner.setErrorLogWriter(errorPrintWriter);
            runner.runScript(reader);
            reader.close();
            session.close();

            cinnamonServer.setDbSessionFactory(dbSessionFactory);
            cinnamonServer.start();
        }
    }
    
    @AfterClass
    public static void shutDownServer() throws Exception{
//        cinnamonServer.getServer().stop();
    }

    /**
     * Simple test to lookup the admin user of the test database - if this works, setup is probably okay.
     */
    @Test
    public void testAdminUserExists() throws Exception {
        Server server = cinnamonServer.getServer();
        UserAccountDao userAccountDao = (UserAccountDao) server.getAttribute(DAO_USER_ACCOUNT);
        UserAccount admin = userAccountDao.getUserAccountByName(ADMIN_USER_NAME);
        Assert.assertEquals(ADMIN_USER_NAME, admin.getName());
        Assert.assertEquals(1L,admin.getId().longValue());
    }
}
