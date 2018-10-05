package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.AttachLifecycleRequest;
import com.dewarim.cinnamon.model.request.IdRequest;
import com.dewarim.cinnamon.model.response.GenericResponse;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import org.apache.http.HttpResponse;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertCinnamonError(response, ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION, SC_FORBIDDEN);
    }

    @Ignore
    @Test
    public void attachLifecycleStateChangeFailed() throws IOException {
        // TODO: should use ChangeAclState with invalid ACL configured
    }

    @Test
    public void attachLifecycleMissingLifecycleState() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(28L, 2L, null);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        assertCinnamonError(response, ErrorCode.LIFECYCLE_STATE_NOT_FOUND, SC_NOT_FOUND);
    }

    @Test
    public void attachLifecycleHappyPathDefaultState() throws IOException {
        AttachLifecycleRequest badOsd   = new AttachLifecycleRequest(28L, 1L, null);
        HttpResponse           response = sendStandardRequest(UrlMapping.LIFECYCLE_STATE__ATTACH_LIFECYCLE, badOsd);
        parseGenericResponse(response);
    }

    @Ignore
    @Test
    public void attachLifecycleHappyPathWithNonDefaultState() throws IOException {
        // TODO: should use ChangeAclState with two states (for example reviewAcl,publishedAcl) configured
    }

    private List<LifecycleState> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        LifecycleStateWrapper stateWrapper = mapper.readValue(response.getEntity().getContent(), LifecycleStateWrapper.class);
        assertNotNull(stateWrapper);
        return stateWrapper.getLifecycleStates();
    }

}
