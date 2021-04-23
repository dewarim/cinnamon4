package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RequestResponseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        CinnamonRequest  cinnmonRequest   = new CinnamonRequest((HttpServletRequest) request);
        CinnamonResponse cinnamonResponse = new CinnamonResponse((HttpServletResponse) response);
        try {
            chain.doFilter(cinnmonRequest, cinnamonResponse);
            cinnamonResponse.renderResponseIfNecessary();
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            cinnamonResponse.generateErrorMessage(errorCode.getHttpResponseCode(), errorCode, e.getMessage());
        }

    }

}
