package com.dewarim.cinnamon.client;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.acl.AclInfoRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.aclGroup.CreateAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.DeleteAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.ListAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.FolderRequest;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.request.folderType.CreateFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.DeleteFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.ListFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.UpdateFolderTypeRequest;
import com.dewarim.cinnamon.model.request.format.CreateFormatRequest;
import com.dewarim.cinnamon.model.request.format.DeleteFormatRequest;
import com.dewarim.cinnamon.model.request.format.ListFormatRequest;
import com.dewarim.cinnamon.model.request.format.UpdateFormatRequest;
import com.dewarim.cinnamon.model.request.group.CreateGroupRequest;
import com.dewarim.cinnamon.model.request.group.DeleteGroupRequest;
import com.dewarim.cinnamon.model.request.group.ListGroupRequest;
import com.dewarim.cinnamon.model.request.group.UpdateGroupRequest;
import com.dewarim.cinnamon.model.request.groupUser.AddUserToGroupsRequest;
import com.dewarim.cinnamon.model.request.groupUser.RemoveUserFromGroupsRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.request.link.LinkWrapper;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.CreateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import com.dewarim.cinnamon.model.request.permission.ChangePermissionsRequest;
import com.dewarim.cinnamon.model.request.permission.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.request.user.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.DisconnectResponse;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.RelationWrapper;
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

import static com.dewarim.cinnamon.api.Constants.*;
import static com.dewarim.cinnamon.api.UrlMapping.OSD__CREATE_OSD;
import static com.dewarim.cinnamon.api.UrlMapping.OSD__UPDATE;
import static com.dewarim.cinnamon.model.request.osd.VersionPredicate.ALL;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_XML;

public class CinnamonClient {

    private static final Logger log = LogManager.getLogger(CinnamonClient.class);

    private       int       port     = 9090;
    private       String    host     = "localhost";
    private       String    protocol = "http";
    private       String    username = "admin";
    private       String    password = "admin";
    private       String    ticket;
    private final XmlMapper mapper   = XML_MAPPER;

    private final Unwrapper<ObjectSystemData, OsdWrapper>           osdUnwrapper          = new Unwrapper<>(OsdWrapper.class);
    private final Unwrapper<FolderType, FolderTypeWrapper>          folderTypeUnwrapper   = new Unwrapper<>(FolderTypeWrapper.class);
    private final Unwrapper<ObjectType, ObjectTypeWrapper>          objectTypeUnwrapper   = new Unwrapper<>(ObjectTypeWrapper.class);
    private final Unwrapper<Folder, FolderWrapper>                  folderUnwrapper       = new Unwrapper<>(FolderWrapper.class);
    private final Unwrapper<Format, FormatWrapper>                  formatUnwrapper       = new Unwrapper<>(FormatWrapper.class);
    private final Unwrapper<Meta, MetaWrapper>                      metaUnwrapper         = new Unwrapper<>(MetaWrapper.class);
    private final Unwrapper<UserInfo, UserWrapper>                  userUnwrapper         = new Unwrapper<>(UserWrapper.class);
    private final Unwrapper<DeleteResponse, DeleteResponse>         deleteResponseWrapper = new Unwrapper<>(DeleteResponse.class);
    private final Unwrapper<CinnamonError, CinnamonErrorWrapper>    errorUnwrapper        = new Unwrapper<>(CinnamonErrorWrapper.class);
    private final Unwrapper<Acl, AclWrapper>                        aclUnwrapper          = new Unwrapper<>(AclWrapper.class);
    private final Unwrapper<AclGroup, AclGroupWrapper>              aclGroupUnwrapper     = new Unwrapper<>(AclGroupWrapper.class);
    private final Unwrapper<Group, GroupWrapper>                    groupUnwrapper        = new Unwrapper<>(GroupWrapper.class);
    private final Unwrapper<Language, LanguageWrapper>              languageUnwrapper     = new Unwrapper<>(LanguageWrapper.class);
    private final Unwrapper<Link, LinkWrapper>                      linkUnwrapper         = new Unwrapper<>(LinkWrapper.class);
    // LinkResponse contains full OSD/Folder objects, Link itself contains only ids.
    private final Unwrapper<LinkResponse, LinkResponseWrapper>      linkResponseUnwrapper = new Unwrapper<>(LinkResponseWrapper.class);
    private final Unwrapper<Relation, RelationWrapper>              relationUnwrapper     = new Unwrapper<>(RelationWrapper.class);
    private final Unwrapper<RelationType, RelationTypeWrapper>      relationTypeUnwrapper = new Unwrapper<>(RelationTypeWrapper.class);
    private final Unwrapper<Permission, PermissionWrapper>          permissionUnwrapper   = new Unwrapper<>(PermissionWrapper.class);
    private final Unwrapper<DisconnectResponse, DisconnectResponse> disconnectUnwrapper   = new Unwrapper<>(DisconnectResponse.class);
    private final Unwrapper<MetasetType, MetasetTypeWrapper>        metasetTypeUnwrapper  = new Unwrapper<>(MetasetTypeWrapper.class);

