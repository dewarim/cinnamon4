package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.FolderTypeDao;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.dao.GroupDao;
import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.ObjectTypeDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.request.config.ListConfigRequest;
import com.dewarim.cinnamon.model.response.ConfigWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Config", urlPatterns = "/")
public class ConfigServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listAllConfigurations":
                listAllConfigurations(request, response);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listAllConfigurations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        xmlMapper.readValue(request.getInputStream(), ListConfigRequest.class);

        ConfigWrapper wrapper = new ConfigWrapper();
        wrapper.setAcls(new AclDao().list());
        wrapper.setFolderTypes(new FolderTypeDao().list());
        wrapper.setFormats(new FormatDao().listFormats());
        wrapper.setGroups(new GroupDao().list());
        wrapper.setIndexItems(new IndexItemDao().listIndexItems());
        wrapper.setLanguages(new LanguageDao().listLanguages());
        wrapper.setLifecycles(new LifecycleDao().listLifecycles());
        wrapper.setMetasetTypes(new MetasetTypeDao().listMetasetTypes());
        wrapper.setObjectTypes(new ObjectTypeDao().listObjectTypes());
        wrapper.setPermissions(new PermissionDao().list());
        wrapper.setRelationTypes(new RelationTypeDao().listRelationTypes());
        wrapper.setUiLanguages(new UiLanguageDao().listUiLanguages());
        wrapper.setUsers(new UserAccountDao().listUserAccountsAsUserInfo());

        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}