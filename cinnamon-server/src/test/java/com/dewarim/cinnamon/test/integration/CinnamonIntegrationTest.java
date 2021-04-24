package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonClient;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 */
public class CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(CinnamonIntegrationTest.class);

    static final String PASSWORD_PARAMETER_NAME = "password";

    static int            cinnamonTestPort = 19999;
    static CinnamonServer cinnamonServer;
    static String         ticket;
    static String         ticketForDoe;
    static String         HOST             = "http://localhost:" + cinnamonTestPort;
    static ObjectMapper   mapper           = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);
    static CinnamonClient client;

    @Before
    public void dumpSession() {
//        log.info("session in db: " + new SessionDao().list().stream().map(Session::getTicket).collect(Collectors.joining(" ")));
//        log.info("current ticket: " + ticket);
    }

    @BeforeClass
    public static void setUpServer() throws Exception {
        if (cinnamonServer == null) {
            log.info("Create new CinnamonServer.");
            cinnamonServer = new CinnamonServer(cinnamonTestPort);

            DbSessionFactory dbSessionFactory = new DbSessionFactory("sql/mybatis.test.properties.xml");

            SqlSession   session          = dbSessionFactory.getSqlSessionFactory().openSession(true);
            Connection   conn             = session.getConnection();
            Reader       reader           = Resources.getResourceAsReader("sql/CreateTestDB.sql");
            ScriptRunner runner           = new ScriptRunner(conn);
            PrintWriter  errorPrintWriter = new PrintWriter(System.out);
            runner.setErrorLogWriter(errorPrintWriter);
            runner.runScript(reader);
            reader.close();
            session.close();

            cinnamonServer.setDbSessionFactory(dbSessionFactory);
            cinnamonServer.start();

            // set data root:
            Path tempDirectory = Files.createTempDirectory("cinnamon-data-root");
            CinnamonServer.config.getServerConfig().setDataRoot(tempDirectory.toAbsolutePath().toString());
            ticket = getAdminTicket();
            log.info("admin ticket: " + ticket);

            client = new CinnamonClient(cinnamonTestPort, "localhost", "http", "doe", "admin");
        }
    }

    /**
     * @return a ticket for the Cinnamon administrator
     */
    protected static String getAdminTicket() throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        String tokenRequestResult = Request.Post(url)
                .bodyForm(Form.form().add("user", "admin").add("password", "admin").build())
                .execute().returnContent().asString();
        XmlMapper          mapper             = new XmlMapper();
        CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
        return cinnamonConnection.getTicket();
    }

    /**
     * @return a ticket for a normal user.
     */
    protected static String getDoesTicket(boolean newTicket) throws IOException {
        if (ticketForDoe == null || newTicket) {
            String url = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
            String tokenRequestResult = Request.Post(url)
                    .bodyForm(Form.form().add("user", "doe").add("password", "admin").build())
                    .execute().returnContent().asString();
            XmlMapper          mapper             = new XmlMapper();
            CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
            ticketForDoe = cinnamonConnection.getTicket();
        }
        return ticketForDoe;
    }

    protected void assertResponseOkay(HttpResponse response) throws IOException {
        Integer statusCode = response.getStatusLine().getStatusCode();
        if (!statusCode.equals(HttpStatus.SC_OK)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().getContent().transferTo(baos);
            log.error("Request failed with:\n{}", baos.toString(StandardCharsets.UTF_8));
            fail("Non-OK status code " + statusCode);
        }
        ;
    }

    protected void assertCinnamonError(HttpResponse response, ErrorCode errorCode) throws IOException {
        String responseText = new String(response.getEntity().getContent().readAllBytes());
        Assert.assertTrue("response should contain errorCode " + errorCode + " but was " + responseText, responseText.contains(errorCode.getCode()));
        assertThat(errorCode.getHttpResponseCode(), equalTo(response.getStatusLine().getStatusCode()));
        CinnamonError cinnamonError = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().get(0);
        assertThat(cinnamonError.getCode(), equalTo(errorCode.getCode()));
    }

    /**
     * Send a POST request with the admin's ticket to the Cinnamon server.
     * The request object will be serialized and put into the
     * request body.
     *
     * @param urlMapping defines the API method you want to call
     * @param request    request object to be sent to the server as XML string.
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
     *
     * @param urlMapping defines the API method you want to call
     * @param request    request object to be sent to the server as XML string.
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

    protected Request createStandardRequestHeader(UrlMapping urlMapping) throws IOException {
        return Request.Post("http://localhost:" + cinnamonTestPort + urlMapping.getPath())
                .addHeader("ticket", getDoesTicket(false));
    }

    protected HttpResponse sendAdminRequest(UrlMapping urlMapping) throws IOException {
        return Request.Post("http://localhost:" + cinnamonTestPort + urlMapping.getPath())
                .addHeader("ticket", ticket)
                .execute().returnResponse();
    }

    protected GenericResponse parseGenericResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        return mapper.readValue(response.getEntity().getContent(), GenericResponse.class);
    }
}
