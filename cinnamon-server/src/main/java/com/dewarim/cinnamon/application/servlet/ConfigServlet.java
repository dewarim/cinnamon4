package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.CmnGroupDao;
import com.dewarim.cinnamon.dao.FolderTypeDao;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.dao.IndexItemDao;
import com.dewarim.cinnamon.dao.LanguageDao;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.ObjectTypeDao;
import com.dewarim.cinnamon.dao.PermissionDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ConfigWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Config", urlPatterns = "/")
public class ConfigServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

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
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void listAllConfigurations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest listRequest = xmlMapper.readValue(request.getInputStream(), ListRequest.class);

        ConfigWrapper wrapper = new ConfigWrapper();
        wrapper.setAcls(new AclDao().list());
        wrapper.setFolderTypes(new FolderTypeDao().listFolderTypes());
        wrapper.setFormats(new FormatDao().listFormats());
        wrapper.setGroups(new CmnGroupDao().listGroups());
        wrapper.setIndexItems(new IndexItemDao().listIndexItems());
        wrapper.setLanguages(new LanguageDao().listLanguages());
        wrapper.setLifecycles(new LifecycleDao().listLifecycles());
        wrapper.setMetasetTypes(new MetasetTypeDao().listMetasetTypes());
        wrapper.setObjectTypes(new ObjectTypeDao().listObjectTypes());
        wrapper.setPermissions(new PermissionDao().listPermissions());
        wrapper.setRelationTypes(new RelationTypeDao().listRelationTypes());
        wrapper.setUiLanguages(new UiLanguageDao().listUiLanguages());
        wrapper.setUsers(new UserAccountDao().listUserAccountsAsUserInfo());

        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}