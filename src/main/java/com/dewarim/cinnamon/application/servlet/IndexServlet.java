package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.dao.IndexJobDao;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.OsdMetaDao;
import com.dewarim.cinnamon.dao.TikaFailureDao;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.index.IndexInfoRequest;
import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import com.dewarim.cinnamon.model.response.index.IndexInfoResponse;
import com.dewarim.cinnamon.model.response.index.ReindexResponse;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
            reindexResponse.setTikaMetasetsDeleted(scheduleTikaExtraction(reindexRequest.osdIds()));
        }
        cinnamonResponse.setResponse(reindexResponse);
    }

    /**
     * Make TikaService extract these documents again - all of them, if the list is empty, which is
     * what a full reindex asks for.
     * <p>
     * Reindexing on its own does not involve Tika: the indexer reads the tika metaset that is
     * already there, and TikaService fills that metaset for OSDs which do not have one yet. So the
     * way to ask for a fresh extraction is to delete the metaset, and to forget the OSD's earlier
     * extraction failures along with it - a document that was written off while the Tika server was
     * broken or misconfigured gets its full set of attempts back.
     *
     * @return the number of tika metasets deleted
     */
    private int scheduleTikaExtraction(List<Long> osdIds) {
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
        if (osdIds.isEmpty()) {
            deleted = osdMetaDao.deleteByTypeId(typeId);
            tikaFailureDao.deleteAll();
        }
        else {
            deleted = osdMetaDao.deleteByTypeIdAndOsdIds(typeId, osdIds);
            tikaFailureDao.deleteByOsdIds(osdIds);
        }
        log.info("Deleted {} tika metasets so they are extracted again.", deleted);
        return deleted;
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