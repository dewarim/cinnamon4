package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.language.CreateLanguageRequest;
import com.dewarim.cinnamon.model.request.language.DeleteLanguageRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.request.language.UpdateLanguageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Language", urlPatterns = "/")
public class LanguageServlet extends HttpServlet implements CruddyServlet<Language> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LanguageDao      languageDao      = new LanguageDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case LANGUAGE__LIST -> list(convertListRequest(request, ListLanguageRequest.class),
                    languageDao, cinnamonResponse);
            case LANGUAGE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            case LANGUAGE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            case LANGUAGE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}