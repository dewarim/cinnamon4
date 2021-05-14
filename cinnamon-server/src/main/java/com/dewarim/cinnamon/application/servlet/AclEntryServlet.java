package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.AclEntryDao;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.aclEntry.AclEntryListRequest;
import com.dewarim.cinnamon.model.request.aclEntry.CreateAclEntryRequest;
import com.dewarim.cinnamon.model.request.aclEntry.DeleteAclEntryRequest;
import com.dewarim.cinnamon.model.request.aclEntry.ListAclEntryRequest;
import com.dewarim.cinnamon.model.request.aclEntry.UpdateAclEntryRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AclEntry", urlPatterns = "/")
public class AclEntryServlet extends HttpServlet implements CruddyServlet<AclEntry> {

    private final ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        AclEntryDao      aclEntryDao      = new AclEntryDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case ACL_ENTRY__LIST_ACL_ENTRIES_BY_GROUP_OR_ACL:
                listAclEntries(request, cinnamonResponse, aclEntryDao);
                break;
            case ACL_ENTRY__LIST:
                list(convertListRequest(request, ListAclEntryRequest.class), aclEntryDao, cinnamonResponse);
                break;
            case ACL_ENTRY__CREATE:
                superuserCheck();
                create(convertCreateRequest(request, CreateAclEntryRequest.class), aclEntryDao, cinnamonResponse);
                break;
            case ACL_ENTRY__DELETE:
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteAclEntryRequest.class), aclEntryDao, cinnamonResponse);
                break;
            case ACL_ENTRY__UPDATE:
                superuserCheck();
                update(convertUpdateRequest(request, UpdateAclEntryRequest.class), aclEntryDao, cinnamonResponse);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listAclEntries(HttpServletRequest request, CinnamonResponse response, AclEntryDao aclEntryDao) throws IOException {
        AclEntryListRequest listRequest = xmlMapper.readValue(request.getInputStream(), AclEntryListRequest.class)
                .validateRequest().orElseThrow(() -> new FailedRequestException(ErrorCode.INVALID_REQUEST));

        List<AclEntry> entries;
        switch (listRequest.getIdType()) {
            case ACL:
                entries = aclEntryDao.getAclEntriesByAclId(listRequest.getId());
                break;
            case GROUP:
                entries = aclEntryDao.getAclEntriesByGroupId(listRequest.getId());
                break;
            default:
                throw new FailedRequestException(ErrorCode.INVALID_ID_TYPE);
        }
        response.setWrapper(new AclEntryWrapper(entries));
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}