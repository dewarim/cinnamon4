package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.OsdMetaDao;
import com.dewarim.cinnamon.dao.TikaFailureDao;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.index.IndexInfoRequest;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.request.osd.VersionPredicate;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.dewarim.cinnamon.api.Constants.SEARCH_SERVICE;
import static com.dewarim.cinnamon.api.Constants.TIKA_METASET_NAME;

@WebServlet(name = "Index", urlPatterns = "/")
public class IndexServlet extends BaseServlet {
    private static final Logger log = LogManager.getLogger(IndexServlet.class);

    private       SearchService searchService;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest cinnamonRequest = (CinnamonRequest) request;

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case INDEX__INFO -> info(cinnamonRequest, cinnamonResponse);
            case INDEX__REINDEX -> reIndex(cinnamonRequest, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void reIndex(CinnamonRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        if (!new AuthorizationService().currentUserIsSuperuser()) {
            throw ErrorCode.REQUIRES_SUPERUSER_STATUS.exception();
        }
        ReindexRequest reindexRequest = request.getMapper().readValue(request.getInputStream(), ReindexRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        IndexJobDao     indexJobDao     = new IndexJobDao();
        ReindexResponse reindexResponse = new ReindexResponse();
        if (reindexRequest.doFullReindex()) {
            IndexJobDao.IndexRows rowCounts = indexJobDao.fullReindex();
            reindexResponse.setDocumentsToIndex(rowCounts.getOsdRowCount());
            reindexResponse.setFoldersToIndex(rowCounts.getFolderRowCount());
        } else {
            if (!reindexRequest.folderIds().isEmpty()) {
                reindexResponse.setFoldersToIndex(indexJobDao.reindexFolders(reindexRequest.folderIds()));
            }
            if (!reindexRequest.osdIds().isEmpty()) {
                reindexResponse.setDocumentsToIndex(indexJobDao.reindexOsds(reindexRequest.osdIds()));
            }
        }
        if (reindexRequest.updateTikaMetaset()) {
            reindexResponse.setTikaMetasetsDeleted(scheduleTikaExtraction(reindexRequest));
        }
        cinnamonResponse.setResponse(reindexResponse);
    }

    /**
     * Make TikaService extract documents again: the whole repository for a full reindex, and
     * otherwise exactly what the request names - the objects it lists, plus the content of the
     * folders it lists.
     * <p>
     * Reindexing on its own does not involve Tika: the indexer reads the tika metaset that is
     * already there, and TikaService fills that metaset for OSDs which do not have one yet. So the
     * way to ask for a fresh extraction is to delete the metaset, and to forget the OSD's earlier
     * extraction failures along with it - a document that was written off while the Tika server was
     * broken or misconfigured gets its full set of attempts back.
     *
     * @return the number of tika metasets deleted
     */
    private int scheduleTikaExtraction(ReindexRequest reindexRequest) {
        Optional<MetasetType> tikaMetasetType = new MetasetTypeDao().getMetasetTypeByName(TIKA_METASET_NAME);
        if (tikaMetasetType.isEmpty()) {
            // Without the metaset type, Tika cannot be in use at all (TikaService refuses to start
            // without it), so there is nothing to re-extract and nothing to complain about.
            log.info("updateTikaMetaset was requested, but there is no '{}' metaset type - ignoring it.",
                    TIKA_METASET_NAME);
            return 0;
        }
        Long           typeId         = tikaMetasetType.get().getId();
        OsdMetaDao     osdMetaDao     = new OsdMetaDao();
        TikaFailureDao tikaFailureDao = new TikaFailureDao();
        int            deleted;
        // Only a full reindex means the whole repository. The condition has to be doFullReindex()
        // and not "no osdIds were given": a request that names folders also arrives here with an
        // empty osdIds list, and would then delete every tika metaset in the repository and have
        // the entire corpus extracted again.
        if (reindexRequest.doFullReindex()) {
            deleted = osdMetaDao.deleteByTypeId(typeId);
            tikaFailureDao.deleteAll();
        }
        else {
            List<Long> osdIds = osdIdsToExtract(reindexRequest);
            if (osdIds.isEmpty()) {
                return 0;
            }
            deleted = osdMetaDao.deleteByTypeIdAndOsdIds(typeId, osdIds);
            tikaFailureDao.deleteByOsdIds(osdIds);
        }
        log.info("Deleted {} tika metasets so they are extracted again.", deleted);
        return deleted;
    }

    /**
     * The objects a partial reindex asks to extract again: the ones it names, plus the content of
     * the folders it names. Not recursively - reindexFolders does not descend into sub folders
     * either, so a folder means that folder.
     */
    private List<Long> osdIdsToExtract(ReindexRequest reindexRequest) {
        if (reindexRequest.folderIds().isEmpty()) {
            return reindexRequest.osdIds();
        }
        OsdDao    osdDao = new OsdDao();
        Set<Long> osdIds = new LinkedHashSet<>(reindexRequest.osdIds());
        for (Long folderId : reindexRequest.folderIds()) {
            osdDao.getObjectsByFolderId(folderId, false, VersionPredicate.ALL)
                    .forEach(osd -> osdIds.add(osd.getId()));
        }
        return new ArrayList<>(osdIds);
    }

    private void info(CinnamonRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        IndexInfoRequest  createRequest = request.getMapper().readValue(request.getInputStream(), IndexInfoRequest.class);
        IndexJobDao       jobDao        = new IndexJobDao();
        int               jobs          = jobDao.countJobs();
        int               failedJobs    = jobDao.countFailedJobs();
        IndexInfoResponse infoResponse  = new IndexInfoResponse();
        if (createRequest.countDocuments()) {
            IndexJobDao.IndexRows docCounts = searchService.countDocs();
            infoResponse.setDocumentsInIndex(docCounts.getOsdRowCount());
            infoResponse.setFoldersInIndex(docCounts.getFolderRowCount());
        }
        if(createRequest.listFailedIndexJobs()){
            infoResponse.setFailedIndexJobs(new IndexJobDao().listFailedIndexJobs());
        }
        TikaFailureDao tikaFailureDao = new TikaFailureDao();
        if (createRequest.listFailedTikaExtractions()) {
            // These rows name objects by id and carry the error text Tika or the content store
            // produced, which includes content paths and exception messages - operator information
            // about objects the caller may have no permission to read at all. The counts below
            // disclose nothing and stay open to every authenticated user, as this endpoint always
            // has been; listFailedIndexJobs stays open for the same reason, having been part of the
            // API since v1.15.1.
            if (!new AuthorizationService().currentUserIsSuperuser()) {
                throw ErrorCode.REQUIRES_SUPERUSER_STATUS.exception();
            }
            infoResponse.setFailedTikaExtractions(tikaFailureDao.list());
        }
        infoResponse.setFailedTikaExtractionCount(tikaFailureDao.count());
        infoResponse.setJobCount(jobs);
        infoResponse.setFailedJobCount(failedJobs);
        cinnamonResponse.setResponse(infoResponse);
    }

    @Override
    public void init() {
        searchService = ((SearchService) getServletContext().getAttribute(SEARCH_SERVICE));
    }
}