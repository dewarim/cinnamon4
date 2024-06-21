package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.*;

public class CinnamonResponse extends HttpServletResponseWrapper {
    private static final Logger log = LogManager.getLogger(CinnamonResponse.class);

    private final ObjectMapper                xmlMapper              = XML_MAPPER;
    private final HttpServletResponse         servletResponse;
    private       Wrapper<?>                  wrapper;
    private       int                         statusCode             = HttpStatus.SC_OK;
    private       ApiResponse                 response;
    private final List<ChangeTriggerResponse> changeTriggerResponses = new ArrayList<>();
    private UserAccount user;

    public CinnamonResponse(HttpServletResponse servletResponse) {
        super(servletResponse);
        this.servletResponse = servletResponse;
    }

    public void generateErrorMessage(int statusCode, ErrorCode errorCode, String message, boolean logResponses) {
        generateErrorMessage(statusCode, errorCode, message, Collections.emptyList(), logResponses);
    }

    public void generateErrorMessage(int statusCode, ErrorCode errorCode, String message, List<CinnamonError> errors, boolean logResponses) {
        this.statusCode = statusCode;
        CinnamonError        error   = new CinnamonError(errorCode.getCode(), message);
        CinnamonErrorWrapper wrapper = new CinnamonErrorWrapper();
        wrapper.getErrors().add(error);
        wrapper.getErrors().addAll(errors);
        wrapper.setChangeTriggerResponses(changeTriggerResponses);
        try {
            servletResponse.setStatus(statusCode);
            servletResponse.setContentType(CONTENT_TYPE_XML);
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.setHeader(HEADER_FIELD_CINNAMON_ERROR, errorCode.name());
            if (logResponses) {
                log.debug("sending response to client:\n{}", xmlMapper.writeValueAsString(wrapper));
            }
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
     * If a response is pending, render it. Otherwise, we assume that a servlet has already
     * written directly to the underlying OutputStream, which is okay for binary responses
     * and raw data (for example: returning non-wrappable Cinnamon3-style metasets)
     */
    public void renderResponseIfNecessary(boolean logResponses) throws IOException {
        if (hasPendingContent()) {
            if (wrapper != null && wrapper instanceof BaseResponse) {
                ((BaseResponse) wrapper).setChangeTriggerResponses(changeTriggerResponses);
            }
            else if (response != null && response instanceof BaseResponse) {
                ((BaseResponse) response).setChangeTriggerResponses(changeTriggerResponses);
            }

            ResponseUtil.responseIsXmlWithStatus(servletResponse, statusCode);
            if (logResponses) {
                String output;
                if (wrapper != null) {
                    output = xmlMapper.writeValueAsString(wrapper);
                }
                else {
                    output = xmlMapper.writeValueAsString(response);
                }
                log.debug("sending response to client:\n{}", output);
                servletResponse.getOutputStream().write(output.getBytes(StandardCharsets.UTF_8));
            }
            else {
                if (wrapper != null) {
                    xmlMapper.writeValue(servletResponse.getOutputStream(), wrapper);
                }
                else {
                    xmlMapper.writeValue(servletResponse.getOutputStream(), response);
                }
            }
        }
    }

    /**
     * For ChangeTriggers which need to inspect the response.
     *
     * @return pending content (wrapper or response) as String
     */
    public String getPendingContentAsString() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (hasPendingContent()) {
            ResponseUtil.responseIsXmlWithStatus(servletResponse, statusCode);
            if (wrapper != null) {
                xmlMapper.writeValue(outputStream, wrapper);
            }
            else {
                xmlMapper.writeValue(outputStream, response);
            }
        }
        return outputStream.toString();
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponse(ApiResponse response) {
        this.response = response;
    }

    public List<ChangeTriggerResponse> getChangeTriggerResponses() {
        return changeTriggerResponses;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        log.debug("store user {} in response object: ",user);
        this.user = user;
    }
}
