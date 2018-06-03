package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LifecycleStateServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void getLifecycleStateWithInvalidRequest() throws IOException{
        IdRequest idRequest = new IdRequest(-1L);
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_LIFECYCLE_STATE, idRequest);
        assertCinnamonError(response, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void getLifecycleStateWhichDoesNotExist() throws IOException{
        IdRequest idRequest = new IdRequest(Long.MAX_VALUE);
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_LIFECYCLE_STATE, idRequest);
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void getLifecycleState() throws IOException{
        IdRequest idRequest = new IdRequest(1L);
        HttpResponse response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__GET_LIFECYCLE_STATE, idRequest);
        List<LifecycleState> lifecycleStates = parseResponse(response);
        assertNotNull(lifecycleStates);
        LifecycleState state = lifecycleStates.get(0);
        assertEquals(1L, (long) state.getId());
        assertEquals("newRenderTask", state.getName());
        assertEquals("<meta>renderserver:x</meta>", state.getConfig());
        assertEquals("NopState", state.getStateClass());
        assertEquals(2L, (long) state.getLifecycleId());
        assertEquals(1L, (long) state.getLifecycleStateForCopyId());
    }

    private List<LifecycleState> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        LifecycleStateWrapper stateWrapper = mapper.readValue(response.getEntity().getContent(), LifecycleStateWrapper.class);
        assertNotNull(stateWrapper);
        return stateWrapper.getLifecycleStates();
    }
    
}
