package com.dewarim.cinnamon.test.integration;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.dewarim.cinnamon.ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER;
import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void testEcho() throws IOException {
        String message = "<xml>test</xml>";
        String echo    = client.testEcho(message);
        assertEquals(message, echo);
    }

    @Test
    public void testBrokenEcho() {
        String message = "<xml-is-broken";
        assertClientError(() -> client.testEcho(message), INVALID_REQUEST);
    }

    @Test
    public void testException() {
        assertClientError(() -> client.testBoom(), INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
    }


}
