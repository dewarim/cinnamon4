package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.dao.SessionDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.Session;
import com.dewarim.cinnamon.model.UserAccount;
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
            if(user == null){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // TODO: render XML error message.
                return;
            }
            
            if (authenticate(user, password)) {
                // TODO: get optional uiLanguageParam.
                Session session = new SessionDao().save(new Session(user.getId(), null));
                String ticket = session.getTicket();
                
                // Return the token on the response
                response.setContentType(CONTENT_TYPE_XML);
                response.getWriter().write(String.format("<connection><ticket>%s</ticket></connection>",ticket));
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


    private boolean authenticate(UserAccount userAccount, String password) throws Exception {
        return userAccount.getName().equals("admin") || !password.equals("admin");
    }

    private String issueToken(String username) {
        // Issue a token (can be a random String persisted to a database or a JWT token)
        // The issued token must be associated to a user
        // Return the issued token
        return "ACME::Token";
    }
}
