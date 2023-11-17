package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.*;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.provider.StateProviderService;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.DefaultPermission.LIFECYCLE_STATE_WRITE;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

@WebServlet(name = "LifecycleState", urlPatterns = "/")
public class LifecycleStateServlet extends BaseServlet implements CruddyServlet<LifecycleState> {

    private final ObjectMapper xmlMapper = XML_MAPPER;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        OsdDao            osdDao           = new OsdDao();
        LifecycleDao      lifecycleDao     = new LifecycleDao();
        LifecycleStateDao stateDao         = new LifecycleStateDao();
        UrlMapping        mapping          = UrlMapping.getByPath(request.getRequestURI());
        CinnamonResponse  cinnamonResponse = (CinnamonResponse) response;
        UserAccount       user             = ThreadLocalSqlSession.getCurrentUser();

        switch (mapping) {
            case LIFECYCLE_STATE__ATTACH_LIFECYCLE -> attachLifecycleState(request, cinnamonResponse, osdDao, lifecycleDao, stateDao, user);
            case LIFECYCLE_STATE__CHANGE_STATE -> changeState(request, cinnamonResponse, osdDao, stateDao, user);
            case LIFECYCLE_STATE__DETACH_LIFECYCLE -> detachLifecycleState(request, cinnamonResponse, osdDao, user);
            case LIFECYCLE_STATE__GET -> getLifecycleState(request, cinnamonResponse);
            case LIFECYCLE_STATE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(request, CreateLifecycleStateRequest.class), stateDao, cinnamonResponse);
            }
            case LIFECYCLE_STATE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(request, DeleteLifecycleStateRequest.class), stateDao, cinnamonResponse);
            }
            case LIFECYCLE_STATE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(request, UpdateLifecycleStateRequest.class), stateDao, cinnamonResponse);
            }
            case LIFECYCLE_STATE__LIST -> list(convertListRequest(request, ListLifecycleStateRequest.class), stateDao, cinnamonResponse);
            case LIFECYCLE_STATE__GET_NEXT_STATES -> getNextStates(request, cinnamonResponse, osdDao, stateDao);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void getNextStates(HttpServletRequest request, CinnamonResponse response, OsdDao osdDao, LifecycleStateDao stateDao) throws IOException {
        IdRequest idRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             osdId = idRequest.getId();
        ObjectSystemData osd   = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        throwUnlessSysMetadataIsReadable(osd);

        LifecycleState        state      = stateDao.getLifecycleStateById(osd.getLifecycleStateId()).orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
        List<LifecycleState>  nextStates = new LifecycleStateDao().getLifecycleStatesByNameList(state.getLifecycleStateConfig().getNextStates());
        LifecycleStateWrapper wrapper    = new LifecycleStateWrapper(nextStates);
        response.setWrapper(wrapper);
    }

    private void changeState(HttpServletRequest request, CinnamonResponse response, OsdDao osdDao, LifecycleStateDao stateDao, UserAccount user) throws IOException {
        ChangeLifecycleStateRequest changeRequest = xmlMapper.readValue(request.getInputStream(), ChangeLifecycleStateRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             osdId   = changeRequest.getOsdId();
        Long             stateId = changeRequest.getStateId();
        ObjectSystemData osd     = osdDao.getObjectById(osdId).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());

        boolean writeAllowed = authorizationService.hasUserOrOwnerPermission(osd, LIFECYCLE_STATE_WRITE, user);
        if (!writeAllowed) {
            throw ErrorCode.NO_LIFECYCLE_STATE_WRITE_PERMISSION.exception();
        }

        LifecycleState           newLcState = stateDao.getLifecycleStateById(stateId).orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException());
        State                    newState   = StateProviderService.getInstance().getStateProvider(newLcState.getStateClass()).getState();
        Optional<LifecycleState> oldLcState = stateDao.getLifecycleStateById(osd.getLifecycleStateId());
        if (oldLcState.isPresent()) {
            State             oldState   = StateProviderService.getInstance().getStateProvider(oldLcState.get().getStateClass()).getState();
            StateChangeResult exitResult = oldState.exit(osd, newState, newLcState.getLifecycleStateConfig());
            if (!exitResult.isSuccessful()) {
                throw new FailedRequestException(ErrorCode.LIFECYCLE_STATE_EXIT_FAILED, exitResult.getCombinedMessages());
            }
        }

        changeStateAndCreateResponse(newState, osd, newLcState, osdDao, response, false);
    }

    private void changeStateAndCreateResponse(State newState, ObjectSystemData osd, LifecycleState lcState, OsdDao osdDao, CinnamonResponse response, boolean forceChange)
            throws IOException {
        StateChangeResult stateChangeResult = newState.enter(osd, lcState.getLifecycleStateConfig());
        // TODO: test only superuser may force change a new state
        if (stateChangeResult.isSuccessful() || (forceChange && authorizationService.currentUserIsSuperuser())) {
            osd.setLifecycleStateId(lcState.getId());
            osdDao.updateOsd(osd, true);
            response.responseIsGenericOkay();
        }
        else {
            throw new FailedRequestException(ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED, stateChangeResult.getCombinedMessages());
        }
    }

    private void detachLifecycleState(HttpServletRequest request, CinnamonResponse response, OsdDao osdDao, UserAccount user) throws IOException {
        IdRequest detachReq = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long             id  = detachReq.getId();
        ObjectSystemData osd = osdDao.getObjectById(id).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        verifyWritePermissionForLifecycleState(osd, user);
        osd.setLifecycleStateId(null);
        osdDao.updateOsd(osd, true);
        response.responseIsGenericOkay();
    }

    private void verifyWritePermissionForLifecycleState(ObjectSystemData osd, UserAccount user) {
        var accessFilter = AccessFilter.getInstance(user);
        if (!accessFilter.hasPermissionOnOwnable(osd, LIFECYCLE_STATE_WRITE, osd)) {
            throw ErrorCode.NO_LIFECYCLE_STATE_WRITE_PERMISSION.exception();
        }
    }

    private void attachLifecycleState(HttpServletRequest request, CinnamonResponse response, OsdDao osdDao, LifecycleDao lifecycleDao, LifecycleStateDao stateDao, UserAccount user) throws IOException {
        AttachLifecycleRequest attachReq = xmlMapper.readValue(request.getInputStream(), AttachLifecycleRequest.class);
        if (attachReq.validated()) {
            ObjectSystemData osd = osdDao.getObjectById(attachReq.getOsdId()).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
            // TODO: should we check for readSysMeta, i.e. if OSD is browsable?
            verifyWritePermissionForLifecycleState(osd, user);

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
            changeStateAndCreateResponse(newState, osd, lifecycleState, osdDao, response, attachReq.isForceChange());

        }
        else {
            ErrorResponseGenerator.generateErrorMessage(response, ErrorCode.INVALID_REQUEST);
        }
    }

    private void getLifecycleState(HttpServletRequest request, CinnamonResponse response) throws IOException {
        IdRequest         stateRequest = xmlMapper.readValue(request.getInputStream(), IdRequest.class);
        LifecycleStateDao stateDao     = new LifecycleStateDao();
        if (stateRequest.validated()) {
            Optional<LifecycleState> state   = stateDao.getLifecycleStateById(stateRequest.getId());
            LifecycleStateWrapper    wrapper = new LifecycleStateWrapper();
            wrapper.setLifecycleStates(Collections.singletonList(state.orElseThrow(ErrorCode.LIFECYCLE_STATE_NOT_FOUND.getException())));
            response.setWrapper(wrapper);
            return;
        }
        ErrorCode.INVALID_REQUEST.throwUp();
    }

    @Override
    public ObjectMapper getMapper() {
        return XML_MAPPER;
    }
}