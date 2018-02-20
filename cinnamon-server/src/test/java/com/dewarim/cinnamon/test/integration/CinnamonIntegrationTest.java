package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;

import static com.dewarim.cinnamon.Constants.ADMIN_USER_NAME;
import static com.dewarim.cinnamon.Constants.DAO_USER_ACCOUNT;

/**
 */
public class CinnamonIntegrationTest {
    
    static int cinnamonTestPort = 19999;
    static CinnamonServer cinnamonServer;
    static String ticket;

    XmlMapper mapper = new XmlMapper();
    
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
        ticket = getAdminTicket();
    }
    
    @AfterClass
    public static void shutDownServer() throws Exception{
//        cinnamonServer.getServer().stop();
    }

    private static String getAdminTicket() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON_CONNECT.getPath();
        String tokenRequestResult = Request.Post(url)
                .bodyForm(Form.form().add("user", "admin").add("pwd", "admin").build())
                .execute().returnContent().asString();
        XmlMapper mapper = new XmlMapper();
        com.dewarim.cinnamon.model.response.Connection connection = mapper.readValue(tokenRequestResult, com.dewarim.cinnamon.model.response.Connection.class);
        return connection.getTicket();
    }
}
