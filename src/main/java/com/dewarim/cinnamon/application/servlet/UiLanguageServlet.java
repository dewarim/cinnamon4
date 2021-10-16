package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "UiLanguage", urlPatterns = "/")
public class UiLanguageServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case UI_LANGUAGE__LIST -> listUiLanguages(request, response);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listUiLanguages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest       listRequest = xmlMapper.readValue(request.getInputStream(), ListUiLanguageRequest.class);
        UiLanguageDao     languageDao = new UiLanguageDao();
        List<UiLanguage>  languages   = languageDao.listUiLanguages();
        UiLanguageWrapper wrapper     = new UiLanguageWrapper();
        wrapper.setUiLanguages(languages);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}