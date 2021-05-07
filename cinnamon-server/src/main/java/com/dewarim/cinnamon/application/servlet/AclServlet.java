package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.acl.AclInfoRequest;
import com.dewarim.cinnamon.model.request.acl.CreateAclRequest;
import com.dewarim.cinnamon.model.request.acl.DeleteAclRequest;
import com.dewarim.cinnamon.model.request.acl.ListAclRequest;
import com.dewarim.cinnamon.model.request.acl.UpdateAclRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Acl", urlPatterns = "/")
public class AclServlet extends HttpServlet implements CruddyServlet<Acl> {

    private static final Logger       log       = LogManager.getLogger(AclServlet.class);
    private final        ObjectMapper xmlMapper = new XmlMapper()
            .configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        CrudDao<Acl>     aclDao           = new AclDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case ACL__CREATE:
                superuserCheck();
                create(convertCreateRequest(request, CreateAclRequest.class), aclDao, cinnamonResponse);
                break;
            case ACL__ACL_INFO:
                getAclByNameOrId(request, response);
                break;
            case ACL__DELETE:
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteAclRequest.class), aclDao, cinnamonResponse);
                break;
            case ACL__LIST:
                list(convertListRequest(request, ListAclRequest.class), aclDao, cinnamonResponse);
                break;
            case ACL__GET_USER_ACLS:
                getUserAcls(request, response);
                break;
            case ACL__UPDATE:
                superuserCheck();
                update(convertUpdateRequest(request, UpdateAclRequest.class), aclDao, cinnamonResponse);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }

    }

    private void getAclByNameOrId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AclInfoRequest aclInfoRequest = xmlMapper.readValue(request.getInputStream(), AclInfoRequest.class);
        AclDao         aclDao         = new AclDao();
        Acl            acl;
        if (aclInfoRequest.byId()) {
            acl = aclDao.getAclById(aclInfoRequest.getAclId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException());
        } else if (aclInfoRequest.byName()) {
            acl = aclDao.getAclByName(aclInfoRequest.getName());
        } else {
            ErrorCode.INFO_REQUEST_WITHOUT_NAME_OR_ID.throwUp();
            return;
        }
        sendWrappedAcls(response, Collections.singletonList(acl));
    }

    private void getUserAcls(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        Long      userId    = idRequest.getId();
        if (userId == null || userId < 1) {
            ErrorCode.ID_PARAM_IS_INVALID.throwUp();
        }
        AclDao    aclDao   = new AclDao();
        List<Acl> userAcls = aclDao.getUserAcls(userId);
        sendWrappedAcls(response, userAcls);
    }

    private void sendWrappedAcls(HttpServletResponse response, List<Acl> acls) throws IOException {
        AclWrapper aclWrapper = new AclWrapper();
        aclWrapper.getAcls().addAll(acls);
        aclWrapper.getAcls().removeAll(Collections.singleton(null));
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), aclWrapper);
    }

    public ObjectMapper getMapper() {
        // TODO: use mapper according to request contentType (XML, JSON)
        return xmlMapper;
    }
}
