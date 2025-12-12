package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.uiLanguage.CreateUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.DeleteUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.request.uiLanguage.UpdateUiLanguageRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "UiLanguage", urlPatterns = "/")
public class UiLanguageServlet extends HttpServlet implements CruddyServlet<UiLanguage> {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UiLanguageDao    languageDao      = new UiLanguageDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {

            case UI_LANGUAGE__LIST -> list(convertListRequest(cinnamonRequest, ListUiLanguageRequest.class),
                    languageDao, cinnamonResponse);
            case UI_LANGUAGE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(cinnamonRequest, CreateUiLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            case UI_LANGUAGE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(cinnamonRequest, UpdateUiLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }
            case UI_LANGUAGE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(cinnamonRequest, DeleteUiLanguageRequest.class),
                        languageDao, cinnamonResponse);
            }

            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

}