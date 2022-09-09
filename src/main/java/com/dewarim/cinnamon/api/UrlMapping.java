package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.model.request.CreateMetaRequest;
import com.dewarim.cinnamon.model.request.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.DeleteAllMetasRequest;
import com.dewarim.cinnamon.model.request.DeleteMetaRequest;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.MetaRequest;
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
import com.dewarim.cinnamon.model.request.configEntry.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.DeleteConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.ListConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.UpdateConfigEntryRequest;
import com.dewarim.cinnamon.model.request.folder.CreateFolderRequest;
import com.dewarim.cinnamon.model.request.folder.DeleteFolderRequest;
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
import com.dewarim.cinnamon.model.request.index.CreateIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.DeleteIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.IndexInfoRequest;
import com.dewarim.cinnamon.model.request.index.ListIndexItemRequest;
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
import com.dewarim.cinnamon.model.request.lifecycleState.ChangeLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.CreateLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.DeleteLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.ListLifecycleStateRequest;
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
import com.dewarim.cinnamon.model.request.objectType.DeleteObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.UpdateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.osd.CopyOsdRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.DeleteOsdRequest;
import com.dewarim.cinnamon.model.request.osd.GetRelationsRequest;
import com.dewarim.cinnamon.model.request.osd.OsdByFolderRequest;
import com.dewarim.cinnamon.model.request.osd.OsdRequest;
import com.dewarim.cinnamon.model.request.osd.SetContentRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.request.permission.ChangePermissionsRequest;
import com.dewarim.cinnamon.model.request.permission.ListPermissionRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.UpdateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.CreateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.DeleteUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.UpdateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.user.CreateUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.GetUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.ListUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.SetPasswordRequest;
import com.dewarim.cinnamon.model.request.user.UpdateUserAccountRequest;
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
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;

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
    ACL__UPDATE("acl", "update", "/api", "", UpdateAclRequest.class, AclWrapper.class),
    CINNAMON__CONNECT("cinnamon", "connect", "", """
            Connect to the cinnamon server by sending a form-encoded username and password.
            """, null, null),
    CINNAMON__DISCONNECT("cinnamon", "disconnect", "", """
            Disconnect from the cinnamon server by invalidating the session ticket.
            """, null, null),
    CINNAMON__INFO("cinnamon", "info", "", """
            Retrieve the server version and build number.
            """, null, null),
    CONFIG_ENTRY__GET("configEntry", "get", "/api", "Retrieve a config entries by names or ids", ConfigEntryRequest.class, ConfigEntryWrapper.class),
    CONFIG_ENTRY__CREATE("configEntry", "create", "/api", "Create a new config entry", CreateConfigEntryRequest.class, ConfigEntryWrapper.class),
    CONFIG_ENTRY__LIST("configEntry", "list", "/api",
            "List all config entries the current user is allowed to see (superuser: all, normal users: only those with public visibility)",
            ListConfigEntryRequest.class, ConfigEntryWrapper.class),
    CONFIG_ENTRY__UPDATE("configEntry", "update", "/api", "Update a list of config entries", UpdateConfigEntryRequest.class, ConfigEntryWrapper.class),
    CONFIG_ENTRY__DELETE("configEntry", "delete", "/api", "Delete a list of config entries", DeleteConfigEntryRequest.class, DeleteResponse.class),
    CONFIG__LIST_ALL_CONFIGURATIONS("config", "listAllConfigurations", "/api", """
            List of all objects the client may want to cache, for example users, object types, groups, permissions, languages etc.
            """, ListConfigRequest.class, ConfigWrapper.class),
    FOLDER_TYPE__CREATE("folderType", "create", "/api", "Create a new folder type", CreateFolderTypeRequest.class, FolderTypeWrapper.class),
    FOLDER_TYPE__DELETE("folderType", "delete", "/api", "Delete a folder type", DeleteFolderTypeRequest.class, DeleteResponse.class),
    FOLDER_TYPE__LIST("folderType", "list", "/api", "List all folder types", ListFolderTypeRequest.class, FolderTypeWrapper.class),
    FOLDER_TYPE__UPDATE("folderType", "update", "/api", "", UpdateFolderTypeRequest.class, FolderTypeWrapper.class),
    FOLDER__CREATE("folder", "create", "/api", "Create a new folder.", CreateFolderRequest.class, null),
    FOLDER__CREATE_META("folder", "createMeta", "/api", "", CreateMetaRequest.class, MetaWrapper.class),
    FOLDER__DELETE("folder", "delete", "/api", "", DeleteFolderRequest.class, DeleteResponse.class),
    FOLDER__DELETE_META("folder", "deleteMeta", "/api", "", DeleteMetaRequest.class, DeleteResponse.class),
    FOLDER__DELETE_ALL_METAS("folder", "deleteAllMetas", "/api", "", DeleteAllMetasRequest.class, DeleteResponse.class),
    FOLDER__GET_FOLDER("folder", "getFolder", "/api", "Fetch a single folder", SingleFolderRequest.class, FolderWrapper.class),
    FOLDER__GET_FOLDERS("folder", "getFolders", "/api", "", FolderRequest.class, FolderWrapper.class),
    FOLDER__GET_FOLDER_BY_PATH("folder", "getFolderByPath", "/api", "", FolderPathRequest.class, FolderWrapper.class),
    FOLDER__GET_META("folder", "getMeta", "/api", "", MetaRequest.class, MetaWrapper.class),
    FOLDER__GET_SUBFOLDERS("folder", "getSubFolders", "/api", "", SingleFolderRequest.class, FolderWrapper.class),
    FOLDER__GET_SUMMARIES("folder", "getSummaries", "/api", "", IdListRequest.class, SummaryWrapper.class),
    FOLDER__SET_SUMMARY("folder", "setSummary", "/api", "Add a summary to an object, for example a short description of this folder's content. Currently single-folder-API.", SetSummaryRequest.class, GenericResponse.class),
    FOLDER__UPDATE("folder", "update", "/api", "", UpdateFolderRequest.class, GenericResponse.class),
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
    INDEX__INFO("index", "info", "/api", "Provides information on the status of the Lucene search index", IndexInfoRequest.class, IndexInfoResponse.class),
    INDEX_ITEM__LIST("indexItem", "list", "/api", "", ListIndexItemRequest.class, IndexItemWrapper.class),
    INDEX_ITEM__CREATE("indexItem", "create", "/api", "", CreateIndexItemRequest.class, IndexItemWrapper.class),
    INDEX_ITEM__UPDATE("indexItem", "udpate", "/api", "", UpdateIndexItemRequest.class, IndexItemWrapper.class),
    INDEX_ITEM__DELETE("indexItem", "delete", "/api", "", DeleteIndexItemRequest.class, GenericResponse.class),
    LANGUAGE__LIST("language", "list", "/api", "", ListLanguageRequest.class, LanguageWrapper.class),
    LANGUAGE__CREATE("language", "create", "/api", "", CreateLanguageRequest.class, LanguageWrapper.class),
    LANGUAGE__UPDATE("language", "update", "/api", "", UpdateLanguageRequest.class, LanguageWrapper.class),
    LANGUAGE__DELETE("language", "delete", "/api", "", DeleteLanguageRequest.class, DeleteResponse.class),
    LIFECYCLE_STATE__CREATE("lifecycleState", "create", "/api", "", CreateLifecycleStateRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE_STATE__DELETE("lifecycleState", "delete", "/api", "", DeleteLifecycleStateRequest.class, DeleteResponse.class),
    LIFECYCLE_STATE__LIST("lifecycleState", "list", "/api", "", ListLifecycleStateRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE_STATE__UPDATE("lifecycleState", "update", "/api", "", UpdateLifecycleStateRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE_STATE__ATTACH_LIFECYCLE("lifecycleState", "attachLifecycle", "/api", "", AttachLifecycleRequest.class, GenericResponse.class),
    LIFECYCLE_STATE__CHANGE_STATE("lifecycleState", "changeState", "/api", "", ChangeLifecycleStateRequest.class, GenericResponse.class),
    LIFECYCLE_STATE__DETACH_LIFECYCLE("lifecycleState", "detachLifecycle", "/api", "", IdRequest.class, GenericResponse.class),
    LIFECYCLE_STATE__GET("lifecycleState", "get", "/api", "", IdRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE_STATE__GET_NEXT_STATES("lifecycleState", "getNextStates", "/api", "", IdRequest.class, LifecycleStateWrapper.class),
    LIFECYCLE__GET("lifecycle", "get", "/api", "", LifecycleRequest.class, LifecycleWrapper.class),
    LIFECYCLE__CREATE("lifecycle", "create", "/api", "Create lifecycles. Note: does not create lifecycle states, defaultStateId should be empty.", CreateLifecycleRequest.class, LifecycleWrapper.class),
    LIFECYCLE__DELETE("lifecycle", "delete", "/api", "Delete lifecycles ", DeleteLifecycleRequest.class, DeleteResponse.class),
    LIFECYCLE__UPDATE("lifecycle", "update", "/api", "Update lifecycles. Note: does not update lifecycle states ", UpdateLifecycleRequest.class, LifecycleWrapper.class),
    LIFECYCLE__LIST("lifecycle", "list", "/api", "List lifecycles ", ListLifecycleRequest.class, LifecycleWrapper.class),
    LINK__CREATE("link", "create", "/api", "", CreateLinkRequest.class, LinkWrapper.class),
    LINK__DELETE("link", "delete", "/api", "", DeleteLinkRequest.class, DeleteResponse.class),
    LINK__GET_LINKS_BY_ID("link", "getLinksById", "/api", "", GetLinksRequest.class, LinkResponseWrapper.class),
    LINK__UPDATE("link", "update", "/api", "", UpdateLinkRequest.class, LinkWrapper.class),
    METASET_TYPE__CREATE("metasetType", "create", "/api", "", CreateMetasetTypeRequest.class, MetasetTypeWrapper.class),
    METASET_TYPE__DELETE("metasetType", "delete", "/api", "", DeleteMetasetTypeRequest.class, DeleteResponse.class),
    METASET_TYPE__LIST("metasetType", "list", "/api", "", ListMetasetTypeRequest.class, MetasetTypeWrapper.class),
    METASET_TYPE__UPDATE("metasetType", "update", "/api", "", UpdateMetasetTypeRequest.class, MetasetTypeWrapper.class),
    NULL_MAPPING("", "", "/api", "", null, null),
    OBJECT_TYPE__LIST("objectType", "list", "/api", "", ListObjectTypeRequest.class, ObjectTypeWrapper.class),
    OBJECT_TYPE__CREATE("objectType", "create", "/api", "", CreateObjectTypeRequest.class, ObjectTypeWrapper.class),
    OBJECT_TYPE__UPDATE("objectType", "update", "/api", "", UpdateObjectTypeRequest.class, ObjectTypeWrapper.class),
    OBJECT_TYPE__DELETE("objectType", "delete", "/api", "", DeleteObjectTypeRequest.class, DeleteResponse.class),
    OSD__COPY("osd", "copy", "/api", "", CopyOsdRequest.class, OsdWrapper.class),
    OSD__CREATE_META("osd", "createMeta", "/api", "", CreateMetaRequest.class, MetaWrapper.class),
    OSD__CREATE_OSD("osd", "createOsd", "/api", """
            Create a new OSD. Requires: this must be a multipart-mime request, with part "createOsdRequest" and optional part "file" if this object
            should contain data.
            """,
            CreateOsdRequest.class, OsdWrapper.class),
    OSD__DELETE_META("osd", "deleteMeta", "/api", "", DeleteMetaRequest.class, DeleteResponse.class),
    OSD__DELETE_ALL_METAS("osd", "deleteAllMetas", "/api", "", DeleteAllMetasRequest.class, DeleteResponse.class),
    OSD__DELETE("osd", "delete", "/api", "", DeleteOsdRequest.class, DeleteResponse.class),
    OSD__GET_CONTENT("osd", "getContent", "/api", "Returns an OSD's content according to it's format's content type.", IdRequest.class, null),
    OSD__GET_META("osd", "getMeta", "/api", "", MetaRequest.class, MetaWrapper.class),
    OSD__GET_OBJECTS_BY_FOLDER_ID("osd", "getObjectsByFolderId", "/api", "", OsdByFolderRequest.class, OsdWrapper.class),
    OSD__GET_OBJECTS_BY_ID("osd", "getObjectsById", "/api", "", OsdRequest.class, OsdWrapper.class),
    OSD__GET_RELATIONS("osd", "getRelations", "/api", "", GetRelationsRequest.class, RelationWrapper.class),
    OSD__GET_SUMMARIES("osd", "getSummaries", "/api", "", IdListRequest.class, SummaryWrapper.class),
    OSD__LOCK("osd", "lock", "/api", "", IdRequest.class, GenericResponse.class),
    OSD__SET_CONTENT("osd", "setContent", "/api", """
            Set an OSD's content. Requires a multipart-mime request, with part "setContentRequest" and part "file".
            """,
            SetContentRequest.class, GenericResponse.class),
    OSD__SET_SUMMARY("osd", "setSummary", "/api", "Add a summary to an object, for example a short description of the content.", SetSummaryRequest.class, GenericResponse.class),
    OSD__UNLOCK("osd", "unlock", "/api", "", IdRequest.class, GenericResponse.class),
    OSD__UPDATE("osd", "update", "/api", "", UpdateOsdRequest.class, GenericResponse.class),
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
    RELATION__SEARCH("relation", "search", "/api",
            "Search for relations matching all( with orMode=false) or some (with orMode=true) criteria. " +
                    "Fields are optional, but at least one field must contain criteria.",
            SearchRelationRequest.class, RelationWrapper.class),
    STATIC__ROOT("static", "", "", "Returns a static file from the server (for example, a favicon.ico if one exists).", null, null),
    TEST__STATUS_200("test", "status200", "", "Returns status code 200", null, null),
    TEST__STATUS_400("test", "status400", "", "Returns status code 400", null, null),
    UI_LANGUAGE__LIST("uiLanguage", "list", "/api", "", ListUiLanguageRequest.class, UiLanguageWrapper.class),
    UI_LANGUAGE__CREATE("uiLanguage", "create", "/api", "", CreateUiLanguageRequest.class, UiLanguageWrapper.class),
    UI_LANGUAGE__UPDATE("uiLanguage", "update", "/api", "", UpdateUiLanguageRequest.class, UiLanguageWrapper.class),
    UI_LANGUAGE__DELETE("uiLanguage", "delete", "/api", "", DeleteUiLanguageRequest.class, DeleteResponse.class),
    USER__CREATE("user", "create", "/api", "", CreateUserAccountRequest.class, UserAccountWrapper.class),
    USER__UPDATE("user", "update", "/api", "", UpdateUserAccountRequest.class, UserAccountWrapper.class),
    USER__LIST("user", "list", "/api", "", ListUserAccountRequest.class, UserAccountWrapper.class),
    USER__SET_PASSWORD("user", "setPassword", "/api", "", SetPasswordRequest.class, GenericResponse.class),
    USER__GET("user", "get", "/api", "", GetUserAccountRequest.class, UserAccountWrapper.class);

    private static final Map<String, UrlMapping>      pathMapping = new ConcurrentHashMap<>();
    private final        String                       servlet;
    private final        String                       action;
    private final        String                       prefix;
    private final        String                       description;
    // TODO: migrate to parameterized version of ApiRequest<?>
    private final        Class<? extends ApiRequest>  requestClass;
    private final        Class<? extends ApiResponse> responseClass;

    /**
     * @param servlet       the servlet handling the url
     * @param action        the action part of the url (the getUser part in /users/getUser?id=1234)
     * @param prefix        a prefix for the servlet - for example, all api servlets are prefixed with /api for
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
