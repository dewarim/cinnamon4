package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.FolderTypeDao;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.folderType.CreateFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.DeleteFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.ListFolderTypeRequest;
import com.dewarim.cinnamon.model.request.folderType.UpdateFolderTypeRequest;
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

@WebServlet(name = "FolderType", urlPatterns = "/")
public class FolderTypeServlet extends HttpServlet implements CruddyServlet<FolderType> {

    private static final Logger       log       = LogManager.getLogger(AclServlet.class);
    private final        ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        FolderTypeDao    folderTypeDao    = new FolderTypeDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case FOLDER_TYPE__LIST:
                list(convertListRequest(request, ListFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
                break;
            case FOLDER_TYPE__CREATE:
                superuserCheck();
                create(convertCreateRequest(request, CreateFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
                break;
            case FOLDER_TYPE__DELETE:
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
                break;
            case FOLDER_TYPE__UPDATE:
                superuserCheck();
                update(convertUpdateRequest(request, UpdateFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }

    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }

}