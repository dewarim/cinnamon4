package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.language.CreateLanguageRequest;
import com.dewarim.cinnamon.model.request.language.DeleteLanguageRequest;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.request.language.UpdateLanguageRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "Language", urlPatterns = "/")
public class LanguageServlet extends HttpServlet implements CruddyServlet<Language> {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LanguageDao             languageDao     = new LanguageDao();
        CinnamonRequest cinnamonRequest = (CinnamonRequest) request;

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case LANGUAGE__LIST -> list(convertListRequest(cinnamonRequest, ListLanguageRequest.class),
                    languageDao, cinnamonResponse);
            case LANGUAGE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(cinnamonRequest, CreateLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            case LANGUAGE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(cinnamonRequest, UpdateLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            case LANGUAGE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(cinnamonRequest, DeleteLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }


}