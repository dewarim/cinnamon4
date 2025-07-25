package com.dewarim.cinnamon.client;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkResolver;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.ConnectionRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.ListUrlMappingInfoRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.aclGroup.CreateAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.DeleteAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.ListAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.request.changeTrigger.CreateChangeTriggerRequest;
import com.dewarim.cinnamon.model.request.changeTrigger.DeleteChangeTriggerRequest;
import com.dewarim.cinnamon.model.request.changeTrigger.ListChangeTriggerRequest;
import com.dewarim.cinnamon.model.request.configEntry.*;
import com.dewarim.cinnamon.model.request.folder.*;
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
import com.dewarim.cinnamon.model.request.index.*;
import com.dewarim.cinnamon.model.request.language.CreateLanguageRequest;
import com.dewarim.cinnamon.model.request.language.DeleteLanguageRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.request.language.UpdateLanguageRequest;
import com.dewarim.cinnamon.model.request.lifecycle.CreateLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.DeleteLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.UpdateLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.*;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import com.dewarim.cinnamon.model.request.meta.*;
import com.dewarim.cinnamon.model.request.metasetType.CreateMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.DeleteMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.UpdateMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.CreateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.osd.*;
import com.dewarim.cinnamon.model.request.permission.ChangePermissionsRequest;
import com.dewarim.cinnamon.model.request.permission.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.request.search.SearchIdsRequest;
import com.dewarim.cinnamon.model.request.search.SearchType;
import com.dewarim.cinnamon.model.request.uiLanguage.CreateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.DeleteUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.UpdateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.user.*;
import com.dewarim.cinnamon.model.response.*;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.*;
import static com.dewarim.cinnamon.api.UrlMapping.*;
import static com.dewarim.cinnamon.model.request.osd.VersionPredicate.ALL;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;

public class CinnamonClient {

    private static final Logger log = LogManager.getLogger(CinnamonClient.class);

    private       int        port       = 9090;
    private       String     host       = "localhost";
    private       String     protocol   = "http";
    private       String     username   = "admin";
    private       String     password   = "admin";
    private       String     ticket;
    private final XmlMapper  mapper     = XML_MAPPER;
    private final HttpClient httpClient = HttpClients.createDefault();

    public static final ThreadLocal<List<ChangeTriggerResponse>> changeTriggerResponseLocal = ThreadLocal.withInitial(ArrayList::new);

    private final Unwrapper<ChangeTrigger, ChangeTriggerWrapper>                 changeTriggerUnwrapper         = new Unwrapper<>(ChangeTriggerWrapper.class);
    private final Unwrapper<ChangeTriggerResponse, ChangeTriggerResponseWrapper> changeTriggerResponseUnwrapper = new Unwrapper<>(ChangeTriggerResponseWrapper.class);
    private final Unwrapper<ConfigEntry, ConfigEntryWrapper>                     configEntryUnwrapper           = new Unwrapper<>(ConfigEntryWrapper.class);
    private final Unwrapper<ObjectSystemData, OsdWrapper>                        osdUnwrapper                   = new Unwrapper<>(OsdWrapper.class);
    private final Unwrapper<FolderType, FolderTypeWrapper>                       folderTypeUnwrapper            = new Unwrapper<>(FolderTypeWrapper.class);
    private final Unwrapper<ObjectType, ObjectTypeWrapper>                       objectTypeUnwrapper            = new Unwrapper<>(ObjectTypeWrapper.class);
    private final Unwrapper<Folder, FolderWrapper>                               folderUnwrapper                = new Unwrapper<>(FolderWrapper.class);
    private final Unwrapper<Format, FormatWrapper>                               formatUnwrapper                = new Unwrapper<>(FormatWrapper.class);
    private final Unwrapper<Meta, MetaWrapper>                                   metaUnwrapper                  = new Unwrapper<>(MetaWrapper.class);
    private final Unwrapper<UserAccount, UserAccountWrapper>                     userUnwrapper                  = new Unwrapper<>(UserAccountWrapper.class);
    private final Unwrapper<DeleteResponse, DeleteResponse>                      deleteResponseWrapper          = new Unwrapper<>(DeleteResponse.class);
    private final Unwrapper<CinnamonError, CinnamonErrorWrapper>                 errorUnwrapper                 = new Unwrapper<>(CinnamonErrorWrapper.class);
    private final Unwrapper<Acl, AclWrapper>                                     aclUnwrapper                   = new Unwrapper<>(AclWrapper.class);
    private final Unwrapper<AclGroup, AclGroupWrapper>                           aclGroupUnwrapper              = new Unwrapper<>(AclGroupWrapper.class);
    private final Unwrapper<Group, GroupWrapper>                                 groupUnwrapper                 = new Unwrapper<>(GroupWrapper.class);
    private final Unwrapper<Language, LanguageWrapper>                           languageUnwrapper              = new Unwrapper<>(LanguageWrapper.class);
    private final Unwrapper<UiLanguage, UiLanguageWrapper>                       uiLanguageUnwrapper            = new Unwrapper<>(UiLanguageWrapper.class);
    private final Unwrapper<Link, LinkWrapper>                                   linkUnwrapper                  = new Unwrapper<>(LinkWrapper.class);
    private final Unwrapper<Lifecycle, LifecycleWrapper>                         lifecycleUnwrapper             = new Unwrapper<>(LifecycleWrapper.class);
    private final Unwrapper<LifecycleState, LifecycleStateWrapper>               lifecycleStateUnwrapper        = new Unwrapper<>(LifecycleStateWrapper.class);
    // LinkResponse contains full OSD/Folder objects, Link itself contains only ids.
    private final Unwrapper<LinkResponse, LinkResponseWrapper>                   linkResponseUnwrapper          = new Unwrapper<>(LinkResponseWrapper.class);
    private final Unwrapper<Relation, RelationWrapper>                           relationUnwrapper              = new Unwrapper<>(RelationWrapper.class);
    private final Unwrapper<RelationType, RelationTypeWrapper>                   relationTypeUnwrapper          = new Unwrapper<>(RelationTypeWrapper.class);
    private final Unwrapper<Summary, SummaryWrapper>                             summaryUnwrapper               = new Unwrapper<>(SummaryWrapper.class);
    private final Unwrapper<Permission, PermissionWrapper>                       permissionUnwrapper            = new Unwrapper<>(PermissionWrapper.class);
    private final Unwrapper<DisconnectResponse, DisconnectResponse>              disconnectUnwrapper            = new Unwrapper<>(DisconnectResponse.class);
    private final Unwrapper<CinnamonConnection, CinnamonConnectionWrapper>       connectUnwrapper               = new Unwrapper<>(CinnamonConnectionWrapper.class);
    private final Unwrapper<MetasetType, MetasetTypeWrapper>                     metasetTypeUnwrapper           = new Unwrapper<>(MetasetTypeWrapper.class);
    private final Unwrapper<IndexItem, IndexItemWrapper>                         indexItemUnwrapper             = new Unwrapper<>(IndexItemWrapper.class);
    private final Unwrapper<UrlMappingInfo, UrlMappingInfoWrapper>               urlMappingInfoWrapperUnwrapper = new Unwrapper<>(UrlMappingInfoWrapper.class);

