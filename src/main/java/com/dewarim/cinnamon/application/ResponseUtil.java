package com.dewarim.cinnamon.application;

import jakarta.servlet.http.HttpServletResponse;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;

public class ResponseUtil {

    public static void responseIsXmlWithStatus(HttpServletResponse response, int statusCode) {
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(statusCode);
    }
}
