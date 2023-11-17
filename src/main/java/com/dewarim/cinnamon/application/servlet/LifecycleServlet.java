package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.lifecycle.CreateLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.DeleteLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.UpdateLifecycleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

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

    @Override
    public ObjectMapper getMapper() {
        return XML_MAPPER;
    }
}