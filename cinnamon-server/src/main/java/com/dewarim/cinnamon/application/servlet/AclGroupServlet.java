package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.AclGroupDao;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.aclGroup.AclGroupListRequest;
import com.dewarim.cinnamon.model.request.aclGroup.CreateAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.DeleteAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.ListAclGroupRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "AclGroup", urlPatterns = "/")
public class AclGroupServlet extends HttpServlet implements CruddyServlet<AclGroup> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        AclGroupDao      aclGroupDao      = new AclGroupDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case ACL_ENTRY__LIST_ACL_ENTRIES_BY_GROUP_OR_ACL:
                listAclGroups(request, cinnamonResponse, aclGroupDao);
                break;
            case ACL_ENTRY__LIST:
                list(convertListRequest(request, ListAclGroupRequest.class), aclGroupDao, cinnamonResponse);
                break;
            case ACL_ENTRY__CREATE:
                superuserCheck();
                create(convertCreateRequest(request, CreateAclGroupRequest.class), aclGroupDao, cinnamonResponse);
                break;
            case ACL_ENTRY__DELETE:
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteAclGroupRequest.class), aclGroupDao, cinnamonResponse);
                break;
            case ACL_ENTRY__UPDATE:
                superuserCheck();
                update(convertUpdateRequest(request, UpdateAclGroupRequest.class), aclGroupDao, cinnamonResponse);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listAclGroups(HttpServletRequest request, CinnamonResponse response, AclGroupDao aclGroupDao) throws IOException {
        AclGroupListRequest listRequest = xmlMapper.readValue(request.getInputStream(), AclGroupListRequest.class)
                .validateRequest().orElseThrow(() -> new FailedRequestException(ErrorCode.INVALID_REQUEST));

        List<AclGroup> entries;
        switch (listRequest.getIdType()) {
            case ACL:
                entries = aclGroupDao.getAclGroupsByAclId(listRequest.getId());
                break;
            case GROUP:
                entries = aclGroupDao.getAclGroupsByGroupId(listRequest.getId());
                break;
            default:
                throw new FailedRequestException(ErrorCode.INVALID_ID_TYPE);
        }
        response.setWrapper(new AclGroupWrapper(entries));
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}