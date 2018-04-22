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
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import javax.servlet.http.HttpServletResponse;
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
    static String ticketForDoe;

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

    /**
     * 
     * @return a ticket for the Cinnamon administrator
     */
    protected static String getAdminTicket() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON_CONNECT.getPath();
        String tokenRequestResult = Request.Post(url)
                .bodyForm(Form.form().add("user", "admin").add("pwd", "admin").build())
                .execute().returnContent().asString();
        XmlMapper mapper = new XmlMapper();
        CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
        return cinnamonConnection.getTicket();
    }

    /**
     * 
     * @return a ticket for a normal user.
     */
    protected static String getDoesTicket(boolean newTicket) throws IOException {
        if(ticketForDoe == null || newTicket) {
            String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON_CONNECT.getPath();
            String tokenRequestResult = Request.Post(url)
                    .bodyForm(Form.form().add("user", "doe").add("pwd", "admin").build())
                    .execute().returnContent().asString();
            XmlMapper mapper = new XmlMapper();
            CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
            ticketForDoe = cinnamonConnection.getTicket();
            return ticketForDoe;
        }
        else {
            return ticketForDoe;
        }
    }
    
    protected void assertResponseOkay(HttpResponse response){
        assertThat(response.getStatusLine().getStatusCode(),equalTo(HttpStatus.SC_OK));
    }
    
    protected void assertCinnamonError(HttpResponse response, ErrorCode errorCode) throws IOException{
        Assert.assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        CinnamonError cinnamonError = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        Assert.assertThat(cinnamonError.getCode(), equalTo(errorCode.getCode()));  
    }  
    
    protected void assertCinnamonError(HttpResponse response, ErrorCode errorCode, int statusCode ) throws IOException{
        Assert.assertThat(response.getStatusLine().getStatusCode(), equalTo(statusCode));
        CinnamonError cinnamonError = mapper.readValue(response.getEntity().getContent(), CinnamonError.class);
        Assert.assertThat(cinnamonError.getCode(), equalTo(errorCode.getCode()));  
    }

    /**
     * Send a POST request with the admin's ticket to the Cinnamon server. 
     * The request object will be serialized and put into the
     * request body.
     * @param urlMapping defines the API method you want to call
     * @param request request object to be sent to the server as XML string.
     * @return the server's response.
     * @throws IOException if connection to server fails for some reason
     */
    protected HttpResponse sendAdminRequest(UrlMapping urlMapping, Object request) throws IOException {
        String requestStr = mapper.writeValueAsString(request);
        return Request.Post("http://localhost:" + cinnamonTestPort + urlMapping.getPath())
                .addHeader("ticket", ticket)
                .bodyString(requestStr, ContentType.APPLICATION_XML)
                .execute().returnResponse();
    }
    
    /**
     * Send a POST request with a normal user's ticket to the Cinnamon server. 
     * The request object will be serialized and put into the request body.
     * @param urlMapping defines the API method you want to call
     * @param request request object to be sent to the server as XML string.
     * @return the server's response.
     * @throws IOException if connection to server fails for some reason
     */
    protected HttpResponse sendStandardRequest(UrlMapping urlMapping, Object request) throws IOException {
        String requestStr = mapper.writeValueAsString(request);
        return Request.Post("http://localhost:" + cinnamonTestPort + urlMapping.getPath())
                .addHeader("ticket", getDoesTicket(false))
                .bodyString(requestStr, ContentType.APPLICATION_XML)
                .execute().returnResponse();
    }
    
    protected HttpResponse sendAdminRequest(UrlMapping urlMapping) throws IOException {
        return Request.Post("http://localhost:" + cinnamonTestPort + urlMapping.getPath())
                .addHeader("ticket", ticket)
                .execute().returnResponse();
    }
}
