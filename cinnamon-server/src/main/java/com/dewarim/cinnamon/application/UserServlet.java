package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.Constants;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.ibatis.session.SqlSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

/**
 */
@WebServlet(name = "User", urlPatterns = "/")
public class UserServlet extends HttpServlet {


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        switch (pathInfo) {
            default:
                showUserInfo(response);
        }

    }

    private void showUserInfo(HttpServletResponse response) throws IOException {

        UserAccountDao userAccountDao = new UserAccountDao();
        UserAccount admin = userAccountDao.getUserAccountByName("admin");
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(admin);
        
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(xml);
    }
}
