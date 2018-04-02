package com.dewarim.cinnamon.application;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    CINNAMON_CONNECT("cinnamon", "connect", ""),
    USER__USER_INFO("user", "userInfo", "/api"),
    ACL__GET_ACLS("acl", "getAcls", "/api"),
    ACL__CREATE_ACL("acl", "createAcl", "/api"),
    ACL__ACL_INFO("acl", "aclInfo", "/api"),
    ACL__DELETE_ACL("acl", "deleteAcl", "/api"),
    ACL__UPDATE_ACL("acl", "updateAcl", "/api"),
    ACL__GET_USER_ACLS("acl", "getUserAcls", "/api"),
    PERMISSION__LIST_PERMISSIONS("permission", "listPermissions", "/api"),
    PERMISSION__GET_USER_PERMISSIONS("permission", "getUserPermissions", "/api"), 
    OSD__GET_OBJECTS_BY_ID("osd","getObjectsById" ,"/api" );

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
