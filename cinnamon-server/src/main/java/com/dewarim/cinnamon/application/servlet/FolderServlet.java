package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.BadArgumentException;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.FolderWrapper;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.application.ErrorResponseGenerator.generateErrorMessage;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@WebServlet(name = "Folder", urlPatterns = "/")
public class FolderServlet extends BaseServlet {

    private ObjectMapper         xmlMapper            = new XmlMapper();
    private AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        UserAccount user      = ThreadLocalSqlSession.getCurrentUser();
        FolderDao   folderDao = new FolderDao();
        try {
            switch (pathInfo) {
                case "/getFolder":
                    getFolder(request, response, user, folderDao);
                    break;
                case "/getFolderByPath":
                    getFolderByPath(request, response, user, folderDao);
                    break;
                case "/getFolders":
                    getFolders(request, response, user, folderDao);
                    break;
                case "/getMeta":
                    getMeta(request, response, user, folderDao);
                    break;
                case "/setSummary":
                    setSummary(request, response, user, folderDao);
                    break;
                case "/getSummaries":
                    getSummaries(request, response, user, folderDao);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode.getHttpResponseCode(), errorCode, e.getMessage());
        }
    }

    /**
     * Note: getMeta allows FolderMetaRequests without metaset name to return all metasets.
     * This usage is deprecated.
     * Current Cinnamon 3 clients expect the arbitrary metaset XML to be added to the DOM of the metaset wrapper.
     * This requires assembling a new DOM tree in memory for each request to getMeta, which is not something you
     * want to see with large metasets.
     */
    private void getMeta(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        MetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), MetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long   folderId = metaRequest.getId();
        Folder folder   = folderDao.getFolderById(folderId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsReadable(folder);

        List<Meta> metaList;
        if (metaRequest.getTypeNames() != null) {
            metaList = new FolderMetaDao().getMetaByNamesAndFolderId(metaRequest.getTypeNames(), folderId);
        } else {
            metaList = new FolderMetaDao().listByFolderId(folderId);
        }

        createMetaResponse(metaRequest, response, metaList, xmlMapper);
    }

    private void getFolderByPath(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderPathRequest pathRequest = xmlMapper.readValue(request.getInputStream(), FolderPathRequest.class);
        if (pathRequest.validated()) {

            List<Folder> rawFolders;
            try {
                rawFolders = folderDao.getFolderByPathWithAncestors(pathRequest.getPath(), pathRequest.isIncludeSummary());
            } catch (BadArgumentException e) {
                generateErrorMessage(response, SC_BAD_REQUEST, e.getErrorCode());
                return;
            }
            List<Folder> folders = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }


    /**
     * Retrieve a single folder, including ancestors.
     */
    private void getFolder(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SingleFolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), SingleFolderRequest.class);
        if (folderRequest.validated()) {
            List<Folder> rawFolders = folderDao.getFolderByIdWithAncestors(folderRequest.getId(), folderRequest.isIncludeSummary());
            List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * Retrieve a list of folders, without including their ancestors.
     */
    private void getFolders(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        FolderRequest folderRequest = xmlMapper.readValue(request.getInputStream(), FolderRequest.class);
        if (folderRequest.validated()) {

            List<Folder> rawFolders = folderDao.getFoldersById(folderRequest.getIds(), folderRequest.isIncludeSummary());
            List<Folder> folders    = new AuthorizationService().filterFoldersByBrowsePermission(rawFolders, user);
            if (folders.isEmpty()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }

            ResponseUtil.responseIsOkayAndXml(response);
            FolderWrapper folderWrapper = new FolderWrapper();
            folderWrapper.setFolders(folders);
            xmlMapper.writeValue(response.getWriter(), folderWrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    private void setSummary(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        Optional<Folder>  folderOpt      = folderDao.getFolderById(summaryRequest.getId());
        if (folderOpt.isPresent()) {
            Folder folder = folderOpt.get();
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.WRITE_OBJECT_SYS_METADATA.getName(), user)) {
                folder.setSummary(summaryRequest.getSummary());
                folderDao.updateFolder(folder);
                ResponseUtil.responseIsOkayAndXml(response);
                xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
                return;
            } else {
                generateErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }
        }
        generateErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
    }

    private void getSummaries(HttpServletRequest request, HttpServletResponse response, UserAccount user, FolderDao folderDao) throws IOException {
        IdListRequest  idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper wrapper       = new SummaryWrapper();
        List<Folder>   folders       = folderDao.getFoldersById(idListRequest.getIdList(), true);
        folders.forEach(folder -> {
            if (authorizationService.hasUserOrOwnerPermission(folder, DefaultPermission.READ_OBJECT_SYS_METADATA.getName(), user)) {
                wrapper.getSummaries().add(folder.getSummary());
            }
        });
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }


}
