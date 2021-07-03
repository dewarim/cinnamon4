package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.model.request.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.ChangeLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.LifecycleRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.request.RelationRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.request.acl.AclInfoRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.acl.UpdateAclRequest;
import com.dewarim.cinnamon.model.request.aclGroup.AclGroupListRequest;
import com.dewarim.cinnamon.model.request.aclGroup.CreateAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.DeleteAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.ListAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.request.config.ListConfigRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.FolderPathRequest;
import com.dewarim.cinnamon.model.request.folder.FolderRequest;
import com.dewarim.cinnamon.model.request.folder.SingleFolderRequest;
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
import com.dewarim.cinnamon.model.request.index.ListIndexItemRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.link.CreateLinkRequest;
import com.dewarim.cinnamon.model.request.link.DeleteLinkRequest;
import com.dewarim.cinnamon.model.request.link.GetLinksRequest;
import com.dewarim.cinnamon.model.request.link.LinkWrapper;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.CreateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.DeleteObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.UpdateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.SetContentRequest;
import com.dewarim.cinnamon.model.request.permission.ChangePermissionsRequest;
import com.dewarim.cinnamon.model.request.permission.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.UpdateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.request.user.ListUserInfoRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.UserInfoRequest;
import com.dewarim.cinnamon.model.request.user.UserPermissionRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.ConfigWrapper;
import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.LinkResponseWrapper;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.UserWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    ACL_GROUP__CREATE("aclGroup", "create", "/api", "Create a new ACL group. Note: permissions parameter is not yet implemented.", CreateAclGroupRequest.class, AclGroupWrapper.class),
    ACL_GROUP__DELETE("aclGroup", "delete", "/api", "", DeleteAclGroupRequest.class, DeleteResponse.class),
    ACL_GROUP__LIST("aclGroup", "list", "/api", "", ListAclGroupRequest.class, AclGroupWrapper.class),
    ACL_GROUP__LIST_BY_GROUP_OR_ACL("aclGroup", "listByGroupOrAcl", "/api", "", AclGroupListRequest.class, AclGroupWrapper.class),
    ACL_GROUP__UPDATE("aclGroup", "update", "/api", "", UpdateAclGroupRequest.class, AclGroupWrapper.class),
    ACL__ACL_INFO("acl", "aclInfo", "/api", "", AclInfoRequest.class, AclWrapper.class),
    ACL__CREATE("acl", "create", "/api", "", CreateAclRequest.class, AclWrapper.class),
    ACL__DELETE("acl", "delete", "/api", "", DeleteAclRequest.class, DeleteResponse.class),
    ACL__GET_USER_ACLS("acl", "getUserAcls", "/api", "", IdRequest.class, AclWrapper.class),
    ACL__LIST("acl", "list", "/api", "", ListAclRequest.class, AclWrapper.class),
    ACL__UPDATE("acl", "updateAcl", "/api", "", UpdateAclRequest.class, AclWrapper.class),
    CINNAMON__CONNECT("cinnamon", "connect", "", """
            Connect to the cinnamon server by sending a form-encoded username and password.
            """, null, null),
    CINNAMON__DISCONNECT("cinnamon", "disconnect", "", """
            Disconnect from the cinnamon server by invalidating the session ticket.
            """, null, null),
    CINNAMON__INFO("cinnamon", "info", "", """
            Retrieve the server version and build number.
            """, null, null),
    CONFIG_ENTRY__GET_CONFIG_ENTRY("configEntry", "getConfigEntry", "/api", "", ConfigEntryRequest.class, ConfigEntryWrapper.class),
    CONFIG_ENTRY__SET_CONFIG_ENTRY("configEntry", "setConfigEntry", "/api", "", CreateConfigEntryRequest.class, ConfigEntryWrapper.class),
    CONFIG__LIST_ALL_CONFIGURATIONS("config", "listAllConfigurations", "/api", "", ListConfigRequest.class, ConfigWrapper.class),
    FOLDER_TYPE__CREATE("folderType", "create", "/api", "", CreateFolderTypeRequest.class, FolderTypeWrapper.class),
    FOLDER_TYPE__DELETE("folderType", "delete", "/api", "", DeleteFolderTypeRequest.class, DeleteResponse.class),
    FOLDER_TYPE__LIST("folderType", "list", "/api", "", ListFolderTypeRequest.class, FolderTypeWrapper.class),
    FOLDER_TYPE__UPDATE("folderType", "update", "/api", "", UpdateFolderTypeRequest.class, FolderTypeWrapper.class),
    FOLDER__CREATE_FOLDER("folder", "createFolder", "/api", "", CreateFolderRequest.class, null),
    FOLDER__CREATE_META("folder", "createMeta", "/api", "", CreateMetaRequest.class, MetaWrapper.class),
    FOLDER__DELETE_META("folder", "deleteMeta", "/api", "", DeleteMetaRequest.class, GenericResponse.class),
    FOLDER__GET_FOLDER("folder", "getFolder", "/api", "Fetch a single folder", SingleFolderRequest.class, FolderWrapper.class),
    FOLDER__GET_FOLDERS("folder", "getFolders", "/api", "", FolderRequest.class, FolderWrapper.class),
    FOLDER__GET_FOLDER_BY_PATH("folder", "getFolderByPath", "/api", "", FolderPathRequest.class, FolderWrapper.class),
    FOLDER__GET_META("folder", "getMeta", "/api", "", MetaRequest.class, MetaWrapper.class),
    FOLDER__GET_SUBFOLDERS("folder", "getSubFolders", "/api", "", SingleFolderRequest.class, FolderWrapper.class),
    FOLDER__GET_SUMMARIES("folder", "getSummaries", "/api", "", IdListRequest.class, SummaryWrapper.class),
    FOLDER__SET_SUMMARY("folder", "setSummary", "/api", "", SetSummaryRequest.class, GenericResponse.class),
    FOLDER__UPDATE_FOLDER("folder", "updateFolder", "/api", "", UpdateFolderRequest.class, GenericResponse.class),
    FORMAT__LIST("format", "list", "/api", "", ListFormatRequest.class, FormatWrapper.class),
    FORMAT__CREATE("format", "create", "/api", "", CreateFormatRequest.class, FormatWrapper.class),
    FORMAT__UPDATE("format", "update", "/api", "", UpdateFormatRequest.class, FormatWrapper.class),
    FORMAT__DELETE("format", "delete", "/api", "", DeleteFormatRequest.class, DeleteResponse.class),
    GROUP__ADD_USER_TO_GROUPS("group", "addUserToGroups", "/api", "", AddUserToGroupsRequest.class, GenericResponse.class),
    GROUP__CREATE("group", "create", "/api", "", CreateGroupRequest.class, GroupWrapper.class),
    GROUP__DELETE("group", "delete", "/api", "", DeleteGroupRequest.class, DeleteResponse.class),
    GROUP__LIST("group", "list", "/api", "", ListGroupRequest.class, GroupWrapper.class),
    GROUP__REMOVE_USER_FROM_GROUPS("group", "removeUserFromGroups", "/api", "", RemoveUserFromGroupsRequest.class, GenericResponse.class),
    GROUP__UPDATE("group", "update", "/api", "", UpdateGroupRequest.class, GroupWrapper.class),
    INDEX_ITEM__LIST_INDEX_ITEMS("indexItem", "listIndexItems", "/api", "", ListIndexItemRequest.class, IndexItemWrapper.class),
    LANGUAGE__LIST__LANGUAGES("language", "listLanguages", "/api", "", ListLanguageRequest.class, LanguageWrapper.class),
    LIFECYCLE_STATE__ATTACH_LIFECYCLE("lifecycleState", "attachLifecycle", "/api", "", AttachLifecycleRequest.class, GenericResponse.class),
    LIFECYCLE_STATE__CHANGE_STATE("lifecycleState", "changeState", "/api", "", ChangeLifecycleStateRequest.class, GenericResponse.class),
    LIFECYCLE_STATE__DETACH_LIFECYCLE("lifecycleState", "detachLifecycle", "/api", "", IdRequest.class, GenericResponse.class),
    LIFECYCLE_STATE__GET_LIFECYCLE_STATE("lifecycleState", "getLifecycleState", "/api", "", IdRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE_STATE__GET_NEXT_STATES("lifecycleState", "getNextStates", "/api", "", IdRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE__GET_LIFECYCLE("lifecycle", "getLifecycle", "/api", "", LifecycleRequest.class, LifecycleWrapper.class),
    LIFECYCLE__LIST_LIFECYCLES("lifecycle", "listLifecycles", "/api", "List lifecycles ", ListLifecycleRequest.class, LifecycleWrapper.class),
    LINK__CREATE("link", "create", "/api", "", CreateLinkRequest.class, LinkWrapper.class),
    LINK__DELETE("link", "delete", "/api", "", DeleteLinkRequest.class, DeleteResponse.class),
    LINK__GET_LINKS_BY_ID("link", "getLinksById", "/api", "", GetLinksRequest.class, LinkResponseWrapper.class),
    LINK__UPDATE("link", "update", "/api", "", UpdateLinkRequest.class, LinkWrapper.class),
    METASET_TYPE__LIST_METASET_TYPES("metasetType", "listMetasetTypes", "/api", "", ListMetasetTypeRequest.class, MetasetTypeWrapper.class),
    NULL_MAPPING("", "", "/api", "", null, null),
    OBJECT_TYPE__LIST("objectType", "list", "/api", "", ListObjectTypeRequest.class, ObjectTypeWrapper.class),
    OBJECT_TYPE__CREATE("objectType", "create", "/api", "", CreateObjectTypeRequest.class, ObjectTypeWrapper.class),
    OBJECT_TYPE__UPDATE("objectType", "udpate", "/api", "", UpdateObjectTypeRequest.class, ObjectTypeWrapper.class),
    OBJECT_TYPE__DELETE("objectType", "delete", "/api", "", DeleteObjectTypeRequest.class, DeleteResponse.class),
    OSD__CREATE_META("osd", "createMeta", "/api", "", CreateMetaRequest.class, MetaWrapper.class),
    OSD__CREATE_OSD("osd", "createOsd", "/api", """
            Create a new OSD. Requires: this must be a multipart-mime request, with part "createOsdRequest" and optional part "file" if this object
            should contain data.
            """,
            CreateOsdRequest.class, OsdWrapper.class),
    OSD__DELETE_META("osd", "deleteMeta", "/api", "", DeleteMetaRequest.class, GenericResponse.class),
    OSD__DELETE_OSDS("osd", "deleteOsds", "/api", "", DeleteOsdRequest.class, DeleteResponse.class),
    OSD__GET_CONTENT("osd", "getContent", "/api", "Returns an OSD's content according to it's format's content type.", IdRequest.class, null),
    OSD__GET_META("osd", "getMeta", "/api", "", MetaRequest.class, MetaWrapper.class),
    OSD__GET_OBJECTS_BY_FOLDER_ID("osd", "getObjectsByFolderId", "/api", "", OsdByFolderRequest.class, OsdWrapper.class),
    OSD__GET_OBJECTS_BY_ID("osd", "getObjectsById", "/api", "", OsdRequest.class, OsdWrapper.class),
    OSD__GET_SUMMARIES("osd", "getSummaries", "/api", "", IdListRequest.class, SummaryWrapper.class),
    OSD__LOCK("osd", "lock", "/api", "", IdRequest.class, GenericResponse.class),
    OSD__SET_CONTENT("osd", "setContent", "/api", """
            Set an OSD's content. Requires a multipart-mime request, with part "setContentRequest" and part "file".
            """,
            SetContentRequest.class, GenericResponse.class),
    OSD__SET_SUMMARY("osd", "setSummary", "/api", "", SetSummaryRequest.class, GenericResponse.class),
    OSD__UNLOCK("osd", "unlock", "/api", "", IdRequest.class, GenericResponse.class),
    OSD__VERSION("osd", "version", "/api", """
            Create a new version of an OSD. Requires a multipart-mime request, with part "createNewVersionRequest" and optional
            part "file", if the new version should contain data.
            """, CreateNewVersionRequest.class, OsdWrapper .class),
    PERMISSION__CHANGE_PERMISSIONS("permission", "changePermissions", "/api", "", ChangePermissionsRequest.class, GenericResponse.class),
    PERMISSION__GET_USER_PERMISSIONS("permission", "getUserPermissions", "/api", "", UserPermissionRequest.class, PermissionWrapper.class),
    PERMISSION__LIST("permission", "list", "/api", "", ListPermissionRequest.class, PermissionWrapper.class),
    RELATION_TYPE__LIST("relationType", "list", "/api", "", ListRelationTypeRequest.class, RelationTypeWrapper.class),
    RELATION_TYPE__CREATE("relationType", "create", "/api", "", CreateRelationTypeRequest.class, RelationTypeWrapper.class),
    RELATION_TYPE__UPDATE("relationType", "update", "/api", "", UpdateRelationTypeRequest.class, RelationTypeWrapper.class),
    RELATION_TYPE__DELETE("relationType", "delete", "/api", "", DeleteRelationTypeRequest.class, DeleteResponse.class),
    RELATION__CREATE("relation", "create", "/api", "", CreateRelationRequest.class, RelationWrapper.class),
    RELATION__DELETE("relation", "delete", "/api", "", DeleteRelationRequest.class, GenericResponse.class),
    RELATION__LIST("relation", "list", "/api", "", RelationRequest.class, RelationWrapper.class),
    STATIC__ROOT("static", "", "", "Returns a static file from the server (for example, a favicon.ico if one exists).", null, null),
    UI_LANGUAGE__LIST_UI_LANGUAGES("uiLanguage", "listUiLanguages", "/api", "", ListUiLanguageRequest.class, UiLanguageWrapper.class),
    USER__LIST_USERS("user", "list", "/api", "", ListUserInfoRequest.class, UserWrapper.class),
    USER__SET_PASSWORD("user", "setPassword", "/api", "", SetPasswordRequest.class, GenericResponse.class),
    USER__USER_INFO("user", "userInfo", "/api", "", UserInfoRequest.class, UserWrapper.class);

    private static final Map<String, UrlMapping>      pathMapping = new ConcurrentHashMap<>();
    private final        String                       servlet;
    private final        String                       action;
    private final        String                       prefix;
    private final        String                       description;
    private final        Class<? extends ApiRequest>  requestClass;
    private final        Class<? extends ApiResponse> responseClass;

    /**
     * @param servlet       the servlet handling the url
     * @param action        the action part of the url (the getUser part in /users/getUser?id=1234)
     * @param prefix        a prefix for the servlet - for example, all api servlets are prefixed with /api for
     * @param requestClass
     * @param responseClass
     */
    UrlMapping(String servlet, String action, String prefix, String description, Class<? extends ApiRequest> requestClass, Class<? extends ApiResponse> responseClass) {
        this.servlet = servlet;
        this.action = action;
        this.prefix = prefix;
        this.description = description;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    public static UrlMapping getByPath(String path) {
        if (pathMapping.isEmpty()) {
            initializePathMapping();
        }
        return pathMapping.getOrDefault(path, UrlMapping.NULL_MAPPING);
    }

    private static void initializePathMapping() {
        for (UrlMapping mapping : values()) {
            pathMapping.put(mapping.prefix + "/" + mapping.servlet + "/" + mapping.action, mapping);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return prefix + "/" + servlet + "/" + action;
    }

    public String getServlet() {
        return servlet;
    }

    public String getAction() {
        return action;
    }

    public Class<? extends ApiRequest> getRequestClass() {
        return requestClass;
    }

    public Class<? extends ApiResponse> getResponseClass() {
        return responseClass;
    }
}
