package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.api.login.LoginResult;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.model.response.DisconnectResponse;
import com.dewarim.cinnamon.security.LoginProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

/**
 */
@WebServlet(name = "Cinnamon", urlPatterns = {"/*"})
public class CinnamonServlet extends HttpServlet {

    private static final Logger log = LogManager.getLogger(CinnamonServlet.class);
    private ObjectMapper xmlMapper = new XmlMapper();
    private final UserAccountDao userAccountDao = new UserAccountDao();
    private LoginProviderService loginProviderService = LoginProviderService.getInstance();
    // TODO: move to constants or use http core?

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            // prevent NPE
            pathInfo = "/";
        }
        switch (pathInfo) {
            default:
                hello(response);
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/connect":
                connect(request, response);
                break;
            case "/disconnect":
                disconnect(request, response);
                break;
            default:
                hello(response);
        }

    }

    private void hello(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Cinnamon 4 Server</h1>");
    }

    private void connect(HttpServletRequest request, HttpServletResponse response) {
        try {
            // TODO: initial parameter check (null, non-empty)
            String                username = request.getParameter("user");
            String                password = request.getParameter("pwd");
            Optional<UserAccount> userOpt     = userAccountDao.getUserAccountByName(username);
            
            if (!userOpt.isPresent()) {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCode.CONNECTION_FAIL_INVALID_USERNAME, "valid username required"
                );
                return;
            }
            
            UserAccount user = userOpt.get();
            if (!user.isActivated()) {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCode.CONNECTION_FAIL_ACCOUNT_INACTIVE, "user account is not active"
                );
                return;
            }

            if (user.isLocked()) {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCode.CONNECTION_FAIL_ACCOUNT_LOCKED, "user account is locked"
                );
                return;
            }

            if (authenticate(user, password)) {
                // TODO: get optional uiLanguageParam.
                Session session = new SessionDao().save(new Session(user.getId(), null));
                CinnamonConnection cinnamonConnection = new CinnamonConnection(session.getTicket());

                // Return the token on the response
                response.setContentType(CONTENT_TYPE_XML);
                response.setStatus(HttpServletResponse.SC_OK);
                xmlMapper.writeValue(response.getWriter(), cinnamonConnection);
            }
            else {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCode.CONNECTION_FAIL_WRONG_PASSWORD, "wrong password");
            }
        } catch (Exception e) {
            // TODO: test with unit test & mocked request which throws exception etc
            log.debug("connect failed for unknown reason:", e);
            ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void disconnect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ticket = request.getHeader("ticket");
        SessionDao sessionDao = new SessionDao();
        Session session = sessionDao.getSessionByTicket(ticket);
        if (session != null) {
            sessionDao.delete(session.getId());
        }
        else {
            ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, ErrorCode.SESSION_NOT_FOUND);
            return;
        }
        DisconnectResponse disconnectResponse = new DisconnectResponse(true);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), disconnectResponse);
    }

    private boolean authenticate(UserAccount userAccount, String password) {
        LoginResult loginResult = loginProviderService.connect(userAccount, password);
        return loginResult.isValidUser();
    }

}
