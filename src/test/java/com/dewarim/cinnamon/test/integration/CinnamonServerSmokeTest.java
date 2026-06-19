package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.client.StandardResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class CinnamonServerSmokeTest extends CinnamonIntegrationTest {

    @Test
    public void serverUsesIsolatedTempBaseResourceAndDoesNotExposeProjectFiles() throws IOException {
        var handler = cinnamonServer.getServer().getHandler();
        WebAppContext webAppContext = assertInstanceOf(WebAppContext.class, handler);

        assertNotNull(webAppContext.getBaseResource(), "Jetty base resource must be configured");
        assertThat(webAppContext.getBaseResource().toString(), containsString("cinnamon-temp-dir"));
        assertTrue(cinnamonServer.getServer().isStarted(), "Server should be started in integration test setup");

        String url = "http://localhost:" + cinnamonTestPort + "/pom.xml";
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.get(url).build(), StandardResponse::new)) {
            assertThat(response.getCode(), equalTo(404));
        }
    }
}

