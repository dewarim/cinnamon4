package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.UpdateRelationTypeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "RelationType", urlPatterns = "/")
public class RelationTypeServlet extends HttpServlet implements CruddyServlet<RelationType> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {


        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        RelationTypeDao  relationTypeDao  = new RelationTypeDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case RELATION_TYPE__LIST:
                list(convertListRequest(request, ListRelationTypeRequest.class), relationTypeDao, cinnamonResponse);
                break;
            case RELATION_TYPE__CREATE:
                superuserCheck();
                create(convertCreateRequest(request, CreateRelationTypeRequest.class),relationTypeDao,cinnamonResponse);
                break;
            case RELATION_TYPE__UPDATE:
                superuserCheck();
                update(convertUpdateRequest(request, UpdateRelationTypeRequest.class),relationTypeDao,cinnamonResponse);
                break;
            case RELATION_TYPE__DELETE:
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteRelationTypeRequest.class),relationTypeDao,cinnamonResponse);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}