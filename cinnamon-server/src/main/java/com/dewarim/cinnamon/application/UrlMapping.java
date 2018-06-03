package com.dewarim.cinnamon.application;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    ACL__ACL_INFO("acl", "aclInfo", "/api"),
    ACL__CREATE_ACL("acl", "createAcl", "/api"),
    ACL__DELETE_ACL("acl", "deleteAcl", "/api"),
    ACL__GET_ACLS("acl", "getAcls", "/api"),
    ACL__GET_USER_ACLS("acl", "getUserAcls", "/api"),
    ACL__UPDATE_ACL("acl", "updateAcl", "/api"),
    CINNAMON__CONNECT("cinnamon", "connect", ""),
    CINNAMON__DISCONNECT("cinnamon", "disconnect", ""),
    CONFIG_ENTRY__GET_CONFIG_ENTRY("configEntry", "getConfigEntry", "/api"),
    CONFIG_ENTRY__SET_CONFIG_ENTRY("configEntry", "setConfigEntry", "/api"),
    CONFIG__LIST_ALL_CONFIGURATIONS("config", "listAllConfigurations", "/api"),
    FOLDER_TYPE__LIST_FOLDER_TYPES("folderType", "listFolderTypes", "/api"),
    FOLDER__GET_SUMMARIES("folder", "getSummaries", "/api"),
    FOLDER__SET_SUMMARY("folder", "setSummary", "/api"),
    FORMAT__LIST_FORMATS("format", "listFormats", "/api"),
    GROUP__LIST_GROUPS("group", "listGroups", "/api"),
    INDEX_ITEM__LIST_INDEX_ITEMS("indexItem", "listIndexItems", "/api"),
    LANGUAGE__LIST__LANGUAGES("language", "listLanguages", "/api"),
    LIFECYCLE__LIST_LIFECYCLES("lifecycle", "listLifecycles", "/api"),
    LIFECYCLE_STATE__GET_LIFECYCLE_STATE("lifecycleState","getLifecycleState" ,"/api" ),
    LINK__CREATE_LINK("link", "createLink", "/api"),
    LINK__DELETE_LINK("link", "deleteLink", "/api"),
    LINK__GET_LINK_BY_ID("link", "getLinkById", "/api"),
    LINK__UPDATE_LINK("link", "updateLink", "/api"),
    METASET_TYPE__LIST_METASET_TYPES("metasetType", "listMetasetTypes", "/api"),
    OBJECT_TYPE__LIST_OBJECT_TYPES("objectType", "listObjectTypes", "/api"),
    OSD__GET_OBJECTS_BY_FOLDER_ID("osd", "getObjectsByFolderId", "/api"),
    OSD__GET_OBJECTS_BY_ID("osd", "getObjectsById", "/api"),
    OSD__GET_SUMMARIES("osd", "getSummaries", "/api"),
    OSD__SET_SUMMARY("osd", "setSummary", "/api"),
    PERMISSION__GET_USER_PERMISSIONS("permission", "getUserPermissions", "/api"),
    PERMISSION__LIST_PERMISSIONS("permission", "listPermissions", "/api"),
    RELATION_TYPE__LIST_RELATION_TYPES("relationType", "listRelationTypes", "/api"),
    RELATION__CREATE_RELATION("relation","createRelation" ,"/api" ),
    RELATION__DELETE_RELATION("relation","deleteRelation" ,"/api" ),
    RELATION__GET_RELATIONS("relation", "getRelations", "/api"),
    STATIC__ROOT("static", "", ""),
    UI_LANGUAGE__LIST_UI_LANGUAGES("uiLanguage", "listUiLanguages", "/api"),
    USER__LIST_USERS("user", "listUsers", "/api"),
    USER__SET_PASSWORD("user", "setPassword", "/api"),
    USER__USER_INFO("user", "userInfo", "/api");

    private String servlet;
    private String action;
    private String prefix;

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
