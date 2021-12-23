package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;


@WebServlet(name = "Relation", urlPatterns = "/")
public class RelationServlet extends HttpServlet implements CruddyServlet<Relation> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        RelationDao      relationDao      = new RelationDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case RELATION__SEARCH -> searchRelations(request, relationDao, cinnamonResponse);
            case RELATION__CREATE -> {
                CreateRequest<Relation> createRequest = convertCreateRequest(request, CreateRelationRequest.class);
                RelationTypeDao         rtDao         = new RelationTypeDao();
                var                     typesExist    = rtDao.verifyAllObjectsFromSetExist(createRequest.list().stream().map(Relation::getTypeId).collect(Collectors.toList()));
                if (!typesExist) {
                    ErrorCode.RELATION_TYPE_NOT_FOUND.throwUp();
                }
                create(createRequest, relationDao, cinnamonResponse);
            }
            case RELATION__DELETE -> deleteRelation(request, relationDao, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void deleteRelation(HttpServletRequest request, RelationDao relationDao, CinnamonResponse response) throws IOException {
        DeleteRelationRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteRelationRequest.class);
        if (deleteRequest.validated()) {
            int affectedRows = relationDao.deleteRelation(deleteRequest.getLeftId(), deleteRequest.getRightId(), deleteRequest.getTypeName());
            switch (affectedRows) {
                case 0 -> ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND_OR_GONE);
                case 1 -> response.responseIsGenericOkay();
                default -> ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.DELETE_AFFECTED_MULTIPLE_ROWS);
            }
            return;
        }
        ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
    }

    private void searchRelations(HttpServletRequest request, RelationDao relationDao, CinnamonResponse response) throws IOException {
        SearchRelationRequest relationRequest = xmlMapper.readValue(request.getInputStream(), SearchRelationRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Relation> relations;
        if(relationRequest.isOrMode()) {
            relations = relationDao.getRelationsOrMode(relationRequest.getLeftIds(), relationRequest.getRightIds(),
                    relationRequest.getNames(), relationRequest.isIncludeMetadata());
        }
        else{
            relations = relationDao.getRelations(relationRequest.getLeftIds(), relationRequest.getRightIds(),
                    relationRequest.getNames(), relationRequest.isIncludeMetadata());
        }
        RelationWrapper wrapper = new RelationWrapper(relations);
        response.setWrapper(wrapper);
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}