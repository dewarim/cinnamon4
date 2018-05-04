package com.dewarim.cinnamon.application;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    CINNAMON__CONNECT("cinnamon", "connect", ""),
    USER__USER_INFO("user", "userInfo", "/api"),
    ACL__GET_ACLS("acl", "getAcls", "/api"),
    ACL__CREATE_ACL("acl", "createAcl", "/api"),
    ACL__ACL_INFO("acl", "aclInfo", "/api"),
    ACL__DELETE_ACL("acl", "deleteAcl", "/api"),
    ACL__UPDATE_ACL("acl", "updateAcl", "/api"),
    ACL__GET_USER_ACLS("acl", "getUserAcls", "/api"),
    PERMISSION__LIST_PERMISSIONS("permission", "listPermissions", "/api"),
    PERMISSION__GET_USER_PERMISSIONS("permission", "getUserPermissions", "/api"),
    OSD__GET_OBJECTS_BY_ID("osd", "getObjectsById", "/api"),
    OSD__GET_OBJECTS_BY_FOLDER_ID("osd", "getObjectsByFolderId", "/api"),
    LANGUAGE__LIST__LANGUAGES("language","listLanguages" ,"/api" ),
    LINK__GET_LINK_BY_ID("link", "getLinkById", "/api"),
    LINK__DELETE_LINK("link", "deleteLink", "/api"),
    LINK__CREATE_LINK("link", "createLink", "/api"),
    LINK__UPDATE_LINK("link", "updateLink", "/api"),
    CINNAMON__DISCONNECT("cinnamon", "disconnect", ""),
    STATIC__ROOT("static", "", ""),
    FORMAT__LIST_FORMATS("format", "listFormats", "/api"),
    OBJECT_TYPE__LIST_OBJECT_TYPES("objectType", "listObjectTypes", "/api"),
    GROUP__LIST_GROUPS("group", "listGroups", "/api"),
    RELATION_TYPE__LIST_RELATION_TYPES("relationType", "listRelationTypes", "/api"),
    FOLDER_TYPE__LIST_FOLDER_TYPES("folderType", "listFolderTypes", "/api"),
    UI_LANGUAGE__LIST_UI_LANGUAGES("uiLanguage", "listUiLanguages", "/api"),
    METASET_TYPE__LIST_METASET_TYPES("metasetType","listMetasetTypes" ,"/api" ),
    INDEX_ITEM__LIST_INDEX_ITEMS("indexItem","listIndexItems" ,"/api" );

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
