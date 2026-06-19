package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.configuration.SecurityConfig;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.filter.UiAuthFilter;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.security.LoginProviderService;
import gg.jte.TemplateEngine;
import gg.jte.output.WriterOutput;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "UiLogin", urlPatterns = "/ui/login")
public class UiLoginServlet extends HttpServlet {

    private static final Logger              log                  = LogManager.getLogger(UiLoginServlet.class);
    private final        UserAccountDao       userAccountDao       = new UserAccountDao();
    private final        LoginProviderService loginProviderService = LoginProviderService.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        renderLogin(response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isBlank() || password == null) {
            renderLogin(response, "Please enter username and password.");
            return;
        }

        Optional<UserAccount> userOpt = userAccountDao.getUserAccountByName(username.trim());
        if (userOpt.isEmpty()) {
            renderLogin(response, "Invalid username or password.");
            return;
        }

        UserAccount user = userOpt.get();
        if (!user.isActivated()) {
            renderLogin(response, "Account is not activated.");
            return;
        }
        if (user.isLocked()) {
            renderLogin(response, "Account is locked.");
            return;
        }
        if (user.isPasswordExpired()) {
            renderLogin(response, "Password has expired. Please contact an administrator.");
            return;
        }

        boolean authenticated = loginProviderService.connect(user, password).isValidUser();
        if (!authenticated) {
            renderLogin(response, "Invalid username or password.");
            return;
        }

        SecurityConfig securityConfig = CinnamonServer.config.getSecurityConfig();
        Session        session        = new SessionDao().save(new Session(user.getId(), securityConfig.getSessionLengthInMillis()));

        Cookie cookie = new Cookie(UiAuthFilter.COOKIE_NAME, session.getTicket());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (securityConfig.getSessionLengthInMillis() / 1000));
        response.addCookie(cookie);
        log.debug("UI login successful for user {}", username);
        response.sendRedirect("/ui/folders");
    }

    private void renderLogin(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        TemplateEngine engine = UiServlet.getTemplateEngine();
        engine.render("login.jte", Map.of("error", errorMessage != null ? errorMessage : ""), new WriterOutput(response.getWriter()));
    }

    // Called from UiServlet when path is /logout
    static void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Clear the session cookie
        Cookie expiredCookie = new Cookie(UiAuthFilter.COOKIE_NAME, "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);

        // Delete the server-side session if present
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> UiAuthFilter.COOKIE_NAME.equals(c.getName()))
                    .findFirst()
                    .ifPresent(c -> {
                        Session session = new SessionDao().getSessionByTicket(c.getValue());
                        if (session != null) {
                            new SessionDao().delete(session.getId());
                        }
                    });
        }

        RequestScope.clearThreadLocal();
        response.sendRedirect("/ui/login");
    }
}
