package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.LifecycleRequest;
import com.dewarim.cinnamon.model.request.lifecycle.ListLifecycleRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class LifecycleServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listLifecycles() throws IOException {
        HttpResponse    response   = sendStandardRequest(UrlMapping.LIFECYCLE__LIST_LIFECYCLES, new ListLifecycleRequest());
        List<Lifecycle> lifecycles = parseResponse(response);

        assertNotNull(lifecycles);
        assertFalse(lifecycles.isEmpty());
        assertEquals(4, lifecycles.size());

        Optional<Lifecycle> typeOpt = lifecycles.stream().filter(lifecycle -> lifecycle.getName().equals("review.lc"))
                .findFirst();
        assertTrue(typeOpt.isPresent());
        Lifecycle type = typeOpt.get();
        assertThat(type.getId(), equalTo(1L));
    }

    @Test
    public void getLifecycleHappyPathWithId() throws IOException {
        HttpResponse    response   = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(1L, null));
        List<Lifecycle> lifecycles = parseResponse(response);
        assertEquals(1, lifecycles.size());
        Lifecycle lifecycle = lifecycles.get(0);
        assertEquals(Long.valueOf(1L), lifecycle.getId());
    }

    @Test
    public void getLifecycleHappyPathWithName() throws IOException {
        HttpResponse    response   = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(null, "render.lc"));
        List<Lifecycle> lifecycles = parseResponse(response);
        assertEquals(1, lifecycles.size());
        Lifecycle lifecycle = lifecycles.get(0);
        assertEquals("render.lc", lifecycle.getName());
        assertEquals(Long.valueOf(2), lifecycle.getId());
        List<LifecycleState> lifecycleStates = lifecycle.getLifecycleStates();
        assertEquals(1, lifecycleStates.size());
        LifecycleState newRenderTaskState = lifecycleStates.get(0);
        assertEquals("newRenderTask",newRenderTaskState.getName());
        assertEquals("NopState",newRenderTaskState.getStateClass());
    }

    @Test
    public void getLifecycleFailOnNotFound() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(Long.MAX_VALUE, null));
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);

        HttpResponse nameNotFoundResponse = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(null, "does-not-exist"));
        assertCinnamonError(nameNotFoundResponse, ErrorCode.OBJECT_NOT_FOUND);
    }

    @Test
    public void getLifecycleFailOnInvalidRequest() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(null, null));
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);

        response = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(-1L, null));
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);

        response = sendStandardRequest(UrlMapping.LIFECYCLE__GET_LIFECYCLE, new LifecycleRequest(null, ""));
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    private List<Lifecycle> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        LifecycleWrapper lifecycleWrapper = mapper.readValue(response.getEntity().getContent(), LifecycleWrapper.class);
        assertNotNull(lifecycleWrapper);
        return lifecycleWrapper.getLifecycles();
    }


}
