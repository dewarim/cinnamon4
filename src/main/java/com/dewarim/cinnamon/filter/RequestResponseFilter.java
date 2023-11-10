package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.TransactionStatus;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RequestResponseFilter implements Filter {

    private static final Logger log = LogManager.getLogger(RequestResponseFilter.class);

    private boolean logResponses = false;

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
        CinnamonResponse cinnamonResponse = new CinnamonResponse((HttpServletResponse) response);
        try {
            CinnamonRequest cinnamonRequest  = new CinnamonRequest((HttpServletRequest) request);
            chain.doFilter(cinnamonRequest, cinnamonResponse);
            //
            ThreadLocalSqlSession.getSqlSession().commit();
            cinnamonResponse.renderResponseIfNecessary(logResponses);
        } catch (FailedRequestException e) {
            ThreadLocalSqlSession.setTransactionStatus(TransactionStatus.ROLLBACK);
            log.debug("Failed request: ", e);
            ErrorCode errorCode = e.getErrorCode();
            if (e.getErrors().isEmpty()) {
                cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, errorCode.getDescription(), logResponses);
            } else {
                cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, errorCode.getDescription(), e.getErrors(), logResponses);
            }
        }

    }

}
