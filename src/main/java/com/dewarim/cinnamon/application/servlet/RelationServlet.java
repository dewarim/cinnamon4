package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.RelationRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;


@WebServlet(name = "Relation", urlPatterns = "/")
public class RelationServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        RelationDao      relationDao      = new RelationDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case RELATION__LIST:
                listRelations(request, relationDao, cinnamonResponse);
                break;
            case RELATION__CREATE:
                createRelation(request, relationDao, cinnamonResponse);
                break;
            case RELATION__DELETE:
                deleteRelation(request, relationDao, cinnamonResponse);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void deleteRelation(HttpServletRequest request, RelationDao relationDao, HttpServletResponse response) throws IOException {
        DeleteRelationRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteRelationRequest.class);
        if (deleteRequest.validated()) {
            int affectedRows = relationDao.deleteRelation(deleteRequest.getLeftId(), deleteRequest.getRightId(), deleteRequest.getTypeName());
            switch (affectedRows) {
                case 0:
                    ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND_OR_GONE);
                    break;
                case 1:
                    // TODO: use CinnamonResponse
                    ResponseUtil.responseIsOkayAndXml(response);
                    xmlMapper.writeValue(response.getWriter(), new GenericResponse(false));
                    break;
                default:
                    ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.DELETE_AFFECTED_MULTIPLE_ROWS);
            }
            return;
        }
        ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
    }

    private void createRelation(HttpServletRequest request, RelationDao relationDao, CinnamonResponse response) throws IOException {
        CreateRelationRequest createRequest = xmlMapper.readValue(request.getInputStream(), CreateRelationRequest.class);
        if (createRequest.validated()) {
            Optional<RelationType> relationTypeOpt = new RelationTypeDao().getRelationTypeByName(createRequest.getTypeName());
            if (relationTypeOpt.isPresent()) {
                List<Relation> newRelation = List.of(new Relation(createRequest.getLeftId(), createRequest.getRightId(), relationTypeOpt.get().getId(), createRequest.getMetadata()));
                relationDao.create(newRelation);
                var relationWrapper = new RelationWrapper(newRelation);
                response.setWrapper(relationWrapper);
                return;
            } else {
                ErrorCode.RELATION_TYPE_NOT_FOUND.throwUp();
            }
            return;
        }
        ErrorCode.INVALID_REQUEST.throwUp();
    }

    private void listRelations(HttpServletRequest request, RelationDao relationDao, CinnamonResponse response) throws IOException {
        RelationRequest relationRequest = xmlMapper.readValue(request.getInputStream(), RelationRequest.class);
        if (relationRequest.validated()) {
            List<Relation> relations = relationDao.getRelations(relationRequest.getLeftIds(), relationRequest.getRightIds(),
                    relationRequest.getNames(), relationRequest.isIncludeMetadata());
            RelationWrapper wrapper = new RelationWrapper(relations);
            response.setWrapper(wrapper);
            return;
        }
        ErrorCode.INVALID_REQUEST.throwUp();

    }

}