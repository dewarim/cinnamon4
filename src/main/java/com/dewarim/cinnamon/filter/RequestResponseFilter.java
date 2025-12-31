package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.TransactionStatus;
import com.dewarim.cinnamon.application.service.AccessLogService;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RequestResponseFilter implements Filter {

    private static final Logger log = LogManager.getLogger(RequestResponseFilter.class);

    private       boolean                        logResponses                   = false;
    private final PostCommitChangeTriggerHandler postCommitChangeTriggerHandler = new PostCommitChangeTriggerHandler();
    private final AccessLogService               accessLogService               = new AccessLogService();

    @Override
    public void init(FilterConfig filterConfig) {

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
            if (ThreadLocalSqlSession.getCurrentUser() != null) {
                userId = ThreadLocalSqlSession.getCurrentUser().getId();
            }
            accessLogService.addEntry(cinnamonRequest, cinnamonResponse, errorWrapper, errorCode, null, userId);
        }
        finally {
            log.debug("RequestResponseFilter: after");
        }

    }

}
