package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.ChangeLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.provider.StateProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.CONTENT_TYPE_XML;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "LifecycleState", urlPatterns = "/")
public class LifecycleStateServlet extends BaseServlet {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        OsdDao            osdDao       = new OsdDao();
        LifecycleDao      lifecycleDao = new LifecycleDao();
        LifecycleStateDao stateDao     = new LifecycleStateDao();
        switch (pathInfo) {
            case "/attachLifecycle":
                attachLifecycleState(request, response, osdDao, lifecycleDao, stateDao);
                break;
            case "/changeState":
                changeState(request, response, osdDao, stateDao);
                break;
            case "/detachLifecycle":
                detachLifecycleState(request, response, osdDao);
                break;
            case "/getLifecycleState":
                getLifecycleState(request, response);
                break;
            case "/getNextStates":
                getNextStates(request, response, osdDao, stateDao);
                break;
            default:
                ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void getNextStates(HttpServletRequest request, HttpServletResponse response, OsdDao osdDao, LifecycleStateDao stateDao) throws IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             osdId = idRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessSysMetadataIsReadable(osd);

        LifecycleState        state      = stateDao.getLifecycleStateById(osd.getLifecycleStateId()).orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
        List<LifecycleState>  nextStates = new LifecycleStateDao().getLifecycleStatesByNameList(state.getLifecycleStateConfig().getNextStates());
        LifecycleStateWrapper wrapper    = new LifecycleStateWrapper(nextStates);
        ResponseUtil.responseIsOkayAndXml(response);
        xmlMapper.writeValue(response.getOutputStream(), wrapper);
    }

    private void changeState(HttpServletRequest request, HttpServletResponse response, OsdDao osdDao, LifecycleStateDao stateDao) throws IOException {
        ChangeLifecycleStateRequest changeRequest = xmlMapper.readValue(request.getInputStream(), ChangeLifecycleStateRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             osdId     = changeRequest.getOsdId();
        Long             stateId   = changeRequest.getStateId();
        String           stateName = changeRequest.getStateName();
        ObjectSystemData osd       = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessSysMetadataIsWritable(osd);

        LifecycleState newLcState;
        if (stateName != null) {
            newLcState = stateDao.getLifecycleStateByName(stateName)
                    .orElseThrow(ErrorCode.LIFECYCLE_STATE_BY_NAME_NOT_FOUND.getException());
        } else {
            newLcState = stateDao.getLifecycleStateById(stateId).orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
        }
        State                    newState   = StateProviderService.getInstance().getStateProvider(newLcState.getStateClass()).getState();
        Optional<LifecycleState> oldLcState = stateDao.getLifecycleStateById(osd.getLifecycleStateId());
        if (oldLcState.isPresent()) {
            State             oldState   = StateProviderService.getInstance().getStateProvider(oldLcState.get().getStateClass()).getState();
            StateChangeResult exitResult = oldState.exit(osd, newState, newLcState.getLifecycleStateConfig());
            if (!exitResult.isSuccessful()) {
                throw new FailedRequestException(ErrorCode.LIFECYCLE_STATE_EXIT_FAILED, exitResult.getCombinedMessages());
            }
        }

        changeStateAndCreateResponse(newState, osd, newLcState, osdDao, response);
    }

    private void changeStateAndCreateResponse(State newState, ObjectSystemData osd, LifecycleState lcState, OsdDao osdDao, HttpServletResponse response)
            throws IOException {
        StateChangeResult stateChangeResult = newState.enter(osd, lcState.getLifecycleStateConfig());
        if (stateChangeResult.isSuccessful()) {
            osd.setLifecycleStateId(lcState.getId());
            osdDao.updateOsd(osd, true);
            ResponseUtil.responseIsGenericOkay(response);
        } else {
            throw new FailedRequestException(ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED, stateChangeResult.getCombinedMessages());
        }
    }

    private void detachLifecycleState(HttpServletRequest request, HttpServletResponse response, OsdDao osdDao) throws IOException {
        IdRequest detachReq = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             id  = detachReq.getId();
        ObjectSystemData osd = osdDao.getObjectById(id).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessSysMetadataIsWritable(osd);

        osd.setLifecycleStateId(null);
        osdDao.updateOsd(osd, true);
        ResponseUtil.responseIsGenericOkay(response);
    }

    private void attachLifecycleState(HttpServletRequest request, HttpServletResponse response, OsdDao osdDao, LifecycleDao lifecycleDao, LifecycleStateDao stateDao) throws IOException {
        AttachLifecycleRequest attachReq = xmlMapper.readValue(request.getInputStream(), AttachLifecycleRequest.class);
        if (attachReq.validated()) {
            ObjectSystemData osd = osdDao.getObjectById(attachReq.getOsdId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
            throwUnlessSysMetadataIsWritable(osd);

            Lifecycle lifecycle = lifecycleDao.getLifecycleById(attachReq.getLifecycleId())
                    .orElseThrow(ErrorCode.LIFECYCLE_NOT_FOUND.getException());

            LifecycleState           lifecycleState;
            Optional<LifecycleState> stateOpt = stateDao.getLifecycleStateById(attachReq.getLifecycleStateId());
            if (stateOpt.isEmpty()) {
                stateOpt = stateDao.getLifecycleStateById(lifecycle.getDefaultStateId());
                if (stateOpt.isEmpty()) {
                    ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND);
                    return;
                }
            }
            lifecycleState = stateOpt.get();
            State newState = StateProviderService.getInstance().getStateProvider(lifecycleState.getStateClass()).getState();
            changeStateAndCreateResponse(newState, osd, lifecycleState, osdDao, response);

        } else {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
        }
    }

    private void getLifecycleState(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IdRequest         stateRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        LifecycleStateDao stateDao     = new LifecycleStateDao();
        if (stateRequest.validated()) {
            Optional<LifecycleState> state = stateDao.getLifecycleStateById(stateRequest.getId());
            if (state.isPresent()) {
                LifecycleStateWrapper wrapper = new LifecycleStateWrapper();
                wrapper.setLifecycleStates(Collections.singletonList(state.get()));
                response.setContentType(CONTENT_TYPE_XML);
                response.setStatus(HttpServletResponse.SC_OK);
                xmlMapper.writeValue(response.getWriter(), wrapper);
            } else {
                ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.OBJECT_NOT_FOUND);
            }
            return;
        }
        ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
    }

}