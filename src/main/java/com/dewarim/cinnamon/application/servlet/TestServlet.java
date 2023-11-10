package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.service.index.ParamParser;
import com.dewarim.cinnamon.model.response.GenericResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;


/**
 *
 */
@WebServlet(name = "Test", urlPatterns = "/")
public class TestServlet extends HttpServlet {

    private static final Logger log = LogManager.getLogger(TestServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        generateResponse(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        generateResponse(request, response);
    }

    private void generateResponse(HttpServletRequest request, HttpServletResponse response)  {
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case TEST__ECHO -> echo(request, cinnamonResponse);
            case TEST__STATUS_200 -> returnStatus(cinnamonResponse, 200, "OK");
            case TEST__STATUS_400 -> returnStatus(cinnamonResponse, 400, "NOT OK");
            case TEST__BOOM -> throw new RuntimeException("testing error handling!");
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void echo(HttpServletRequest request, CinnamonResponse cinnamonResponse) {
        try {
            String input = new String(request.getInputStream().readAllBytes());
            ParamParser.parseXml(input, "Input is not well-formed XML.");
            cinnamonResponse.setStatusCode(HttpServletResponse.SC_OK);
            cinnamonResponse.setContentType(CONTENT_TYPE_XML);
            cinnamonResponse.setCharacterEncoding("UTF-8");
            cinnamonResponse.getWriter().write(input);
        }
        catch (Exception e){
            throw ErrorCode.INVALID_REQUEST.exception();
        }
    }

    private void returnStatus(CinnamonResponse response, int status, String message) {
        response.setStatusCode(status);
        GenericResponse genericResponse = new GenericResponse(status == HttpStatus.OK_200);
        genericResponse.setMessage(message);
        response.setResponse(genericResponse);
    }

}
