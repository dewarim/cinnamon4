package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ErrorResponseGenerator {

    private static final ObjectMapper xmlMapper = new XmlMapper();

    public static void generateErrorMessage(HttpServletResponse response, ErrorCode errorCode, String message) {
        CinnamonError error = new CinnamonError(errorCode.name(), message);
        try {
            response.setStatus(errorCode.getHttpResponseCode());
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            CinnamonErrorWrapper wrapper = new CinnamonErrorWrapper(error);
            xmlMapper.writeValue(response.getWriter(), wrapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
        }
    }

    public static void superuserRequired(HttpServletResponse response) {
        generateErrorMessage(response, ErrorCode.REQUIRES_SUPERUSER_STATUS, "");
    }

    public static void generateErrorMessage(HttpServletResponse response, ErrorCode errorCode) {
        generateErrorMessage(response, errorCode, null);
    }

}
