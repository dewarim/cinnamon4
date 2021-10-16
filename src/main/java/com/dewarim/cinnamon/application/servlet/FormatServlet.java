package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.format.CreateFormatRequest;
import com.dewarim.cinnamon.model.request.format.DeleteFormatRequest;
import com.dewarim.cinnamon.model.request.format.ListFormatRequest;
import com.dewarim.cinnamon.model.request.format.UpdateFormatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Format", urlPatterns = "/")
public class FormatServlet extends HttpServlet implements CruddyServlet<Format> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        FormatDao        formatDao        = new FormatDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case FORMAT__LIST -> list(convertListRequest(request, ListFormatRequest.class), formatDao, cinnamonResponse);
            case FORMAT__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateFormatRequest.class), formatDao, cinnamonResponse);
            }
            case FORMAT__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateFormatRequest.class), formatDao, cinnamonResponse);
            }
            case FORMAT__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteFormatRequest.class), formatDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
