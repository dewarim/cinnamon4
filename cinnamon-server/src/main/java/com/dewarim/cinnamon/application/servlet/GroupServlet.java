package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.GroupDao;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.group.CreateGroupRequest;
import com.dewarim.cinnamon.model.request.group.DeleteGroupRequest;
import com.dewarim.cinnamon.model.request.group.ListGroupRequest;
import com.dewarim.cinnamon.model.request.group.UpdateGroupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "Group", urlPatterns = "/")
public class GroupServlet extends HttpServlet implements CruddyServlet<Group> {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        GroupDao         groupDao         = new GroupDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case GROUP__CREATE:
                superuserCheck();
                create(convertCreateRequest(request, CreateGroupRequest.class), groupDao, cinnamonResponse);
                break;
            case GROUP__LIST:
                list(convertListRequest(request, ListGroupRequest.class), groupDao, cinnamonResponse);
                break;
            case GROUP__DELETE:
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteGroupRequest.class), groupDao, cinnamonResponse);
                break;
            case GROUP__UPDATE:
                superuserCheck();
                update(convertUpdateRequest(request, UpdateGroupRequest.class), groupDao, cinnamonResponse);
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