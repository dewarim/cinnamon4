package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ErrorResponseGenerator;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.FailedRequestException;
import com.dewarim.cinnamon.dao.LifecycleDao;
import com.dewarim.cinnamon.dao.LifecycleStateDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.provider.StateProviderService;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.dewarim.cinnamon.Constants.CONTENT_TYPE_XML;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@WebServlet(name = "LifecycleState", urlPatterns = "/")
public class LifecycleStateServlet extends HttpServlet {

    private              ObjectMapper                     xmlMapper                          = new XmlMapper();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        OsdDao            osdDao       = new OsdDao();
        LifecycleDao      lifecycleDao = new LifecycleDao();
        LifecycleStateDao stateDao     = new LifecycleStateDao();
        try {
            switch (pathInfo) {
                case "/attachLifecycle":
                    attachLifecycleState(request, response, osdDao, lifecycleDao, stateDao);
                    break;
                case "/changeState":
                    break;
                case "/detachLifecycle":
                    detachLifecycleState(request,response, osdDao);
                    break;
                case "/getLifecycleState":
                    getLifecycleState(request, response);
                    break;
                case "/getNextStates":
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FailedRequestException e) {
            ErrorCode errorCode = e.getErrorCode();
            ErrorResponseGenerator.generateErrorMessage(response, errorCode.getHttpResponseCode(), errorCode);
        }
    }

    private void detachLifecycleState(HttpServletRequest request, HttpServletResponse response, OsdDao osdDao) throws IOException {
        IdRequest detachReq = xmlMapper.readValue(request.getInputStream(), IdRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
        Long id = detachReq.getId();
        ObjectSystemData osd = osdDao.getObjectById(id).orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        UserAccount      user         = ThreadLocalSqlSession.getCurrentUser();
        boolean          writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user);
        if (!writeAllowed) {
            ErrorResponseGenerator.generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
            return;
        }
        osd.setLifecycleStateId(null);
        osdDao.updateOsd(osd);
        ResponseUtil.responseIsGenericOkay(response);
    }

    private void attachLifecycleState(HttpServletRequest request, HttpServletResponse response, OsdDao osdDao, LifecycleDao lifecycleDao, LifecycleStateDao stateDao) throws IOException {
        AttachLifecycleRequest attachReq = xmlMapper.readValue(request.getInputStream(), AttachLifecycleRequest.class);
        if (attachReq.validated()) {
            Optional<ObjectSystemData> osdOpt = osdDao.getObjectById(attachReq.getOsdId());
            if (!osdOpt.isPresent()) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
            ObjectSystemData osd          = osdOpt.get();
            UserAccount      user         = ThreadLocalSqlSession.getCurrentUser();
            boolean          writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(osd, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user);
            if (!writeAllowed) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_FORBIDDEN, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
                return;
            }

            Optional<Lifecycle> lifecycleOpt = lifecycleDao.getLifecycleById(attachReq.getLifecycleId());
            if (!lifecycleOpt.isPresent()) {
                ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.LIFECYCLE_NOT_FOUND);
                return;
            }
            Lifecycle lifecycle = lifecycleOpt.get();

            LifecycleState           lifecycleState;
            Optional<LifecycleState> stateOpt = stateDao.getLifecycleStateById(attachReq.getLifecycleStateId());
            if (!stateOpt.isPresent()) {
                stateOpt = stateDao.getLifecycleStateById(lifecycle.getDefaultStateId());
                if (!stateOpt.isPresent()) {
                    ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.LIFECYCLE_STATE_NOT_FOUND);
                    return;
                }
            }
            lifecycleState = stateOpt.get();
            State             state             = StateProviderService.getInstance().getStateProvider(lifecycleState.getStateClass()).getState();
            StateChangeResult stateChangeResult = state.checkEnteringObject(osd, lifecycleState.getLifecycleStateConfig());
            if (stateChangeResult.isSuccessful()) {
                osd.setLifecycleStateId(lifecycleState.getId());
                osdDao.updateOsd(osd);
                ResponseUtil.responseIsGenericOkay(response);
                return;
            } else {
                ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED, stateChangeResult.toString());
                return;
            }
        } else {
            ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);
        }
    }

    private void getLifecycleState(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // ignore listRequest for now, just make sure it's valid xml:
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
                return;
            } else {
                ErrorResponseGenerator.generateErrorMessage(response, SC_NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND);
                return;
            }
        }
        ErrorResponseGenerator.generateErrorMessage(response, SC_BAD_REQUEST, ErrorCode.INVALID_REQUEST);

    }

}