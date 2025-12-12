package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.DbSessionFactory;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.dao.GroupDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.request.ConnectionRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.response.CinnamonConnectionWrapper;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.test.TestObjectHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.DefaultPermission.BROWSE;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(CinnamonIntegrationTest.class);

    static final String PASSWORD_PARAMETER_NAME = "password";
    static final String XML_SUMMARY             = "<xml>a summary</xml>";

    static int            cinnamonTestPort = 19999;
    static CinnamonServer cinnamonServer;
    static String         ticket;
    static String         ticketForDoe;
    static String         HOST             = "http://localhost:" + cinnamonTestPort;
    static ObjectMapper   mapper           = XML_MAPPER;
    static CinnamonClient client;
    static CinnamonClient adminClient;
    static long           userId           = 2;
    static long           adminId          = 1;
    static HttpClient     httpClient       = HttpClients.createDefault();
    /**
     * id of folder where the standard test user can create test objects
     */
    static long           createFolderId   = 0;
    static Folder         creationFolder;

    /**
     * An ACL with all permissions for the test user.
     * Whenever we are not explicitly testing permissions, we should use this.
     */
    static Acl defaultCreationAcl;

    @BeforeAll
    public static void setUpServer() throws Exception {
        if (cinnamonServer == null) {
            log.info("Create new CinnamonServer.");
            cinnamonServer = new CinnamonServer(cinnamonTestPort);
            // set data root:
            Path tempDirectory = Files.createTempDirectory("cinnamon-data-root");
            CinnamonServer.config.getServerConfig().setDataRoot(tempDirectory.toAbsolutePath().toString());

            CinnamonServer.config.getServerConfig().setLog4jConfigPath("src/test/resources/log4j2-test.xml");
            CinnamonServer.config.getServerConfig().setLogResponses(true);

            DbSessionFactory dbSessionFactory = new DbSessionFactory("sql/mybatis.test.properties.xml");

            SqlSession session = dbSessionFactory.getSqlSessionFactory().openSession(true);
            Connection conn    = session.getConnection();
            try (Reader reader = Resources.getResourceAsReader("sql/CreateTestDB.sql")) {
                ScriptRunner runner           = new ScriptRunner(conn);
                PrintWriter  errorPrintWriter = new PrintWriter(System.out);
                runner.setErrorLogWriter(errorPrintWriter);
                runner.runScript(reader);
            }
            session.close();

            Path tempIndexDir = Files.createTempDirectory("cinnamon-index-root");
            CinnamonServer.config.getLuceneConfig().setIndexPath(tempIndexDir.toAbsolutePath().toString());

            ThreadLocalSqlSession.setDbSessionFactory(dbSessionFactory);
            cinnamonServer.setDbSessionFactory(dbSessionFactory);
            cinnamonServer.start();

            ticket = getAdminTicket();
            log.info("admin ticket: {}", ticket);

            client = new CinnamonClient(cinnamonTestPort, "localhost", "http", "doe", "admin");
//            client.setResponseContentType(CinnamonContentType.JSON);
//            client.setMapper(JSON_MAPPER);
            adminClient = new CinnamonClient(cinnamonTestPort, "localhost", "http", "admin", "admin");
            // TODO: rename to value of DEFAULT_ACL once CreateTestDb is cleaned up.
            // create a default ACL with browse permissions
            TestObjectHolder.defaultAcl = prepareAclGroupWithPermissions("default.acl", List.of(BROWSE))
                    .acl;
            // set the root folder to use the default ACL, so it is visible for the normale user
            Folder root = adminClient.getFolderById(1L, false);
            root.setAclId(TestObjectHolder.defaultAcl.getId());
            adminClient.updateFolder(root);

            var toh = prepareAclGroupWithPermissions("creation.acl", Arrays.stream(DefaultPermission.values()).toList());
            defaultCreationAcl = toh.acl;
            creationFolder = adminClient.createFolder(1L, "creation", adminId, defaultCreationAcl.getId(), 1L);
            createFolderId = creationFolder.getId();
            TestObjectHolder.defaultCreationFolderId = createFolderId;
            TestObjectHolder.defaultCreationAcl = defaultCreationAcl;
        }
    }

    /**
     * @return a ticket for the Cinnamon administrator
     */
    protected static String getAdminTicket() throws IOException {
        var    request = new ConnectionRequest("admin", "admin", null);
        String url     = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.post(url)
                .setEntity(mapper.writeValueAsString(request))
//                .addHeader(ACCEPT, CONTENT_TYPE_XML)
//                .addHeader(CONTENT_TYPE, CONTENT_TYPE_XML)
                .build(), StandardResponse::new)) {
            if (response.getCode() != HttpStatus.SC_OK) {
                throw new IllegalStateException("Failed to get admin ticket: response code is " + response.getCode());
            }
            CinnamonConnectionWrapper cinnamonConnection = XML_MAPPER.readValue(response.getEntity().getContent(), CinnamonConnectionWrapper.class);
            return cinnamonConnection.list().getFirst().getTicket();
        }
    }

    /**
     * @return a ticket for a normal user.
     */
    protected static String getDoesTicket(boolean newTicket) throws IOException {
        if (ticketForDoe == null || newTicket) {
            var    request = new ConnectionRequest("doe", "admin", null);
            String url     = "http://localhost:" + cinnamonTestPort + UrlMapping.CINNAMON__CONNECT.getPath();
            try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.post(url)
                    .setEntity(mapper.writeValueAsString(request))
                    .build(), StandardResponse::new)) {
                CinnamonConnectionWrapper cinnamonConnection = XML_MAPPER.readValue(response.getEntity().getContent(), CinnamonConnectionWrapper.class);
                ticketForDoe = cinnamonConnection.list().getFirst().getTicket();
            }
        }
        return ticketForDoe;
    }

    protected void assertResponseOkay(StandardResponse response) throws IOException {
        Integer statusCode = response.getCode();
        if (!statusCode.equals(HttpStatus.SC_OK)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().getContent().transferTo(baos);
            log.error("Request failed with:\n{}", baos.toString(StandardCharsets.UTF_8));
            fail("Non-OK status code " + statusCode);
        }
    }

    protected CinnamonError assertCinnamonError(StandardResponse response, ErrorCode errorCode) throws IOException {
        String responseText = new String(response.getEntity().getContent().readAllBytes());
        assertTrue(responseText.contains(errorCode.getCode()), "response should contain errorCode " + errorCode + " but was " + responseText);
        assertThat(errorCode.getHttpResponseCode(), equalTo(response.getCode()));
        CinnamonError cinnamonError = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class).getErrors().getFirst();
        assertEquals(errorCode.getCode(), cinnamonError.getCode());
        return cinnamonError;
    }

    protected void assertClientError(Executable executable, ErrorCode... errorCode) {
        CinnamonClientException ex     = assertThrows(CinnamonClientException.class, executable);
        List<CinnamonError>     errors = ex.getErrorWrapper().getErrors();
        boolean allErrorsFound = Arrays.stream(errorCode).allMatch(code -> {
                    boolean found = errors.stream().anyMatch(error -> error.getCode().equals(code.getCode()));
                    if (!found) {
                        log.error("missing expected error: {}; got: {}", code, errors.stream().map(CinnamonError::getCode).collect(Collectors.joining(",")));
                    }
                    return found;
                }
        );
        assertTrue(allErrorsFound);
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
    protected StandardResponse sendAdminRequest(UrlMapping urlMapping, Object request) throws IOException {
        String requestStr = mapper.writeValueAsString(request);
        String url        = "http://localhost:" + cinnamonTestPort + urlMapping.getPath();
        return httpClient.execute(ClassicRequestBuilder.post(url)
                .addHeader("ticket", ticket)
                .setEntity(requestStr, adminClient.getCinnamonContentType().getContentType())
                .build(), StandardResponse::new);
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
    protected StandardResponse sendStandardRequest(UrlMapping urlMapping, Object request) throws IOException {
        String requestStr = client.getCinnamonContentType().getObjectMapper().writeValueAsString(request);
        String url        = "http://localhost:" + cinnamonTestPort + urlMapping.getPath();
        return httpClient.execute(ClassicRequestBuilder.post(url)
                .addHeader("ticket", getDoesTicket(false))
                .setEntity(requestStr, client.getCinnamonContentType().getContentType())
                .build(), StandardResponse::new);
    }

    protected void sendStandardRequestAndAssertError(UrlMapping urlMapping, Object request, ErrorCode errorCode) throws IOException {
        String requestStr = client.getCinnamonContentType().getObjectMapper().writeValueAsString(request);
        String url        = "http://localhost:" + cinnamonTestPort + urlMapping.getPath();
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.post(url)
                .addHeader("ticket", getDoesTicket(false))
                .setEntity(requestStr, client.getCinnamonContentType().getContentType())
                .build(), StandardResponse::new)) {
            assertCinnamonError(response, errorCode);
        }
    }

    protected ClassicHttpRequest createStandardRequestHeader(UrlMapping urlMapping) throws IOException {
        String url = "http://localhost:" + cinnamonTestPort + urlMapping.getPath();
        return ClassicRequestBuilder.post(url)
                .addHeader("ticket", getDoesTicket(false))
                .addHeader(ACCEPT, client.getCinnamonContentType().getContentType().toString())
                .build();
    }

    // TODO: use this in FolderServletIntegrationTests, too

    protected static Long addUserToAclGroupWithPermissions(String aclName, List<DefaultPermission> permissions) throws IOException {
        TestObjectHolder toh = new TestObjectHolder(adminClient, userId);
        return toh.createAcl(aclName)
                .createGroup(aclName)
                .createAclGroup()
                .addPermissions(permissions)
                .addUserToGroup(userId)
                .acl.getId();
    }

    protected static TestObjectHolder prepareAclGroupWithPermissions(List<DefaultPermission> permissions) throws IOException {
        return prepareAclGroupWithPermissions(UUID.randomUUID().toString(), permissions);
    }

    protected static TestObjectHolder prepareAclGroupWithPermissions(String name, List<DefaultPermission> permissions) throws IOException {
        return new TestObjectHolder(adminClient, userId)
                .createAcl(name)
                .createGroup(name)
                .addUserToGroup(userId)
                .createAclGroup()
                .addPermissions(permissions);
    }

    protected static TestObjectHolder prepareAclGroupWithOwnerPermissions(List<DefaultPermission> permissions) throws IOException {
        return prepareAclGroupWithOwnerPermissions(UUID.randomUUID().toString(), permissions);
    }

    protected static TestObjectHolder prepareAclGroupWithOwnerPermissions(String name, List<DefaultPermission> permissions) throws IOException {
        var toh = new TestObjectHolder(adminClient, userId);
        toh.group = new GroupDao().getOwnerGroup();
        toh.createAcl(name)
                .createAclGroup()
                .addPermissions(permissions);
        return toh;
    }

    protected Acl getReviewerAcl() {
        return new TestObjectHolder(client).getAcls().stream().filter(a -> a.getName().equals("reviewers.acl")).toList().getFirst();
    }

    /**
     * Verify that a search request for an exact id finds the expected number of documents.
     *
     * @param fieldName field used by IndexService in the Lucene index
     * @param id        id of an indexed LongPoint
     * @param expected  number of OSDs you expect
     */
    public static boolean verifySearchFindsPointField(String fieldName, Long id, int expected) {
        return verifySearchFindsPointField(fieldName, id, expected, SearchType.OSD);
    }

    public static boolean verifySearchFindsPointField(String fieldName, Long id, int expected, SearchType searchType) {
        try {
            var        response = client.search("<ExactPointQuery fieldName='__NAME__' value='__ID__' type='long'/>".replace("__ID__", id.toString()).replace("__NAME__", fieldName), searchType);
            List<Long> ids      = new ArrayList<>();
            switch (searchType) {
                case OSD -> ids = response.getOsdIds();
                case FOLDER -> ids = response.getFolderIds();
                case ALL -> {
                    ids.addAll(response.getFolderIds());
                    ids.addAll(response.getOsdIds());
                }
            }
            log.debug("found: {} items vs {} expected", ids.size(), expected);
            if (ids.size() == expected) {
                return true;
            }
            log.debug("response: {}", response);
            return false;
        } catch (
                IOException e) {
            return false;
        }
    }

    public File getPomXml() {
        return new File("pom.xml");
    }

}