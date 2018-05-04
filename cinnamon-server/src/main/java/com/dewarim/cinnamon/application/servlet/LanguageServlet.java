package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Language", urlPatterns = "/")
public class LanguageServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listLanguages":
                listLanguages(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void listLanguages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest         listRequest = xmlMapper.readValue(request.getInputStream(), ListRequest.class);
        LanguageDao     typeDao     = new LanguageDao();
        List<Language>  types       = typeDao.listLanguages();
        LanguageWrapper wrapper     = new LanguageWrapper();
        wrapper.setLanguages(types);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}