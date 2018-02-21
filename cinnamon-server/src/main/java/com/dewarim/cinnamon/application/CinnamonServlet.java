package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.CinnamonConnection;
import com.dewarim.cinnamon.security.HashMaker;
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

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

/**
 */
@WebServlet(name = "Cinnamon", urlPatterns = {"/*"})
public class CinnamonServlet extends HttpServlet {

    private static final Logger log = LogManager.getLogger(CinnamonServlet.class);
    private ObjectMapper xmlMapper = new XmlMapper();
    private final UserAccountDao userAccountDao = new UserAccountDao();
    // TODO: move to constants or use http core?

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

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
            default:
                hello(response);
        }

    }

    private void hello(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello Servlet</h1>");
    }

    private void connect(HttpServletRequest request, HttpServletResponse response) {
        try {
            // TODO: initial parameter check (null, non-empty)
            String username = request.getParameter("user");
            String password = request.getParameter("pwd");
            UserAccount user = userAccountDao.getUserAccountByName(username);
            if (user == null) {
                ErrorResponseGenerator.generateErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCode.CONNECTION_FAIL_INVALID_USERNAME, "valid username required"
                );
                return;
            }

            if (authenticate(user, password)) {
                // TODO: get optional uiLanguageParam.
                Session session = new SessionDao().save(new Session(user.getId(), null));
                CinnamonConnection cinnamonConnection = new CinnamonConnection(session.getTicket());

                // Return the token on the response
                response.setContentType(CONTENT_TYPE_XML);
                xmlMapper.writeValue(response.getWriter(), cinnamonConnection);
//                response.getWriter().write(String.format("<connection><ticket>%s</ticket></connection>",session.getTicket()));
            }
            else {
                // render error
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // TODO: render XML error message.
            }

        } catch (Exception e) {
            log.debug(e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // TODO: render XML error message.
        }
    }


    private boolean authenticate(UserAccount userAccount, String password) {
        return HashMaker.compareWithHash(password, userAccount.getPassword());
    }

}