    private boolean generateTicketIfNull = true;

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
        if ((ticket == null && generateTicketIfNull) || newTicket) {
            String url = "http://localhost:" + port + UrlMapping.CINNAMON__CONNECT.getPath();
            var response = Request.Post(url)
                    .bodyForm(Form.form().add("user", username).add("password", password).build())
                    .execute().returnResponse();
            verifyResponseIsOkay(response);
            String             tokenRequestResult = new String(response.getEntity().getContent().readAllBytes());
            CinnamonConnection cinnamonConnection = mapper.readValue(tokenRequestResult, CinnamonConnection.class);
            ticket = cinnamonConnection.getTicket();
        }
        return ticket;
    }

    public ObjectSystemData getOsdById(long id, boolean includeSummary, boolean includeCustomMetadata) throws IOException {
        OsdRequest   osdRequest = new OsdRequest(Collections.singletonList(id), includeSummary, includeCustomMetadata);
        HttpResponse response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        return unwrapOsds(response, 1).get(0);
    }

    /**
     * Get a list of OSDs. Do not check if all requested OSDs are returned.
     */
    public List<ObjectSystemData> getOsds(List<Long> ids, boolean includeSummary, boolean includeCustomMetadata) throws IOException {
        OsdRequest   osdRequest = new OsdRequest(ids, includeSummary, includeCustomMetadata);
        HttpResponse response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        return unwrapOsds(response, EXPECTED_SIZE_ANY);
    }

    public OsdWrapper getOsdsInFolder(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, ALL);
        HttpResponse       response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), OsdWrapper.class);
    }

    public OsdWrapper getOsdsInFolder(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata,
                                      VersionPredicate versionPredicate) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, versionPredicate);
        HttpResponse       response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), OsdWrapper.class);
    }

    private List<ObjectSystemData> unwrapOsds(HttpResponse response, Integer expectedSize) throws IOException {
        verifyResponseIsOkay(response);
        return osdUnwrapper.unwrap(response, expectedSize);
    }

    public List<MetasetType> listMetasetTypes() throws IOException {
        var request  = new ListMetasetTypeRequest();
        var response = sendStandardRequest(UrlMapping.METASET_TYPE__LIST, request);
        return metasetTypeUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<FolderType> listFolderTypes() throws IOException {
        var response = sendStandardRequest(UrlMapping.FOLDER_TYPE__LIST, new ListFolderTypeRequest());
        return folderTypeUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
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
        if (response.containsHeader(HEADER_FIELD_CINNAMON_ERROR)) {
            CinnamonErrorWrapper wrapper = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class);
            throw new CinnamonClientException(wrapper);
        }
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
    public ObjectSystemData version(CreateNewVersionRequest versionRequest) throws IOException {
        HttpEntity request = createSimpleMultipartEntity(CREATE_NEW_VERSION, versionRequest);
        return unwrapOsds(sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request), 1).get(0);
    }

    public boolean deleteOsd(Long id) throws IOException {
        return deleteOsd(id, false);
    }

    public boolean deleteOsd(Long id, boolean deleteDescendants) throws IOException {
        return deleteOsd(id, deleteDescendants, false);
    }

    public boolean deleteOsd(Long id, boolean deleteDescendants, boolean deleteAllVersions) throws IOException {
        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(Collections.singletonList(id), deleteDescendants, deleteAllVersions);
        HttpResponse     response      = sendStandardRequest(UrlMapping.OSD__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    public boolean deleteOsds(List<Long> id, boolean deleteDescendants) throws IOException {
        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(id, deleteDescendants, false);
        HttpResponse     response      = sendStandardRequest(UrlMapping.OSD__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    public ObjectSystemData createOsd(CreateOsdRequest createOsdRequest) throws IOException {
        HttpEntity   request  = createSimpleMultipartEntity(CREATE_NEW_OSD, createOsdRequest);
        HttpResponse response = sendStandardMultipartRequest(OSD__CREATE_OSD, request);
        return osdUnwrapper.unwrap(response, 1).get(0);
    }

    public boolean lockOsd(Long id) throws IOException {
        IdRequest idRequest = new IdRequest(id);
        var       response  = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    public boolean updateOsd(UpdateOsdRequest updateOsdRequest) throws IOException {
        HttpResponse response = sendStandardRequest(OSD__UPDATE, updateOsdRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    // Folders
    public Folder getFolderById(Long id, boolean includeSummary) throws IOException {
        return getFolders(Collections.singletonList(id), includeSummary).get(0);
    }

    public Folder createFolder(Long parentId, String name, Long ownerId, Long aclId, Long typeId) throws IOException {
        var request  = new CreateFolderRequest(name, parentId, null, ownerId, aclId, typeId);
        var response = sendStandardRequest(UrlMapping.FOLDER__CREATE_FOLDER, request);
        return folderUnwrapper.unwrap(response, 1).get(0);
    }

    public List<Folder> getFolders(List<Long> ids, boolean includeSummary) throws IOException {
        FolderRequest folderRequest = new FolderRequest(ids, includeSummary);
        HttpResponse  response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDERS, folderRequest);
        return folderUnwrapper.unwrap(response, ids.size());
    }

    public List<Meta> createFolderMeta(CreateMetaRequest metaRequest) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, metaRequest);
        return metaUnwrapper.unwrap(response, 1);
    }

    public void updateFolders(UpdateFolderRequest updateFolderRequest) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER__UPDATE_FOLDER, updateFolderRequest);
        verifyResponseIsOkay(response);
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

    // Acls
    public List<Acl> createAcl(List<String> names) throws IOException {
        CreateAclRequest aclRequest = new CreateAclRequest(names);
        HttpResponse     response   = sendStandardRequest(UrlMapping.ACL__CREATE, aclRequest);
        return aclUnwrapper.unwrap(response, aclRequest.list().size());
    }

    public boolean deleteAcl(List<Long> ids) throws IOException {
        DeleteAclRequest deleteRequest = new DeleteAclRequest(ids);
        var              response      = sendStandardRequest(UrlMapping.ACL__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    public Acl getAclByName(String name) throws IOException {
        var response = sendStandardRequest(UrlMapping.ACL__ACL_INFO, new AclInfoRequest(null, name));
        return aclUnwrapper.unwrap(response, 1).get(0);
    }

    public Acl getAclById(Long id) throws IOException {
        var response = sendStandardRequest(UrlMapping.ACL__ACL_INFO, new AclInfoRequest(id, null));
        return aclUnwrapper.unwrap(response, 1).get(0);
    }

    public List<Acl> listAcls() throws IOException {
        var response = sendStandardRequest(UrlMapping.ACL__LIST, new ListAclRequest());
        return aclUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    // Formats
    public List<Format> listFormats() throws IOException {
        var response = sendStandardRequest(UrlMapping.FORMAT__LIST, new ListFormatRequest());
        return formatUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    // AclGroups
    public List<AclGroup> listAclGroups() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.ACL_GROUP__LIST, new ListAclGroupRequest());
        return aclGroupUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<AclGroup> createAclGroups(List<AclGroup> aclGroups) throws IOException {
        CreateAclGroupRequest request  = new CreateAclGroupRequest(aclGroups);
        HttpResponse          response = sendStandardRequest(UrlMapping.ACL_GROUP__CREATE, request);
        return aclGroupUnwrapper.unwrap(response, aclGroups.size());
    }

    public List<AclGroup> updateAclGroups(UpdateAclGroupRequest request) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.ACL_GROUP__UPDATE, request);
        return aclGroupUnwrapper.unwrap(response, request.list().size());
    }

    public boolean deleteAclGroups(List<Long> ids) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.ACL_GROUP__DELETE, new DeleteAclGroupRequest(ids));
        return verifyDeleteResponse(response);
    }

    // Groups
    public List<Group> createGroups(List<String> groupNames) throws IOException {
        var request  = new CreateGroupRequest(groupNames);
        var response = sendStandardRequest(UrlMapping.GROUP__CREATE, request);
        return groupUnwrapper.unwrap(response, groupNames.size());
    }

    public List<Group> listGroups() throws IOException {
        var request  = new ListGroupRequest();
        var response = sendStandardRequest(UrlMapping.GROUP__LIST, request);
        return groupUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public boolean deleteGroups(List<Long> ids) throws IOException {
        var request  = new DeleteGroupRequest(ids);
        var response = sendStandardRequest(UrlMapping.GROUP__DELETE, request);
        return verifyDeleteResponse(response);
    }

    public List<Group> updateGroups(List<Group> groups) throws IOException {
        var request  = new UpdateGroupRequest(groups);
        var response = sendStandardRequest(UrlMapping.GROUP__UPDATE, request);
        return groupUnwrapper.unwrap(response, groups.size());
    }

    // GroupUsers
    public void addUserToGroups(Long userId, List<Long> ids) throws IOException {
        var request  = new AddUserToGroupsRequest(ids, userId);
        var response = sendStandardRequest(UrlMapping.GROUP__ADD_USER_TO_GROUPS, request);
        verifyResponseIsOkay(response);
    }

    public void removeUserFromGroups(Long userId, List<Long> ids) throws IOException {
        var request  = new RemoveUserFromGroupsRequest(userId, ids);
        var response = sendStandardRequest(UrlMapping.GROUP__REMOVE_USER_FROM_GROUPS, request);
        verifyResponseIsOkay(response);
    }

    // Links
    public List<Link> updateLinks(List<Link> links) throws IOException {
        var updateRequest = new UpdateLinkRequest(links);
        var response      = sendStandardRequest(UrlMapping.LINK__UPDATE, updateRequest);
        return linkUnwrapper.unwrap(response, links.size());
    }

    public List<Link> createLinks(List<Link> links) throws IOException {
        var createLinkRequest = new CreateLinkRequest(links);
        var response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        return linkUnwrapper.unwrap(response, links.size());
    }

    public Link createLink(Long parentId, LinkType type, Long aclId, Long ownerId, Long folderId, Long objectId) throws IOException {
        var link              = new Link(type, ownerId, aclId, parentId, folderId, objectId);
        var createLinkRequest = new CreateLinkRequest(List.of(link));
        var response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        return linkUnwrapper.unwrap(response, 1).get(0);
    }

    public Link updateLink(Link link) throws IOException {
        var updateLinkRequest = new UpdateLinkRequest(List.of(link));
        var response          = sendStandardRequest(UrlMapping.LINK__UPDATE, updateLinkRequest);
        return linkUnwrapper.unwrap(response, 1).get(0);
    }

    public boolean deleteLinks(List<Long> ids) throws IOException {
        var request  = new DeleteLinkRequest(ids);
        var response = sendStandardRequest(UrlMapping.LINK__DELETE, request);
        return verifyDeleteResponse(response);
    }

    public List<LinkResponse> getLinksById(List<Long> ids, boolean includeSummary) throws IOException {
        var request  = new GetLinksRequest(ids, includeSummary);
        var response = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, request);
        return linkResponseUnwrapper.unwrap(response, ids.size());
    }

    public LinkResponse getLinkById(Long id, boolean includeSummary) throws IOException {
        var request  = new GetLinksRequest(List.of(id), includeSummary);
        var response = sendStandardRequest(UrlMapping.LINK__GET_LINKS_BY_ID, request);
        return linkResponseUnwrapper.unwrap(response, 1).get(0);
    }

    // ObjectTypes

    public List<ObjectType> listObjectTypes() throws IOException {
        var response = sendStandardRequest(UrlMapping.OBJECT_TYPE__LIST, new ListObjectTypeRequest());
        return objectTypeUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    // Permissions
    public List<Permission> getUserPermissions(Long userId, Long aclId) throws IOException {
        var request  = new UserPermissionRequest(userId, aclId);
        var response = sendStandardRequest(UrlMapping.PERMISSION__GET_USER_PERMISSIONS, request);
        return permissionUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<Permission> listPermissions() throws IOException {
        var response = sendStandardRequest(UrlMapping.PERMISSION__LIST, new ListPermissionRequest());
        return permissionUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public void addAndRemovePermissions(Long aclGroupId, List<Long> permissionsToAdd, List<Long> permissionsToRemove) throws IOException {
        var request  = new ChangePermissionsRequest(aclGroupId, permissionsToAdd, permissionsToRemove);
        var response = sendStandardRequest(UrlMapping.PERMISSION__CHANGE_PERMISSIONS, request);
        verifyResponseIsOkay(response);
    }

    // Relations
    public Relation createRelation(Long leftId, Long rightId, String name, String metadata) throws IOException {
        // create
        var createRequest = new CreateRelationRequest(leftId, rightId, name, metadata);
        var response      = sendStandardRequest(UrlMapping.RELATION__CREATE, createRequest);
        return relationUnwrapper.unwrap(response, 1).get(0);
    }

    public boolean deleteRelationTypes(List<Long> ids) throws IOException {
        var deleteRequest = new DeleteRelationTypeRequest(ids);
        var response      = sendStandardRequest(UrlMapping.RELATION_TYPE__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    // RelationTypes
    public List<RelationType> createRelationTypes(List<RelationType> relationTypes) throws IOException {
        var createRequest = new CreateRelationTypeRequest(relationTypes);
        var response      = sendStandardRequest(UrlMapping.RELATION_TYPE__CREATE, createRequest);
        return relationTypeUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    private HttpEntity createSimpleMultipartEntity(String fieldname, Object contentRequest) throws IOException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addTextBody(fieldname, mapper.writeValueAsString(contentRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        return entityBuilder.build();
    }

    public String getTicket() {
        return ticket;
    }

    public void setGenerateTicketIfNull(boolean generateTicketIfNull) {
        this.generateTicketIfNull = generateTicketIfNull;
    }

    public static void main(String[] args) throws IOException {
        CinnamonClient client = new CinnamonClient();
        client.ticket = client.getTicket(true);
        log.debug(client.getOsdById(1, false, false));
    }

    public boolean disconnect() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.CINNAMON__DISCONNECT, null);
        return disconnectUnwrapper.unwrap(response, 1).get(0).isDisconnectSuccessful();
    }

    public void connect() throws IOException {
        getTicket(true);
    }

    public List<ObjectType> createObjectTypes(List<String> names) throws IOException {
        var createRequest = new CreateObjectTypeRequest(names);
        var response      = sendStandardRequest(UrlMapping.OBJECT_TYPE__CREATE, createRequest);
        return objectTypeUnwrapper.unwrap(response, names.size());
    }

    public List<Language> listLanguages() throws IOException {
        var request  = new ListLanguageRequest();
        var response = sendStandardRequest(UrlMapping.LANGUAGE__LIST__LANGUAGES, request);
        return languageUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public Format createFormat(String contentType, String extension, String name, long defaultObjectTypeId) throws IOException {
        var request = new CreateFormatRequest(List.of(new Format(contentType,extension,name,defaultObjectTypeId)));
        var response = sendStandardRequest(UrlMapping.FORMAT__CREATE, request);
        return formatUnwrapper.unwrap(response, 1).get(0);
    }

    public void updateFormat(Format format) throws IOException {
        var request = new UpdateFormatRequest(List.of(format));
        var response = sendStandardRequest(UrlMapping.FORMAT__UPDATE, request);
        verifyResponseIsOkay(response);
    }

    public void deleteFormat(Long id) throws IOException {
        var request = new DeleteFormatRequest(List.of(id));
        var response = sendStandardRequest(UrlMapping.FORMAT__DELETE, request);
        verifyResponseIsOkay(response);
    }
}

