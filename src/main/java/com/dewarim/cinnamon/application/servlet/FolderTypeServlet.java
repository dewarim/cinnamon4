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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "FolderType", urlPatterns = "/")
public class FolderTypeServlet extends HttpServlet implements CruddyServlet<FolderType> {

    private final        ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        FolderTypeDao    folderTypeDao    = new FolderTypeDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case FOLDER_TYPE__LIST -> list(convertListRequest(request, ListFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
            case FOLDER_TYPE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
            }
            case FOLDER_TYPE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
            }
            case FOLDER_TYPE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateFolderTypeRequest.class), folderTypeDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }

    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }

}