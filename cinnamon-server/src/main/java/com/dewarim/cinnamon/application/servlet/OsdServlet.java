package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.FormatDao;
import com.dewarim.cinnamon.dao.LinkDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.servlet.http.HttpServletResponse.*;

@MultipartConfig
@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends HttpServlet {

    private              ObjectMapper         xmlMapper            = new XmlMapper();
    private              AuthorizationService authorizationService = new AuthorizationService();
    private static final Logger               log                  = LogManager.getLogger(OsdServlet.class);
    private static final String               MULTIPART            = "multipart/";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        UserAccount user   = ThreadLocalSqlSession.getCurrentUser();
        OsdDao      osdDao = new OsdDao();

        switch (pathInfo) {
            case "/getContent":
                getContent(request, response, user, osdDao);
                break;
            case "/getObjectsByFolderId":
                getObjectsByFolderId(request, response, user, osdDao);
                break;
            case "/getObjectsById":
                getObjectsById(request, response, user, osdDao);
                break;
            case "/getSummaries":
                getSummaries(request, response, user, osdDao);
                break;
            case "/lock":
                lock(request, response, user, osdDao);
                break;
            case "/setContent":
                setContent(request, response, user, osdDao);
                break;
            case "/setSummary":
                setSummary(request, response, user, osdDao);
                break;
            case "/unlock":
                unlock(request, response, user, osdDao);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    private void lock(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        if (idRequest.validated()) {
            Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(idRequest.getId());
            if (!osdOpt.isPresent()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            ObjectSystemData osd         = osdOpt.get();
            boolean          lockAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.LOCK, user);
            if (!lockAllowed) {
                generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.NO_LOCK_PERMISSION);
                return;
            }
            Long lockHolder = osd.getLockerId();
            if (lockHolder != null) {
                if (lockHolder.equals(user.getId())) {
                    // trying to lock your own object: NOP
                    ResponseUtil.responseIsGenericOkay(response);
                    return;
                } else {
                    generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.OBJECT_LOCKED_BY_OTHER_USER);
                    return;
                }
            }
            osd.setLockerId(user.getId());
            osdDao.updateOsd(osd);
            ResponseUtil.responseIsOkayAndXml(response);
            xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }


    private void unlock(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        if (idRequest.validated()) {
            Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(idRequest.getId());
            if (!osdOpt.isPresent()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            ObjectSystemData osd           = osdOpt.get();
            boolean          unlockAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.LOCK, user);
            if (!unlockAllowed) {
                generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.NO_LOCK_PERMISSION);
                return;
            }

            Long lockHolder = osd.getLockerId();
            if (lockHolder != null) {
                UserAccountDao userDao = new UserAccountDao();
                // superuser may remove locks from other users.
                if (lockHolder.equals(user.getId()) || userDao.isSuperuser(user)) {
                    osd.setLockerId(null);
                    osdDao.updateOsd(osd);
                    ResponseUtil.responseIsGenericOkay(response);
                    return;
                } else {
                    // trying to unlock another user's lock: nope.
                    generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.OBJECT_LOCKED_BY_OTHER_USER);
                    return;
                }
            } else {
                // trying to unlock an unlocked object: NOP
                ResponseUtil.responseIsGenericOkay(response);
            }
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    private void getContent(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        if (idRequest.validated()) {
            Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(idRequest.getId());
            if (!osdOpt.isPresent()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            ObjectSystemData osd         = osdOpt.get();
            boolean          readAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.READ_OBJECT_CONTENT, user);
            if (!readAllowed) {
                generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.NO_READ_PERMISSION);
                return;
            }
            if (osd.getContentSize() == null || osd.getContentSize() == 0) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_HAS_NO_CONTENT);
                return;
            }
            Optional<Format> formatOpt = new FormatDao().getFormatById(osd.getFormatId());
            // no regular error response for missing format - this should only be possible if the database is corrupted.
            Format format = formatOpt.orElseThrow(
                    () -> new ServletException(String.format("Encountered object #%d with content but non-existing formatId #%d.",
                            osd.getId(), osd.getFormatId())));
            ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
            InputStream     contentStream   = contentProvider.getContentStream(osd);
            response.setContentType(format.getContentType());
            response.setStatus(SC_OK);
            contentStream.transferTo(response.getOutputStream());
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    private void setContent(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith(MULTIPART)) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.NOT_MULTIPART_UPLOAD);
            return;
        }
        Part contentRequest = request.getPart("setContentRequest");
        if (contentRequest == null) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
            return;
        }
        Part file = request.getPart("file");
        if (file == null) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.MISSING_FILE_PARAMETER);
            return;
        }
        SetContentRequest setContentRequest = xmlMapper.readValue(contentRequest.getInputStream(), SetContentRequest.class);
        if (setContentRequest.validated()) {
            Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(setContentRequest.getId());
            if (!osdOpt.isPresent()) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            ObjectSystemData osd          = osdOpt.get();
            boolean          writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.WRITE_OBJECT_CONTENT, user);
            if (!writeAllowed) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.NO_WRITE_PERMISSION);
                return;
            }
            FormatDao        formatDao = new FormatDao();
            Optional<Format> formatOpt = formatDao.getFormatById(setContentRequest.getFormatId());
            if (!formatOpt.isPresent()) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.FORMAT_NOT_FOUND);
                return;
            }
            // store file in tmp dir:
            Path tempFile       = Files.createTempFile("cinnamon-upload-", ".data");
            File tempOutputFile = tempFile.toFile();
            long bytesWritten   = Files.copy(file.getInputStream(), tempFile, REPLACE_EXISTING);

            // get content provider and store data:
            ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
            ContentMetadata metadata        = contentProvider.writeContentStream(osd, new FileInputStream(tempOutputFile));
            osd.setContentHash(metadata.getContentHash());
            osd.setContentPath(metadata.getContentPath());
            osd.setContentSize(metadata.getContentSize());
            osd.setFormatId(formatOpt.get().getId());
            osdDao.updateOsd(osd);
            GenericResponse genericResponse = new GenericResponse(true);
            ResponseUtil.responseIsOkayAndXml(response);
            xmlMapper.writeValue(response.getOutputStream(), genericResponse);
        } else {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST, "setContentRequest parameter is invalid");
        }
    }

    private void setSummary(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        SetSummaryRequest          summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        Optional<ObjectSystemData> osdOpt         = osdDao.getObjectById(summaryRequest.getId());
        if (osdOpt.isPresent()) {
            ObjectSystemData osd = osdOpt.get();
            if (authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.WRITE_OBJECT_SYS_METADATA.getName(), user)) {
                osd.setSummary(summaryRequest.getSummary());
                osdDao.updateOsd(osd);
                ResponseUtil.responseIsOkayAndXml(response);
                xmlMapper.writeValue(response.getWriter(), new GenericResponse(true));
                return;
            } else {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }
        }
        ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
    }

    private void getSummaries(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        IdListRequest          idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper         wrapper       = new SummaryWrapper();
        List<ObjectSystemData> osds          = osdDao.getObjectsById(idListRequest.getIdList(), true);
        osds.forEach(osd -> {
            if (authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.READ_OBJECT_SYS_METADATA.getName(), user)) {
                wrapper.getSummaries().add(osd.getSummary());
            }
        });
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void getObjectsById(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        OsdRequest             osdRequest   = xmlMapper.readValue(request.getInputStream(), OsdRequest.class);
        List<ObjectSystemData> osds         = osdDao.getObjectsById(osdRequest.getIds(), osdRequest.isIncludeSummary());
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void getObjectsByFolderId(HttpServletRequest request, HttpServletResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        OsdByFolderRequest     osdRequest     = xmlMapper.readValue(request.getInputStream(), OsdByFolderRequest.class);
        Long                   folderId       = osdRequest.getFolderId();
        boolean                includeSummary = osdRequest.isIncludeSummary();
        List<ObjectSystemData> osds           = osdDao.getObjectsByFolderId(folderId, includeSummary);
        List<ObjectSystemData> filteredOsds   = authorizationService.filterObjectsByBrowsePermission(osds, user);

        LinkDao    linkDao       = new LinkDao();
        List<Link> links         = linkDao.getLinksByFolderId(folderId);
        List<Link> filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        wrapper.setLinks(filteredLinks);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

    private void generateErrorMessage(HttpServletResponse response, int statusCode, ErrorCode errorCode) {
        ErrorResponseGenerator.generateErrorMessage(response, statusCode, errorCode);
    }
}
