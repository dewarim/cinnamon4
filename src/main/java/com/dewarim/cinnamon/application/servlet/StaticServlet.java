package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;

import java.io.IOException;
import java.io.InputStream;


/**
 */
@WebServlet(name = "Static", urlPatterns = "/")
public class StaticServlet extends HttpServlet {

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
            ErrorCode.STATIC__NO_PATH_TRAVERSAL.throwUp();
        }
        handleStaticContentRequest(pathInfo, request, response);
    }

    private void handleStaticContentRequest(String pathInfo, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/static" + pathInfo)) {
            if (inputStream == null) {
                ErrorResponseGenerator.generateErrorMessage(request, response, ErrorCode.FILE_NOT_FOUND);
                return;
            }
            String defaultMimeByExtension = MimeTypes.getDefaultMimeByExtension(pathInfo);
            response.setContentType(defaultMimeByExtension);
            switch (defaultMimeByExtension) {
                case "application/xml", "text/plain", "text/css", "text/html", "text/javascript" ->
                        response.setCharacterEncoding("UTF-8");
                default -> {
                } // do not set character encoding.
            }
            response.setStatus(HttpServletResponse.SC_OK);
            inputStream.transferTo(response.getOutputStream());
        }
    }
}
