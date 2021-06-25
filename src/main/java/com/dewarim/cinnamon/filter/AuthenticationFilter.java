package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
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
import java.util.Date;
import java.util.Optional;

public class AuthenticationFilter implements Filter {
    private static final Logger log = LogManager.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            log.debug("AuthenticationFilter: before");
            HttpServletRequest  servletRequest  = (HttpServletRequest) request;
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            String              ticket          = servletRequest.getHeader("ticket");
            if (ticket == null || ticket.trim().isEmpty()) {
                failAuthentication(servletResponse, ErrorCode.AUTHENTICATION_FAIL_NO_TICKET_GIVEN);
                return;
            }

            Session cinnamonSession = new SessionDao().getSessionByTicket(ticket);
            if (cinnamonSession == null) {
                failAuthentication(servletResponse, ErrorCode.AUTHENTICATION_FAIL_NO_SESSION_FOUND);
                return;
            }

            if (cinnamonSession.getExpires().getTime() < new Date().getTime()) {
                failAuthentication(servletResponse, ErrorCode.AUTHENTICATION_FAIL_SESSION_EXPIRED);
                return;
            }

            Optional<UserAccount> userAccountOpt = new UserAccountDao().getUserAccountById(cinnamonSession.getUserId());
            if (userAccountOpt.isEmpty() || !userAccountOpt.get().isActivated()) {
                failAuthentication(servletResponse, ErrorCode.AUTHENTICATION_FAIL_USER_NOT_FOUND);
                return;
            }
            
            ThreadLocalSqlSession.setCurrentUser(userAccountOpt.get());
            chain.doFilter(request, response);
        } finally {
            log.debug("AuthenticationFilter: after");
        }

    }

    @Override
    public void destroy() {

    }

    private void failAuthentication(HttpServletResponse servletResponse, ErrorCode errorCode) {
        ErrorResponseGenerator.generateErrorMessage(servletResponse, errorCode);
    }


}