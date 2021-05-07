package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.AclEntryDao;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.AclEntryListRequest;
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
public class AclEntryServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case ACL_ENTRY__LIST_ACL_ENTRIES:
                listAclEntries(request, response);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listAclEntries(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclEntryListRequest listRequest = xmlMapper.readValue(request.getInputStream(), AclEntryListRequest.class)
                .validateRequest().orElseThrow(() -> new FailedRequestException(ErrorCode.INVALID_REQUEST));

        AclEntryDao aclEntryDao = new AclEntryDao();

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

        AclEntryWrapper wrapper = new AclEntryWrapper(entries);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}