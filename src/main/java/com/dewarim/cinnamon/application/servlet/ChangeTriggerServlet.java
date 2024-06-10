package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.ChangeTriggerDao;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.request.changeTrigger.CreateChangeTriggerRequest;
import com.dewarim.cinnamon.model.request.changeTrigger.DeleteChangeTriggerRequest;
import com.dewarim.cinnamon.model.request.changeTrigger.ListChangeTriggerRequest;
import com.dewarim.cinnamon.model.request.changeTrigger.UpdateChangeTriggerRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpStatus;

import java.io.IOException;

import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "ChangeTrigger", urlPatterns = "/")
public class ChangeTriggerServlet extends HttpServlet implements CruddyServlet<ChangeTrigger> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        ChangeTriggerDao        changeTriggerDao        = new ChangeTriggerDao();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case CHANGE_TRIGGER__LIST -> list(convertListRequest(request, ListChangeTriggerRequest.class), changeTriggerDao, cinnamonResponse);
            case CHANGE_TRIGGER__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateChangeTriggerRequest.class), changeTriggerDao, cinnamonResponse);
            }
            case CHANGE_TRIGGER__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateChangeTriggerRequest.class), changeTriggerDao, cinnamonResponse);
            }
            case CHANGE_TRIGGER__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteChangeTriggerRequest.class), changeTriggerDao, cinnamonResponse);
            }
            case CHANGE_TRIGGER__NOP -> {
                nop(request,response,cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void nop(HttpServletRequest request, HttpServletResponse response, CinnamonResponse cinnamonResponse) {
        var ctrWrapper = new ChangeTriggerResponseWrapper();
        cinnamonResponse.setWrapper(ctrWrapper);
        cinnamonResponse.setStatusCode(HttpStatus.SC_OK);
    }

    @Override
    public ObjectMapper getMapper() {
        return xmlMapper;
    }
}
