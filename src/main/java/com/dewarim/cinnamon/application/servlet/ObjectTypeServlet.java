package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.ObjectTypeDao;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.objectType.CreateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.DeleteObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.UpdateObjectTypeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "ObjectType", urlPatterns = "/")
public class ObjectTypeServlet extends HttpServlet implements CruddyServlet<ObjectType> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        ObjectTypeDao    objectTypeDao    = new ObjectTypeDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case OBJECT_TYPE__LIST -> list(convertListRequest(request, ListObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            case OBJECT_TYPE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            }
            // TODO: add test for OBJECT_TYPE__UPDATE
            case OBJECT_TYPE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            }
            // TODO: add test for OBJECT_TYPE__DELETE
            case OBJECT_TYPE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
