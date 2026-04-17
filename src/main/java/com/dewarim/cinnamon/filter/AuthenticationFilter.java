package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
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
                failAuthentication(servletRequest, servletResponse, ErrorCode.AUTHENTICATION_FAIL_NO_TICKET_GIVEN);
                return;
            }

            Session cinnamonSession = new SessionDao().getSessionByTicket(ticket);
            if (cinnamonSession == null) {
                failAuthentication(servletRequest, servletResponse, ErrorCode.AUTHENTICATION_FAIL_NO_SESSION_FOUND);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            if (cinnamonSession.getExpires().isBefore(now)) {
                failAuthentication(servletRequest, servletResponse, ErrorCode.AUTHENTICATION_FAIL_SESSION_EXPIRED);
                return;
            }

            Optional<UserAccount> userAccountOpt = new UserAccountDao().getUserAccountById(cinnamonSession.getUserId());
            if (userAccountOpt.isEmpty() || !userAccountOpt.get().isActivated()) {
                failAuthentication(servletRequest, servletResponse, ErrorCode.AUTHENTICATION_FAIL_USER_NOT_FOUND);
                return;
            }
            LocalDateTime expirationDate = now.plusNanos(CinnamonServer.config.getSecurityConfig().getSessionLengthInMillis() * 1_000_000L);
            cinnamonSession.setExpires(expirationDate);
            new SessionDao().update(cinnamonSession);
            RequestScope.setCurrentUser(userAccountOpt.get());
            chain.doFilter(request, response);
        } finally {
            log.debug("AuthenticationFilter: after");
        }

    }

    private void failAuthentication(HttpServletRequest request, HttpServletResponse servletResponse, ErrorCode errorCode) {
        log.info("Authentication failed: {}", errorCode);
        ErrorResponseGenerator.generateErrorMessage(request, servletResponse, errorCode);
    }


}
