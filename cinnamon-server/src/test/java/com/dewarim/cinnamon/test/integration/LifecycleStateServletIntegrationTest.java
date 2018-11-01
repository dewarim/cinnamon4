package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.ChangeLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LifecycleStateServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void getLifecycleStateWithInvalidRequest() throws IOException {
        IdRequest    idRequest = new IdRequest(-1L);
        HttpResponse response  = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_LIFECYCLE_STATE, idRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getLifecycleStateWhichDoesNotExist() throws IOException {
        IdRequest    idRequest = new IdRequest(Long.MAX_VALUE);
        HttpResponse response  = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_LIFECYCLE_STATE, idRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getLifecycleState() throws IOException {
        IdRequest            idRequest       = new IdRequest(1L);
        HttpResponse         response        = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_LIFECYCLE_STATE, idRequest);
        List<LifecycleState> lifecycleStates = parseResponse(response);
        assertNotNull(lifecycleStates);
        LifecycleState state = lifecycleStates.get(0);
        assertEquals(1L, (long) state.getId());
        assertEquals("newRenderTask", state.getName());
        assertEquals("NopState", state.getStateClass());
        assertEquals(2L, (long) state.getLifecycleId());
        assertEquals(1L, (long) state.getLifecycleStateForCopyId());

        assertEquals("<config><properties><property><name>render.server.host</name><value>localhost</value></property></properties></config>", state.getConfig());
        LifecycleStateConfig lifecycleStateConfig = state.getLifecycleStateConfig();
        assertNotNull(lifecycleStateConfig);
        assertEquals("localhost", lifecycleStateConfig.getPropertyValues("render.server.host").get(0));
    }

    @Test
    public void attachLifecycleInvalidRequest() throws IOException {
        // TODO: add unit test for ALR to cover all branches of invalid requests.
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(null, 1L, 1L);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);

        AttachLifecycleRequest badLifecycle = new AttachLifecycleRequest(28L, null, 1L);
        response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badLifecycle);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);

        AttachLifecycleRequest badLifecycleState = new AttachLifecycleRequest(28L, 1L, 0L);
        response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badLifecycleState);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void attachLifecycleMissingOsd() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(Long.MAX_VALUE, 1L, 1L);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void attachLifecycleMissingLifecycle() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(28L, Long.MAX_VALUE, 1L);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void attachLifecycleMissingWritePermission() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(27L, 1L, 1L);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION, SC_UNAUTHORIZED);
    }

    @Test
    public void changeStateFailInStateClass() throws IOException {
        // should fail on oldState.exit
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(32L, null, 4L);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_EXIT_FAILED);

    }

    @Test
    public void attachLifecycleStateChangeFailed() throws IOException {
        // should fail on newState.enter
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(33L, null, 4L);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED);
    }

    @Test
    public void attachLifecycleMissingLifecycleState() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(28L, 2L, null);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void attachLifecycleHappyPathDefaultState() throws IOException {
        AttachLifecycleRequest attachRequest = new AttachLifecycleRequest(28L, 1L, null);
        HttpResponse           response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, attachRequest);
        parseGenericResponse(response);

        // check if lifecycle state is really attached to OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(28L);
        assertEquals((Long) 1L, osd.getLifecycleStateId());
    }

    @Test
    public void attachLifecycleHappyPathWithNonDefaultState() throws IOException {
        AttachLifecycleRequest attachRequest = new AttachLifecycleRequest(30L, 3L, 2L);
        HttpResponse           response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, attachRequest);
        parseGenericResponse(response);

        // check if lifecycle state is really attached to OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(30L);
        assertEquals((Long) 2L, osd.getLifecycleStateId());
    }

    @Test
    public void detachLifecycleInvalidRequest() throws IOException {
        IdRequest    idRequest      = new IdRequest(0L);
        HttpResponse detachResponse = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest);
        assertCinnamonError(detachResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void detachLifecycleNonExistentOsd() throws IOException {
        IdRequest    idRequest      = new IdRequest(Long.MAX_VALUE);
        HttpResponse detachResponse = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest);
        assertCinnamonError(detachResponse, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void detachLifecycleHappyPath() throws IOException {
        // attach lifecycle:
        AttachLifecycleRequest attachRequest = new AttachLifecycleRequest(29L, 1L, null);
        HttpResponse           response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, attachRequest);
        assertResponseOkay(response);

        // detach lifecycle
        IdRequest    idRequest      = new IdRequest(29L);
        HttpResponse detachResponse = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest);
        assertResponseOkay(detachResponse);

        // check if lifecycle state is really detached from OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(29L);
        assertNull(osd.getLifecycleStateId());
    }

    @Test
    public void changeStateInvalidRequest() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(34L, null, null);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void changeStateMissingObject() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(Long.MAX_VALUE, "foo", null);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void changeStateNamedStateNotFound() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(34L, "foo", null);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_BY_NAME_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void changeStateWithStateNotFound() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(34L, null, Long.MAX_VALUE);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void changeStateHappyPath() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(31L, null, 3L);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        parseGenericResponse(response);

        // check if lifecycle state is really set on the OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(31L);
        assertEquals((Long) 3L, osd.getLifecycleStateId());

        // check if ACL has changed:
        assertEquals((Long) 1L, osd.getAclId());
    }


    private List<LifecycleState> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        LifecycleStateWrapper stateWrapper = mapper.readValue(response.getEntity().getContent(), LifecycleStateWrapper.class);
        assertNotNull(stateWrapper);
        return stateWrapper.getLifecycleStates();
    }

}
