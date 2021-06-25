package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.api.Constants.HEADER_FIELD_CINNAMON_ERROR;

public class ErrorResponseGenerator {

    private static final ObjectMapper xmlMapper = new XmlMapper();

    public static void generateErrorMessage(HttpServletResponse response, ErrorCode errorCode, String message) {
        CinnamonError error = new CinnamonError(errorCode.name(), message);
        CinnamonErrorWrapper wrapper = new CinnamonErrorWrapper();
        wrapper.getErrors().add(error);
        try {
            response.setStatus(errorCode.getHttpResponseCode());
            response.setContentType(CONTENT_TYPE_XML);
            response.setCharacterEncoding("UTF-8");
            response.setHeader(HEADER_FIELD_CINNAMON_ERROR, errorCode.name());
            xmlMapper.writeValue(response.getWriter(), wrapper);
        } catch (IOException e) {
            throw new CinnamonException("Failed to generate error message:", e);
        }
        finally {
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
        }
    }

    public static void generateErrorMessage(HttpServletResponse response, ErrorCode errorCode) {
        generateErrorMessage(response, errorCode, null);
    }

}
