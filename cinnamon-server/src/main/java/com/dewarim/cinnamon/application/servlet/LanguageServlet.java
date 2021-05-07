package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Language", urlPatterns = "/")
public class LanguageServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

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
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listLanguages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest     listRequest = xmlMapper.readValue(request.getInputStream(), ListLanguageRequest.class);
        LanguageDao     languageDao = new LanguageDao();
        List<Language>  languages   = languageDao.listLanguages();
        LanguageWrapper wrapper     = new LanguageWrapper();
        wrapper.setLanguages(languages);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}