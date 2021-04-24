package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.CinnamonClientException;
import com.dewarim.cinnamon.Unwrapper;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class CinnamonClient {

    private static final Logger log = LogManager.getLogger(CinnamonClient.class);

    private int       port     = 9090;
    private String    host     = "localhost";
    private String    protocol = "http";
    private String    username = "admin";
    private String    password = "admin";
    private String    ticket;
    private XmlMapper mapper   = new XmlMapper();

    private Unwrapper<ObjectSystemData, OsdWrapper> osdUnwrapper = new Unwrapper<>();


    public CinnamonClient() {
    }

    public CinnamonClient(int port, String host, String protocol, String username, String password) {
        this.port = port;
        this.host = host;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
    }

    private HttpResponse sendStandardMultipartRequest(UrlMapping urlMapping, HttpEntity multipartEntity) throws IOException {
        return Request.Post(String.format("%s://%s:%s", protocol, host, port) + urlMapping.getPath())
                .addHeader("ticket", getTicket(false))
                .body(multipartEntity).execute().returnResponse();
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
        return Request.Post("http://localhost:" + port + urlMapping.getPath())
                .addHeader("ticket", getTicket(false))
                .bodyString(requestStr, ContentType.APPLICATION_XML)
                .execute().returnResponse();
    }

    public UserInfo getUser(String name) throws IOException {
        UserInfoRequest userInfoRequest  = new UserInfoRequest(null, name);
        HttpResponse          userInfoResponse = sendStandardRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        return unwrapUsers(userInfoResponse, 1).get(0);
    }

    public UserInfo getUser(Long id) throws IOException {
        UserInfoRequest userInfoRequest  = new UserInfoRequest(id,null);
        HttpResponse          userInfoResponse = sendStandardRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        return unwrapUsers(userInfoResponse, 1).get(0);
    }

    protected String getTicket(boolean newTicket) throws IOException {
        if (ticket == null || newTicket) {
            String url = "http://localhost:" + port + UrlMapping.CINNAMON__CONNECT.getPath();
            String tokenRequestResult = Request.Post(url)
                    .bodyForm(Form.form().add("user", username).add("password", password).build())
                    .execute().returnContent().asString();
            CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
            ticket = cinnamonConnection.getTicket();
        }
        return ticket;
    }

    public ObjectSystemData getOsdById(long id, boolean includeSummary) throws IOException {
        OsdRequest   osdRequest = new OsdRequest(Collections.singletonList(id), includeSummary);
        HttpResponse response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        return unwrapOsds(response, 1).get(0);
    }

    private List<ObjectSystemData> unwrapOsds(HttpResponse response, Integer expectedSize) throws IOException {
        verifyResponseIsOkay(response);
        return osdUnwrapper.unwrap(response, expectedSize);
    }

//    public ObjectSystemData createOsdWithoutContent(long aclId, ){
//        CreateOsdRequest request = new CreateOsdRequest();
//        request.setAclId(CREATE_ACL_ID);
//        request.setName("new osd");
//        request.setOwnerId(STANDARD_USER_ID);
//        request.setParentId(CREATE_FOLDER_ID);
//        request.setTypeId(DEFAULT_OBJECT_TYPE_ID);
//        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
//                .addTextBody("createOsdRequest", mapper.writeValueAsString(request),
//                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
//        HttpResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
//        assertResponseOkay(response);
//        List<ObjectSystemData> objectSystemData = unwrapOsds(response, 1);
//        ObjectSystemData       osd              = objectSystemData.get(0);
//        assertEquals("new osd", osd.getName());
//        assertEquals(STANDARD_USER_ID, osd.getOwnerId());
//        assertEquals(STANDARD_USER_ID, osd.getModifierId());
//        assertEquals(CREATE_ACL_ID, osd.getAclId());
//        assertEquals(DEFAULT_OBJECT_TYPE_ID, osd.getTypeId());
//        assertEquals(CREATE_FOLDER_ID, osd.getParentId());
//    }

    private void verifyResponseIsOkay(HttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() != SC_OK) {
            log.error(new String(response.getEntity().getContent().readAllBytes()));
            throw new CinnamonClientException(response.getStatusLine().getReasonPhrase());
        }
    }

    private GenericResponse parseGenericResponse(HttpResponse response) throws IOException {
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), GenericResponse.class);
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public boolean deleteOsd(Long id) throws IOException {
        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(Collections.singletonList(id));
        HttpResponse     response      = sendStandardRequest(UrlMapping.OSD__DELETE_OSDS, deleteRequest);
        return parseGenericResponse(response).isSuccessful();
    }


    private List<UserInfo> unwrapUsers(HttpResponse response, Integer expectedSize) throws IOException {
        verifyResponseIsOkay(response);
        List<UserInfo> users = mapper.readValue(response.getEntity().getContent(), UserWrapper.class).getUsers();
        if (expectedSize != null) {
            if (users == null || users.isEmpty()) {
                throw new CinnamonClientException("No users objects found in response");
            }
            if (!expectedSize.equals(users.size())) {
                String message = String.format("Unexpected number of users found: %s instead of %s ", users.size(), expectedSize);
                throw new CinnamonClientException(message);
            }
        }
        return users;
    }

    public static void main(String[] args) throws IOException {
        CinnamonClient client = new CinnamonClient();
        client.ticket = client.getTicket(true);
        log.debug(client.getOsdById(1, false));
    }

}

