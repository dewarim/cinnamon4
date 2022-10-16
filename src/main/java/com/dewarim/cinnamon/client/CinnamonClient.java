package com.dewarim.cinnamon.client;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.DeleteAllMetasRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.acl.AclInfoRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.aclGroup.CreateAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.DeleteAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.ListAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.request.configEntry.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.DeleteConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.ListConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.UpdateConfigEntryRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.DeleteFolderRequest;
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
import com.dewarim.cinnamon.model.request.index.CreateIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.DeleteIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.IndexInfoRequest;
import com.dewarim.cinnamon.model.request.index.ListIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.index.UpdateIndexItemRequest;
import com.dewarim.cinnamon.model.request.language.CreateLanguageRequest;
import com.dewarim.cinnamon.model.request.language.DeleteLanguageRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.request.language.UpdateLanguageRequest;
import com.dewarim.cinnamon.model.request.lifecycle.CreateLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.DeleteLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.LifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.UpdateLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.CreateLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.UpdateLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.request.link.LinkWrapper;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import com.dewarim.cinnamon.model.request.metasetType.CreateMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.DeleteMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.UpdateMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.CreateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.osd.CopyOsdRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.GetRelationsRequest;
import com.dewarim.cinnamon.model.request.osd.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.SetContentRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import com.dewarim.cinnamon.model.request.permission.ChangePermissionsRequest;
import com.dewarim.cinnamon.model.request.permission.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.CreateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.DeleteUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.UpdateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.user.CreateUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.GetUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.ListUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.SetUserConfigRequest;
import com.dewarim.cinnamon.model.request.user.UpdateUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.DisconnectResponse;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.model.response.Summary;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.*;
import static com.dewarim.cinnamon.api.UrlMapping.*;
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

    private final Unwrapper<ConfigEntry, ConfigEntryWrapper>        configEntryUnwrapper    = new Unwrapper<>(ConfigEntryWrapper.class);
    private final Unwrapper<ObjectSystemData, OsdWrapper>           osdUnwrapper            = new Unwrapper<>(OsdWrapper.class);
    private final Unwrapper<FolderType, FolderTypeWrapper>          folderTypeUnwrapper     = new Unwrapper<>(FolderTypeWrapper.class);
    private final Unwrapper<ObjectType, ObjectTypeWrapper>          objectTypeUnwrapper     = new Unwrapper<>(ObjectTypeWrapper.class);
    private final Unwrapper<Folder, FolderWrapper>                  folderUnwrapper         = new Unwrapper<>(FolderWrapper.class);
    private final Unwrapper<Format, FormatWrapper>                  formatUnwrapper         = new Unwrapper<>(FormatWrapper.class);
    private final Unwrapper<Meta, MetaWrapper>                      metaUnwrapper           = new Unwrapper<>(MetaWrapper.class);
    private final Unwrapper<UserAccount, UserAccountWrapper>        userUnwrapper           = new Unwrapper<>(UserAccountWrapper.class);
    private final Unwrapper<DeleteResponse, DeleteResponse>         deleteResponseWrapper   = new Unwrapper<>(DeleteResponse.class);
    private final Unwrapper<CinnamonError, CinnamonErrorWrapper>    errorUnwrapper          = new Unwrapper<>(CinnamonErrorWrapper.class);
    private final Unwrapper<Acl, AclWrapper>                        aclUnwrapper            = new Unwrapper<>(AclWrapper.class);
    private final Unwrapper<AclGroup, AclGroupWrapper>              aclGroupUnwrapper       = new Unwrapper<>(AclGroupWrapper.class);
    private final Unwrapper<Group, GroupWrapper>                    groupUnwrapper          = new Unwrapper<>(GroupWrapper.class);
    private final Unwrapper<Language, LanguageWrapper>              languageUnwrapper       = new Unwrapper<>(LanguageWrapper.class);
    private final Unwrapper<UiLanguage, UiLanguageWrapper>          uiLanguageUnwrapper     = new Unwrapper<>(UiLanguageWrapper.class);
    private final Unwrapper<Link, LinkWrapper>                      linkUnwrapper           = new Unwrapper<>(LinkWrapper.class);
    private final Unwrapper<Lifecycle, LifecycleWrapper>            lifecycleUnwrapper      = new Unwrapper<>(LifecycleWrapper.class);
    private final Unwrapper<LifecycleState, LifecycleStateWrapper>  lifecycleStateUnwrapper = new Unwrapper<>(LifecycleStateWrapper.class);
    // LinkResponse contains full OSD/Folder objects, Link itself contains only ids.
    private final Unwrapper<LinkResponse, LinkResponseWrapper>      linkResponseUnwrapper   = new Unwrapper<>(LinkResponseWrapper.class);
    private final Unwrapper<Relation, RelationWrapper>              relationUnwrapper       = new Unwrapper<>(RelationWrapper.class);
    private final Unwrapper<RelationType, RelationTypeWrapper>      relationTypeUnwrapper   = new Unwrapper<>(RelationTypeWrapper.class);
    private final Unwrapper<Summary, SummaryWrapper>                summaryUnwrapper        = new Unwrapper<>(SummaryWrapper.class);
    private final Unwrapper<Permission, PermissionWrapper>          permissionUnwrapper     = new Unwrapper<>(PermissionWrapper.class);
    private final Unwrapper<DisconnectResponse, DisconnectResponse> disconnectUnwrapper     = new Unwrapper<>(DisconnectResponse.class);
    private final Unwrapper<MetasetType, MetasetTypeWrapper>        metasetTypeUnwrapper    = new Unwrapper<>(MetasetTypeWrapper.class);
    private final Unwrapper<IndexItem, IndexItemWrapper>            indexItemUnwrapper      = new Unwrapper<>(IndexItemWrapper.class);

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
                .bodyString(requestStr, APPLICATION_XML.withCharset(StandardCharsets.UTF_8))
                .execute().returnResponse();
    }

    public UserAccount getUser(String name) throws IOException {
        GetUserAccountRequest userInfoRequest = new GetUserAccountRequest(null, name);
        HttpResponse          response        = sendStandardRequest(UrlMapping.USER__GET, userInfoRequest);
        return userUnwrapper.unwrap(response, 1).get(0);
    }

    public UserAccount getUser(Long id) throws IOException {
        GetUserAccountRequest request  = new GetUserAccountRequest(id, null);
        HttpResponse          response = sendStandardRequest(UrlMapping.USER__GET, request);
        return userUnwrapper.unwrap(response, 1).get(0);
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

    public OsdWrapper getOsdsInFolderWrapped(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, ALL);
        HttpResponse       response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), OsdWrapper.class);
    }

    public List<ObjectSystemData> getOsdsInFolder(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, ALL);
        HttpResponse       response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return unwrapOsds(response, EXPECTED_SIZE_ANY);
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

    public boolean setContentOnLockedOsd(Long osdId, Long formatId, File content) throws IOException {
        FileBody fileBody = new FileBody(content);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("setContentRequest", mapper.writeValueAsString(new SetContentRequest(osdId, formatId)),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8))
                .addPart("file", fileBody)
                .build();
        var response = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, entity);
        return parseGenericResponse(response).isSuccessful();
    }

    public InputStream getContent(Long osdId) throws IOException {
        IdRequest    idRequest = new IdRequest(osdId);
        HttpResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        verifyResponseIsOkay(response);
        return response.getEntity().getContent();
    }

    public boolean lockOsd(Long id) throws IOException {
        IdRequest idRequest = new IdRequest(id);
        var       response  = sendStandardRequest(UrlMapping.OSD__LOCK, idRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    public boolean unlockOsd(Long id) throws IOException{
        IdRequest idRequest = new IdRequest(id);
        var       response  = sendStandardRequest(UrlMapping.OSD__UNLOCK, idRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    public boolean updateOsd(UpdateOsdRequest updateOsdRequest) throws IOException {
        HttpResponse response = sendStandardRequest(OSD__UPDATE, updateOsdRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    public boolean setPassword(Long userId, String password) throws IOException{
        HttpResponse response=sendStandardRequest(USER__SET_PASSWORD, new SetPasswordRequest(userId,password));
        return parseGenericResponse(response).isSuccessful();
    }

    // Folders
    public Folder getFolderById(Long id, boolean includeSummary) throws IOException {
        return getFolders(Collections.singletonList(id), includeSummary).get(0);
    }

    public Folder createFolder(Long parentId, String name, Long ownerId, Long aclId, Long typeId) throws IOException {
        var request  = new CreateFolderRequest(name, parentId, null, ownerId, aclId, typeId);
        var response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
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

    public List<Meta> createOsdMeta(CreateMetaRequest metaRequest) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.OSD__CREATE_META, metaRequest);
        return metaUnwrapper.unwrap(response, 1);
    }

    public Meta createOsdMeta(Long osdId, String content, Long metaTypeId) throws IOException {
        return createOsdMeta(new CreateMetaRequest(osdId, content, metaTypeId)).get(0);
    }

    public void updateFolders(UpdateFolderRequest updateFolderRequest) throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.FOLDER__UPDATE, updateFolderRequest);
        verifyResponseIsOkay(response);
    }

    // FolderTypes
    public List<FolderType> createFolderTypes(List<String> names) throws IOException {
        var          folderTypes = names.stream().map(FolderType::new).collect(Collectors.toList());
        HttpResponse response    = sendStandardRequest(UrlMapping.FOLDER_TYPE__CREATE, new CreateFolderTypeRequest(folderTypes));
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
        CreateAclRequest aclRequest = new CreateAclRequest(names.stream().map(Acl::new).collect(Collectors.toList()));
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
    public List<Group> createGroupsByName(List<String> groupNames) throws IOException {
        var request  = new CreateGroupRequest(groupNames.stream().map(Group::new).collect(Collectors.toList()));
        var response = sendStandardRequest(UrlMapping.GROUP__CREATE, request);
        return groupUnwrapper.unwrap(response, groupNames.size());
    }

    public List<Group> createGroups(List<Group> groups) throws IOException {
        var request  = new CreateGroupRequest(groups);
        var response = sendStandardRequest(UrlMapping.GROUP__CREATE, request);
        return groupUnwrapper.unwrap(response, groups.size());
    }

    public Group createGroup(Group group) throws IOException {
        var request  = new CreateGroupRequest(List.of(group));
        var response = sendStandardRequest(UrlMapping.GROUP__CREATE, request);
        return groupUnwrapper.unwrap(response, 1).get(0);
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
        var request  = new AddUserToGroupsRequest(userId, ids);
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
    public Relation createRelation(Long leftId, Long rightId, Long typeId, String metadata) throws IOException {
        // create
        var createRequest = new CreateRelationRequest(leftId, rightId, typeId, metadata);
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

    public RelationType createRelationType(RelationType relationType) throws IOException {
        return createRelationTypes(List.of(relationType)).get(0);
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
        client.createFolder(6L, "test", 1L, 1L, 1L);
        log.debug(client.getOsdById(1, false, false).toString());
    }

    public boolean disconnect() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.CINNAMON__DISCONNECT, null);
        return disconnectUnwrapper.unwrap(response, 1).get(0).isDisconnectSuccessful();
    }

    public void connect() throws IOException {
        getTicket(true);
    }

    public List<ObjectType> createObjectTypes(List<String> names) throws IOException {
        var createRequest = new CreateObjectTypeRequest(names.stream().map(ObjectType::new).collect(Collectors.toList()));
        var response      = sendStandardRequest(UrlMapping.OBJECT_TYPE__CREATE, createRequest);
        return objectTypeUnwrapper.unwrap(response, names.size());
    }

    public List<Language> listLanguages() throws IOException {
        var request  = new ListLanguageRequest();
        var response = sendStandardRequest(UrlMapping.LANGUAGE__LIST, request);
        return languageUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public Format createFormat(String contentType, String extension, String name, long defaultObjectTypeId) throws IOException {
        var request  = new CreateFormatRequest(List.of(new Format(contentType, extension, name, defaultObjectTypeId)));
        var response = sendStandardRequest(UrlMapping.FORMAT__CREATE, request);
        return formatUnwrapper.unwrap(response, 1).get(0);
    }

    public void updateFormat(Format format) throws IOException {
        var request  = new UpdateFormatRequest(List.of(format));
        var response = sendStandardRequest(UrlMapping.FORMAT__UPDATE, request);
        verifyResponseIsOkay(response);
    }

    public void deleteFormat(Long id) throws IOException {
        var request  = new DeleteFormatRequest(List.of(id));
        var response = sendStandardRequest(UrlMapping.FORMAT__DELETE, request);
        verifyResponseIsOkay(response);
    }

    public Language createLanguage(String isoCode) throws IOException {
        var request  = new CreateLanguageRequest(List.of(new Language(isoCode)));
        var response = sendStandardRequest(UrlMapping.LANGUAGE__CREATE, request);
        return languageUnwrapper.unwrap(response, 1).get(0);
    }

    public List<Language> createLanguages(List<String> isoCodes) throws IOException {
        var request  = new CreateLanguageRequest(isoCodes.stream().map(Language::new).collect(Collectors.toList()));
        var response = sendStandardRequest(UrlMapping.LANGUAGE__CREATE, request);
        return languageUnwrapper.unwrap(response, isoCodes.size());
    }

    public void updateLanguage(Language language) throws IOException {
        var request  = new UpdateLanguageRequest(List.of(language));
        var response = sendStandardRequest(UrlMapping.LANGUAGE__UPDATE, request);
        verifyResponseIsOkay(response);
    }

    public boolean deleteLanguage(Long id) throws IOException {
        var request  = new DeleteLanguageRequest(id);
        var response = sendStandardRequest(UrlMapping.LANGUAGE__DELETE, request);
        return verifyDeleteResponse(response);
    }

    public UiLanguage createUiLanguage(String isoCode) throws IOException {
        var request  = new CreateUiLanguageRequest(List.of(new UiLanguage(isoCode)));
        var response = sendStandardRequest(UrlMapping.UI_LANGUAGE__CREATE, request);
        return uiLanguageUnwrapper.unwrap(response, 1).get(0);
    }

    public void updateUiLanguage(UiLanguage language) throws IOException {
        var request  = new UpdateUiLanguageRequest(List.of(language));
        var response = sendStandardRequest(UrlMapping.UI_LANGUAGE__UPDATE, request);
        verifyResponseIsOkay(response);
    }

    public boolean deleteUiLanguage(Long id) throws IOException {
        var request  = new DeleteUiLanguageRequest(id);
        var response = sendStandardRequest(UrlMapping.UI_LANGUAGE__DELETE, request);
        return verifyDeleteResponse(response);
    }

    public List<UiLanguage> listUiLanguages() throws IOException {
        var request  = new ListUiLanguageRequest();
        var response = sendStandardRequest(UrlMapping.UI_LANGUAGE__LIST, request);
        return uiLanguageUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public MetasetType createMetasetType(String name, boolean unique) throws IOException {
        var request  = new CreateMetasetTypeRequest(name, unique);
        var response = sendStandardRequest(UrlMapping.METASET_TYPE__CREATE, request);
        return metasetTypeUnwrapper.unwrap(response, 1).get(0);
    }

    public void updateMetasetType(MetasetType metasetType) throws IOException {
        var request  = new UpdateMetasetTypeRequest(List.of(metasetType));
        var response = sendStandardRequest(UrlMapping.METASET_TYPE__UPDATE, request);
        verifyResponseIsOkay(response);
    }

    public boolean deleteMetasetType(Long id) throws IOException {
        var request  = new DeleteMetasetTypeRequest(id);
        var response = sendStandardRequest(UrlMapping.METASET_TYPE__DELETE, request);
        return verifyDeleteResponse(response);
    }

    public ConfigEntry createConfigEntry(ConfigEntry configEntry) throws IOException {
        var request  = new CreateConfigEntryRequest(configEntry.getName(), configEntry.getConfig(), configEntry.isPublicVisibility());
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__CREATE, request);
        return configEntryUnwrapper.unwrap(response, 1).get(0);
    }

    public List<ConfigEntry> listConfigEntries() throws IOException {
        var request  = new ListConfigEntryRequest();
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__LIST, request);
        return configEntryUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public ConfigEntry updateConfigEntry(ConfigEntry entry) throws IOException {
        var request  = new UpdateConfigEntryRequest(List.of(entry));
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__UPDATE, request);
        return configEntryUnwrapper.unwrap(response, 1).get(0);
    }

    public void deleteConfigEntry(Long id) throws IOException {
        var request  = new DeleteConfigEntryRequest(id);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__DELETE, request);
        verifyDeleteResponse(response);
    }

    public ConfigEntry getConfigEntry(String name) throws IOException {
        var request  = new ConfigEntryRequest(name);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, request);
        return configEntryUnwrapper.unwrap(response, 1).get(0);
    }

    public List<ConfigEntry> getConfigEntries(List<Long> ids) throws IOException {
        var request  = new ConfigEntryRequest().getIds().addAll(ids);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, request);
        return configEntryUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public ConfigEntry getConfigEntry(Long id) throws IOException {
        var request  = new ConfigEntryRequest(id);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, request);
        return configEntryUnwrapper.unwrap(response, 1).get(0);
    }

    public List<UserAccount> listUsers() throws IOException {
        var request  = new ListUserAccountRequest();
        var response = sendStandardRequest(UrlMapping.USER__LIST, request);
        return userUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public UserAccount createUser(UserAccount user) throws IOException {
        var request = new CreateUserAccountRequest();
        request.list().add(user);
        var response = sendStandardRequest(UrlMapping.USER__CREATE, request);
        return userUnwrapper.unwrap(response, 1).get(0);
    }

    public List<Meta> getOsdMetas(Long id) throws IOException {
        var request  = new MetaRequest(id, null);
        var response = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        return metaUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }
    public List<Meta> getOsdMetas(Long id, List<Long> typeIds) throws IOException {
        var request  = new MetaRequest(id, typeIds);
        var response = sendStandardRequest(UrlMapping.OSD__GET_META, request);
        return metaUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<Meta> getFolderMetas(Long id) throws IOException {
        var request  = new MetaRequest(id, null);
        var response = sendStandardRequest(UrlMapping.FOLDER__GET_META, request);
        return metaUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public UserAccount updateUser(UserAccount user) throws IOException {
        var request  = new UpdateUserAccountRequest(List.of(user));
        var response = sendStandardRequest(UrlMapping.USER__UPDATE, request);
        return userUnwrapper.unwrap(response, 1).get(0);
    }

    public List<ObjectSystemData> copyOsds(long targetFolderId, List<Long> ids) throws IOException {
        var copyOsdRequest = new CopyOsdRequest(ids, targetFolderId, null);
        var response       = sendStandardRequest(UrlMapping.OSD__COPY, copyOsdRequest);
        return osdUnwrapper.unwrap(response, ids.size());
    }

    public List<ObjectSystemData> copyOsds(long targetFolderId, List<Long> ids, List<Long> metasetTypeIds) throws IOException {
        var copyOsdRequest = new CopyOsdRequest(ids, targetFolderId, metasetTypeIds);
        var response       = sendStandardRequest(UrlMapping.OSD__COPY, copyOsdRequest);
        return osdUnwrapper.unwrap(response, ids.size());
    }

    public List<Relation> getRelations(List<Long> ids) throws IOException {
        var request  = new GetRelationsRequest(ids, true);
        var response = sendStandardRequest(UrlMapping.OSD__GET_RELATIONS, request);
        return relationUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<Relation> getRelationsWithCriteria(List<Long> leftIds, List<Long> rightIds, Collection<String> names, boolean includeMetadata, boolean orMode) throws IOException {
        var request  = new SearchRelationRequest(leftIds, rightIds, names, includeMetadata, orMode);
        var response = sendStandardRequest(UrlMapping.OSD__GET_RELATIONS, request);
        return relationUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public Lifecycle createLifecycle(String name) throws IOException {
        var request  = new CreateLifecycleRequest(List.of(new Lifecycle(name, null)));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__CREATE, request);
        return lifecycleUnwrapper.unwrap(response, 1).get(0);
    }

    public LifecycleState createLifecycleState(LifecycleState lifecycleState) throws IOException {
        var request  = new CreateLifecycleStateRequest(List.of(lifecycleState));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CREATE, request);
        return lifecycleStateUnwrapper.unwrap(response, 1).get(0);
    }

    public void attachLifecycle(Long osdId, Long lifecycleId, Long lifecycleStateId) throws IOException {
        var request  = new AttachLifecycleRequest(osdId, lifecycleId, lifecycleStateId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, request);
        verifyResponseIsOkay(response);
    }

    public List<Lifecycle> listLifecycles() throws IOException {
        var request  = new ListLifecycleRequest();
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__LIST, request);
        return lifecycleUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public Lifecycle getLifecycle(Long lifecycleId) throws IOException {
        var request  = new LifecycleRequest(lifecycleId, null);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__GET, request);
        return lifecycleUnwrapper.unwrap(response, 1).get(0);
    }

    public Lifecycle getLifecycle(String name) throws IOException {
        var request  = new LifecycleRequest(null, name);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__GET, request);
        return lifecycleUnwrapper.unwrap(response, 1).get(0);

    }

    public Lifecycle updateLifecycle(Lifecycle lifecycle) throws IOException {
        var request  = new UpdateLifecycleRequest(List.of(lifecycle));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__UPDATE, request);
        return lifecycleUnwrapper.unwrap(response, 1).get(0);
    }

    public void deleteLifecycle(Long lifecycleId) throws IOException {
        var request  = new DeleteLifecycleRequest(List.of(lifecycleId));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__DELETE, request);
        verifyDeleteResponse(response);
    }

    public List<LifecycleState> getNextLifecycleStates(long lifecycleStateId) throws IOException {
        var request  = new IdRequest(lifecycleStateId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request);
        return lifecycleStateUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public LifecycleState getLifecycleState(long lifecycleStateId) throws IOException {
        var request  = new IdRequest(lifecycleStateId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET, request);
        return lifecycleStateUnwrapper.unwrap(response, 1).get(0);
    }

    public LifecycleState updateLifecycleState(LifecycleState lcs) throws IOException {
        var request  = new UpdateLifecycleStateRequest(List.of(lcs));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__UPDATE, request);
        return lifecycleStateUnwrapper.unwrap(response, 1).get(0);
    }

    public void deleteLifecycleState(Long lifecycleStateId) throws IOException {
        var request  = new DeleteLifecycleRequest(lifecycleStateId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__DELETE, request);
        verifyDeleteResponse(response);
    }

    public List<Summary> getFolderSummaries(List<Long> folderIds) throws IOException {
        var request  = new IdListRequest(folderIds);
        var response = sendStandardRequest(UrlMapping.FOLDER__GET_SUMMARIES, request);
        return summaryUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public void deleteFolderMeta(Long metaId) throws IOException {
        var request  = new DeleteMetaRequest(metaId);
        var response = sendStandardRequest(UrlMapping.FOLDER__DELETE_META, request);
        verifyDeleteResponse(response);
    }

    public void deleteAllFolderMeta(Long folderId) throws IOException {
        var request  = new DeleteAllMetasRequest(folderId);
        var response = sendStandardRequest(FOLDER__DELETE_ALL_METAS, request);
        verifyDeleteResponse(response);
    }
    public void deleteAllOsdMeta(Long osdId) throws IOException {
        var request  = new DeleteAllMetasRequest(osdId);
        var response = sendStandardRequest(OSD__DELETE_ALL_METAS, request);
        verifyDeleteResponse(response);
    }

    public void deleteOsdMeta(Long metaId) throws IOException {
        var request  = new DeleteMetaRequest(metaId);
        var response = sendStandardRequest(UrlMapping.OSD__DELETE_META, request);
        verifyDeleteResponse(response);
    }

    public void deleteFolder(List<Long> folderId, boolean deleteRecursively, boolean deleteContent) throws IOException {
        var request  = new DeleteFolderRequest(folderId, deleteRecursively, deleteContent);
        var response = sendStandardRequest(UrlMapping.FOLDER__DELETE, request);
        verifyDeleteResponse(response);
    }

    public void deleteFolder(Long folderId, boolean deleteRecursively, boolean deleteContent) throws IOException {
        deleteFolder(List.of(folderId), deleteRecursively, deleteContent);
    }

    public List<IndexItem> listIndexItems() throws IOException{
        var request = new ListIndexItemRequest();
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__LIST, request);
        return indexItemUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public IndexItem createIndexItem(IndexItem indexItem) throws IOException {
        var request = new CreateIndexItemRequest(List.of(indexItem));
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__CREATE, request);
        return indexItemUnwrapper.unwrap(response, 1).get(0);
    }

    public void deleteIndexItem(Long id) throws IOException{
        var request = new DeleteIndexItemRequest(id);
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__DELETE,request);
        verifyDeleteResponse(response);
    }

    public IndexItem updateIndexItem(IndexItem indexItem) throws IOException {
        var request = new UpdateIndexItemRequest(List.of(indexItem));
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__UPDATE,request);
        return indexItemUnwrapper.unwrap(response,1).get(0);
    }

    public void deleteRelation(Long id) throws IOException {
        var request = new DeleteRelationRequest(id);
        var response = sendStandardRequest(UrlMapping.RELATION__DELETE, request);
        verifyDeleteResponse(response);
    }

    public void setUserConfig(Long userId, String config) throws IOException{
        var request = new SetUserConfigRequest(userId, config);
        var response = sendStandardRequest(USER__SET_CONFIG, request);
        verifyResponseIsOkay(response);
    }

    public IndexInfoResponse getIndexInfo(boolean countDocuments) throws IOException{
        var request = new IndexInfoRequest(countDocuments);
        HttpResponse response = sendStandardRequest(INDEX__INFO, request);
        return new SingletonUnwrapper<IndexInfoResponse>(IndexInfoResponse.class).unwrap(response);
    }

    public ReindexResponse reindex( ReindexRequest request) throws IOException {
        HttpResponse response = sendStandardRequest(INDEX__REINDEX, request);
        return new SingletonUnwrapper<ReindexResponse>(ReindexResponse.class).unwrap(response);
    }

    static class SingletonUnwrapper<S>{

        private final Class<? extends S> clazz;

        private final static XmlMapper mapper   = XML_MAPPER;

        SingletonUnwrapper(Class<? extends S> clazz) {
            this.clazz = clazz;
        }

        public S unwrap(HttpResponse response) throws IOException {
            checkResponseForErrors(response, mapper);
            return mapper.readValue(response.getEntity().getContent(), clazz);
        }
    }

    static void checkResponseForErrors(HttpResponse response, XmlMapper mapper) throws IOException {
        if (response.containsHeader(HEADER_FIELD_CINNAMON_ERROR)) {
            CinnamonErrorWrapper wrapper = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class);
            log.warn("Found errors: "+wrapper.getErrors().stream().map(CinnamonError::toString).collect(Collectors.joining(",")));
            throw new CinnamonClientException(wrapper);
        }
        if (response.getStatusLine().getStatusCode() != SC_OK) {
            StatusLine statusLine = response.getStatusLine();
            String     message    = statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
            log.warn("Failed to unwrap non-okay response with status: " + message);
            log.info("Response: " + new String(response.getEntity().getContent().readAllBytes()));
            throw new CinnamonClientException(message);
        }
    }
}
