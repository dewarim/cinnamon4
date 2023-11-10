package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.ProviderClass;
import com.dewarim.cinnamon.model.ProviderType;
import com.dewarim.cinnamon.model.UrlMappingInfo;
import com.dewarim.cinnamon.model.request.config.ListConfigRequest;
import com.dewarim.cinnamon.model.response.ConfigWrapper;
import com.dewarim.cinnamon.model.response.UrlMappingInfoWrapper;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.provider.StateProviderService;
import com.dewarim.cinnamon.security.LoginProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.ErrorCode.NEED_EXTERNAL_LOGGING_CONFIG;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "Config", urlPatterns = "/")
public class ConfigServlet extends HttpServlet {
    private static final Logger log = LogManager.getLogger(CinnamonServlet.class);

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CONFIG__LIST_ALL_CONFIGURATIONS -> listAllConfigurations(request, cinnamonResponse);
            case CONFIG__URL_MAPPINGS -> listUrlMappings(request,cinnamonResponse);
            case CONFIG__RELOAD_LOGGING -> reloadLogging(request, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void reloadLogging(HttpServletRequest request, CinnamonResponse cinnamonResponse) {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            throw ErrorCode.REQUIRES_SUPERUSER_STATUS.getException().get();
        }
        String log4jConfigPath = CinnamonServer.config.getServerConfig().getLog4jConfigPath();
        if(log4jConfigPath.isEmpty()) {
            log.info("will not reload logging without valid config");
            cinnamonResponse.generateErrorMessage(HttpServletResponse.SC_BAD_REQUEST, ErrorCode.NEED_EXTERNAL_LOGGING_CONFIG, NEED_EXTERNAL_LOGGING_CONFIG.getDescription());
        }
        else {
            log.info("reconfigure logging to use: "+log4jConfigPath);
            Configurator.reconfigure(URI.create(log4jConfigPath));
            cinnamonResponse.responseIsGenericOkay();
        }
    }

    private void listUrlMappings(HttpServletRequest request, CinnamonResponse cinnamonResponse) {
        List<UrlMappingInfo> urlMappingInfos = Arrays.stream(UrlMapping.values()).toList().stream()
                .map(urlMapping -> new UrlMappingInfo(urlMapping.getServlet(), urlMapping.getAction(), urlMapping.getPath(), urlMapping.getDescription()))
                        .collect(Collectors.toList());
        cinnamonResponse.setWrapper(new UrlMappingInfoWrapper(urlMappingInfos));
    }

    private void listAllConfigurations(HttpServletRequest request, CinnamonResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        xmlMapper.readValue(request.getInputStream(), ListConfigRequest.class);

        ConfigWrapper wrapper = new ConfigWrapper();
        wrapper.setAcls(new AclDao().list());
        wrapper.setFolderTypes(new FolderTypeDao().list());
        wrapper.setFormats(new FormatDao().list());
        wrapper.setGroups(new GroupDao().list());
        wrapper.setIndexItems(new IndexItemDao().list());
        wrapper.setLanguages(new LanguageDao().list());
        wrapper.setLifecycles(new LifecycleDao().list());
        wrapper.setMetasetTypes(new MetasetTypeDao().list());
        wrapper.setObjectTypes(new ObjectTypeDao().list());
        wrapper.setPermissions(new PermissionDao().list());
        wrapper.setRelationTypes(new RelationTypeDao().list());
        wrapper.setUiLanguages(new UiLanguageDao().list());
        wrapper.setUsers(new UserAccountDao().list());
        wrapper.setProviderClasses(generateProviderClassList());
        response.setWrapper(wrapper);
    }

    private List<ProviderClass> generateProviderClassList() {
        List<ProviderClass> providerClasses = new ArrayList<>();
        providerClasses.addAll(ContentProviderService.getInstance().getProviderList().stream()
                .map(provider -> new ProviderClass(ProviderType.CONTENT_PROVIDER, provider.getName()))
                .toList());
        providerClasses.addAll(StateProviderService.getInstance().getProviderList().stream()
                .map(provider -> new ProviderClass(ProviderType.STATE_PROVIDER, provider.getName()))
                .toList());
        providerClasses.addAll(LoginProviderService.getInstance().getProviderList().stream()
                .map(provider -> new ProviderClass(ProviderType.LOGIN_PROVIDER, provider.getName()))
                .toList());
        return providerClasses;
    }

}