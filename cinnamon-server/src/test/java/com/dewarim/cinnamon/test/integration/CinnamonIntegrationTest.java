package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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

    protected static String getAdminTicket() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON_CONNECT.getPath();
        String tokenRequestResult = Request.Post(url)
                .bodyForm(Form.form().add("user", "admin").add("pwd", "admin").build())
                .execute().returnContent().asString();
        XmlMapper mapper = new XmlMapper();
        CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
        return cinnamonConnection.getTicket();
    }
    
    protected void assertResponseOkay(HttpResponse response){
        assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_OK));
    }
    
    protected void assertCinnamonError(HttpResponse response, ErrorCode errorCode) throws IOException{
        Assert.assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        CinnamonError cinnamonError = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        Assert.assertThat(cinnamonError.getCode(), equalTo(errorCode.getCode()));  
    }
}
