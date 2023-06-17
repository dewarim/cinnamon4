package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.lifecycle.LifecycleRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.ErrorCode.OBJECT_NOT_FOUND;
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
        Lifecycle lifecycle = client.getLifecycle("render.lc");
        assertEquals("render.lc", lifecycle.getName());
        assertEquals(Long.valueOf(2), lifecycle.getId());
        List<LifecycleState> lifecycleStates = lifecycle.getLifecycleStates();
        assertEquals(1, lifecycleStates.size());
        LifecycleState newRenderTaskState = lifecycleStates.get(0);
        assertEquals("newRenderTask", newRenderTaskState.getName());
        assertEquals("com.dewarim.cinnamon.lifecycle.NopState", newRenderTaskState.getStateClass());
    }

    @Test
    public void getLifecycleFailOnNotFound() throws IOException {
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE__GET, new LifecycleRequest(Long.MAX_VALUE, null), OBJECT_NOT_FOUND);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE__GET, new LifecycleRequest(null, "does-not-exist"), OBJECT_NOT_FOUND);
    }

    @Test
    public void getLifecycleFailOnInvalidRequest() throws IOException {
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE__GET, new LifecycleRequest(null, null), INVALID_REQUEST);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE__GET, new LifecycleRequest(-1L, null), INVALID_REQUEST);
        sendStandardRequestAndAssertError(UrlMapping.LIFECYCLE__GET, new LifecycleRequest(null, ""), INVALID_REQUEST);
    }

    @Test
    public void createLifecycleTest() throws IOException {
        Lifecycle lifecycle = adminClient.createLifecycle("foo-cycle");
        assertNotNull(lifecycle.getId());
        assertEquals("foo-cycle", lifecycle.getName());
        Lifecycle fooCycle = client.getLifecycle("foo-cycle");
        assertEquals(lifecycle, fooCycle);
    }

    @Test
    public void createLifecycleAsNormalUser() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createLifecycle("failed-create"));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void createLifecycleInvalidRequest() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createLifecycle(null));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    public void updateLifecycleTest() throws IOException {
        var lifecycle = adminClient.createLifecycle("update-me-cycle");
        var lifecycleState = adminClient.createLifecycleState(
                new LifecycleState("some-state", "<config/>", NopState.class.getName(), lifecycle.getId(), null));
        lifecycle.setDefaultStateId(lifecycleState.getId());
        Lifecycle updatedCycle = adminClient.updateLifecycle(lifecycle);
        assertEquals(1, updatedCycle.getLifecycleStates().size());
        assertEquals(lifecycleState, updatedCycle.getLifecycleStates().get(0));
        updatedCycle.getLifecycleStates().clear();
        assertEquals(lifecycle, updatedCycle);
    }

    @Test
    public void updateLifecycleTestAsNormalUser() throws IOException {
        var                     lifecycle = adminClient.createLifecycle("update-me-cycle-fail");
        CinnamonClientException ex        = assertThrows(CinnamonClientException.class, () -> client.updateLifecycle(lifecycle));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateLifecycleTestInvalidRequest() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.updateLifecycle(new Lifecycle()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
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
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteLifecycle(lifecycle.getId()));
        assertEquals(ErrorCode.DB_DELETE_FAILED, ex.getErrorCode());
    }

    @Test
    public void deleteLifecycleAsNormalUserTest() throws IOException {
        var                     lifecycle = adminClient.createLifecycle("delete-me-cycle-fail");
        CinnamonClientException ex        = assertThrows(CinnamonClientException.class, () -> client.deleteLifecycle(lifecycle.getId()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteLifecycleInvalidRequest() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteLifecycle(-1L));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

}
