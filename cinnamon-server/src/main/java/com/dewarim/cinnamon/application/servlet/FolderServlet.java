package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.FolderDao;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.IdListRequest;
import com.dewarim.cinnamon.model.request.SetSummaryRequest;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "Folder", urlPatterns = "/")
public class FolderServlet extends HttpServlet {

    private ObjectMapper         xmlMapper            = new XmlMapper();
    private AuthorizationService authorizationService = new AuthorizationService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        UserAccount user      = ThreadLocalSqlSession.getCurrentUser();
        FolderDao   folderDao = new FolderDao();

        switch (pathInfo) {
            case "/setSummary":
                setSummary(request, response, user, folderDao);
                break;
            case "/getSummaries":
                getSummaries(request, response, user, folderDao);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
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
            }
            else {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }
        }
        ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
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