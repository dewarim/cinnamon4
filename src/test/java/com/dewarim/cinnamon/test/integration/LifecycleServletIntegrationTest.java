package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.*;
import static com.dewarim.cinnamon.test.integration.LifecycleStateServletIntegrationTest.CONFIG;
import static com.dewarim.cinnamon.test.integration.LifecycleStateServletIntegrationTest.NOP_STATE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class LifecycleServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listLifecycles() throws IOException {
        List<Lifecycle> lifecycles = client.listLifecycles();

        assertNotNull(lifecycles);
        assertFalse(lifecycles.isEmpty());
        // actual list depends on whether OsdServletIntegrationTest has run and added another lifecycle.
        assertTrue(lifecycles.size() >= 4);

        Optional<Lifecycle> typeOpt = lifecycles.stream().filter(lifecycle -> lifecycle.getName().equals("review.lc"))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        Lifecycle type = typeOpt.get();
        assertThat(type.getId(), equalTo(1L));
    }

    @Test
    public void getLifecycleHappyPathWithId() throws IOException {
        Lifecycle lifecycle = client.getLifecycle(1L);
        assertEquals(Long.valueOf(1L), lifecycle.getId());
    }

    @Test
    public void getLifecycleHappyPathWithName() throws IOException {
        Lifecycle lifecycle = client.getLifecycleByName("render.lc");
        assertEquals("render.lc", lifecycle.getName());
        assertEquals(Long.valueOf(2), lifecycle.getId());
        List<LifecycleState> lifecycleStates = lifecycle.getLifecycleStates();
        assertEquals(1, lifecycleStates.size());
        LifecycleState newRenderTaskState = lifecycleStates.getFirst();
        assertEquals("newRenderTask", newRenderTaskState.getName());
        assertEquals("com.dewarim.cinnamon.lifecycle.NopState", newRenderTaskState.getStateClass());
    }

    @Test
    public void createLifecycleTest() throws IOException {
        Lifecycle lifecycle = adminClient.createLifecycle("foo-cycle");
        assertNotNull(lifecycle.getId());
        assertEquals("foo-cycle", lifecycle.getName());
        Lifecycle fooCycle = client.listLifecycles().stream()
                .filter(lifecycle1 -> lifecycle1.getName().equals("foo-cycle"))
                .findFirst().orElseThrow();
        assertEquals(lifecycle, fooCycle);
    }

    @Test
    public void createLifecycleAsNormalUser() {
        assertClientError(() -> client.createLifecycle("failed-create"), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void createLifecycleInvalidRequest() {
        assertClientError(() -> adminClient.createLifecycle(null), INVALID_REQUEST);
    }

    @Test
    public void updateLifecycleTest() throws IOException {
        var lifecycle = adminClient.createLifecycle("update-me-cycle");
        var lifecycleState = adminClient.createLifecycleState(
                new LifecycleState("some-state", "<config/>", NopState.class.getName(), lifecycle.getId(), null));
        lifecycle.setDefaultStateId(lifecycleState.getId());
        Lifecycle updatedCycle = adminClient.updateLifecycle(lifecycle);
        assertEquals(1, updatedCycle.getLifecycleStates().size());
        assertEquals(lifecycleState, updatedCycle.getLifecycleStates().getFirst());
        updatedCycle.getLifecycleStates().clear();
        assertEquals(lifecycle, updatedCycle);
    }

    @Test
    public void updateLifecycleTestAsNormalUser() throws IOException {
        var lifecycle = adminClient.createLifecycle("update-me-cycle-fail");
        assertClientError(() -> client.updateLifecycle(lifecycle), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void updateLifecycleTestInvalidRequest() {
        assertClientError(() -> adminClient.updateLifecycle(new Lifecycle()), INVALID_REQUEST);
    }

    @Test
    public void deleteLifecycleTest() throws IOException {
        var lifecycle = adminClient.createLifecycle("delete-me");
        adminClient.deleteLifecycle(lifecycle.getId());
        assertTrue(client.listLifecycles().stream().noneMatch(lc -> lc.getName().equals("delete-me")));
    }

    @Test
    public void deleteShouldFailWhenInUse() throws IOException {
        var lifecycle = adminClient.createLifecycle("delete-me-fail-in-use");
        var lcs = adminClient.createLifecycleState(
                new LifecycleState("delete-me-fail-lc-in-use", CONFIG, NOP_STATE, lifecycle.getId(), null));
        assertClientError(() -> adminClient.deleteLifecycle(lifecycle.getId()), DB_DELETE_FAILED);
    }

    @Test
    public void deleteLifecycleAsNormalUserTest() throws IOException {
        var lifecycle = adminClient.createLifecycle("delete-me-cycle-fail");
        assertClientError(() -> client.deleteLifecycle(lifecycle.getId()), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void deleteLifecycleInvalidRequest() {
        assertClientError(() -> adminClient.deleteLifecycle(-1L), INVALID_REQUEST);
    }

}
