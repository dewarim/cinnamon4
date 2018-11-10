package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.dao.AclEntryDao;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.AclEntryListRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AclEntry", urlPatterns = "/")
public class AclEntryServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        try {
            switch (pathInfo) {
                case "/listAclEntries":
                    listAclEntries(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode.getHttpResponseCode(), errorCode, e.getMessage());
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