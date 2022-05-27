package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.model.response.GenericResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;


/**
 *
 */
@WebServlet(name = "Test", urlPatterns = "/")
public class TestServlet extends HttpServlet {

    private static final Logger log = LogManager.getLogger(TestServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        generateResponse(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        generateResponse(request, response);
    }

    private void generateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        log.debug("Last-Insert-ID: "+request.getHeader("cinnamon-last-insert-id"));
        switch (mapping) {
            case TEST__STATUS_200 -> returnStatus(cinnamonResponse, 200, "OK");
            case TEST__STATUS_400 -> returnStatus(cinnamonResponse, 400, "NOT OK");
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void returnStatus(CinnamonResponse response, int status, String message) {
        response.setStatusCode(status);
        GenericResponse genericResponse = new GenericResponse(status == HttpStatus.OK_200);
        genericResponse.setMessage(message);
        response.setGenericResponse(genericResponse);
    }

}
