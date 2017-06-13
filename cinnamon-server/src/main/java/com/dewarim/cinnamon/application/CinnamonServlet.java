package com.dewarim.cinnamon.application;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
@WebServlet(name="Cinnamon", urlPatterns = "/cinnamon")
public class CinnamonServlet extends HttpServlet {

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello Servlet</h1>");
//        response.getWriter().println("session=" + request.getSession(true).getId());
    }
}