    private boolean generateTicketIfNull = true;

    public CinnamonClient() {
    }


    public CinnamonClient(int port, String host, String protocol, String username, String password) {
        this.port     = port;
        this.host     = host;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
    }

    /**
     * Create a new Cinnamon client using the old client's connection data.
     */
    public CinnamonClient(CinnamonClient client, String username, String password) {
        this.port     = client.getPort();
        this.host     = client.getHost();
        this.protocol = client.getProtocol();
        this.username = username;
        this.password = password;
    }

    private StandardResponse sendStandardMultipartRequest(UrlMapping urlMapping, HttpEntity multipartEntity) throws IOException {
        ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create("POST")
                .setUri((String.format("%s://%s:%s", protocol, host, port) + urlMapping.getPath()))
                .addHeader("ticket", getTicket(false))
                .setEntity(multipartEntity);
        return httpClient.execute(requestBuilder.build(), StandardResponse::new);
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
        String requestStr = mapper.writeValueAsString(request);
        ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create("POST")
                .setUri((String.format("%s://%s:%s", protocol, host, port) + urlMapping.getPath()))
                .addHeader("ticket", getTicket(false))
                .setEntity(requestStr, APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        return httpClient.execute(requestBuilder.build(), StandardResponse::new);
    }

    protected StandardResponse sendConnectionRequest(Object request) throws IOException {
        String requestStr = mapper.writeValueAsString(request);
        ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create("POST")
                .setUri((String.format("%s://%s:%s", protocol, host, port) + UrlMapping.CINNAMON__CONNECT.getPath()))
                .setEntity(requestStr, APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        return httpClient.execute(requestBuilder.build(), StandardResponse::new);
    }

    public static String responseToString(StandardResponse response) throws IOException {
        return new String(response.getEntity().getContent().readAllBytes());
    }

    public List<Acl> getAclsOfUser(long userId) throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.ACL__GET_USER_ACLS, new IdRequest(userId));
        return aclUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public void deleteUser(Long userId, Long assetReceiverId) throws IOException {
        var response = sendStandardRequest(USER__DELETE, new DeleteUserAccountRequest(userId, assetReceiverId));
        verifyResponseIsOkay(response);
    }

    public void testBoom() throws IOException {
        var response = sendStandardRequest(TEST__BOOM, null);
        verifyResponseIsOkay(response);
    }

    public List<ConfigEntry> createConfigEntries(List<ConfigEntry> configEntries) throws IOException {
        var request  = new CreateConfigEntryRequest(configEntries);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__CREATE, request);
        return configEntryUnwrapper.unwrap(response, configEntries.size());
    }

    public void deleteConfigEntries(List<Long> configEntryIds) throws IOException {
        var request  = new DeleteConfigEntryRequest(configEntryIds);
        var response = sendStandardRequest(CONFIG_ENTRY__DELETE, request);
        verifyResponseIsOkay(response);
    }

    public void copyToExistingOsd(ObjectSystemData sourceOsd, ObjectSystemData targetOsd, List<Long> metasetTypeIds, boolean copyContent) throws IOException {
        var request  = new CopyToExistingOsdRequest(List.of(new CopyTask(sourceOsd.getId(), targetOsd.getId(), copyContent, metasetTypeIds)));
        var response = sendStandardRequest(OSD__COPY_TO_EXISTING, request);
        verifyResponseIsOkay(response);
    }

    public void deleteChangeTrigger(long versionId) throws IOException {
        var request  = new DeleteChangeTriggerRequest(versionId);
        var response = sendStandardRequest(CHANGE_TRIGGER__DELETE, request);
        verifyResponseIsOkay(response);
    }

    public List<ChangeTriggerResponse> changeTriggerNop(boolean ignoreError) throws IOException {
        var response = sendStandardRequest(CHANGE_TRIGGER__NOP, "");
        return changeTriggerResponseUnwrapper.unwrap(response, EXPECTED_SIZE_ANY, ignoreError);
    }

