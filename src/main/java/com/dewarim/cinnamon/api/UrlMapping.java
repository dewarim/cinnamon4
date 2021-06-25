package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.model.request.aclGroup.AclGroupListRequest;
import com.dewarim.cinnamon.model.request.aclGroup.CreateAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.DeleteAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.ListAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.DeleteResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    ACL_GROUP__CREATE("aclGroup", "create", "/api", "", CreateAclGroupRequest.class, AclGroupWrapper.class),
    ACL_GROUP__DELETE("aclGroup", "delete", "/api", "", DeleteAclGroupRequest.class, DeleteResponse.class),
    ACL_GROUP__LIST("aclGroup", "list", "/api", "", ListAclGroupRequest.class, AclGroupWrapper.class),
    ACL_GROUP__LIST_BY_GROUP_OR_ACL("aclGroup", "listByGroupOrAcl", "/api", "", AclGroupListRequest.class, AclGroupWrapper.class),
    ACL_GROUP__UPDATE("aclGroup", "update", "/api", "", UpdateAclGroupRequest.class, AclGroupWrapper.class),
    ACL__ACL_INFO("acl", "aclInfo", "/api", "", null, null),
    ACL__CREATE("acl", "create", "/api", "", null, null),
    ACL__DELETE("acl", "delete", "/api", "", null, null),
    ACL__GET_USER_ACLS("acl", "getUserAcls", "/api", "", null, null),
    ACL__LIST("acl", "list", "/api", "", null, null),
    ACL__UPDATE("acl", "updateAcl", "/api", "", null, null),
    CINNAMON__CONNECT("cinnamon", "connect", "", """
            Connect to the cinnamon server by sending a form-encoded username and password.
            """, null, null),
    CINNAMON__DISCONNECT("cinnamon", "disconnect", "", """
            Disconnect from the cinnamon server by invalidating the session ticket.
            """, null, null),
    CINNAMON__INFO("cinnamon", "info", "", """
            Retrieve the server version and build number.
            """, null, null),
    CONFIG_ENTRY__GET_CONFIG_ENTRY("configEntry", "getConfigEntry", "/api", "", null, null),
    CONFIG_ENTRY__SET_CONFIG_ENTRY("configEntry", "setConfigEntry", "/api", "", null, null),
    CONFIG__LIST_ALL_CONFIGURATIONS("config", "listAllConfigurations", "/api", "", null, null),
    FOLDER_TYPE__CREATE("folderType", "create", "/api", "", null, null),
    FOLDER_TYPE__DELETE("folderType", "delete", "/api", "", null, null),
    FOLDER_TYPE__LIST("folderType", "list", "/api", "", null, null),
    FOLDER_TYPE__UPDATE("folderType", "update", "/api", "", null, null),
    FOLDER__CREATE_FOLDER("folder", "createFolder", "/api", "", null, null),
    FOLDER__CREATE_META("folder", "createMeta", "/api", "", null, null),
    FOLDER__DELETE_META("folder", "deleteMeta", "/api", "", null, null),
    FOLDER__GET_FOLDER("folder", "getFolder", "/api", "", null, null),
    FOLDER__GET_FOLDERS("folder", "getFolders", "/api", "", null, null),
    FOLDER__GET_FOLDER_BY_PATH("folder", "getFolderByPath", "/api", "", null, null),
    FOLDER__GET_META("folder", "getMeta", "/api", "", null, null),
    FOLDER__GET_SUBFOLDERS("folder", "getSubFolders", "/api", "", null, null),
    FOLDER__GET_SUMMARIES("folder", "getSummaries", "/api", "", null, null),
    FOLDER__SET_SUMMARY("folder", "setSummary", "/api", "", null, null),
    FOLDER__UPDATE_FOLDER("folder", "updateFolder", "/api", "", null, null),
    FORMAT__LIST("format", "list", "/api", "", null, null),
    FORMAT__CREATE("format", "create", "/api", "", null, null),
    FORMAT__UPDATE("format", "update", "/api", "", null, null),
    FORMAT__DELETE("format", "delete", "/api", "", null, null),
    GROUP__ADD_USER_TO_GROUPS("group", "addUserToGroups", "/api", "", null, null),
    GROUP__CREATE("group", "create", "/api", "", null, null),
    GROUP__DELETE("group", "delete", "/api", "", null, null),
    GROUP__LIST("group", "list", "/api", "", null, null),
    GROUP__REMOVE_USER_FROM_GROUPS("group", "removeUserFromGroups", "/api", "", null, null),
    GROUP__UPDATE("group", "update", "/api", "", null, null),
    INDEX_ITEM__LIST_INDEX_ITEMS("indexItem", "listIndexItems", "/api", "", null, null),
    LANGUAGE__LIST__LANGUAGES("language", "listLanguages", "/api", "", null, null),
    LIFECYCLE_STATE__ATTACH_LIFECYCLE("lifecycleState", "attachLifecycle", "/api", "", null, null),
    LIFECYCLE_STATE__CHANGE_STATE("lifecycleState", "changeState", "/api", "", null, null),
    LIFECYCLE_STATE__DETACH_LIFECYCLE("lifecycleState", "detachLifecycle", "/api", "", null, null),
    LIFECYCLE_STATE__GET_LIFECYCLE_STATE("lifecycleState", "getLifecycleState", "/api", "", null, null),
    LIFECYCLE_STATE__GET_NEXT_STATES("lifecycleState", "getNextStates", "/api", "", null, null),
    LIFECYCLE__GET_LIFECYCLE("lifecycle", "getLifecycle", "/api", "", null, null),
    LIFECYCLE__LIST_LIFECYCLES("lifecycle", "listLifecycles", "/api", "", null, null),
    LINK__CREATE("link", "create", "/api", "", null, null),
    LINK__DELETE("link", "delete", "/api", "", null, null),
    LINK__GET_LINKS_BY_ID("link", "getLinksById", "/api", "", null, null),
    LINK__UPDATE("link", "update", "/api", "", null, null),
    METASET_TYPE__LIST_METASET_TYPES("metasetType", "listMetasetTypes", "/api", "", null, null),
    NULL_MAPPING("", "", "/api", "", null, null),
    OBJECT_TYPE__LIST("objectType", "list", "/api", "", null, null),
    OBJECT_TYPE__CREATE("objectType", "create", "/api", "", null, null),
    OBJECT_TYPE__UPDATE("objectType", "udpate", "/api", "", null, null),
    OBJECT_TYPE__DELETE("objectType", "delete", "/api", "", null, null),
    OSD__CREATE_META("osd", "createMeta", "/api", "", null, null),
    OSD__CREATE_OSD("osd", "createOsd", "/api", "", null, null),
    OSD__DELETE_META("osd", "deleteMeta", "/api", "", null, null),
    OSD__DELETE_OSDS("osd", "deleteOsds", "/api", "", null, null),
    OSD__GET_CONTENT("osd", "getContent", "/api", "", null, null),
    OSD__GET_META("osd", "getMeta", "/api", "", null, null),
    OSD__GET_OBJECTS_BY_FOLDER_ID("osd", "getObjectsByFolderId", "/api", "", null, null),
    OSD__GET_OBJECTS_BY_ID("osd", "getObjectsById", "/api", "", null, null),
    OSD__GET_SUMMARIES("osd", "getSummaries", "/api", "", null, null),
    OSD__LOCK("osd", "lock", "/api", "", null, null),
    OSD__SET_CONTENT("osd", "setContent", "/api", "", null, null),
    OSD__SET_SUMMARY("osd", "setSummary", "/api", "", null, null),
    OSD__UNLOCK("osd", "unlock", "/api", "", null, null),
    OSD__VERSION("osd", "version", "/api", "", null, null),
    PERMISSION__CHANGE_PERMISSIONS("permission", "changePermissions", "/api", "", null, null),
    PERMISSION__GET_USER_PERMISSIONS("permission", "getUserPermissions", "/api", "", null, null),
    PERMISSION__LIST("permission", "list", "/api", "", null, null),
    RELATION_TYPE__LIST("relationType", "list", "/api", "", null, null),
    RELATION_TYPE__CREATE("relationType", "create", "/api", "", null, null),
    RELATION_TYPE__UPDATE("relationType", "update", "/api", "", null, null),
    RELATION_TYPE__DELETE("relationType", "delete", "/api", "", null, null),
    RELATION__CREATE("relation", "create", "/api", "", null, null),
    RELATION__DELETE("relation", "delete", "/api", "", null, null),
    RELATION__LIST("relation", "list", "/api", "", null, null),
    STATIC__ROOT("static", "", "", "", null, null),
    UI_LANGUAGE__LIST_UI_LANGUAGES("uiLanguage", "listUiLanguages", "/api", "", null, null),
    USER__LIST_USERS("user", "list", "/api", "", null, null),
    USER__SET_PASSWORD("user", "setPassword", "/api", "", null, null),
    USER__USER_INFO("user", "userInfo", "/api", "", null, null);

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
