package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.relationType.CreateRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.DeleteRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.request.relationType.UpdateRelationTypeRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "RelationType", urlPatterns = "/")
public class RelationTypeServlet extends HttpServlet implements CruddyServlet<RelationType> {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {


        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        RelationTypeDao  relationTypeDao  = new RelationTypeDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case RELATION_TYPE__LIST ->
                    list(convertListRequest(cinnamonRequest, ListRelationTypeRequest.class), relationTypeDao, cinnamonResponse);
            case RELATION_TYPE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(cinnamonRequest, CreateRelationTypeRequest.class), relationTypeDao, cinnamonResponse);
            }
            case RELATION_TYPE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(cinnamonRequest, UpdateRelationTypeRequest.class), relationTypeDao, cinnamonResponse);
            }
            case RELATION_TYPE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(cinnamonRequest, DeleteRelationTypeRequest.class), relationTypeDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }


}