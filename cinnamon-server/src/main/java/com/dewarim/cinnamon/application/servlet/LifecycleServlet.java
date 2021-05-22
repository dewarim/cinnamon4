package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.LifecycleRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.application.ErrorResponseGenerator.generateErrorMessage;

@WebServlet(name = "Lifecycle", urlPatterns = "/")
public class LifecycleServlet extends HttpServlet {

    private final ObjectMapper xmlMapper = new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

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
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
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
                generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            List<LifecycleState> lifecycleStates = new LifecycleStateDao().getLifecycleStatesByLifecycleId(lifecycle.getId());
            lifecycle.setLifecycleStates(lifecycleStates);
            ResponseUtil.responseIsOkayAndXml(response);
            LifecycleWrapper wrapper = new LifecycleWrapper();
            wrapper.setLifecycles(Collections.singletonList(lifecycle));
            xmlMapper.writeValue(response.getWriter(), wrapper);
        } else {
            generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
        }
    }

    private void listLifecycles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
        ListRequest      listRequest  = xmlMapper.readValue(request.getInputStream(), ListLifecycleRequest.class);
        LifecycleDao     lifecycleDao = new LifecycleDao();
        List<Lifecycle>  lifecycles   = lifecycleDao.listLifecycles();
        LifecycleWrapper wrapper      = new LifecycleWrapper();
        wrapper.setLifecycles(lifecycles);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getWriter(), wrapper);
    }

}