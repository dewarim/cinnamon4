package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.format.CreateFormatRequest;
import com.dewarim.cinnamon.model.request.format.DeleteFormatRequest;
import com.dewarim.cinnamon.model.request.format.ListFormatRequest;
import com.dewarim.cinnamon.model.request.format.UpdateFormatRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "Format", urlPatterns = "/")
public class FormatServlet extends HttpServlet implements CruddyServlet<Format> {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        FormatDao        formatDao        = new FormatDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case FORMAT__LIST ->
                    list(convertListRequest(cinnamonRequest, ListFormatRequest.class), formatDao, cinnamonResponse);
            case FORMAT__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(cinnamonRequest, CreateFormatRequest.class), formatDao, cinnamonResponse);
            }
            case FORMAT__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(cinnamonRequest, UpdateFormatRequest.class), formatDao, cinnamonResponse);
            }
            case FORMAT__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(cinnamonRequest, DeleteFormatRequest.class), formatDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

}
