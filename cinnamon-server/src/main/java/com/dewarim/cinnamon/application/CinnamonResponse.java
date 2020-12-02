package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

public class CinnamonResponse extends HttpServletResponseWrapper {

    private final ObjectMapper        xmlMapper  = new XmlMapper();
    private final HttpServletResponse response;
    private       Wrapper             wrapper;
    private       int                 statusCode = HttpStatus.SC_OK;

    public CinnamonResponse(HttpServletResponse response) {
        super(response);
        this.response = response;
    }

    public void generateErrorMessage(int statusCode, ErrorCode errorCode, String message) {
        CinnamonError error = new CinnamonError(errorCode.getCode(), message);
        try {
            response.setStatus(statusCode);
            response.setContentType(CONTENT_TYPE_XML);
            response.setCharacterEncoding("UTF-8");
            xmlMapper.writeValue(response.getWriter(), error);
        } catch (IOException e) {
            throw new CinnamonException("Failed to generate error message:", e);
        }
    }

    public void responseIsGenericOkay() {
        setWrapper(new GenericResponse(true));
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    private boolean hasPendingContent() {
        return wrapper != null;
    }

    /**
     * If a response is pending, render it. Otherwise we assume that a servlet has already
     * written directly to the underlying OutputStream, which is okay for binary responses
     * and raw data (for example: returning non-wrappable Cinnamon3-style metasets)
     */
    public void renderResponseIfNecessary() throws IOException {
        if (hasPendingContent()) {
            ResponseUtil.responseIsXmlWithStatus(response, statusCode);
            xmlMapper.writeValue(response.getOutputStream(), wrapper);
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}