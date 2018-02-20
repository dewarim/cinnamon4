package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationFilter implements Filter {
    private static final Logger log = LogManager.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            log.debug("AuthenticationFilter: before");
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            String ticket = servletRequest.getHeader("ticket");
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

            UserAccount userAccount = new UserAccountDao().getUserAccountById(cinnamonSession.getUserId());
            if (userAccount == null || !userAccount.isActivated()) {
                failAuthentication(servletResponse, ErrorCode.AUTHENTICATION_FAIL_USER_NOT_FOUND);
                return;
            }
            
            ThreadLocalSqlSession.setCurrentUser(userAccount);
            chain.doFilter(request, response);
        } finally {
            log.debug("AuthenticationFilter: after");
        }

    }

    @Override
    public void destroy() {

    }

    private void failAuthentication(HttpServletResponse servletResponse, ErrorCode errorCode) {
        ErrorResponseGenerator.generateErrorMessage(servletResponse,
                HttpServletResponse.SC_FORBIDDEN, errorCode, "authentication failed");
    }


}