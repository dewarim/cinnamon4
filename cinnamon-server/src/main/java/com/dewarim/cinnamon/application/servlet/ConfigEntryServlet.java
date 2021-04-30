package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.ConfigEntryDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "ConfigEntry", urlPatterns = "/")
public class ConfigEntryServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        try {
            switch (pathInfo) {
                case "/getConfigEntry":
                    getConfigEntry(request, response);
                    break;
                case "/setConfigEntry":
                    setConfigEntry(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode, e.getMessage());
        }
    }

    private void getConfigEntry(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ConfigEntryRequest    configRequest  = xmlMapper.readValue(request.getInputStream(), ConfigEntryRequest.class);
        ConfigEntryDao        configEntryDao = new ConfigEntryDao();
        Optional<ConfigEntry> entryByName    = configEntryDao.getConfigEntryByName(configRequest.getName());
        if (entryByName.isPresent()) {
            ConfigEntry entry = entryByName.get();
            if (!entry.isPublicVisibility() && !callerIsSuperuser()) {
                ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.UNAUTHORIZED);
                return;
            }

            ConfigEntryWrapper wrapper = new ConfigEntryWrapper();
            wrapper.getConfigEntries().add(entryByName.get());
            response.setContentType(CONTENT_TYPE_XML);
            response.setStatus(HttpServletResponse.SC_OK);
            xmlMapper.writeValue(response.getWriter(), wrapper);
        } else {
            ErrorCode.OBJECT_NOT_FOUND.throwUp();
        }
    }

    private void setConfigEntry(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!callerIsSuperuser()) {
            ErrorCode.UNAUTHORIZED.throwUp();
        }
        CreateConfigEntryRequest creationRequest = xmlMapper.readValue(request.getInputStream(), CreateConfigEntryRequest.class);

        ConfigEntry           configEntry    = new ConfigEntry(creationRequest.getName(), creationRequest.getConfig(), creationRequest.isPublicVisibility());
        ConfigEntryDao        configEntryDao = new ConfigEntryDao();
        Optional<ConfigEntry> existingEntry  = configEntryDao.getConfigEntryByName(creationRequest.getName());
        if (existingEntry.isPresent()) {
            configEntryDao.updateConfigEntry(configEntry);
        } else {
            configEntryDao.insertConfigEntry(configEntry);
        }

        GenericResponse genericResponse = new GenericResponse(true);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), genericResponse);
    }

    private boolean callerIsSuperuser() {
        UserAccount    userAccount = ThreadLocalSqlSession.getCurrentUser();
        UserAccountDao userDao     = new UserAccountDao();
        return userDao.isSuperuser(userAccount);
    }

}