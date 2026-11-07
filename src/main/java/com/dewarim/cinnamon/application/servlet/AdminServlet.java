package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.IndexEventDao;
import com.dewarim.cinnamon.dao.TikaFailureDao;
import com.dewarim.cinnamon.dao.event.AccessLogDao;
import com.dewarim.cinnamon.model.event.AccessLogEntry;
import com.dewarim.cinnamon.model.index.IndexEvent;
import com.dewarim.cinnamon.model.index.TikaFailure;
import com.dewarim.cinnamon.model.request.admin.ListAccessLogRequest;
import com.dewarim.cinnamon.model.request.admin.ListFailedTikaExtractionsRequest;
import com.dewarim.cinnamon.model.request.admin.ListIndexEventsRequest;
import com.dewarim.cinnamon.model.response.AccessLogWrapper;
import com.dewarim.cinnamon.model.response.IndexEventWrapper;
import com.dewarim.cinnamon.model.response.TikaFailureWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * The server's report on its own operation: what went wrong while indexing, and which requests
 * failed. Nothing here is about the content of the repository, which is why these endpoints are not
 * on the resources they report about.
 * <p>
 * Everything under /api/admin requires superuser status, checked once below rather than per method:
 * these rows name objects by id and carry content paths, request bodies and exception messages for
 * objects the caller may have no permission to read at all. A new endpoint added to this servlet is
 * therefore closed by default - which is the point of having the servlet.
 */
@WebServlet(name = "Admin", urlPatterns = "/")
public class AdminServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;

        if (!new AuthorizationService().currentUserIsSuperuser()) {
            throw ErrorCode.REQUIRES_SUPERUSER_STATUS.exception();
        }

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case ADMIN__LIST_INDEX_EVENTS -> listIndexEvents(cinnamonRequest, cinnamonResponse);
            case ADMIN__LIST_ACCESS_LOG -> listAccessLog(cinnamonRequest, cinnamonResponse);
            case ADMIN__LIST_FAILED_TIKA_EXTRACTIONS -> listFailedTikaExtractions(cinnamonRequest, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    /**
     * The newest index_events rows: what Lucene or the indexer said about a job it could not do.
     * The index itself reports its status at index/info - this is the error text behind the failure
     * counts reported there.
     * <p>
     * Indexing only: a document Tika could not extract is indexed all the same, just without Tika
     * metadata, and is recorded in tika_failures rather than here - see
     * {@link #listFailedTikaExtractions}.
     */
    private void listIndexEvents(CinnamonRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        ListIndexEventsRequest listRequest = request.getMapper()
                .readValue(request.getInputStream(), ListIndexEventsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<IndexEvent> events = new IndexEventDao()
                .listNewest(listRequest.limit(), listRequest.eventType());
        cinnamonResponse.setWrapper(new IndexEventWrapper(events));
    }

    /**
     * The objects whose Tika extraction failed, with the attempts made and the last error. The other
     * half of "why is this document not searchable": the document is in the index, but without the
     * metadata Tika would have extracted from its content.
     * <p>
     * index/info reports the same rows for listFailedTikaExtractions and keeps doing so - this is
     * where the count it also reports can be followed up, with a limit the caller chooses.
     */
    private void listFailedTikaExtractions(CinnamonRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        ListFailedTikaExtractionsRequest listRequest = request.getMapper()
                .readValue(request.getInputStream(), ListFailedTikaExtractionsRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        List<TikaFailure> failures = new TikaFailureDao().list(listRequest.limit());
        cinnamonResponse.setWrapper(new TikaFailureWrapper(failures));
    }

    /**
     * The newest access_log rows: the requests that failed, with the request as it arrived and the
     * error that was sent back. Written only for failed requests, and only while
     * loggingConfig.logErrorsToDatabase is on, so an empty list means either that nothing has
     * failed or that the log is switched off.
     */
    private void listAccessLog(CinnamonRequest request, CinnamonResponse cinnamonResponse) throws IOException {
        ListAccessLogRequest listRequest = request.getMapper()
                .readValue(request.getInputStream(), ListAccessLogRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        // The access log lives in its own database session (CinnamonServer.getAccessLogSession),
        // which the DAO opens and closes per call - it is not part of this request's transaction.
        List<AccessLogEntry> entries = new AccessLogDao().list(listRequest.limit());
        cinnamonResponse.setWrapper(new AccessLogWrapper(entries));
    }
}
