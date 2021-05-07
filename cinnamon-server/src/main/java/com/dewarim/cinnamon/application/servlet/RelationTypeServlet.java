package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.relationType.ListRelationTypeRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "RelationType", urlPatterns = "/")
public class RelationTypeServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listRelationTypes":
                listRelationTypes(request, response);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listRelationTypes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest         listRequest = xmlMapper.readValue(request.getInputStream(), ListRelationTypeRequest.class);
        RelationTypeDao     typeDao     = new RelationTypeDao();
        List<RelationType>  types       = typeDao.listRelationTypes();
        RelationTypeWrapper wrapper     = new RelationTypeWrapper();
        wrapper.setRelationTypes(types);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}