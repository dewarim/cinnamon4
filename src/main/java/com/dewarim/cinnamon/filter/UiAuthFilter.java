package com.dewarim.cinnamon.filter;

import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * Cookie-based authentication filter for the /ui/* endpoints.
 * Reads the "cinnamonTicket" HttpOnly cookie set at login.
 * Bypasses auth for /ui/login and /ui/logout so those pages remain accessible without a valid session.
 */
public class UiAuthFilter implements Filter {

    private static final Logger log              = LogManager.getLogger(UiAuthFilter.class);
    public static final  String COOKIE_NAME      = "cinnamonTicket";
    private static final String LOGIN_PATH       = "/login";
    private static final String LOGOUT_PATH      = "/logout";
    private static final String LOGIN_REDIRECT   = "/ui/login";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  httpRequest  = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Reconstruct the full path: servletPath covers the matched part, pathInfo the rest.
        // For an exact-match servlet like UiLoginServlet ("/ui/login"), getPathInfo() is null.
        String servletPath = httpRequest.getServletPath();
        String pathInfo    = httpRequest.getPathInfo() != null ? httpRequest.getPathInfo() : "";
        String fullPath    = servletPath + pathInfo;

        // Allow login and logout pages without authentication
        if (fullPath.equals("/ui/login") || fullPath.startsWith("/ui/login/")
                || fullPath.equals("/ui/logout")) {
            chain.doFilter(request, response);
            return;
        }

        Optional<String> ticket = extractTicket(httpRequest);
        if (ticket.isEmpty()) {
            httpResponse.sendRedirect(LOGIN_REDIRECT);
            return;
        }

        Session session = new SessionDao().getSessionByTicket(ticket.get());
        if (session == null || session.getExpires().isBefore(LocalDateTime.now())) {
            log.debug("UI auth: invalid or expired session");
            httpResponse.sendRedirect(LOGIN_REDIRECT);
            return;
        }

        UserAccount user = new UserAccountDao().getUserAccountById(session.getUserId()).orElse(null);
        if (user == null || !user.isActivated() || user.isLocked()) {
            httpResponse.sendRedirect(LOGIN_REDIRECT);
            return;
        }

        RequestScope.setCurrentUser(user);
        chain.doFilter(request, response);
    }

    private Optional<String> extractTicket(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
