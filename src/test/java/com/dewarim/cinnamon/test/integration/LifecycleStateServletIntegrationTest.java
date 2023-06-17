package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.application.service.index.ParamParser;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.ChangeLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.CREATE_OBJECT;
import static com.dewarim.cinnamon.DefaultPermission.LIFECYCLE_STATE_WRITE;
import static com.dewarim.cinnamon.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

public class LifecycleStateServletIntegrationTest extends CinnamonIntegrationTest {
    private final static Logger log       = LogManager.getLogger(LifecycleStateServletIntegrationTest.class);
    public static final  String CONFIG    = "<config/>";
    public static final  String NOP_STATE = NopState.class.getName();

    @Test
    public void getLifecycleStateWithInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.getLifecycle(-1L));
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
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
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleInvalidRequest");
        long osdId = toh.osd.getId();
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(null, 1L, 1L, false));
        assertEquals(INVALID_REQUEST, ex.getErrorCode());

        ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(osdId, null, 1L, false));
        assertEquals(INVALID_REQUEST, ex.getErrorCode());

        ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(osdId, 1L, 0L, false));
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void attachLifecycleMissingOsd() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(Long.MAX_VALUE, 1L, 1L, false);
        try (ClassicHttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd)) {
            assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
        }
    }

    @Test
    public void attachLifecycleMissingLifecycle() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleInvalidRequest");
        long osdId = toh.osd.getId();
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(osdId, Long.MAX_VALUE, 1L, false));
        assertEquals(LIFECYCLE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void attachLifecycleMissingWritePermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, adminId)
                .createAcl("attachLifecycleMissingWritePermission")
                .createGroup("attachLifecycleMissingWritePermission")
                .createAclGroup()
                .addUserToGroup(userId)
                .addPermissions(List.of(DefaultPermission.READ_OBJECT_SYS_METADATA))
                .createOsd("attachLifecycleMissingWritePermission");
        long osdId = toh.osd.getId();

        var ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(osdId, 1L, 1L, false));
        assertEquals(NO_LIFECYCLE_STATE_WRITE_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void changeStateFailInStateClass() throws IOException {
        // should fail on oldState.exit
        var toh = new TestObjectHolder(client, userId)
                .createOsd("changeStateFailInStateClass");
        adminClient.attachLifecycle(toh.osd.getId(), 4L, 4L, true);
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.changeLifecycleState(toh.osd.getId(), 4L));
        assertEquals(ErrorCode.LIFECYCLE_STATE_EXIT_FAILED, ex.getErrorCode());
    }

    @Test
    public void attachLifecycleStateChangeFailed() throws IOException {
        // should fail on newState.enter
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleStateChangeFailed");
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(toh.osd.getId(), 4L, 4L, false));
        assertEquals(ErrorCode.LIFECYCLE_STATE_CHANGE_FAILED, ex.getErrorCode());
    }

    @Test
    public void attachLifecycleMissingLifecycleState() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleMissingLifecycleState");
        long osdId = toh.osd.getId();
        var ex = assertThrows(CinnamonClientException.class,
                () -> client.attachLifecycle(osdId, 2L, null, false));
        assertEquals(LIFECYCLE_STATE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void attachLifecycleHappyPathDefaultState() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleMissingLifecycleState");
        long osdId = toh.osd.getId();
        client.attachLifecycle(osdId, 1L, null, false);
        // check if lifecycle state is really attached to OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(osdId);
        assertEquals((Long) 1L, osd.getLifecycleStateId());
    }

    @Test
    public void attachLifecycleHappyPathWithNonDefaultState() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleHappyPathWithNonDefaultState");
        long osdId = toh.osd.getId();
        client.attachLifecycle(osdId, 3L, 2L, false);
        ObjectSystemData osd = adminClient.getOsdById(osdId, false, false);

        // check if lifecycle state is really attached to OSD:
        assertEquals(2L, osd.getLifecycleStateId());
        assertEquals(4L, osd.getAclId());
    }

    @Test
    public void detachLifecycleInvalidRequest() throws IOException {
        IdRequest    idRequest      = new IdRequest(0L);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest,INVALID_REQUEST);
    }

    @Test
    public void detachLifecycleNonExistentOsd() throws IOException {
        IdRequest    idRequest      = new IdRequest(Long.MAX_VALUE);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest,OBJECT_NOT_FOUND);
    }

    @Test
    public void detachLifecycleHappyPath() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("detachLifecycleHappyPath");
        long osdId = toh.osd.getId();

        // attach lifecycle:
        client.attachLifecycle(osdId, 1L, null, false);

        // detach lifecycle
        client.detachLifecycle(osdId);

        // check if lifecycle state is really detached from OSD:
        ObjectSystemData osd = client.getOsdById(osdId, false, false);
        assertNull(osd.getLifecycleStateId());
    }

    @Test
    public void changeStateInvalidRequest() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.changeLifecycleState(null, null));
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void changeStateMissingObject() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(Long.MAX_VALUE, Long.MAX_VALUE);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest,OBJECT_NOT_FOUND);
    }

    @Test
    public void changeStateWithStateNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("changeStateWithStateNotFound");
        var ex = assertThrows(CinnamonClientException.class, () -> client.changeLifecycleState(toh.osd.getId(), Long.MAX_VALUE));
        assertEquals(LIFECYCLE_STATE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void changeStateHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT, LIFECYCLE_STATE_WRITE))
                .createOsd("changeStateHappyPath");
        long osdId = toh.osd.getId();
        adminClient.attachLifecycle(osdId, 2L, 2L, true);
        // when the test-lifecycle is attached, it changes the ACL to one with all permissions, so
        // we need to change to a more restricted here to verify LIFECYCLE_STATE_WRITE is allowed.
        adminClient.lockOsd(osdId);
        adminClient.updateOsd(new UpdateOsdRequest(osdId, null, null, null, toh.acl.getId(), null, null));
        client.changeLifecycleState(osdId, 3L);
        // we have to fetch the OSD via adminClient, because after change to ACL#1, it's no longer browsable
        // for the normal test user
        ObjectSystemData osd = adminClient.getOsdById(osdId, false, false);
        assertEquals(3L, osd.getLifecycleStateId());
        // should change acl:
        assertEquals((Long) 1L, osd.getAclId());
    }

    @Test
    public void changeStateNoLifecycleStateWritePermission() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT))
                .createOsd("changeStateNoLifecycleStateWritePermission");
        long osdId = toh.osd.getId();
        adminClient.attachLifecycle(osdId, 2L, 2L, true);
        // when the test-lifecycle is attached, it changes the ACL to one with all permissions, so
        // we need to change to a more restricted here to verify LIFECYCLE_STATE_WRITE is allowed.
        adminClient.lockOsd(osdId);
        adminClient.updateOsd(new UpdateOsdRequest(osdId, null, null, null, toh.acl.getId(), null, null));
        var ex = assertThrows(CinnamonClientException.class, () -> client.changeLifecycleState(osdId, 3L));
        assertEquals(NO_LIFECYCLE_STATE_WRITE_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void getNextStatesInvalidRequest() throws IOException {
        IdRequest    request  = new IdRequest();
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request,INVALID_REQUEST);
    }

    @Test
    public void getNextStatesOsdNotFound() throws IOException {
        IdRequest    request  = new IdRequest(Long.MAX_VALUE);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request,OBJECT_NOT_FOUND);
    }

    @Test
    public void getNextStatesLifecycleStateNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("getNextStatesLifecycleStateNotFound");
        var ex = assertThrows(CinnamonClientException.class, () -> client.getNextLifecycleStates(toh.osd.getId()));
        assertEquals(LIFECYCLE_STATE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void getNextStatesRequiresReadPermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, adminId)
                .createAcl("no permissions for read system meta")
                .createOsd("getNextStatesRequiresReadPermission");
        var ex = assertThrows(CinnamonClientException.class, () -> client.getNextLifecycleStates(toh.osd.getId()));
        assertEquals(NO_READ_OBJECT_SYS_METADATA_PERMISSION, ex.getErrorCode());
    }

    @Test
    public void getNextStatesHappyPath() throws IOException {
        var  toh   = new TestObjectHolder(client, userId).createOsd("getNextStatesHappyPath");
        Long osdId = toh.osd.getId();
        adminClient.attachLifecycle(osdId, 3L, 2L, true);
        List<LifecycleState> lifecycleStates = client.getNextLifecycleStates(osdId);
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
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
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
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
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
        assertEquals(INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void verifySerialization() throws IOException {
        String xmlResponse;
        try (ClassicHttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE__LIST, new ListLifecycleRequest())) {
            xmlResponse = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        }
        Document     document    = ParamParser.parseXmlToDocument(xmlResponse);
        log.info("xmlResponse: " + xmlResponse);
        List<Node> nodes = document.selectNodes("//lifecycleStates/lifecycleState");
        assertTrue(nodes.size() > 0);
    }
}
