package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.index.CreateIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.DeleteIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.ListIndexItemRequest;
import com.dewarim.cinnamon.model.request.index.UpdateIndexItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "IndexItem", urlPatterns = "/")
public class IndexItemServlet extends HttpServlet implements CruddyServlet<IndexItem>{

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        IndexItemDao indexItemDao = new IndexItemDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case INDEX_ITEM__LIST -> list(convertListRequest(request, ListIndexItemRequest.class), indexItemDao, cinnamonResponse);
            case INDEX_ITEM__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateIndexItemRequest.class), indexItemDao, cinnamonResponse);
            }
            case INDEX_ITEM__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateIndexItemRequest.class), indexItemDao, cinnamonResponse);
            }
            case INDEX_ITEM__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteIndexItemRequest.class), indexItemDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}