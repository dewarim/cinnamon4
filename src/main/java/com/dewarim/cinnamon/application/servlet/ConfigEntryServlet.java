package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.ConfigEntryDao;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "ConfigEntry", urlPatterns = "/")
public class ConfigEntryServlet extends HttpServlet implements CruddyServlet<ConfigEntry> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CONFIG_ENTRY__GET_CONFIG_ENTRY -> getConfigEntry(request, cinnamonResponse);
            case CONFIG_ENTRY__SET_CONFIG_ENTRY -> setConfigEntry(request, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void getConfigEntry(HttpServletRequest request, CinnamonResponse response) throws IOException {
        ConfigEntryRequest    configRequest  = xmlMapper.readValue(request.getInputStream(), ConfigEntryRequest.class);
        ConfigEntryDao        configEntryDao = new ConfigEntryDao();
        Optional<ConfigEntry> entryByName    = configEntryDao.getConfigEntryByName(configRequest.getName());
        if (entryByName.isPresent()) {
            ConfigEntry entry = entryByName.get();
            if (!entry.isPublicVisibility()) {
                superuserCheck();
            }

            ConfigEntryWrapper wrapper = new ConfigEntryWrapper();
            wrapper.getConfigEntries().add(entryByName.get());
            response.setWrapper(wrapper);
        } else {
            ErrorCode.OBJECT_NOT_FOUND.throwUp();
        }
    }

    private void setConfigEntry(HttpServletRequest request, CinnamonResponse response) throws IOException {
        superuserCheck();
        CreateConfigEntryRequest creationRequest = xmlMapper.readValue(request.getInputStream(), CreateConfigEntryRequest.class);

        ConfigEntry           configEntry    = new ConfigEntry(creationRequest.getName(), creationRequest.getConfig(), creationRequest.isPublicVisibility());
        ConfigEntryDao        configEntryDao = new ConfigEntryDao();
        Optional<ConfigEntry> existingEntry  = configEntryDao.getConfigEntryByName(creationRequest.getName());
        if (existingEntry.isPresent()) {
            configEntryDao.updateConfigEntry(configEntry);
        } else {
            configEntryDao.insertConfigEntry(configEntry);
        }

        ConfigEntryWrapper wrapper = new ConfigEntryWrapper(configEntry);
        response.setWrapper(wrapper);
    }


    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}