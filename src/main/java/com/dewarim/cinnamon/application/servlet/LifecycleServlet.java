package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.LifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.CreateLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.DeleteLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.UpdateLifecycleRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static com.dewarim.cinnamon.application.ErrorResponseGenerator.generateErrorMessage;

@WebServlet(name = "Lifecycle", urlPatterns = "/")
public class LifecycleServlet extends HttpServlet implements CruddyServlet<Lifecycle> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        LifecycleDao     lifecycleDao     = new LifecycleDao();
        switch (mapping) {
            case LIFECYCLE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateLifecycleRequest.class), lifecycleDao, cinnamonResponse);
            }
            case LIFECYCLE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteLifecycleRequest.class), lifecycleDao, cinnamonResponse);
            }
            case LIFECYCLE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateLifecycleRequest.class), lifecycleDao, cinnamonResponse);
                addLifecycleStates(cinnamonResponse);
            }
            case LIFECYCLE__LIST -> {
                list(convertListRequest(request, ListLifecycleRequest.class), lifecycleDao, cinnamonResponse);
                addLifecycleStates(cinnamonResponse);
            }
            case LIFECYCLE__GET -> getLifecycle(request, response);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void addLifecycleStates(CinnamonResponse cinnamonResponse) {
        List<Lifecycle>      lifecycles   = (List<Lifecycle>) cinnamonResponse.getWrapper().list();
        LifecycleStateDao    stateDao     = new LifecycleStateDao();
        Map<Long, Lifecycle> lifecycleMap = lifecycles.stream().collect(Collectors.toMap(Lifecycle::getId, Function.identity()));
        stateDao.list().stream()
                .filter(state -> lifecycleMap.containsKey(state.getLifecycleId()))
                .forEach(state -> lifecycleMap.get(state.getLifecycleId()).getLifecycleStates().add(state));
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

    @Override
    public ObjectMapper getMapper() {
        return XML_MAPPER;
    }
}