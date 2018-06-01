package com.dewarim.cinnamon.application;

import javax.servlet.http.HttpServletResponse;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

public class ResponseUtil {

    /**
     * Set the response's content type to XML and the response status to 200/Okay.
     */
    public static void responseIsOkayAndXml(HttpServletResponse response){
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK); 
    }

    public static void responseIsXmlWithStatus(HttpServletResponse response, int statusCode) {
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(statusCode);
    }
}
