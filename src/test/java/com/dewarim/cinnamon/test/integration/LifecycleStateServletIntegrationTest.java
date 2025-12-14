package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.application.service.index.ParamParser;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycleState.ChangeLifecycleStateRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.model.response.CinnamonContentType;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.dewarim.cinnamon.DefaultPermission.*;
import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.api.Constants.ACL_DEFAULT;
import static org.junit.jupiter.api.Assertions.*;

public class LifecycleStateServletIntegrationTest extends CinnamonIntegrationTest {
    private final static Logger log       = LogManager.getLogger(LifecycleStateServletIntegrationTest.class);
    public static final  String CONFIG    = "<config/>";
    public static final  String NOP_STATE = NopState.class.getName();

    @Test
    public void getLifecycleStateWithInvalidRequest() {
        assertClientError(() -> client.getLifecycleState(-1L), INVALID_REQUEST);
    }

    @Test
    public void getLifecycleStateWhichDoesNotExist() {
        assertClientError(() -> client.getLifecycleState(Long.MAX_VALUE), LIFECYCLE_STATE_NOT_FOUND);
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
        assertEquals("localhost", lifecycleStateConfig.getPropertyValues("render.server.host").getFirst());
    }

    @Test
    public void attachLifecycleInvalidRequest() throws IOException {
        Long osdId = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleInvalidRequest")
                .osd.getId();
        assertClientError(() -> client.attachLifecycle(null, 1L, 1L, false), INVALID_REQUEST);
        assertClientError(() -> client.attachLifecycle(osdId, null, 1L, false), INVALID_REQUEST);
        assertClientError(() -> client.attachLifecycle(osdId, 1L, 0L, false), INVALID_REQUEST);
    }