    public List<ObjectSystemData> getOsdReferences(Long folderId, boolean includeSummary) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, true, false, VersionPredicate.HEAD);
        StandardResponse   response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        String     content = new String(response.getEntity().getContent().readAllBytes());
        OsdWrapper wrapper = mapper.readValue(content, OsdWrapper.class);
        return wrapper.getReferences();
    }

    public List<Link> getOsdLinksInFolder(Long folderId, boolean includeSummary) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, true, false, VersionPredicate.HEAD);
        StandardResponse   response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        String      content = new String(response.getEntity().getContent().readAllBytes());
        LinkWrapper wrapper = mapper.readValue(content, LinkWrapper.class);
        return wrapper.getLinks();
    }

    public class WrappedRequest<T, W extends Wrapper<T>> {
        public List<T> send(UrlMapping urlMapping, Object request, Unwrapper<T, W> unwrapper, int expectedSize) throws IOException {
            String requestStr = mapper.writeValueAsString(request);
            ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create("POST")
                    .setUri((String.format("%s://%s:%s", protocol, host, port) + urlMapping.getPath()))
                    .addHeader("ticket", getTicket(false))
                    .addHeader("Content-type", APPLICATION_XML.withCharset(StandardCharsets.UTF_8).toString())
                    .setEntity(requestStr);
            return httpClient.execute(requestBuilder.build(), response -> {
                StandardResponse standardResponse = new StandardResponse(response);
                verifyResponseIsOkay(standardResponse);
                return unwrapper.unwrap(standardResponse, expectedSize);
            });
        }
    }

    public UserAccount getUser(String name) throws IOException {
        GetUserAccountRequest request = new GetUserAccountRequest(null, name);
        return new WrappedRequest<UserAccount, UserAccountWrapper>().send(USER__GET, request, userUnwrapper, 1).getFirst();
    }

    public UserAccount getUser(Long id) throws IOException {
        GetUserAccountRequest request = new GetUserAccountRequest(id, null);
        return new WrappedRequest<UserAccount, UserAccountWrapper>().send(USER__GET, request, userUnwrapper, 1).getFirst();
    }

    protected String getTicket(boolean newTicket) throws IOException {
        if ((ticket == null && generateTicketIfNull) || newTicket) {
            var request            = new ConnectionRequest(username, password, null);
            var response           = sendConnectionRequest(request);
            var cinnamonConnection = connectUnwrapper.unwrap(response, 1).getFirst();
            ticket = cinnamonConnection.getTicket();
        }
        return ticket;
    }

    public ObjectSystemData getOsdById(long id, boolean includeSummary, boolean includeCustomMetadata) throws IOException {
        OsdRequest       osdRequest = new OsdRequest(Collections.singletonList(id), includeSummary, includeCustomMetadata);
        StandardResponse response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        return unwrapOsds(response, 1).getFirst();
    }

    /**
     * Get a list of OSDs. Do not check if all requested OSDs are returned.
     */
    public List<ObjectSystemData> getOsdsById(List<Long> ids, boolean includeSummary, boolean includeCustomMetadata) throws IOException {
        OsdRequest       osdRequest = new OsdRequest(ids, includeSummary, includeCustomMetadata);
        StandardResponse response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_ID, osdRequest);
        return unwrapOsds(response, EXPECTED_SIZE_ANY);
    }

    public OsdWrapper getOsdsInFolderWrapped(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, ALL);
        StandardResponse   response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), OsdWrapper.class);
    }

    public List<ObjectSystemData> getOsdsInFolder(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, ALL);
        StandardResponse   response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return unwrapOsds(response, EXPECTED_SIZE_ANY);
    }

    public void setSummary(Long osdId, String summary) throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(osdId, summary);
        StandardResponse  response       = sendStandardRequest(UrlMapping.OSD__SET_SUMMARY, summaryRequest);
        verifyResponseIsOkay(response);
    }

    public void setFolderSummary(Long folderId, String summary) throws IOException {
        SetSummaryRequest summaryRequest = new SetSummaryRequest(folderId, summary);
        StandardResponse  response       = sendStandardRequest(FOLDER__SET_SUMMARY, summaryRequest);
        verifyResponseIsOkay(response);
    }

    public OsdWrapper getOsdsInFolder(Long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata,
                                      VersionPredicate versionPredicate) throws IOException {
        OsdByFolderRequest osdRequest = new OsdByFolderRequest(folderId, includeSummary, linksAsOsd, includeCustomMetadata, versionPredicate);
        StandardResponse   response   = sendStandardRequest(UrlMapping.OSD__GET_OBJECTS_BY_FOLDER_ID, osdRequest);
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), OsdWrapper.class);
    }

    private List<ObjectSystemData> unwrapOsds(StandardResponse response, Integer expectedSize) throws IOException {
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
//                .addTextBody(CINNAMON_REQUEST_PART, mapper.writeValueAsString(request),
//                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
//        StandardResponse response = sendStandardMultipartRequest(UrlMapping.OSD__CREATE_OSD, entityBuilder.build());
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

    private void verifyResponseIsOkay(StandardResponse response) throws IOException {
        if (response.containsHeader(HEADER_FIELD_CINNAMON_ERROR)) {
            CinnamonErrorWrapper wrapper = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class);
            throw new CinnamonClientException(wrapper);
        }
        if (response.getCode() != SC_OK) {
            log.error(new String(response.getEntity().getContent().readAllBytes()));
            throw new CinnamonClientException(String.valueOf(response.getCode()));
        }
    }

    private GenericResponse parseGenericResponse(StandardResponse response) throws IOException {
        verifyResponseIsOkay(response);
        return mapper.readValue(response.getEntity().getContent(), GenericResponse.class);
    }

    private boolean verifyDeleteResponse(StandardResponse response) throws IOException {
        return deleteResponseWrapper.unwrap(response, EXPECTED_SIZE_ANY).getFirst().isSuccess();
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    // OSDs
    public ObjectSystemData version(CreateNewVersionRequest versionRequest) throws IOException {
        HttpEntity request = createSimpleMultipartEntity(CINNAMON_REQUEST_PART, versionRequest);
        return unwrapOsds(sendStandardMultipartRequest(UrlMapping.OSD__VERSION, request), 1).getFirst();
    }

    public ObjectSystemData versionWithContent(CreateNewVersionRequest versionRequest, File content) throws IOException {
        FileBody fileBody = new FileBody(content);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .addTextBody(CINNAMON_REQUEST_PART, mapper.writeValueAsString(versionRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        return unwrapOsds(sendStandardMultipartRequest(UrlMapping.OSD__VERSION, entityBuilder.build()), 1).getFirst();
    }

    public boolean deleteOsd(Long id) throws IOException {
        return deleteOsd(id, false);
    }

    public boolean deleteOsd(Long id, boolean deleteDescendants) throws IOException {
        return deleteOsd(id, deleteDescendants, false);
    }

    public boolean deleteOsd(Long id, boolean deleteDescendants, boolean deleteAllVersions) throws IOException {
        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(Collections.singletonList(id), deleteDescendants, deleteAllVersions);
        StandardResponse response      = sendStandardRequest(UrlMapping.OSD__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    public boolean deleteOsds(List<Long> id, boolean deleteDescendants) throws IOException {
        DeleteOsdRequest deleteRequest = new DeleteOsdRequest(id, deleteDescendants, false);
        var              response      = sendStandardRequest(UrlMapping.OSD__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    public ObjectSystemData createOsd(CreateOsdRequest createOsdRequest) throws IOException {
        HttpEntity request  = createSimpleMultipartEntity(CINNAMON_REQUEST_PART, createOsdRequest);
        var        response = sendStandardMultipartRequest(OSD__CREATE_OSD, request);
        return osdUnwrapper.unwrap(response, 1).getFirst();
    }

    public ObjectSystemData createOsdWithContent(CreateOsdRequest createOsdRequest, File content) throws IOException {
        FileBody fileBody = new FileBody(content);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .addTextBody(CINNAMON_REQUEST_PART, mapper.writeValueAsString(createOsdRequest),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8));
        var response = sendStandardMultipartRequest(OSD__CREATE_OSD, entityBuilder.build());
        return osdUnwrapper.unwrap(response, 1).getFirst();
    }

    public boolean setContentOnLockedOsd(Long osdId, Long formatId, File content) throws IOException {
        FileBody fileBody = new FileBody(content);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody(CINNAMON_REQUEST_PART, mapper.writeValueAsString(new SetContentRequest(osdId, formatId)),
                        APPLICATION_XML.withCharset(StandardCharsets.UTF_8))
                .addPart("file", fileBody)
                .build();
        var response = sendStandardMultipartRequest(UrlMapping.OSD__SET_CONTENT, entity);
        return parseGenericResponse(response).isSuccessful();
    }

    public InputStream getContent(Long osdId) throws IOException {
        IdRequest        idRequest = new IdRequest(osdId);
        StandardResponse response  = sendStandardRequest(UrlMapping.OSD__GET_CONTENT, idRequest);
        verifyResponseIsOkay(response);
        return response.getEntity().getContent();
    }

    public void lockOsd(Long id) throws IOException {
        IdListRequest idListRequest = new IdListRequest(List.of(id));
        var           response      = sendStandardRequest(UrlMapping.OSD__LOCK, idListRequest);
        verifyResponseIsOkay(response);
    }

    public void lockOsd(List<Long> ids) throws IOException {
        IdListRequest idListRequest = new IdListRequest(ids);
        var           response      = sendStandardRequest(UrlMapping.OSD__LOCK, idListRequest);
        verifyResponseIsOkay(response);
    }

    public void unlockOsd(Long id) throws IOException {
        IdListRequest idListRequest = new IdListRequest(List.of(id));
        var           response      = sendStandardRequest(UrlMapping.OSD__UNLOCK, idListRequest);
        verifyResponseIsOkay(response);
    }

    public void unlockOsd(List<Long> ids) throws IOException {
        IdListRequest idListRequest = new IdListRequest(ids);
        var           response      = sendStandardRequest(UrlMapping.OSD__UNLOCK, idListRequest);
        verifyResponseIsOkay(response);
    }

    public boolean updateOsd(UpdateOsdRequest updateOsdRequest) throws IOException {
        StandardResponse response = sendStandardRequest(OSD__UPDATE, updateOsdRequest);
        return parseGenericResponse(response).isSuccessful();
    }

    public boolean setPassword(Long userId, String password) throws IOException {
        StandardResponse response = sendStandardRequest(USER__SET_PASSWORD, new SetPasswordRequest(userId, password));
        return parseGenericResponse(response).isSuccessful();
    }

    // Folders
    public Folder getFolderById(Long id, boolean includeSummary) throws IOException {
        return getFolders(Collections.singletonList(id), includeSummary).getFirst();
    }

    public List<Folder> getFolderByIdWithAncestors(Long id, boolean includeSummary) throws IOException {
        SingleFolderRequest request  = new SingleFolderRequest(id, includeSummary);
        var                 response = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDER, request);
        return folderUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public Folder createFolder(Long parentId, String name, Long ownerId, Long aclId, Long typeId) throws IOException {
        var request  = new CreateFolderRequest(name, parentId, null, ownerId, aclId, typeId);
        var response = sendStandardRequest(UrlMapping.FOLDER__CREATE, request);
        return folderUnwrapper.unwrap(response, 1).getFirst();
    }

    public List<Folder> getFolders(List<Long> ids, boolean includeSummary) throws IOException {
        FolderRequest    folderRequest = new FolderRequest(ids, includeSummary);
        StandardResponse response      = sendStandardRequest(UrlMapping.FOLDER__GET_FOLDERS, folderRequest);
        return folderUnwrapper.unwrap(response, ids.size());
    }

    public List<Meta> createFolderMeta(CreateMetaRequest metaRequest) throws IOException {
        var response = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, metaRequest);
        return metaUnwrapper.unwrap(response, 1);
    }

    public List<Meta> createFolderMeta(Long folderId, String content, Long metaTypeId) throws IOException {
        CreateMetaRequest metaRequest = new CreateMetaRequest(folderId, content, metaTypeId);
        var               response    = sendStandardRequest(UrlMapping.FOLDER__CREATE_META, metaRequest);
        return metaUnwrapper.unwrap(response, 1);
    }

    public List<Meta> createOsdMeta(CreateMetaRequest metaRequest) throws IOException {
        var response = sendStandardRequest(UrlMapping.OSD__CREATE_META, metaRequest);
        return metaUnwrapper.unwrap(response, metaRequest.getMetas().size());
    }

    public Meta createOsdMeta(Long osdId, String content, Long metaTypeId) throws IOException {
        return createOsdMeta(new CreateMetaRequest(osdId, content, metaTypeId)).getFirst();
    }

    public void updateFolder(UpdateFolderRequest updateFolderRequest) throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.FOLDER__UPDATE, updateFolderRequest);
        verifyResponseIsOkay(response);
    }

    public void updateFolder(Folder folder) throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(folder.getId(), folder.getParentId(), folder.getName(),
                folder.getOwnerId(), folder.getTypeId(), folder.getAclId());
        StandardResponse response = sendStandardRequest(UrlMapping.FOLDER__UPDATE, request);
        verifyResponseIsOkay(response);
    }

    // FolderTypes
    public List<FolderType> createFolderTypes(List<String> names) throws IOException {
        var              folderTypes = names.stream().map(FolderType::new).collect(Collectors.toList());
        StandardResponse response    = sendStandardRequest(UrlMapping.FOLDER_TYPE__CREATE, new CreateFolderTypeRequest(folderTypes));
        return folderTypeUnwrapper.unwrap(response, names.size());
    }

    public boolean deleteFolderTypes(List<Long> ids) throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.FOLDER_TYPE__DELETE, new DeleteFolderTypeRequest(ids));
        return verifyDeleteResponse(response);
    }

    public List<FolderType> updateFolderTypes(List<FolderType> types) throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.FOLDER_TYPE__UPDATE, new UpdateFolderTypeRequest(types));
        return folderTypeUnwrapper.unwrap(response, types.size());
    }

    // Acls
    public List<Acl> createAcls(List<String> names) throws IOException {
        CreateAclRequest aclRequest = new CreateAclRequest(names.stream().map(Acl::new).collect(Collectors.toList()));
        StandardResponse response   = sendStandardRequest(UrlMapping.ACL__CREATE, aclRequest);
        return aclUnwrapper.unwrap(response, aclRequest.list().size());
    }

    public Acl createAcl(String name) throws IOException {
        CreateAclRequest aclRequest = new CreateAclRequest(List.of(new Acl(name)));
        StandardResponse response   = sendStandardRequest(UrlMapping.ACL__CREATE, aclRequest);
        return aclUnwrapper.unwrap(response, 1).getFirst();
    }

    public boolean deleteAcl(List<Long> ids) throws IOException {
        DeleteAclRequest deleteRequest = new DeleteAclRequest(ids);
        var              response      = sendStandardRequest(UrlMapping.ACL__DELETE, deleteRequest);
        return verifyDeleteResponse(response);
    }

    public Acl getAclByName(String name) throws IOException {
        var response = sendStandardRequest(ACL__LIST, new ListAclRequest());
        return aclUnwrapper.unwrap(response, EXPECTED_SIZE_ANY).stream()
                .filter(acl -> acl.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new CinnamonClientException(ErrorCode.ACL_NOT_FOUND));
    }

    public Acl getAclById(Long id) throws IOException {
        var response = sendStandardRequest(ACL__LIST, new ListAclRequest());
        return aclUnwrapper.unwrap(response, EXPECTED_SIZE_ANY).stream()
                .filter(acl -> acl.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CinnamonClientException(ErrorCode.ACL_NOT_FOUND));
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

    public List<ChangeTrigger> listChangeTriggers() throws IOException {
        var response = sendStandardRequest(CHANGE_TRIGGER__LIST, new ListChangeTriggerRequest());
        return changeTriggerUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    // AclGroups
    public List<AclGroup> listAclGroups() throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.ACL_GROUP__LIST, new ListAclGroupRequest());
        return aclGroupUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<AclGroup> createAclGroups(List<AclGroup> aclGroups) throws IOException {
        CreateAclGroupRequest request  = new CreateAclGroupRequest(aclGroups);
        StandardResponse      response = sendStandardRequest(UrlMapping.ACL_GROUP__CREATE, request);
        return aclGroupUnwrapper.unwrap(response, aclGroups.size());
    }

    public List<AclGroup> updateAclGroups(UpdateAclGroupRequest request) throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.ACL_GROUP__UPDATE, request);
        return aclGroupUnwrapper.unwrap(response, request.list().size());
    }

    public boolean deleteAclGroups(List<Long> ids) throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.ACL_GROUP__DELETE, new DeleteAclGroupRequest(ids));
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
        return groupUnwrapper.unwrap(response, 1).getFirst();
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

    public boolean deleteGroups(List<Long> ids, boolean recursive) throws IOException {
        var request  = new DeleteGroupRequest(ids, recursive);
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

    public Link createLinkToFolder(Long parentId, Long aclId, Long ownerId, Long folderId) throws IOException {
        var link              = new Link(null, LinkType.FOLDER, ownerId, aclId, parentId, folderId, null, LinkResolver.FIXED);
        var createLinkRequest = new CreateLinkRequest(List.of(link));
        var response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        return linkUnwrapper.unwrap(response, 1).getFirst();
    }

    public Link createLinkToOsd(Long parentId, Long aclId, Long ownerId, Long objectId, LinkResolver resolver) throws IOException {
        var link              = new Link(null, LinkType.OBJECT, ownerId, aclId, parentId, null, objectId, resolver);
        var createLinkRequest = new CreateLinkRequest(List.of(link));
        var response          = sendStandardRequest(UrlMapping.LINK__CREATE, createLinkRequest);
        return linkUnwrapper.unwrap(response, 1).getFirst();
    }

    public Link updateLink(Link link) throws IOException {
        var updateLinkRequest = new UpdateLinkRequest(List.of(link));
        var response          = sendStandardRequest(UrlMapping.LINK__UPDATE, updateLinkRequest);
        return linkUnwrapper.unwrap(response, 1).getFirst();
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
        return linkResponseUnwrapper.unwrap(response, 1).getFirst();
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
        return relationUnwrapper.unwrap(response, 1).getFirst();
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
        return createRelationTypes(List.of(relationType)).getFirst();
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
        StandardResponse response = sendStandardRequest(UrlMapping.CINNAMON__DISCONNECT, null);
        return disconnectUnwrapper.unwrap(response, 1).getFirst().isDisconnectSuccessful();
    }

    /**
     * re-connect this client with the server, using the stored username/password.
     * The new ticket will be saved internally.
     */
    public void connect() throws IOException {
        getTicket(true);
    }

    /**
     * Connect to a server and return the response.
     *
     * @param username the username
     * @param password the password
     * @param format   optional format: if null, use "xml". Currently implemented alternatives to xml: "text"
     * @return the raw response as a String (depending on format, may be XML or plain text)
     * @throws IOException if connection fails
     */
    public String connect(String username, String password, String format) throws IOException {
        String            responseFormat = Objects.requireNonNullElse(format, "xml");
        ConnectionRequest request        = new ConnectionRequest(username, password, responseFormat);
        StandardResponse  response       = sendConnectionRequest(request);
        verifyResponseIsOkay(response);
        return new String(response.getEntity().getContent().readAllBytes());
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

    public Format createFormat(String contentType, String extension, String name, Long defaultObjectTypeId, IndexMode indexMode) throws IOException {
        var request  = new CreateFormatRequest(List.of(new Format(contentType, extension, name, defaultObjectTypeId, indexMode)));
        var response = sendStandardRequest(UrlMapping.FORMAT__CREATE, request);
        return formatUnwrapper.unwrap(response, 1).getFirst();
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
        return languageUnwrapper.unwrap(response, 1).getFirst();
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
        return uiLanguageUnwrapper.unwrap(response, 1).getFirst();
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
        return metasetTypeUnwrapper.unwrap(response, 1).getFirst();
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
        return configEntryUnwrapper.unwrap(response, 1).getFirst();
    }

    public List<ConfigEntry> listConfigEntries() throws IOException {
        var request  = new ListConfigEntryRequest();
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__LIST, request);
        return configEntryUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public ConfigEntry updateConfigEntry(ConfigEntry entry) throws IOException {
        var request  = new UpdateConfigEntryRequest(List.of(entry));
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__UPDATE, request);
        return configEntryUnwrapper.unwrap(response, 1).getFirst();
    }

    public void deleteConfigEntry(Long id) throws IOException {
        var request  = new DeleteConfigEntryRequest(id);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__DELETE, request);
        verifyDeleteResponse(response);
    }

    public List<ConfigEntry> getConfigEntries(List<Long> ids) throws IOException {
        var request = new ConfigEntryRequest();
        request.getIds().addAll(ids);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, request);
        return configEntryUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public ConfigEntry getConfigEntry(Long id) throws IOException {
        var request  = new ConfigEntryRequest(id);
        var response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, request);
        return configEntryUnwrapper.unwrap(response, 1).getFirst();
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
        return userUnwrapper.unwrap(response, 1).getFirst();
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
        return userUnwrapper.unwrap(response, 1).getFirst();
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

    public List<Relation> searchRelations(List<Long> leftIds, List<Long> rightIds, Collection<Long> relationTypeIds, boolean includeMetadata, boolean orMode) throws IOException {
        var request  = new SearchRelationRequest(leftIds, rightIds, relationTypeIds, includeMetadata, orMode);
        var response = sendStandardRequest(RELATION__SEARCH, request);
        return relationUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public Lifecycle createLifecycle(String name) throws IOException {
        var request  = new CreateLifecycleRequest(List.of(new Lifecycle(name, null)));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__CREATE, request);
        return lifecycleUnwrapper.unwrap(response, 1).getFirst();
    }

    public LifecycleState createLifecycleState(LifecycleState lifecycleState) throws IOException {
        var request  = new CreateLifecycleStateRequest(List.of(lifecycleState));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CREATE, request);
        return lifecycleStateUnwrapper.unwrap(response, 1).getFirst();
    }

    public void changeLifecycleState(Long osdId, Long stateId) throws IOException {
        var request  = new ChangeLifecycleStateRequest(osdId, stateId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, request);
        verifyResponseIsOkay(response);
    }

    public void attachLifecycle(Long osdId, Long lifecycleId, Long lifecycleStateId, boolean forceChange) throws IOException {
        var request  = new AttachLifecycleRequest(osdId, lifecycleId, lifecycleStateId, forceChange);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, request);
        verifyResponseIsOkay(response);
    }

    public void detachLifecycle(Long osdId) throws IOException {
        var request  = new IdRequest(osdId);
        var response = sendStandardRequest(LIFECYCLE_STATE__DETACH_LIFECYCLE, request);
        verifyResponseIsOkay(response);
    }

    public List<Lifecycle> listLifecycles() throws IOException {
        var request  = new ListLifecycleRequest();
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__LIST, request);
        return lifecycleUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    // convenience function for tests
    public Lifecycle getLifecycle(Long lifecycleId) throws IOException {
        var request  = new ListLifecycleStateRequest();
        var response = sendStandardRequest(LIFECYCLE__LIST, request);
        return lifecycleUnwrapper.unwrap(response, EXPECTED_SIZE_ANY)
                .stream().filter(lifecycle -> lifecycle.getId().equals(lifecycleId))
                .findFirst().orElseThrow(ErrorCode.LIFECYCLE_NOT_FOUND.getException());
    }

    // convenience function for tests
    public Lifecycle getLifecycleByName(String name) throws IOException {
        var response = sendStandardRequest(LIFECYCLE__LIST, new ListLifecycleStateRequest());
        return lifecycleUnwrapper.unwrap(response, EXPECTED_SIZE_ANY)
                .stream().filter(lifecycle -> lifecycle.getName().equals(name))
                .findFirst().orElseThrow();
    }

    public Lifecycle updateLifecycle(Lifecycle lifecycle) throws IOException {
        var request  = new UpdateLifecycleRequest(List.of(lifecycle));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__UPDATE, request);
        return lifecycleUnwrapper.unwrap(response, 1).getFirst();
    }

    public void deleteLifecycle(Long lifecycleId) throws IOException {
        var request  = new DeleteLifecycleRequest(List.of(lifecycleId));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE__DELETE, request);
        verifyDeleteResponse(response);
    }

    public List<LifecycleState> getNextLifecycleStates(long osdId) throws IOException {
        var request  = new IdRequest(osdId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request);
        return lifecycleStateUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public LifecycleState getLifecycleState(long lifecycleStateId) throws IOException {
        var request  = new IdRequest(lifecycleStateId);
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET, request);
        return lifecycleStateUnwrapper.unwrap(response, 1).getFirst();
    }

    public LifecycleState updateLifecycleState(LifecycleState lcs) throws IOException {
        var request  = new UpdateLifecycleStateRequest(List.of(lcs));
        var response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__UPDATE, request);
        return lifecycleStateUnwrapper.unwrap(response, 1).getFirst();
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

    public List<Summary> getOsdSummaries(List<Long> osdIds) throws IOException {
        var request  = new IdListRequest(osdIds);
        var response = sendStandardRequest(UrlMapping.OSD__GET_SUMMARIES, request);
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

    public List<IndexItem> listIndexItems() throws IOException {
        var request  = new ListIndexItemRequest();
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__LIST, request);
        return indexItemUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public IndexItem createIndexItem(IndexItem indexItem) throws IOException {
        var request  = new CreateIndexItemRequest(List.of(indexItem));
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__CREATE, request);
        return indexItemUnwrapper.unwrap(response, 1).getFirst();
    }

    public void deleteIndexItem(Long id) throws IOException {
        var request  = new DeleteIndexItemRequest(id);
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__DELETE, request);
        verifyDeleteResponse(response);
    }

    public IndexItem updateIndexItem(IndexItem indexItem) throws IOException {
        var request  = new UpdateIndexItemRequest(List.of(indexItem));
        var response = sendStandardRequest(UrlMapping.INDEX_ITEM__UPDATE, request);
        return indexItemUnwrapper.unwrap(response, 1).getFirst();
    }

    public void deleteRelation(Long id) throws IOException {
        var request  = new DeleteRelationRequest(id);
        var response = sendStandardRequest(UrlMapping.RELATION__DELETE, request);
        verifyDeleteResponse(response);
    }

    public void setUserConfig(Long userId, String config) throws IOException {
        var request  = new SetUserConfigRequest(userId, config);
        var response = sendStandardRequest(USER__SET_CONFIG, request);
        verifyResponseIsOkay(response);
    }

    public IndexInfoResponse getIndexInfo(boolean countDocuments, boolean listFailedIndexJobs) throws IOException {
        var              request  = new IndexInfoRequest(countDocuments, listFailedIndexJobs);
        StandardResponse response = sendStandardRequest(INDEX__INFO, request);
        return new SingletonUnwrapper<>(IndexInfoResponse.class).unwrap(response);
    }

    public ReindexResponse reindex(ReindexRequest request) throws IOException {
        StandardResponse response = sendStandardRequest(INDEX__REINDEX, request);
        return new SingletonUnwrapper<>(ReindexResponse.class).unwrap(response);
    }

    public SearchIdsResponse search(String query, SearchType searchType) throws IOException {
        SearchIdsRequest request  = new SearchIdsRequest(searchType, query);
        StandardResponse response = sendStandardRequest(SEARCH__IDS, request);
        return new SingletonUnwrapper<>(SearchIdsResponse.class).unwrap(response);
    }

    public List<Folder> getFoldersByPath(String path, boolean includeSummary) throws IOException {
        var              request  = new FolderPathRequest(path, includeSummary);
        StandardResponse response = sendStandardRequest(FOLDER__GET_FOLDER_BY_PATH, request);
        return folderUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public List<Folder> getFoldersByRelativePath(String path, Long parentId, boolean includeSummary) throws IOException {
        var              request  = new FolderByRelativePathRequest(path, parentId, includeSummary);
        StandardResponse response = sendStandardRequest(FOLDER__GET_FOLDER_BY_RELATIVE_PATH, request);
        return folderUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    /**
     * Fetch folders by path without summary.
     */
    public List<Folder> getFoldersByPath(String path) throws IOException {
        return getFoldersByPath(path, false);
    }

    public List<Folder> getSubFolders(Long parentFolderId, boolean includeSummary) throws IOException {
        SingleFolderRequest request  = new SingleFolderRequest(parentFolderId, includeSummary);
        StandardResponse    response = sendStandardRequest(UrlMapping.FOLDER__GET_SUBFOLDERS, request);
        return folderUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public FolderWrapper getSubFolderWrapper(Long parentFolderId, boolean includeSummary) throws IOException {
        SingleFolderRequest request  = new SingleFolderRequest(parentFolderId, includeSummary);
        StandardResponse    response = sendStandardRequest(UrlMapping.FOLDER__GET_SUBFOLDERS, request);
        verifyResponseIsOkay(response);
        String content = new String(response.getEntity().getContent().readAllBytes());
        return mapper.readValue(content, FolderWrapper.class);
    }

    public void updateFolderMeta(Meta meta) throws IOException {
        UpdateMetaRequest request  = new UpdateMetaRequest(List.of(meta));
        StandardResponse  response = sendStandardRequest(FOLDER__UPDATE_META_CONTENT, request);
        verifyResponseIsOkay(response);
    }

    public void updateOsdMeta(Meta meta) throws IOException {
        UpdateMetaRequest request  = new UpdateMetaRequest(List.of(meta));
        StandardResponse  response = sendStandardRequest(OSD__UPDATE_META_CONTENT, request);
        verifyResponseIsOkay(response);
    }

    public String testEcho(String message) throws IOException {
        var response = httpClient.execute(ClassicRequestBuilder.post("http://localhost:" + port + TEST__ECHO.getPath())
                .addHeader("ticket", getTicket(false))
                .setEntity(message, APPLICATION_XML.withCharset(StandardCharsets.UTF_8))
                .build(), StandardResponse::new);
        verifyResponseIsOkay(response);
        return new String(response.getEntity().getContent().readAllBytes());
    }

    public void reloadLogging() throws IOException {
        var response = httpClient.execute(ClassicRequestBuilder.post("http://localhost:" + port + CONFIG__RELOAD_LOGGING.getPath())
                .addHeader("ticket", getTicket(false))
                .build(), StandardResponse::new);
        verifyResponseIsOkay(response);
    }

    public List<UrlMappingInfo> listUrlMappings() throws IOException {
        ListUrlMappingInfoRequest request  = new ListUrlMappingInfoRequest();
        StandardResponse          response = sendStandardRequest(CONFIG__URL_MAPPINGS, request);
        return urlMappingInfoWrapperUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    public ChangeTrigger createChangeTrigger(ChangeTrigger changeTrigger) throws IOException {
        CreateChangeTriggerRequest request  = new CreateChangeTriggerRequest(List.of(changeTrigger));
        StandardResponse           response = sendStandardRequest(CHANGE_TRIGGER__CREATE, request);
        return changeTriggerUnwrapper.unwrap(response, 1).getFirst();
    }

    public List<RelationType> getRelationTypes() throws IOException {
        StandardResponse response = sendStandardRequest(UrlMapping.RELATION_TYPE__LIST, new ListRelationTypeRequest());
        return relationTypeUnwrapper.unwrap(response, EXPECTED_SIZE_ANY);
    }

    static class SingletonUnwrapper<S> {

        private final Class<? extends S> clazz;

        private final static XmlMapper mapper = XML_MAPPER;

        SingletonUnwrapper(Class<? extends S> clazz) {
            this.clazz = clazz;
        }

        public S unwrap(StandardResponse response) throws IOException {
            checkResponseForErrors(response, mapper);
            return mapper.readValue(response.getEntity().getContent(), clazz);
        }
    }

    static void checkResponseForErrors(StandardResponse response, XmlMapper mapper) throws IOException {
        if (response.containsHeader(HEADER_FIELD_CINNAMON_ERROR)) {
            CinnamonErrorWrapper wrapper = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class);
            log.warn("Found errors: {}", wrapper.getErrors().stream().map(CinnamonError::toString).collect(Collectors.joining(",")));
            CinnamonClient.changeTriggerResponseLocal.get().addAll(wrapper.getChangeTriggerResponses());
            throw new CinnamonClientException(wrapper);
        }
        if (response.getCode() != SC_OK) {
            String message = String.valueOf(response.getCode());
            log.warn("Failed to unwrap non-okay response with status: {}", message);
            log.info("Response: {}", new String(response.getEntity().getContent().readAllBytes()));
            throw new CinnamonClientException(message);
        }
    }

    @Override
    public String toString() {
        return "CinnamonClient{" +
                "port=" + port +
                ", host='" + host + '\'' +
                ", protocol='" + protocol + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", ticket='" + ticket + '\'' +
                '}';
    }
}
