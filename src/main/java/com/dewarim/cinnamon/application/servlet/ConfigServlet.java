package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
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

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Config", urlPatterns = "/")
public class ConfigServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CONFIG__LIST_ALL_CONFIGURATIONS -> listAllConfigurations(request, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void listAllConfigurations(HttpServletRequest request, CinnamonResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        xmlMapper.readValue(request.getInputStream(), ListConfigRequest.class);

        ConfigWrapper wrapper = new ConfigWrapper();
        wrapper.setAcls(new AclDao().list());
        wrapper.setFolderTypes(new FolderTypeDao().list());
        wrapper.setFormats(new FormatDao().list());
        wrapper.setGroups(new GroupDao().list());
        wrapper.setIndexItems(new IndexItemDao().listIndexItems());
        wrapper.setLanguages(new LanguageDao().listLanguages());
        wrapper.setLifecycles(new LifecycleDao().listLifecycles());
        wrapper.setMetasetTypes(new MetasetTypeDao().list());
        wrapper.setObjectTypes(new ObjectTypeDao().list());
        wrapper.setPermissions(new PermissionDao().list());
        wrapper.setRelationTypes(new RelationTypeDao().list());
        wrapper.setUiLanguages(new UiLanguageDao().listUiLanguages());
        wrapper.setUsers(new UserAccountDao().listUserAccountsAsUserInfo());

        response.setWrapper(wrapper);
    }

}