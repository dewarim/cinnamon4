package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.*;

public class CinnamonResponse extends HttpServletResponseWrapper {

    private final ObjectMapper        xmlMapper  = XML_MAPPER;
    private final HttpServletResponse servletResponse;
    private       Wrapper<?>          wrapper;
    private       int                 statusCode = HttpStatus.SC_OK;
    private       ApiResponse         response;

    public CinnamonResponse(HttpServletResponse servletResponse) {
        super(servletResponse);
        this.servletResponse = servletResponse;
    }

    public void generateErrorMessage(int statusCode, ErrorCode errorCode, String message) {
        generateErrorMessage(statusCode, errorCode, message, Collections.emptyList());
    }

    public void generateErrorMessage(int statusCode, ErrorCode errorCode, String message, List<CinnamonError> errors) {
        CinnamonError        error   = new CinnamonError(errorCode.getCode(), message);
        CinnamonErrorWrapper wrapper = new CinnamonErrorWrapper();
        wrapper.getErrors().add(error);
        wrapper.getErrors().addAll(errors);
        try {
            servletResponse.setStatus(statusCode);
            servletResponse.setContentType(CONTENT_TYPE_XML);
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.setHeader(HEADER_FIELD_CINNAMON_ERROR, errorCode.name());
            xmlMapper.writeValue(servletResponse.getWriter(), wrapper);
        } catch (IOException e) {
            throw new CinnamonException("Failed to generate error message:", e);
        }
    }

    public void responseIsGenericOkay() {
        response = new GenericResponse(true);
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    private boolean hasPendingContent() {
        return wrapper != null || response != null;
    }

    /**
     * If a response is pending, render it. Otherwise we assume that a servlet has already
     * written directly to the underlying OutputStream, which is okay for binary responses
     * and raw data (for example: returning non-wrappable Cinnamon3-style metasets)
     */
    public void renderResponseIfNecessary() throws IOException {
        if (hasPendingContent()) {
            ResponseUtil.responseIsXmlWithStatus(servletResponse, statusCode);
            if (wrapper != null) {
                xmlMapper.writeValue(servletResponse.getOutputStream(), wrapper);
            } else {
                xmlMapper.writeValue(servletResponse.getOutputStream(), response);
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponse(ApiResponse response) {
        this.response = response;
    }
}
