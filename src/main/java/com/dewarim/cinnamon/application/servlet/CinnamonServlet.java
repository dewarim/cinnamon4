package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.login.LoginResult;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.configuration.SecurityConfig;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.DisconnectResponse;
import com.dewarim.cinnamon.security.LoginProviderService;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static com.dewarim.cinnamon.ErrorCode.CONNECTION_FAIL_WRONG_PASSWORD;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

/**
 *
 */
@WebServlet(name = "Cinnamon", urlPatterns = {"/*"})
public class CinnamonServlet extends HttpServlet {

    private static final Logger               log                  = LogManager.getLogger(CinnamonServlet.class);
    private final        ObjectMapper         xmlMapper            = XML_MAPPER;
    private final        UserAccountDao       userAccountDao       = new UserAccountDao();
    private final        LoginProviderService loginProviderService = LoginProviderService.getInstance();
    private final        CinnamonVersion      cinnamonVersion      = new CinnamonVersion();
    // TODO: move to constants or use http core?

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CINNAMON__INFO -> info(request, response);
            default -> hello(response);
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CINNAMON__CONNECT -> connect(request, cinnamonResponse);
            case CINNAMON__DISCONNECT -> disconnect(request, cinnamonResponse);
            default -> hello(cinnamonResponse);
        }
    }

    private void info(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/xml");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(xmlMapper.writeValueAsString(cinnamonVersion));
    }

    private void hello(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Cinnamon 4 Server</h1>");
    }

    private void connect(HttpServletRequest request, CinnamonResponse response) {
        String username = request.getParameter("user");
        String password = request.getParameter("password");
        // TODO: maybe add a ConnectionRequest object (with fields user & password) for consistency
        SecurityConfig securityConfig = CinnamonServer.config.getSecurityConfig();

        if (username == null || username.isEmpty()) {
            ErrorCode.CONNECTION_FAIL_INVALID_USERNAME.throwUp();
        }

        Optional<UserAccount> userOpt = userAccountDao.getUserAccountByName(username);
        if (userOpt.isEmpty()) {
            ErrorCode.CONNECTION_FAIL_INVALID_USERNAME.throwUp();
        }

        UserAccount user = userOpt.get();
        if (!user.isActivated()) {
            ErrorCode.CONNECTION_FAIL_ACCOUNT_INACTIVE.throwUp();
        }

        if (user.isLocked()) {
            ErrorCode.CONNECTION_FAIL_ACCOUNT_LOCKED.throwUp();
        }

        if (authenticate(user, password)) {
            // TODO: get optional uiLanguageParam.
            long               sessionLengthInMillis = securityConfig.getSessionLengthInMillis();
            Session            session               = new SessionDao().save(new Session(user.getId(), sessionLengthInMillis));
            CinnamonConnection cinnamonConnection    = new CinnamonConnection(session.getTicket());
            response.setWrapper(cinnamonConnection);
        } else {
            CONNECTION_FAIL_WRONG_PASSWORD.throwUp();
        }

    }

    private void disconnect(HttpServletRequest request, CinnamonResponse response) throws IOException {
        String ticket = request.getHeader("ticket");
        if (ticket == null || ticket.isBlank()) {
            ErrorCode.AUTHENTICATION_FAIL_NO_TICKET_GIVEN.throwUp();
        }
        SessionDao sessionDao = new SessionDao();
        Session    session    = sessionDao.getSessionByTicket(ticket);
        if (session != null) {
            sessionDao.delete(session.getId());
        } else {
            ErrorCode.SESSION_NOT_FOUND.throwUp();
            return;
        }
        DisconnectResponse disconnectResponse = new DisconnectResponse(true);
        response.setWrapper(disconnectResponse);
    }

    private boolean authenticate(UserAccount userAccount, String password) {
        LoginResult loginResult = loginProviderService.connect(userAccount, password);
        return loginResult.isValidUser();
    }

    @JsonRootName("CinnamonServer")
    public static class CinnamonVersion {
        private final String version;
        private final String build;

        public CinnamonVersion() {
            version = CinnamonServer.VERSION;
            try {
                Properties properties = new Properties();
                properties.load(getClass().getResourceAsStream("/buildNumber.properties"));
                build = properties.getProperty("buildNumber0");
            } catch (IOException e) {
                throw new CinnamonException("Could not load build number.");
            }
        }

        public String getVersion() {
            return version;
        }

        public String getBuild() {
            return build;
        }
    }

    public CinnamonVersion getCinnamonVersion() {
        return cinnamonVersion;
    }
}
