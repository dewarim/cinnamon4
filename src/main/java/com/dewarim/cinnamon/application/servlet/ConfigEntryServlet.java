package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.ConfigEntryDao;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.configEntry.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.DeleteConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.ListConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.UpdateConfigEntryRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "ConfigEntry", urlPatterns = "/")
public class ConfigEntryServlet extends HttpServlet implements CruddyServlet<ConfigEntry> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        ConfigEntryDao   configEntryDao   = new ConfigEntryDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CONFIG_ENTRY__LIST -> list(convertListRequest(request, ListConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            case CONFIG_ENTRY__GET -> getConfigEntry(request, cinnamonResponse);
            case CONFIG_ENTRY__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            }
            case CONFIG_ENTRY__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            }
            case CONFIG_ENTRY__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    @Override
    public void list(ListRequest<ConfigEntry> listRequest, CrudDao<ConfigEntry> dao, CinnamonResponse cinnamonResponse) {
        boolean isSuperuser = UserAccountDao.currentUserIsSuperuser();
        List<ConfigEntry> configEntries = dao.list().stream().filter(entry -> {
            if (entry.isPublicVisibility()) {
                return true;
            }
            return isSuperuser;
        }).collect(Collectors.toList());
        Wrapper<ConfigEntry> wrapper = new ConfigEntryWrapper(configEntries);
        cinnamonResponse.setWrapper(wrapper);
    }

    private void getConfigEntry(HttpServletRequest request, CinnamonResponse response) throws IOException {
        ConfigEntryRequest configRequest = xmlMapper.readValue(request.getInputStream(), ConfigEntryRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ConfigEntryDao configEntryDao = new ConfigEntryDao();
        List<ConfigEntry> entries;
        if (configRequest.getIds() != null && configRequest.getIds().size() > 0) {
            entries = configEntryDao.getObjectsById(configRequest.getIds());
        }
        else{
            entries = configEntryDao.getConfigEntriesByName(configRequest.getNames());
        }
        boolean isSuperuser = UserAccountDao.currentUserIsSuperuser();
        List<ConfigEntry> filtered = entries.stream().filter(entry -> {
            if (entry.isPublicVisibility()) {
                return true;
            }
            return isSuperuser;
        }).collect(Collectors.toList());
        ConfigEntryWrapper wrapper = new ConfigEntryWrapper(filtered);
        response.setWrapper(wrapper);
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}