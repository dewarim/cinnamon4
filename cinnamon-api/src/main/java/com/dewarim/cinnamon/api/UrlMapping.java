package com.dewarim.cinnamon.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    ACL_GROUP__CREATE("aclGroup", "create", "/api"),
    ACL_GROUP__DELETE("aclGroup", "delete", "/api"),
    ACL_GROUP__LIST("aclGroup", "list", "/api"),
    ACL_GROUP__LIST_BY_GROUP_OR_ACL("aclGroup", "listByGroupOrAcl", "/api"),
    ACL_GROUP__UPDATE("aclGroup", "update", "/api"),
    ACL__ACL_INFO("acl", "aclInfo", "/api"),
    ACL__CREATE("acl", "create", "/api"),
    ACL__DELETE("acl", "delete", "/api"),
    ACL__GET_USER_ACLS("acl", "getUserAcls", "/api"),
    ACL__LIST("acl", "list", "/api"),
    ACL__UPDATE("acl", "updateAcl", "/api"),
    CINNAMON__CONNECT("cinnamon", "connect", ""),
    CINNAMON__DISCONNECT("cinnamon", "disconnect", ""),
    CINNAMON__INFO("cinnamon", "info", ""),
    CONFIG_ENTRY__GET_CONFIG_ENTRY("configEntry", "getConfigEntry", "/api"),
    CONFIG_ENTRY__SET_CONFIG_ENTRY("configEntry", "setConfigEntry", "/api"),
    CONFIG__LIST_ALL_CONFIGURATIONS("config", "listAllConfigurations", "/api"),
    FOLDER_TYPE__CREATE("folderType", "create", "/api"),
    FOLDER_TYPE__DELETE("folderType", "delete", "/api"),
    FOLDER_TYPE__LIST("folderType", "list", "/api"),
    FOLDER_TYPE__UPDATE("folderType", "update", "/api"),
    FOLDER__CREATE_FOLDER("folder", "createFolder", "/api"),
    FOLDER__CREATE_META("folder", "createMeta", "/api"),
    FOLDER__DELETE_META("folder", "deleteMeta", "/api"),
    FOLDER__GET_FOLDER("folder", "getFolder", "/api"),
    FOLDER__GET_FOLDERS("folder", "getFolders", "/api"),
    FOLDER__GET_FOLDER_BY_PATH("folder", "getFolderByPath", "/api"),
    FOLDER__GET_META("folder", "getMeta", "/api"),
    FOLDER__GET_SUBFOLDERS("folder", "getSubFolders", "/api"),
    FOLDER__GET_SUMMARIES("folder", "getSummaries", "/api"),
    FOLDER__SET_SUMMARY("folder", "setSummary", "/api"),
    FOLDER__UPDATE_FOLDER("folder", "updateFolder", "/api"),
    FORMAT__LIST("format", "list", "/api"),
    FORMAT__CREATE("format", "create", "/api"),
    FORMAT__UPDATE("format", "update", "/api"),
    FORMAT__DELETE("format", "delete", "/api"),
    GROUP__ADD_USER_TO_GROUPS("group", "addUserToGroups", "/api"),
    GROUP__CREATE("group", "create", "/api"),
    GROUP__DELETE("group", "delete", "/api"),
    GROUP__LIST("group", "list", "/api"),
    GROUP__REMOVE_USER_FROM_GROUPS("group", "removeUserFromGroups", "/api"),
    GROUP__UPDATE("group", "update", "/api"),
    INDEX_ITEM__LIST_INDEX_ITEMS("indexItem", "listIndexItems", "/api"),
    LANGUAGE__LIST__LANGUAGES("language", "listLanguages", "/api"),
    LIFECYCLE_STATE__ATTACH_LIFECYCLE("lifecycleState", "attachLifecycle", "/api"),
    LIFECYCLE_STATE__CHANGE_STATE("lifecycleState", "changeState", "/api"),
    LIFECYCLE_STATE__DETACH_LIFECYCLE("lifecycleState", "detachLifecycle", "/api"),
    LIFECYCLE_STATE__GET_LIFECYCLE_STATE("lifecycleState", "getLifecycleState", "/api"),
    LIFECYCLE_STATE__GET_NEXT_STATES("lifecycleState", "getNextStates", "/api"),
    LIFECYCLE__GET_LIFECYCLE("lifecycle", "getLifecycle", "/api"),
    LIFECYCLE__LIST_LIFECYCLES("lifecycle", "listLifecycles", "/api"),
    LINK__CREATE("link", "create", "/api"),
    LINK__DELETE("link", "delete", "/api"),
    LINK__GET_LINKS_BY_ID("link", "getLinksById", "/api"),
    LINK__UPDATE("link", "update", "/api"),
    METASET_TYPE__LIST_METASET_TYPES("metasetType", "listMetasetTypes", "/api"),
    NULL_MAPPING("", "", "/api"),
    OBJECT_TYPE__LIST_OBJECT_TYPES("objectType", "listObjectTypes", "/api"),
    OSD__CREATE_META("osd", "createMeta", "/api"),
    OSD__CREATE_OSD("osd", "createOsd", "/api"),
    OSD__DELETE_META("osd", "deleteMeta", "/api"),
    OSD__DELETE_OSDS("osd", "deleteOsds", "/api"),
    OSD__GET_CONTENT("osd", "getContent", "/api"),
    OSD__GET_META("osd", "getMeta", "/api"),
    OSD__GET_OBJECTS_BY_FOLDER_ID("osd", "getObjectsByFolderId", "/api"),
    OSD__GET_OBJECTS_BY_ID("osd", "getObjectsById", "/api"),
    OSD__GET_SUMMARIES("osd", "getSummaries", "/api"),
    OSD__LOCK("osd", "lock", "/api"),
    OSD__SET_CONTENT("osd", "setContent", "/api"),
    OSD__SET_SUMMARY("osd", "setSummary", "/api"),
    OSD__UNLOCK("osd", "unlock", "/api"),
    OSD__VERSION("osd", "version", "/api"),
    PERMISSION__CHANGE_PERMISSIONS("permission", "changePermissions", "/api"),
    PERMISSION__GET_USER_PERMISSIONS("permission", "getUserPermissions", "/api"),
    PERMISSION__LIST("permission", "list", "/api"),
    RELATION_TYPE__LIST_RELATION_TYPES("relationType", "listRelationTypes", "/api"),
    RELATION__CREATE_RELATION("relation", "createRelation", "/api"),
    RELATION__DELETE_RELATION("relation", "deleteRelation", "/api"),
    RELATION__GET_RELATIONS("relation", "getRelations", "/api"),
    STATIC__ROOT("static", "", ""),
    UI_LANGUAGE__LIST_UI_LANGUAGES("uiLanguage", "listUiLanguages", "/api"),
    USER__LIST_USERS("user", "list", "/api"),
    USER__SET_PASSWORD("user", "setPassword", "/api"),
    USER__USER_INFO("user", "userInfo", "/api"),
    ;


    private static final Map<String, UrlMapping> pathMapping = new ConcurrentHashMap<>();
    private final        String                  servlet;
    private final        String                  action;
    private final        String                  prefix;

    /**
     * @param servlet the servlet handling the url
     * @param action  the action part of the url (the getUser part in /users/getUser?id=1234)
     * @param prefix  a prefix for the servlet - for example, all api servlets are prefixed with /api for
     *                authentication filtering.
     */
    UrlMapping(String servlet, String action, String prefix) {
        this.servlet = servlet;
        this.action = action;
        this.prefix = prefix;
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

    public String getPath() {
        return prefix + "/" + servlet + "/" + action;
    }

    public String getServlet() {
        return servlet;
    }

    public String getAction() {
        return action;
    }
}
