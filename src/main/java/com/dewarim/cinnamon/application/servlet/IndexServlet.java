package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.dao.IndexJobDao;
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

import static com.dewarim.cinnamon.api.Constants.SEARCH_SERVICE;

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
            if (!reindexRequest.getFolderIds().isEmpty()) {
                reindexResponse.setFoldersToIndex(indexJobDao.reindexFolders(reindexRequest.getFolderIds()));
            }
            if (!reindexRequest.getOsdIds().isEmpty()) {
                reindexResponse.setDocumentsToIndex(indexJobDao.reindexOsds(reindexRequest.getOsdIds()));
            }
        }
        cinnamonResponse.setResponse(reindexResponse);
    }

    private void info(CinnamonRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        IndexInfoRequest  createRequest = request.getMapper().readValue(request.getInputStream(), IndexInfoRequest.class);
        IndexJobDao       jobDao        = new IndexJobDao();
        int               jobs          = jobDao.countJobs();
        int               failedJobs    = jobDao.countFailedJobs();
        IndexInfoResponse infoResponse  = new IndexInfoResponse();
        if (createRequest.isCountDocuments()) {
            IndexJobDao.IndexRows docCounts = searchService.countDocs();
            infoResponse.setDocumentsInIndex(docCounts.getOsdRowCount());
            infoResponse.setFoldersInIndex(docCounts.getFolderRowCount());
        }
        if(createRequest.isListFailedIndexJobs()){
            infoResponse.setFailedIndexJobs(new IndexJobDao().listFailedIndexJobs());
        }
        infoResponse.setJobCount(jobs);
        infoResponse.setFailedJobCount(failedJobs);
        cinnamonResponse.setResponse(infoResponse);
    }

    @Override
    public void init() {
        searchService = ((SearchService) getServletContext().getAttribute(SEARCH_SERVICE));
    }
}