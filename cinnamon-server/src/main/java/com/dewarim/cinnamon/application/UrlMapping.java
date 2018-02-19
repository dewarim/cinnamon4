package com.dewarim.cinnamon.application;

/**
 * All API url mappings.
 * <p>
 * Convention: Servlet name + "__" + method name (replacing camelCase with upper-case SNAKE_CASE),
 */
public enum UrlMapping {

    USER__USER_INFO("user", "userInfo");

    private String servlet;
    private String action;

    UrlMapping(String servlet, String action) {
        this.servlet = servlet;
        this.action = action;
    }

    public String getPath() {
        return "/api/" + servlet + "/" + action;
    }

    public String getServlet() {
        return servlet;
    }

    public String getAction() {
        return action;
    }
}
