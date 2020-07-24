package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.*;
import com.dewarim.cinnamon.model.request.osd.*;
import com.dewarim.cinnamon.model.response.OsdWrapper;
import com.dewarim.cinnamon.model.response.SummaryWrapper;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.provider.StateProviderService;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.LANGUAGE_UNDETERMINED_ISO_CODE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@MultipartConfig
@WebServlet(name = "Osd", urlPatterns = "/")
public class OsdServlet extends BaseServlet {

    private              ObjectMapper         xmlMapper            = new XmlMapper();
    private              AuthorizationService authorizationService = new AuthorizationService();
    private static final Logger               log                  = LogManager.getLogger(OsdServlet.class);
    private static final String               MULTIPART            = "multipart/";

    public OsdServlet() {
        super();
        // Would like to use UNWRAP_ROOT_VALUE to prevent an IdRequest being mistaken for a CreateNewVersionRequest.
        // but currently this featuer does not work on XML input:
        // xmlMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        UserAccount      user             = ThreadLocalSqlSession.getCurrentUser();
        OsdDao           osdDao           = new OsdDao();
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        switch (pathInfo) {
            case "/createOsd":
                createOsd(request, cinnamonResponse, user, osdDao);
                break;
            case "/createMeta":
                createMeta(request, cinnamonResponse, user, osdDao);
                break;
            case "/delete":
                deleteOsd(request,cinnamonResponse,user,osdDao);
                break;
            case "/deleteMeta":
                deleteMeta(request, cinnamonResponse, user, osdDao);
                break;
            case "/getContent":
                getContent(request, cinnamonResponse, user, osdDao);
                break;
            case "/getMeta":
                getMeta(request, cinnamonResponse, user, osdDao);
                break;
            case "/getObjectsByFolderId":
                getObjectsByFolderId(request, cinnamonResponse, user, osdDao);
                break;
            case "/getObjectsById":
                getObjectsById(request, cinnamonResponse, user, osdDao);
                break;
            case "/getSummaries":
                getSummaries(request, cinnamonResponse, user, osdDao);
                break;
            case "/lock":
                lock(request, cinnamonResponse, user, osdDao);
                break;
            case "/setContent":
                setContent(request, cinnamonResponse, user, osdDao);
                break;
            case "/setSummary":
                setSummary(request, cinnamonResponse, user, osdDao);
                break;
            case "/unlock":
                unlock(request, cinnamonResponse, user, osdDao);
                break;
            case "/version":
                newVersion(request, cinnamonResponse, user, osdDao);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void deleteOsd(HttpServletRequest request, CinnamonResponse cinnamonResponse, UserAccount user, OsdDao osdDao) throws IOException {
        DeleteOsdRequest deleteRequest = xmlMapper.readValue(request.getInputStream(), DeleteOsdRequest.class)
                         .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<ObjectSystemData> osds = osdDao.getObjectsById(deleteRequest.getIds(),false);
         // BLOCKED: TODO: determine how to handle partial deletes / protected relations / missing objects (error, ignore, extra flags?)
        throw new CinnamonException("Not implemented yet!");
    }

    private void createOsd(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException, ServletException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart("createOsdRequest");
        if (contentRequest == null) {
            throw ErrorCode.MISSING_REQUEST_PAYLOAD.exception();
        }

        CreateOsdRequest createRequest = xmlMapper.readValue(contentRequest.getInputStream(), CreateOsdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        // check parent folder exists
        Folder parentFolder = new FolderDao().getFolderById(createRequest.getParentId())
                .orElseThrow(ErrorCode.PARENT_FOLDER_NOT_FOUND.getException());

        // check acl of parent folder
        boolean createAllowed = new AuthorizationService().userHasPermission(parentFolder.getAclId(), DefaultPermission.CREATE_OBJECT.getName(), user);
        if (!createAllowed) {
            throw ErrorCode.NO_CREATE_PERMISSION.exception();
        }

        ObjectSystemData osd = new ObjectSystemData();
        osd.setParentId(parentFolder.getId());
        osd.setName(createRequest.getName());

        // check acl exists
        Long aclId = new AclDao().getAclByIdOpt(createRequest.getAclId()).orElseThrow(ErrorCode.ACL_NOT_FOUND.getException())
                .getId();
        osd.setAclId(aclId);

        // check owner exists
        Long ownerId = new UserAccountDao().getUserAccountById(createRequest.getOwnerId())
                .orElseThrow(ErrorCode.USER_ACCOUNT_NOT_FOUND.getException()).getId();
        osd.setOwnerId(ownerId);

        // check type exists
        Long typeId = new ObjectTypeDao().getObjectTypeById(createRequest.getTypeId())
                .orElseThrow(ErrorCode.OBJECT_TYPE_NOT_FOUND.getException()).getId();
        osd.setTypeId(typeId);

        // check lifecycleState (if necessary)
        if (createRequest.getLifecycleStateId() != null) {
            Long lifecycleStateId = new LifecycleStateDao().getLifecycleStateById(createRequest.getLifecycleStateId())
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException()).getId();
            osd.setLifecycleStateId(lifecycleStateId);
            // TODO: + enterState?
        }

        // check language if given
        final Long languageId;
        if (createRequest.getLanguageId() != null) {
            languageId = new LanguageDao().getLanguageById(createRequest.getLanguageId())
                    .orElseThrow(ErrorCode.LANGUAGE_NOT_FOUND.getException()).getId();
        } else {
            languageId = new LanguageDao().getLanguageByIsoCode(LANGUAGE_UNDETERMINED_ISO_CODE)
                    .orElseThrow(ErrorCode.DB_IS_MISSING_LANGUAGE_CODE.getException()).getId();
        }
        osd.setLanguageId(languageId);

        osd.setCreatorId(user.getId());
        osd.setModifierId(user.getId());
        osd.setSummary(createRequest.getSummary());

        osd = osdDao.saveOsd(osd);

        Part file = request.getPart("file");
        if (file != null) {
            storeFileUpload(file.getInputStream(), osd, createRequest.getFormatId());
            osdDao.updateOsd(osd, false);
        }
        OsdWrapper osdWrapper = new OsdWrapper(Collections.singletonList(osd));
        response.setWrapper(osdWrapper);
    }

    private void deleteMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        DeleteMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), DeleteMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             osdId = metaRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsWritable(osd, user);

        List<Meta> metas;
        OsdMetaDao metaDao = new OsdMetaDao();
        if (metaRequest.getMetaId() != null) {
            metas = Collections.singletonList(metaDao.getOsdMetaById(metaRequest.getMetaId()));
        } else {
            metas = metaDao.getMetaByNamesAndOsd(Collections.singletonList(metaRequest.getTypeName()), osdId);
        }
        if (metas.isEmpty()) {
            throw ErrorCode.METASET_NOT_FOUND.exception();
        }
        metas.forEach(meta -> {
            boolean deleteSuccess = metaDao.deleteById(meta.getId()) == 1;
            if (!deleteSuccess) {
                throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, "Failed to delete metaSet #" + meta.getId());
            }
        });
        response.responseIsGenericOkay();
    }

    /**
     * Note: getMeta allows OsdMetaRequests without metaset name to return all metasets.
     * This usage is deprecated.
     * Current Cinnamon 3 clients expect the arbitrary metaset XML to be added to the DOM of the metaset wrapper.
     * This requires assembling a new DOM tree in memory for each request to getMeta, which is not something you
     * want to see with large metasets.
     */
    private void getMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        MetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), MetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long             osdId = metaRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsReadable(osd);

        List<Meta> metaList;
        if (metaRequest.getTypeNames() != null) {
            metaList = new OsdMetaDao().getMetaByNamesAndOsd(metaRequest.getTypeNames(), osdId);
        } else {
            metaList = new OsdMetaDao().listByOsd(osdId);
        }

        createMetaResponse(metaRequest, response, metaList);
    }

    private void createMeta(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        CreateMetaRequest metaRequest = xmlMapper.readValue(request.getInputStream(), CreateMetaRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        Long             osdId = metaRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessCustomMetaIsWritable(osd, user);
        MetasetType metaType = determineMetasetType(metaRequest.getTypeId(), metaRequest.getTypeName());

        // does meta already exist and is unique?
        OsdMetaDao metaDao = new OsdMetaDao();
        List<Meta> metas   = metaDao.getMetaByNamesAndOsd(Collections.singletonList(metaType.getName()), osdId);
        if (metaType.getUnique() && metas.size() > 0) {
            throw ErrorCode.METASET_IS_UNIQUE_AND_ALREADY_EXISTS.exception();
        }

        Meta meta = metaDao.createMeta(metaRequest, metaType);
        createMetaResponse(new MetaRequest(), response, Collections.singletonList(meta));
    }

    private MetasetType determineMetasetType(Long typeId, String typeName) {
        if (typeId != null) {
            return new MetasetTypeDao().getMetasetTypeById(typeId)
                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
        } else {
            return new MetasetTypeDao().getMetasetTypeByName(typeName)
                    .orElseThrow(ErrorCode.METASET_TYPE_NOT_FOUND.getException());
        }
    }

    private void lock(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        ObjectSystemData osd         = osdDao.getObjectById(idRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        boolean          lockAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.LOCK, user);
        if (!lockAllowed) {
            throw ErrorCode.NO_LOCK_PERMISSION.exception();
        }
        Long lockHolder = osd.getLockerId();
        if (lockHolder != null) {
            if (lockHolder.equals(user.getId())) {
                // trying to lock your own object: NOP
                response.responseIsGenericOkay();
                return;
            } else {
                throw ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.exception();
            }
        }
        osd.setLockerId(user.getId());
        osdDao.updateOsd(osd, false);
        response.responseIsGenericOkay();
    }


    private void unlock(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData osd = osdDao.getObjectById(idRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        new AuthorizationService().throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.LOCK, user,
                ErrorCode.NO_LOCK_PERMISSION);

        Long lockHolder = osd.getLockerId();
        if (lockHolder != null) {
            UserAccountDao userDao = new UserAccountDao();
            // superuser may remove locks from other users.
            if (lockHolder.equals(user.getId()) || userDao.isSuperuser(user)) {
                osd.setLockerId(null);
                osdDao.updateOsd(osd, false);
                response.responseIsGenericOkay();
            } else {
                // trying to unlock another user's lock: nope.
                throw ErrorCode.OBJECT_LOCKED_BY_OTHER_USER.exception();
            }
        } else {
            // trying to unlock an unlocked object: NOP
            response.responseIsGenericOkay();
        }
    }

    private void getContent(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData osd = osdDao.getObjectById(idRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        new AuthorizationService().throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.READ_OBJECT_CONTENT, user,
                ErrorCode.NO_READ_PERMISSION);
        if (osd.getContentSize() == null || osd.getContentSize() == 0) {
            throw ErrorCode.OBJECT_HAS_NO_CONTENT.exception();
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
    }

    private void setContent(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart("setContentRequest");
        if (contentRequest == null) {
            throw ErrorCode.INVALID_REQUEST.exception();
        }
        Part file = request.getPart(("file"));
        if (file == null) {
            throw ErrorCode.MISSING_FILE_PARAMETER.exception();
        }
        SetContentRequest setContentRequest = xmlMapper.readValue(contentRequest.getInputStream(), SetContentRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData osd          = osdDao.getObjectById(setContentRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        boolean          writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.WRITE_OBJECT_CONTENT, user);
        if (!writeAllowed) {
            throw ErrorCode.NO_WRITE_PERMISSION.exception();
        }
        boolean isLocker = user.getId().equals(osd.getLockerId());
        if (!isLocker) {
            throw ErrorCode.OBJECT_MUST_BE_LOCKED_BY_USER.exception();
        }

        storeFileUpload(file.getInputStream(), osd, setContentRequest.getFormatId());
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void deleteTempFile(File tempFile) {
        boolean deleteResult = tempFile.delete();
        if (!deleteResult) {
            log.warn("Could not delete temporary upload file " + tempFile.getAbsolutePath());
        }
    }

    private void storeFileUpload(InputStream inputStream, ObjectSystemData osd, Long formatId) throws IOException {
        FormatDao formatDao = new FormatDao();
        Format    format    = formatDao.getFormatById(formatId).orElseThrow(ErrorCode.FORMAT_NOT_FOUND.getException());

        // store file in tmp dir:
        Path tempFile       = Files.createTempFile("cinnamon-upload-", ".data");
        File tempOutputFile = tempFile.toFile();
        long bytesWritten   = Files.copy(inputStream, tempFile, REPLACE_EXISTING);

        // get content provider and store data:
        ContentProvider contentProvider = ContentProviderService.getInstance().getContentProvider(osd.getContentProvider());
        ContentMetadata metadata        = contentProvider.writeContentStream(osd, new FileInputStream(tempOutputFile));
        osd.setContentHash(metadata.getContentHash());
        osd.setContentPath(metadata.getContentPath());
        osd.setContentSize(metadata.getContentSize());
        osd.setFormatId(format.getId());
        deleteTempFile(tempOutputFile);
    }

    private void setSummary(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        SetSummaryRequest summaryRequest = xmlMapper.readValue(request.getInputStream(), SetSummaryRequest.class);
        ObjectSystemData  osd            = osdDao.getObjectById(summaryRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(osd, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user,
                ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
        osd.setSummary(summaryRequest.getSummary());
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void getSummaries(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        IdListRequest          idListRequest = xmlMapper.readValue(request.getInputStream(), IdListRequest.class);
        SummaryWrapper         wrapper       = new SummaryWrapper();
        List<ObjectSystemData> osds          = osdDao.getObjectsById(idListRequest.getIdList(), true);
        osds.forEach(osd -> {
            if (authorizationService.hasUserOrOwnerPermission(osd, DefaultPermission.READ_OBJECT_SYS_METADATA.getName(), user)) {
                wrapper.getSummaries().add(osd.getSummary());
            }
        });
        response.setWrapper(wrapper);
    }

    private void getObjectsById(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        OsdRequest             osdRequest   = xmlMapper.readValue(request.getInputStream(), OsdRequest.class);
        List<ObjectSystemData> osds         = osdDao.getObjectsById(osdRequest.getIds(), osdRequest.isIncludeSummary());
        List<ObjectSystemData> filteredOsds = authorizationService.filterObjectsByBrowsePermission(osds, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        response.setWrapper(wrapper);
    }

    private void getObjectsByFolderId(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws IOException {
        OsdByFolderRequest     osdRequest     = xmlMapper.readValue(request.getInputStream(), OsdByFolderRequest.class);
        long                   folderId       = osdRequest.getFolderId();
        boolean                includeSummary = osdRequest.isIncludeSummary();
        List<ObjectSystemData> osds           = osdDao.getObjectsByFolderId(folderId, includeSummary);
        List<ObjectSystemData> filteredOsds   = authorizationService.filterObjectsByBrowsePermission(osds, user);

        LinkDao    linkDao       = new LinkDao();
        List<Link> links         = linkDao.getLinksByFolderId(folderId);
        List<Link> filteredLinks = authorizationService.filterLinksByBrowsePermission(links, user);

        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(filteredOsds);
        wrapper.setLinks(filteredLinks);
        response.setWrapper(wrapper);
    }

    private void newVersion(HttpServletRequest request, CinnamonResponse response, UserAccount user, OsdDao osdDao) throws ServletException, IOException {
        verifyIsMultipart(request);
        Part contentRequest = request.getPart("createNewVersionRequest");
        CreateNewVersionRequest versionRequest = xmlMapper.readValue(contentRequest.getInputStream(), CreateNewVersionRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        ObjectSystemData preOsd = osdDao.getObjectById(versionRequest.getId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        authorizationService.throwUpUnlessUserOrOwnerHasPermission(preOsd, DefaultPermission.VERSION_OBJECT, user,
                ErrorCode.NO_VERSION_PERMISSION);

        Optional<String> lastDescendantVersion = osdDao.findLastDescendantVersion(preOsd.getId());
        ObjectSystemData osd                   = preOsd.createNewVersion(user, lastDescendantVersion.orElse(null));

        /*
         * fixLatestHeadAndBranch:
         * When a new version is created, the predecessor is no longer the latest version in head or branch:
         */
        preOsd.setLatestBranch(false);
        preOsd.setLatestHead(false);
        osdDao.updateOsd(preOsd, false);

        if (osd.getCmnVersion().matches("^\\d+$")) {
            osd.setLatestHead(true);
        }
        // save here to generate Id so metasets can be created and linked to the OSD.
        ObjectSystemData savedOsd = osdDao.saveOsd(osd);

        // storeMetadata
        if (versionRequest.hasMetaRequests()) {
            OsdMetaDao            osdMetaDao = new OsdMetaDao();
            final List<ErrorCode> errorCodes = new ArrayList<>();
            versionRequest.getMetaRequests().forEach(metadata -> {
                        try {
                            MetasetType       metasetType       = determineMetasetType(metadata.getTypeId(), metadata.getTypeName());
                            CreateMetaRequest createMetaRequest = new CreateMetaRequest(osd.getId(), metadata.getContent(), metasetType.getName());
                            osdMetaDao.createMeta(createMetaRequest, metasetType);
                        } catch (FailedRequestException e) {
                            errorCodes.add(e.getErrorCode());
                        } catch (Exception e) {
                            errorCodes.add(ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
                        }
                    }
            );
            if (errorCodes.size() > 0) {
                // for now, just throw the first error.
                throw errorCodes.get(0).exception();
            }
        }

        // saveFileUpload
        Part file = request.getPart("file");
        if (file != null) {
            storeFileUpload(file.getInputStream(), osd, versionRequest.getFormatId());
        }

        // set lifecycle state
        if (preOsd.getLifecycleStateId() != null) {
            LifecycleState lifecycleState = new LifecycleStateDao()
                    .getLifecycleStateById(preOsd.getLifecycleStateId())
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
            State             stateImpl         = StateProviderService.getInstance().getStateProvider(lifecycleState.getStateClass()).getState();
            StateChangeResult attachStateResult = stateImpl.enter(osd, lifecycleState.getLifecycleStateConfig());
            if (!attachStateResult.isSuccessful()) {
                throw ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED.exception();
            }
            osd.setLifecycleStateId(lifecycleState.getLifecycleStateForCopyId());
        }
        osdDao.updateOsd(osd, false);
        OsdWrapper wrapper = new OsdWrapper();
        wrapper.setOsds(Collections.singletonList(savedOsd));
        response.setWrapper(wrapper);
    }

    private void verifyIsMultipart(HttpServletRequest request) {
        String contentType = Optional.ofNullable(request.getContentType())
                .orElseThrow(ErrorCode.NO_CONTENT_TYPE_IN_HEADER.getException());
        if (!contentType.toLowerCase().startsWith(MULTIPART)) {
            throw ErrorCode.NOT_MULTIPART_UPLOAD.exception();
        }
    }

}
