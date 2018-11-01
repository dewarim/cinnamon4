package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.model.response.GenericResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

public class ResponseUtil {

    /**
     * Set the response's content type to XML and the response status to 200/Okay.
     */
    public static void responseIsOkayAndXml(HttpServletResponse response){
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK); 
    }

    /**
     * Set the response status to 200/Okay, along with a XML generic success message.
     */
    public static void responseIsGenericOkay(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        new XmlMapper().writeValue(response.getWriter(), new GenericResponse(true));
    }

    public static void responseIsXmlWithStatus(HttpServletResponse response, int statusCode) {
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(statusCode);
    }
}