    @Test
    public void attachLifecycleMissingOsd() throws IOException {
        AttachLifecycleRequest badOsd = new AttachLifecycleRequest(Long.MAX_VALUE, 1L, 1L, false);
        try (StandardResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd)) {
            assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
        }
    }

    @Test
    public void attachLifecycleMissingLifecycle() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd("attachLifecycleInvalidRequest");
        long osdId = toh.osd.getId();
        assertClientError(
                () -> client.attachLifecycle(osdId, Long.MAX_VALUE, 1L, false), LIFECYCLE_NOT_FOUND);
    }

    @Test
    public void attachLifecycleMissingWritePermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, adminId)
                .createAcl("attachLifecycleMissingWritePermission")
                .createGroup("attachLifecycleMissingWritePermission")
                .createAclGroup()
                .addUserToGroup(userId)
                .addPermissions(List.of(DefaultPermission.BROWSE))
                .createOsd("attachLifecycleMissingWritePermission");
        long osdId = toh.osd.getId();

        assertClientError(
                () -> client.attachLifecycle(osdId, 1L, 1L, false), NO_LIFECYCLE_STATE_WRITE_PERMISSION);
    }

    @Test
    public void changeStateFailInStateClass() throws IOException {
        // should fail on oldState.exit
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        adminClient.attachLifecycle(toh.osd.getId(), 4L, 4L, true);
        assertClientError(
                () -> client.changeLifecycleState(toh.osd.getId(), 4L), LIFECYCLE_STATE_EXIT_FAILED);
    }

    @Test
    public void attachLifecycleStateChangeFailed() throws IOException {
        // should fail on newState.enter
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        assertClientError(
                () -> client.attachLifecycle(toh.osd.getId(), 4L, 4L, false),
                LIFECYCLE_STATE_CHANGE_FAILED);
    }

    @Test
    public void attachLifecycleMissingLifecycleState() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        long osdId = toh.osd.getId();
        assertClientError(
                () -> client.attachLifecycle(osdId, 2L, null, false), LIFECYCLE_STATE_NOT_FOUND);
    }

    @Test
    public void attachLifecycleHappyPathDefaultState() throws IOException {
        var toh = new TestObjectHolder(client, userId)
                .createOsd();
        long osdId = toh.osd.getId();
        client.attachLifecycle(osdId, 1L, null, false);
        // check if lifecycle state is really attached to OSD:
        ObjectSystemData osd = new OsdServletIntegrationTest().fetchSingleOsd(osdId);
        assertEquals((Long) 1L, osd.getLifecycleStateId());
    }

    @Test()
    public void attachLifecycleHappyPathWithNonDefaultState() throws IOException {
        var       toh       = new TestObjectHolder(client, userId).createOsd();
        long      osdId     = toh.osd.getId();
        Lifecycle lifecycle = adminClient.createLifecycle("attachLifecycleHappyPathWithNonDefaultState");
        LifecycleState state = adminClient.createLifecycleState(new LifecycleState("authoring2", """
                <config>
                <properties><property><name>aclName</name><value>reviewers.acl</value></property></properties>
                <nextStates><name>published2</name></nextStates>
                </config>
                """,
                "com.dewarim.cinnamon.lifecycle.ChangeAclState", lifecycle.getId(), null));
        LifecycleState state2 = adminClient.createLifecycleState(new LifecycleState("published2", """
                <config>
                <properties><property><name>aclName</name><value>_default_acl</value></property></properties>
                 <nextStates><name>authoring2</name></nextStates>
                 </config>
                """, "com.dewarim.cinnamon.lifecycle.ChangeAclState", lifecycle.getId(), null));
        log.info("trying to attach lifecycle");
        client.attachLifecycle(osdId, lifecycle.getId(), state.getId(), false);

        ObjectSystemData osd = adminClient.getOsdById(osdId, false, false);

        // check if lifecycle state is really attached to OSD:
        assertEquals(state.getId(), osd.getLifecycleStateId());
        assertEquals(toh.getAcls().stream()
                .filter(a -> a.getName().equals("reviewers.acl"))
                .findFirst().orElseThrow().getId(), osd.getAclId());
    }

    @Test
    public void detachLifecycleInvalidRequest() throws IOException {
        IdRequest idRequest = new IdRequest(0L);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest, INVALID_REQUEST);
    }

    @Test
    public void detachLifecycleNonExistentOsd() throws IOException {
        IdRequest idRequest = new IdRequest(Long.MAX_VALUE);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__DETACH_LIFECYCLE, idRequest, OBJECT_NOT_FOUND);
    }

    @Test
    public void detachLifecycleHappyPath() throws IOException {
        var  toh   = new TestObjectHolder(client, userId).createOsd();
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
        assertClientError(() -> client.changeLifecycleState(null, null), INVALID_REQUEST);
    }

    @Test
    public void changeStateMissingObject() throws IOException {
        ChangeLifecycleStateRequest attachRequest = new ChangeLifecycleStateRequest(Long.MAX_VALUE, Long.MAX_VALUE);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__CHANGE_STATE, attachRequest, OBJECT_NOT_FOUND);
    }

    @Test
    public void changeStateWithStateNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId).createOsd();
        assertClientError(() -> client.changeLifecycleState(toh.osd.getId(), Long.MAX_VALUE), LIFECYCLE_STATE_NOT_FOUND);
    }

    @Test
    public void changeStateHappyPathWithVersioning() throws IOException {
        List<DefaultPermission> permissions = List.of(CREATE_OBJECT, LIFECYCLE_STATE_WRITE, VERSION_OBJECT, BROWSE, READ_OBJECT_CUSTOM_METADATA);
        var                     adminToh    = prepareAclGroupWithPermissions(permissions);
        var                     versionAcl  = adminToh.acl;
        adminToh = prepareAclGroupWithPermissions(permissions)
                .createLifecycle();
        var reviewAcl = adminToh.acl;
        String config = """
                <config>
                <properties><property><name>aclName</name><value>__acl__</value></property></properties>
                <nextStates><name>published</name></nextStates>
                </config>
                """;
        LifecycleState versionState = new LifecycleState(adminToh.createRandomName(), config.replace("__acl__", versionAcl.getName()),
                "com.dewarim.cinnamon.lifecycle.ChangeAclState",
                adminToh.lifecycle.getId(),
                null);

        LifecycleState reviewState = new LifecycleState(adminToh.createRandomName(), config.replace("__acl__", reviewAcl.getName()),
                "com.dewarim.cinnamon.lifecycle.ChangeAclState",
                adminToh.lifecycle.getId(),
                null);


        adminToh.createLifecycleState(versionState);
        reviewState.setLifecycleStateForCopyId(adminToh.lifecycleState.getId());

        adminToh.createLifecycleState(reviewState)
                // make reviewState the default state:
                .updateLifecycleDefaultState()
                .createOsd()
                // attach the current lifecycle with the current default state to the current OSD
                .attachLifecycle();

        // verify after attach: osd should have new aclId after attach-with-enter
        var userToh = new TestObjectHolder(client, userId);
        userToh.loadOsd(adminToh.osd.getId());

        // we have set the ACL to the new admin acl:
        assertEquals(client.getAclById(userToh.osd.getAclId()).getId(), reviewAcl.getId());
        userToh.version();
        // after versioning, expect the OSD's acl to be the one specified by lifecycleStateForCopy (versionState -> new acl):
        assertEquals(client.getAclById(userToh.osd.getAclId()).getId(), versionAcl.getId());
    }

    @Test
    public void changeStateHappyPathWithAttachDefaultState() throws IOException {
        var adminToh = prepareAclGroupWithPermissions(
                List.of(CREATE_OBJECT, LIFECYCLE_STATE_WRITE, VERSION_OBJECT, BROWSE, READ_OBJECT_CUSTOM_METADATA))
                .createLifecycle();
        String config = """
                <config>
                <properties><property><name>aclName</name><value>__acl__</value></property></properties>
                <nextStates><name>published</name></nextStates>
                </config>
                """;
        LifecycleState defaultState = new LifecycleState(adminToh.createRandomName(), config.replace("__acl__", adminToh.acl.getName()),
                "com.dewarim.cinnamon.lifecycle.ChangeAclState",
                adminToh.lifecycle.getId(),
                null);

        adminToh.createLifecycleState(defaultState)
                .createOsd()
                .setAclByNameOnOsd(ACL_DEFAULT)
                .updateLifecycleDefaultState()
                // attach the current lifecycle with the current lifecycle state to the current OSD
                .attachLifecycle();

        // verify after attach: osd should have new aclId after attach-with-enter
        var userToh = new TestObjectHolder(client, userId);
        userToh.loadOsd(adminToh.osd.getId());
        assertEquals(userToh.osd.getAclId(), (adminToh.acl.getId()));
    }

    @Test
    public void changeStateHappyPath() throws IOException {
        var toh = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT, LIFECYCLE_STATE_WRITE))
                .createOsd();
        long osdId = toh.osd.getId();
        adminClient.attachLifecycle(osdId, 2L, 2L, true);
        // when the test-lifecycle is attached, it changes the ACL to one with all permissions, so
        // we need to change to a more restricted here to verify LIFECYCLE_STATE_WRITE is allowed.
        adminClient.lockOsd(osdId);
        adminClient.updateOsd(new UpdateOsdRequest(osdId, null, null, null, toh.acl.getId(), null, null, true, false));
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
        var  toh   = prepareAclGroupWithPermissions(List.of(CREATE_OBJECT)).createOsd();
        long osdId = toh.osd.getId();
        adminClient.attachLifecycle(osdId, 2L, 2L, true);
        // when the test-lifecycle is attached, it changes the ACL to one with all permissions, so
        // we need to change to a more restricted here to verify LIFECYCLE_STATE_WRITE is allowed.
        adminClient.lockOsd(osdId);
        adminClient.updateOsd(new UpdateOsdRequest(osdId, null, null, null, toh.acl.getId(), null, null, false, false));
        assertClientError(() -> client.changeLifecycleState(osdId, 3L), NO_LIFECYCLE_STATE_WRITE_PERMISSION);
    }

    @Test
    public void getNextStatesInvalidRequest() throws IOException {
        IdRequest request = new IdRequest();
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request, INVALID_REQUEST);
    }

    @Test
    public void getNextStatesOsdNotFound() throws IOException {
        IdRequest request = new IdRequest(Long.MAX_VALUE);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE_STATE__GET_NEXT_STATES, request, OBJECT_NOT_FOUND);
    }

    @Test
    public void getNextStatesLifecycleStateNotFound() throws IOException {
        var toh = new TestObjectHolder(client, userId).createOsd();
        assertClientError(() -> client.getNextLifecycleStates(toh.osd.getId()), LIFECYCLE_STATE_NOT_FOUND);
    }

    @Test
    public void getNextStatesRequiresReadPermission() throws IOException {
        var toh = new TestObjectHolder(adminClient, adminId)
                .createAcl("no permissions for read system meta")
                .createOsd("getNextStatesRequiresReadPermission");
        assertClientError(() -> client.getNextLifecycleStates(toh.osd.getId()), NO_BROWSE_PERMISSION);
    }

    @Test
    public void getNextStatesHappyPath() throws IOException {
        var  toh   = new TestObjectHolder(client, userId).createOsd("getNextStatesHappyPath");
        Long osdId = toh.osd.getId();
        adminClient.attachLifecycle(osdId, 3L, 2L, true);
        List<LifecycleState> lifecycleStates = client.getNextLifecycleStates(osdId);
        assertTrue(lifecycleStates.size() > 0);
        LifecycleState state = lifecycleStates.getFirst();
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
        assertClientError(() ->
                client.createLifecycleState(new LifecycleState("create-it-fail", CONFIG,
                        NOP_STATE, lifecycle.getId(), null)), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void createLifecycleStateInvalidRequest() {
        assertClientError(() -> adminClient.createLifecycleState(new LifecycleState()), INVALID_REQUEST);
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
        assertClientError(() -> client.updateLifecycleState(lcs), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void updateLifecycleStateInvalidRequest() {
        assertClientError(() -> adminClient.updateLifecycleState(new LifecycleState()), INVALID_REQUEST);
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
        assertClientError(() -> adminClient.deleteLifecycleState(lcs.getId()), DB_DELETE_FAILED);
    }

    @Test
    public void deleteLifecycleStateAsNormalUser() throws IOException {
        var lifecycle = adminClient.createLifecycle("deleteLifecycleStateAsNormalUser");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("deleteLifecycleStateAsNormalUser", CONFIG, NOP_STATE, lifecycle.getId(), null));
        assertClientError(() -> client.deleteLifecycleState(lcs.getId()), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void deleteLifecycleStateInvalidRequest() {
        assertClientError(() -> adminClient.deleteLifecycleState(-1L), INVALID_REQUEST);
    }

    @Test
    public void verifySerialization() throws IOException {
        String xmlResponse;
        try (StandardResponse response = sendStandardRequestWithContentType(UrlMapping.LIFECYCLE__LIST, new ListLifecycleRequest(), CinnamonContentType.XML)) {
            xmlResponse = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        }
        Document document = ParamParser.parseXmlToDocument(xmlResponse);
        log.info("xmlResponse: {}", xmlResponse);
        List<Node> nodes = document.selectNodes("//lifecycleStates/lifecycleState");
        assertTrue(nodes.size() > 0);
    }
}
