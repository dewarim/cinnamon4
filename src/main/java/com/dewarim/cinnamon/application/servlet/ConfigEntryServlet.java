package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.ConfigEntryDao;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.configEntry.*;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@WebServlet(name = "ConfigEntry", urlPatterns = "/")
public class ConfigEntryServlet extends HttpServlet implements CruddyServlet<ConfigEntry> {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest cinnamonRequest = (CinnamonRequest) request;

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        ConfigEntryDao   configEntryDao   = new ConfigEntryDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CONFIG_ENTRY__LIST -> list(convertListRequest(cinnamonRequest, ListConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            case CONFIG_ENTRY__GET -> getConfigEntry(cinnamonRequest, cinnamonResponse);
            case CONFIG_ENTRY__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(cinnamonRequest, CreateConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            }
            case CONFIG_ENTRY__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(cinnamonRequest, UpdateConfigEntryRequest.class), configEntryDao, cinnamonResponse);
            }
            case CONFIG_ENTRY__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(cinnamonRequest, DeleteConfigEntryRequest.class), configEntryDao, cinnamonResponse);
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

    private void getConfigEntry(CinnamonRequest request, CinnamonResponse response) throws IOException {
        ConfigEntryRequest configRequest = request.getMapper().readValue(request.getInputStream(), ConfigEntryRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ConfigEntryDao configEntryDao = new ConfigEntryDao();
        List<ConfigEntry> entries = configEntryDao.getObjectsById(configRequest.getIds());
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


}