package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.RelationRequest;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@WebServlet(name = "Relation", urlPatterns = "/")
public class RelationServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/getRelations":
                listRelations(request, response);
                break;
            case "/createRelation":
                createRelation(request, response);
                break;
            case "/deleteRelation":
                deleteRelation(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void deleteRelation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DeleteRelationRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteRelationRequest.class);
        if (deleteRequest.validated()) {
            RelationDao dao          = new RelationDao();
            int         affectedRows = dao.deleteRelation(deleteRequest.getLeftId(), deleteRequest.getRightId(), deleteRequest.getTypeName());
            switch (affectedRows) {
                case 0:
                    ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND_OR_GONE);
                    break;
                case 1:
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

    private void createRelation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CreateRelationRequest createRequest = xmlMapper.readValue(request.getInputStream(), CreateRelationRequest.class);
        if (createRequest.validated()) {
            Optional<RelationType> relationTypeOpt = new RelationTypeDao().getRelationTypeByName(createRequest.getTypeName());
            if (relationTypeOpt.isPresent()) {
                RelationDao relationDao = new RelationDao();
                Relation    newRelation = new Relation(createRequest.getLeftId(), createRequest.getRightId(), relationTypeOpt.get().getId(), createRequest.getMetadata());
                relationDao.createRelation(newRelation);
                ResponseUtil.responseIsOkayAndXml(response);
                xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
                return;
            } else {
                ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.RELATION_TYPE_NOT_FOUND);
            }
            return;
        }
        ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
    }

    private void listRelations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RelationRequest relationRequest = xmlMapper.readValue(request.getInputStream(), RelationRequest.class);
        if (relationRequest.validated()) {
            RelationDao relationDao = new RelationDao();
            List<Relation> types = relationDao.getRelations(relationRequest.getLeftIds(), relationRequest.getRightIds(),
                    relationRequest.getNames(), relationRequest.isIncludeMetadata());
            RelationWrapper wrapper = new RelationWrapper();
            wrapper.setRelations(types);
            ResponseUtil.responseIsOkayAndXml(response);
            xmlMapper.writeValue(response.getWriter(), wrapper);
            return;
        }
        ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);

    }

}