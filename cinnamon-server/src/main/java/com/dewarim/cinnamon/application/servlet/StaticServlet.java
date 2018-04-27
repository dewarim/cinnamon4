package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.eclipse.jetty.http.MimeTypes;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 */
@WebServlet(name = "Static", urlPatterns = "/")
public class StaticServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        if (pathInfo.isEmpty() || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        // note: pathInfo seems to filter out ../ automatically, but I think it's useful to be sure.
        if (request.getRequestURI().contains("../")) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.STATIC__NO_PATH_TRAVERSAL);
            return;
        }
        handleStaticContentRequest(pathInfo, response);
    }

    private void handleStaticContentRequest(String pathInfo, HttpServletResponse response) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/static" + pathInfo);
        if (inputStream == null) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.FILE_NOT_FOUND);
            return;
        }
        String defaultMimeByExtension = MimeTypes.getDefaultMimeByExtension(pathInfo);
        response.setContentType(defaultMimeByExtension);
        switch (defaultMimeByExtension) {
            case "application/xml":
            case "text/plain":
            case "text/css":
            case "text/html":
            case "text/javascript":
                response.setCharacterEncoding("UTF-8");
                break;
            default: // do not set character encoding.
        }
        response.setStatus(HttpServletResponse.SC_OK);
        inputStream.transferTo(response.getOutputStream());
    }
}
