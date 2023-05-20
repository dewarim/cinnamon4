package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestServletIntegrationTest extends CinnamonIntegrationTest{
    
    @Test
    public void testEcho() throws IOException {
        String message = "<xml>test</xml>";
        String echo       = client.testEcho(message);
        assertEquals(message,echo);
    }
    @Test
    public void testBrokenEcho()  {
        String message = "<xml-is-broken";
        CinnamonClientException exception = assertThrows(CinnamonClientException.class, () -> client.testEcho(message));
        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }


}
