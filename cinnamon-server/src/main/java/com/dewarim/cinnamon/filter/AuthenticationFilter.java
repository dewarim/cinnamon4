package com.dewarim.cinnamon.filter;

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
import java.util.Optional;

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
            String ticket = servletRequest.getHeader("ticket");
            // TODO: check for empty / malformed ticket
            if (ticket == null) {
                failAuthentication(response);
                return;
            }

            Session cinnamonSession = new SessionDao().getSessionByTicket(ticket);
            if (cinnamonSession == null) {
                // TODO: include reason?
                failAuthentication(response);
                return;
            }
            if (cinnamonSession.getExpires().getTime() > new Date().getTime()) {
                // TODO: include reason?
                failAuthentication(response);
                return;
            }
            UserAccount userAccount = new UserAccountDao().getUserAccountById(cinnamonSession.getUserId());
            // TODO: check user exists / is active?
            // probably okay to assume the user exists, as we have their ticket. But the account could have been invalidated.

            ThreadLocalSqlSession.setCurrentUser(userAccount);
            chain.doFilter(request, response);
        }
//        catch (Throwable t) {
//            
//        }
        finally {
            log.debug("AuthenticationFilter: after");
        }

    }

    @Override
    public void destroy() {

    }

    private void failAuthentication(ServletResponse servletResponse) {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.reset();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }


}