package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RequestResponseFilter implements Filter {

    private static final Logger log = LogManager.getLogger(RequestResponseFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        CinnamonRequest  cinnamonRequest   = new CinnamonRequest((HttpServletRequest) request);
        CinnamonResponse cinnamonResponse = new CinnamonResponse((HttpServletResponse) response);
        try {
            chain.doFilter(cinnamonRequest, cinnamonResponse);
            cinnamonResponse.renderResponseIfNecessary();
        } catch (FailedRequestException e) {
            log.debug("Failed request: ",e);
            ErrorCode errorCode = e.getErrorCode();
            if(e.getErrors().isEmpty()){
                cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, e.getMessage());
            }
            else{
                cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, e.getMessage(), e.getErrors());
            }
        }

    }

}
