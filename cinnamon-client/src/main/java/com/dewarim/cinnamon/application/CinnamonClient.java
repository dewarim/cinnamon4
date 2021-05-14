package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.CinnamonClientException;
import com.dewarim.cinnamon.Unwrapper;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.aclEntry.CreateAclEntryRequest;
import com.dewarim.cinnamon.model.request.aclEntry.DeleteAclEntryRequest;
import com.dewarim.cinnamon.model.request.aclEntry.ListAclEntryRequest;
import com.dewarim.cinnamon.model.request.aclEntry.UpdateAclEntryRequest;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.request.folderType.CreateFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.DeleteFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.UpdateFolderTypeRequest;
import com.dewarim.cinnamon.model.request.group.CreateGroupRequest;
import com.dewarim.cinnamon.model.request.group.DeleteGroupRequest;
import com.dewarim.cinnamon.model.request.group.ListGroupRequest;
import com.dewarim.cinnamon.model.request.group.UpdateGroupRequest;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CREATE_NEW_VERSION;
import static com.dewarim.cinnamon.api.Constants.EXPECTED_SIZE_ANY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_XML;

public class CinnamonClient {

    private static final Logger log = LogManager.getLogger(CinnamonClient.class);

    private int       port     = 9090;
    private String    host     = "localhost";
    private String    protocol = "http";
    private String    username = "admin";
    private String    password = "admin";
    private String    ticket;
    private XmlMapper mapper   = new XmlMapper();

