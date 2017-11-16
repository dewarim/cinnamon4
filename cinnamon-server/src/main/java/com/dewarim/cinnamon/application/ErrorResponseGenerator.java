package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.model.response.CinnamonError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class ErrorResponseGenerator {
    
    private static ObjectMapper xmlMapper = new XmlMapper();
    
    static void generateErrorMessage(HttpServletResponse response, int statusCode, String code, String message){
        CinnamonError error = new CinnamonError(code, message);
        try {
            response.setStatus(statusCode);
            xmlMapper.writeValue(response.getWriter(), error);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
}
