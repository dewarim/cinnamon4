package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;

@WebServlet(name = "Lifecycle", urlPatterns = "/")
public class LifecycleServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/listLifecycles":
                listLifecycles(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void listLifecycles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest      listRequest  = xmlMapper.readValue(request.getInputStream(), ListRequest.class);
        LifecycleDao     lifecycleDao = new LifecycleDao();
        List<Lifecycle>  lifecycles   = lifecycleDao.listLifecycles();
        LifecycleWrapper wrapper      = new LifecycleWrapper();
        wrapper.setLifecycles(lifecycles);
        response.setContentType(CONTENT_TYPE_XML);
        response.setStatus(HttpServletResponse.SC_OK);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}