    private final Unwrapper<ObjectSystemData, OsdWrapper>        osdUnwrapper          = new Unwrapper<>(OsdWrapper.class);
    private final Unwrapper<FolderType, FolderTypeWrapper>       folderTypeUnwrapper   = new Unwrapper<>(FolderTypeWrapper.class);
    private final Unwrapper<Folder, FolderWrapper>               folderUnwrapper       = new Unwrapper<>(FolderWrapper.class);
    private final Unwrapper<Meta, MetaWrapper>                   metaUnwrapper         = new Unwrapper<>(MetaWrapper.class);
    private final Unwrapper<UserInfo, UserWrapper>               userUnwrapper         = new Unwrapper<>(UserWrapper.class);
    private final Unwrapper<DeleteResponse, DeleteResponse>      deleteResponseWrapper = new Unwrapper<>(DeleteResponse.class);
    private final Unwrapper<CinnamonError, CinnamonErrorWrapper> errorUnwrapper        = new Unwrapper<>(CinnamonErrorWrapper.class);
    private final Unwrapper<Acl, AclWrapper>                     aclUnwrapper          = new Unwrapper<>(AclWrapper.class);
    private final Unwrapper<AclEntry, AclEntryWrapper>           aclEntryUnwrapper     = new Unwrapper<>(AclEntryWrapper.class);
    private final Unwrapper<Group, GroupWrapper>                 groupUnWrapper        = new Unwrapper<>(GroupWrapper.class);

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
        HttpResponse    userInfoResponse = sendStandardRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        return userUnwrapper.unwrap(userInfoResponse, 1).get(0);
    }

    public UserInfo getUser(Long id) throws IOException {
        UserInfoRequest userInfoRequest  = new UserInfoRequest(id, null);
        HttpResponse    userInfoResponse = sendStandardRequest(UrlMapping.USER__USER_INFO, userInfoRequest);
        return userUnwrapper.unwrap(userInfoResponse, 1).get(0);
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

    private boolean verifyDeleteResponse(HttpResponse response) throws IOException {
        return deleteResponseWrapper.unwrap(response, EXPECTED_SIZE_ANY).get(0).isSuccess();
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    // OSDs
    public List<ObjectSystemData> version(CreateNewVersionRequest versionRequest) throws IOException {
        HttpEntity request = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        return unwrapOsds(sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request), 1);
    }

    public boolean deleteOsd(Long id) throws IOException {
        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(Collections.singletonList(id));
        HttpResponse     response      = sendStandardRequest(UrlMapping.OSD__DELETE_OSDS, deleteRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    // FolderTypes
    public List<FolderType> createFolderTypes(List<String> names) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER_TYPE__CREATE, new CreateFolderTypeRequest(names));
        return folderTypeUnwrapper.unwrap(response, names.size());
    }

    public boolean deleteFolderTypes(List<Long> ids) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER_TYPE__DELETE, new DeleteFolderTypeRequest(ids));
        return verifyDeleteResponse(response);
    }

    public List<FolderType> updateFolderTypes(List<FolderType> types) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER_TYPE__UPDATE, new UpdateFolderTypeRequest(types));
        return folderTypeUnwrapper.unwrap(response, types.size());
    }

    // Folders
    public List<Meta> createFolderMeta(CreateMetaRequest metaRequest) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, metaRequest);
        return metaUnwrapper.unwrap(response, 1);
    }

    public List<Folder> updateFolders(UpdateFolderRequest updateFolderRequest) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER__UPDATE_FOLDER, updateFolderRequest);
        return folderUnwrapper.unwrap(response, 1);
    }

    // Acls
    public List<Acl> createAcl(List<String> names) throws IOException {
        CreateAclRequest aclRequest = new CreateAclRequest(names);
        HttpResponse     response   = sendStandardRequest(UrlMapping.ACL__CREATE, aclRequest);
        return aclUnwrapper.unwrap(response, aclRequest.list().size());
    }

    public boolean deleteAcl(List<Long> ids) throws IOException{
        DeleteAclRequest deleteRequest = new DeleteAclRequest(ids);
        var response = sendStandardRequest(UrlMapping.ACL__DELETE,deleteRequest);
        return verifyDeleteResponse(response);
    }

    // AclEntries
    public List<AclEntry> listAclEntries() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST, new ListAclEntryRequest());
        return aclEntryUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<AclEntry> createAclEntries(List<AclEntry> aclEntries) throws IOException {
        CreateAclEntryRequest request  = new CreateAclEntryRequest(aclEntries);
        HttpResponse          response = sendStandardRequest(UrlMapping.ACL_ENTRY__CREATE, request);
        return aclEntryUnwrapper.unwrap(response, aclEntries.size());
    }

    public List<AclEntry> updateAclEntries(UpdateAclEntryRequest request) throws IOException{
        HttpResponse response=sendStandardRequest(UrlMapping.ACL_ENTRY__UPDATE, request);
        return aclEntryUnwrapper.unwrap(response, request.list().size());
    }

    public boolean deleteAclEntries(List<Long> ids) throws IOException{
        HttpResponse response = sendStandardRequest(UrlMapping.ACL_ENTRY__DELETE, new DeleteAclEntryRequest(ids));
        return verifyDeleteResponse(response);
    }

    // Groups
    public List<Group> createGroups(List<String> groupNames) throws IOException {
        var request  = new CreateGroupRequest(groupNames);
        var response = sendStandardRequest(UrlMapping.GROUP__CREATE, request);
        return groupUnWrapper.unwrap(response,groupNames.size());
    }

    public List<Group> listGroups() throws IOException{
        var request = new ListGroupRequest();
        var response = sendStandardRequest(UrlMapping.GROUP__LIST, request);
        return groupUnWrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public boolean deleteGroups(List<Long> ids) throws IOException {
        var request = new DeleteGroupRequest(ids);
        var response = sendStandardRequest(UrlMapping.GROUP__DELETE, request);
        return verifyDeleteResponse(response);
    }

    public List<Group> updateGroups(List<Group> groups) throws IOException{
        var request = new UpdateGroupRequest(groups);
        var response = sendStandardRequest(UrlMapping.GROUP__UPDATE, request);
        return groupUnWrapper.unwrap(response, groups.size());
    }

    private HttpEntity createSimpleMultipartEntity(String fieldname, Object contentRequest) throws IOException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody(fieldname, mapper.writeValueAsString(contentRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        return entityBuilder.build();
    }

    public static void main(String[] args) throws IOException {
        CinnamonClient client = new CinnamonClient();
        client.ticket = client.getTicket(true);
        log.debug(client.getOsdById(1, false));
    }

}

