package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.*;
import com.dewarim.cinnamon.application.service.AccessLogService;
import com.dewarim.cinnamon.application.service.SearchService;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.SEARCH_SERVICE;

public class RequestResponseFilter implements Filter {

    private static final Logger log = LogManager.getLogger(RequestResponseFilter.class);

    private       boolean                        logResponses                   = false;
    private final PostCommitChangeTriggerHandler postCommitChangeTriggerHandler = new PostCommitChangeTriggerHandler();
    private final AccessLogService               accessLogService               = new AccessLogService();
    private       SearchService                  searchService;

    @Override
    public void init(FilterConfig filterConfig) {
        searchService = (SearchService) filterConfig.getServletContext().getAttribute(SEARCH_SERVICE);
    }

    public RequestResponseFilter() {
    }

    public RequestResponseFilter(boolean logResponses) {
        this.logResponses = logResponses;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        CinnamonResponse cinnamonResponse = new CinnamonResponse((HttpServletRequest) request, (HttpServletResponse) response);
        CinnamonRequest  cinnamonRequest  = new CinnamonRequest((HttpServletRequest) request, (HttpServletResponse) response);
        try {
            chain.doFilter(cinnamonRequest, cinnamonResponse);
            ThreadLocalSqlSession.getSqlSession().commit();
            postCommitChangeTriggerHandler.executeTriggers(cinnamonRequest, cinnamonResponse);

            // Must happen before the response is written: once the body is flushed, the client is
            // free to send its next request.
            waitUntilIndexedIfRequested();

            cinnamonResponse.renderResponseIfNecessary();
        } catch (FailedRequestException e) {
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
            log.debug("Failed request: ", e);
            ErrorCode            errorCode = e.getErrorCode();
            String               message   = e.getMessage() != null ? e.getMessage() : errorCode.getDescription();
            CinnamonErrorWrapper errorWrapper;
            if (e.getErrors().isEmpty()) {
                errorWrapper = cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, message, logResponses);
            } else {
                errorWrapper = cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, message, e.getErrors(), logResponses);
            }

            Long userId = null;
            if (RequestScope.getCurrentUser() != null) {
                userId = RequestScope.getCurrentUser().getId();
            }
            if(CinnamonServer.isAccessLogEnabled()) {
                accessLogService.addEntry(cinnamonRequest, cinnamonResponse, errorWrapper, errorCode, null, userId);
                accessLogService.truncateLogIfNecessary();
            }
        }
        finally {
            log.debug("RequestResponseFilter: after");
        }

    }

    /**
     * With waitUntilSearchable enabled, wait here - after the commit, before the response body is
     * written - until the items created or updated by this request are searchable.
     * <p>
     * This used to happen in {@link DbSessionFilter}, which is the outermost filter and therefore
     * only runs <i>after</i> {@code renderResponseIfNecessary()} has already written the body. Jetty
     * buffers small responses until the request completes, which hid the problem, but a response
     * larger than the output buffer (32 KB by default) is flushed immediately - and the client could
     * then issue its next request before the server had waited for the index at all, which defeats
     * the whole point of the setting.
     * <p>
     * All index jobs that are waited for are created while the servlet runs, so the commit above has
     * already made them visible to IndexService. Post-commit change triggers only call out to
     * external services and do not add index jobs, so running them first is safe.
     * <p>
     * Trade-off: the request's SqlSession stays open across the wait, so its pooled connection is
     * held for roughly millisToWaitBetweenRuns longer than before, when DbSessionFilter closed the
     * session first. That only applies when waitUntilSearchable is switched on, which already
     * documents itself as making responses about a second slower.
     */
    private void waitUntilIndexedIfRequested() {
        if (!CinnamonServer.getConfig().getLuceneConfig().isWaitUntilSearchable()) {
            return;
        }
        List<IndexJob> indexJobs = RequestScope.getIndexJobs();
        if (indexJobs.isEmpty()) {
            return;
        }
        if (searchService == null) {
            log.warn("waitUntilSearchable is enabled, but this filter has no SearchService:" +
                    " cannot wait for {} index jobs.", indexJobs.size());
            return;
        }
        log.debug("Will wait until items from {} index jobs are searchable.", indexJobs.size());
        searchService.waitUntilIndexed(indexJobs);
    }

}
