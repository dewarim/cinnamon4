package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.metasetType.CreateMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.DeleteMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.ListMetasetTypeRequest;
import com.dewarim.cinnamon.model.request.metasetType.UpdateMetasetTypeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "MetasetType", urlPatterns = "/")
public class MetasetTypeServlet extends HttpServlet implements CruddyServlet<MetasetType> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        MetasetTypeDao   metasetTypeDao   = new MetasetTypeDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case METASET_TYPE__LIST -> list(convertListRequest(request, ListMetasetTypeRequest.class),
                    metasetTypeDao, cinnamonResponse);
            case METASET_TYPE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateMetasetTypeRequest.class),
                        metasetTypeDao, cinnamonResponse);
            }
            case METASET_TYPE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateMetasetTypeRequest.class),
                        metasetTypeDao, cinnamonResponse);
            }
            case METASET_TYPE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteMetasetTypeRequest.class),
                        metasetTypeDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}