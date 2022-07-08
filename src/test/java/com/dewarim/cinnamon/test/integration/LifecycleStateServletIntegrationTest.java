package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.ChangeLifecycleStateRequest;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LifecycleStateServletIntegrationTest extends CinnamonIntegrationTest {

    public static final String CONFIG    = "<config/>";
    public static final String NOP_STATE = NopState.class.getName();

    @Test
    public void getLifecycleStateWithInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLifecycle(-1L));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void getLifecycleStateWhichDoesNotExist() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLifecycle(Long.MAX_VALUE));
        assertEquals(ErrorCode.OBJECT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void getLifecycleState() throws IOException {
        LifecycleState state = client.getLifecycleState(1L);
        assertEquals(1L, (long) state.getId());
        assertEquals("newRenderTask", state.getName());
        assertEquals("com.dewarim.cinnamon.lifecycle.NopState", state.getStateClass());
        assertEquals(2L, (long) state.getLifecycleId());
        assertEquals(1L, (long) state.getLifecycleStateForCopyId());

        assertEquals("<config><properties><property><name>render.server.host</name><value>localhost</value></property></properties><nextStates/></config>", state.getConfig());
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
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void attachLifecycleMissingLifecycle() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(28L, Long.MAX_VALUE, 1L);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_NOT_FOUND);
    }

    @Test
    public void attachLifecycleMissingWritePermission() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(27L, 1L, 1L);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION);
    }

    @Test
    public void changeStateFailInStateClass() throws IOException {
        // should fail on oldState.exit
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(32L, 4L);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_EXIT_FAILED);

    }

    @Test
    public void attachLifecycleStateChangeFailed() throws IOException {
        // should fail on newState.enter
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(33L, 4L);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED);
    }

    @Test
    public void attachLifecycleMissingLifecycleState() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(28L, 2L, null);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND);
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
        assertCinnamonError(detachResponse, ErrorCode.OBJECT_NOT_FOUND);
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
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(34L, null);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void changeStateMissingObject() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(Long.MAX_VALUE, Long.MAX_VALUE);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void changeStateWithStateNotFound() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(34L,  Long.MAX_VALUE);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND);
    }

    @Test
    public void changeStateHappyPath() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(31L,  3L);
        HttpResponse                response      = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest);
        parseGenericResponse(response);

        // check if lifecycle state is really set on the OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(31L);
        assertEquals((Long) 3L, osd.getLifecycleStateId());

        // check if ACL has changed:
        assertEquals((Long) 1L, osd.getAclId());
    }

    @Test
    public void getNextStatesInvalidRequest() throws IOException {
        IdRequest    request  = new IdRequest();
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getNextStatesOsdNotFound() throws IOException {
        IdRequest    request  = new IdRequest(Long.MAX_VALUE);
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getNextStatesLifecycleStateNotFound() throws IOException {
        // osd#34 also used in changeStateWithStateNotFound
        IdRequest    request  = new IdRequest(34L);
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND);
    }

    @Test
    public void getNextStatesRequiresReadPermission() throws IOException {
        IdRequest    request  = new IdRequest(27L);
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request);
        assertCinnamonError(response, ErrorCode.NO_READ_OBJECT_SYS_METADATA_PERMISSION);
    }

    @Test
    public void getNextStatesHappyPath() throws IOException {
        List<LifecycleState> lifecycleStates = client.getNextLifecycleStates(35L);
        assertTrue(lifecycleStates.size() > 0);
        LifecycleState state = lifecycleStates.get(0);
        assertEquals("published", state.getName());
    }

    @Test
    public void createLifecycleState() throws IOException {
        var lifecycle = adminClient.createLifecycle("for-lcs-create");
        var lifecycleState = adminClient.createLifecycleState(
                new LifecycleState("create-it", CONFIG, NOP_STATE, lifecycle.getId(), null));
        var createdState = client.getLifecycleState(lifecycleState.getId());
        assertEquals(lifecycleState, createdState);
        assertEquals("create-it", createdState.getName());
    }

    @Test
    public void createLifecycleStateAsNormalUser() throws IOException {
        var lifecycle = adminClient.createLifecycle("for-lcs-create-fail");
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.createLifecycleState(new LifecycleState("create-it-fail", CONFIG,
                        NOP_STATE, lifecycle.getId(), null)));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void createLifecycleStateInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.createLifecycleState(new LifecycleState()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void updateLifecycleState() throws IOException {
        var lifecycle = adminClient.createLifecycle("for-lcs-update");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("update-me", CONFIG, NOP_STATE, lifecycle.getId(), null));
        lcs.setLifecycleStateForCopyId(lcs.getId());
        adminClient.updateLifecycleState(lcs);
        LifecycleState updatedLcs = client.getLifecycleState(lcs.getId());
        assertEquals(lcs, updatedLcs);
    }

    @Test
    public void updateLifecycleStateAsNormalUser() throws IOException {
        var lifecycle = adminClient.createLifecycle("for-lcs-update-fail");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("update-me-fail", CONFIG, NOP_STATE, lifecycle.getId(), null));
        var ex = assertThrows(CinnamonClientException.class, () -> client.updateLifecycleState(lcs));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateLifecycleStateInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.updateLifecycleState(new LifecycleState()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void deleteLifecycleState() throws IOException {
        var lifecycle = adminClient.createLifecycle("for-lcs-delete");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("delete-me-lcs", CONFIG, NOP_STATE, lifecycle.getId(), null));
        adminClient.deleteLifecycleState(lcs.getId());
        assertTrue(client.listLifecycles().stream().noneMatch(l -> l.getName().equals(lcs.getName())));
    }

    @Test
    public void deleteShouldFailWhenInUse() throws IOException {
        var lifecycle = adminClient.createLifecycle("for-lcs-delete-in-use");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("delete-me-lcs-in-use", CONFIG, NOP_STATE, lifecycle.getId(), null));
        lifecycle.setDefaultStateId(lcs.getId());
        adminClient.updateLifecycle(lifecycle);
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteLifecycleState(lcs.getId()));
        assertEquals(ErrorCode.DB_DELETE_FAILED, ex.getErrorCode());
    }

    @Test
    public void deleteLifecycleStateAsNormalUser() throws IOException {
        var lifecycle = adminClient.createLifecycle("deleteLifecycleStateAsNormalUser");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("deleteLifecycleStateAsNormalUser", CONFIG, NOP_STATE, lifecycle.getId(), null));
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteLifecycleState(lcs.getId()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteLifecycleStateInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteLifecycleState(-1L));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void verifySerialization() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE__LIST, new ListLifecycleRequest());
        String       xmlResponse        = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        // <lifecycleState> was shown as <lifecycleStates>
        assertTrue(xmlResponse.contains("<lifecycleStates><lifecycleState>"));
    }
}
