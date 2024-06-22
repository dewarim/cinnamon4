package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.api.Constants.HEADER_FIELD_CINNAMON_ERROR;

public class ErrorResponseGenerator {
    private static final Logger log = LogManager.getLogger(ErrorResponseGenerator.class);

    private static final ObjectMapper xmlMapper = new XmlMapper();

    public static void generateErrorMessage(HttpServletResponse response, ErrorCode errorCode, String message) {
        CinnamonError        error   = new CinnamonError(errorCode.name(), message);
        CinnamonErrorWrapper wrapper = new CinnamonErrorWrapper();
        wrapper.getErrors().add(error);
        try {
            response.setStatus(errorCode.getHttpResponseCode());
            response.setContentType(CONTENT_TYPE_XML);
            response.setCharacterEncoding("UTF-8");
            response.setHeader(HEADER_FIELD_CINNAMON_ERROR, errorCode.name());
            Writer writer = null;
            try {
                writer = response.getWriter();
            } catch (IllegalStateException e) {
                if ("STREAM".equals(e.getMessage())) {
                    log.debug("Failed to get writer from Jetty, will try to write error to stream", e);
                    xmlMapper.writeValue(response.getOutputStream(), wrapper);
                }
                else {
                    throw e;
                }
            }
            xmlMapper.writeValue(writer, wrapper);
        } catch (IllegalStateException | IOException e) {
            String msg = String.format("Error occurred while generating error message, code: %s message: %s ", errorCode.getCode(), message);
            log.info(msg, e);
            throw new CinnamonException(msg, e);
        } finally {
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
        }
    }

    public static void generateErrorMessage(HttpServletResponse response, ErrorCode errorCode) {
        generateErrorMessage(response, errorCode, null);
    }

}
