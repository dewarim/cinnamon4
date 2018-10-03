package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.LifecycleRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.application.ErrorResponseGenerator.generateErrorMessage;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@WebServlet(name = "Lifecycle", urlPatterns = "/")
public class LifecycleServlet extends HttpServlet {

    private ObjectMapper xmlMapper = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        switch (pathInfo) {
            case "/getLifecycle":
                getLifecycle(request, response);
                break;
            case "/listLifecycles":
                listLifecycles(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void getLifecycle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LifecycleRequest lifecycleRequest = xmlMapper.readValue(request.getInputStream(), LifecycleRequest.class);
        if (lifecycleRequest.validated()) {
            Lifecycle           lifecycle;
            Long                id           = lifecycleRequest.getId();
            LifecycleDao        lifecycleDao = new LifecycleDao();
            Optional<Lifecycle> lifecycleOpt = lifecycleDao.getLifecycleById(id);
            lifecycle = lifecycleOpt.orElseGet(() -> lifecycleDao.getLifecycleByName(lifecycleRequest.getName()).orElse(null));
            if (lifecycle == null) {
                generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            ResponseUtil.responseIsOkayAndXml(response);
            LifecycleWrapper wrapper = new LifecycleWrapper();
            wrapper.setLifecycles(Collections.singletonList(lifecycle));
            xmlMapper.writeValue(response.getWriter(), wrapper);
        } else {
            generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    private void listLifecycles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest      listRequest  = xmlMapper.readValue(request.getInputStream(), ListRequest.class);
        LifecycleDao     lifecycleDao = new LifecycleDao();
        List<Lifecycle>  lifecycles   = lifecycleDao.listLifecycles();
        LifecycleWrapper wrapper      = new LifecycleWrapper();
        wrapper.setLifecycles(lifecycles);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}