package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.configuration.ChangeTriggerConfig;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.osd.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerResponse;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.apache.hc.core5.http.ssl.TLS;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.matchers.Times;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.dewarim.cinnamon.model.ChangeTriggerType.MICROSERVICE;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * TODO: create test script that runs against standalone Cinnamon server for index/search/changeTrigger tests.
 * Note: this test is broken. The manual changeTriggerResponseTest will work and actually call the MCT
 * and generate the expected changeTriggerResponses.
 * <br>
 * But due to DB session magic when running this in the same thread as the DB session, this will not actually create
 * the change triggers, rendering the test useless.
 */
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {MicroserviceChangeTriggerIntegrationTest.MOCK_PORT})
public class MicroserviceChangeTriggerIntegrationTest extends CinnamonIntegrationTest {

    public final static int MOCK_PORT = 20001;

    private final static ChangeTrigger mctCreate  = new ChangeTrigger(1L, "create-ct", "osd", "create", true,
            false, true, false, "<config><remoteServer>http://localhost:" + MOCK_PORT + "/echo</remoteServer></config>",
            MICROSERVICE, 200, true);
    private final static ChangeTrigger mctVersion = new ChangeTrigger(1L, "version-ct", "osd", "version", true,
            false, true, false, "<config><remoteServer>https://localhost:" + MOCK_PORT + "/echo2</remoteServer></config>",
            MICROSERVICE, 200, true);

    private static long    mctCreateId;
    private static long    mctVersionId;
    private static boolean triggerExists;

    private static MockServerClient mockClient;

    @BeforeAll
    public static void beforeEachTest(MockServerClient mockServerClient) throws IOException {
        mockClient = mockServerClient;
        if (!triggerExists) {
            mctCreateId   = adminClient.createChangeTrigger(mctCreate).getId();
            mctVersionId  = adminClient.createChangeTrigger(mctVersion).getId();
            triggerExists = true;
            ChangeTriggerConfig changeTriggerConfig = CinnamonServer.config.getChangeTriggerConfig();
            changeTriggerConfig.setUseCustomTrustStore(true);

            // https://github.com/mock-server/mockserver/issues/1837
            // downgrade to TLS 1.2 because mock-server does weird things:
            changeTriggerConfig.setTlsVersions(Stream.of(TLS.V_1_2, TLS.V_1_3).map(Enum::toString).toList());
            MICROSERVICE.trigger.configure(changeTriggerConfig);
        }
    }

    @AfterAll
    public static void afterAll() throws IOException {
        adminClient.deleteChangeTrigger(mctCreateId);
        adminClient.deleteChangeTrigger(mctVersionId);
    }

    @Test
    public void changeTriggerTest() throws IOException {
        mockClient.when(
                        request()
                                .withMethod("POST")
                                .withPath("/echo"),
                        Times.exactly(2)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("Hello Test")
                );

        TestObjectHolder toh = new TestObjectHolder(client, userId);
        ObjectSystemData osd = toh.createOsd().osd;
        assertNull(osd.getContentSize());
    }

    @Test
    @Disabled("you need to insert ChangeTrigger for changeTriggerResponseTest in CreateTestDB")
    public void changeTriggerResponseTest() throws IOException {
        mockClient.when(
                        request()
                                .withMethod("POST")
                                .withPath("/echo"),
                        Times.exactly(2)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("Hello Test")
                );

        TestObjectHolder toh = new TestObjectHolder(client, userId);
        ObjectSystemData osd = toh.createOsd().osd;
        assertNull(osd.getContentSize());

        List<ChangeTriggerResponse> changeTriggerResponses = CinnamonClient.changeTriggerResponseLocal.get();
        assertEquals(2, changeTriggerResponses.size());
        ChangeTriggerResponse changeTriggerResponse = changeTriggerResponses.get(0);
        assertEquals("Hello Test", changeTriggerResponse.getResponse());
        assertEquals(SC_OK, changeTriggerResponse.getHttpCode());
        assertEquals("http://localhost:" + MOCK_PORT + "/echo", changeTriggerResponse.getUrl());
    }

    // bug reported by Boris: after versioning, document file length is 0
    @Test
    public void versionShouldNotGenerateContentWithZeroLength() throws IOException {
        mockClient.when(
                        request()
                                .withMethod("POST")
                                .withPath("/echo2"),
                        Times.exactly(2)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("Hello Test")
                );

        var              versionRequest = createBunnyOsdAndReturnVersionRequest();
        ObjectSystemData osd            = client.versionWithContent(versionRequest, getBunnyFile());
        assertEquals(Long.valueOf(getBunnyFile().length()), osd.getContentSize());
        assertEquals(versionRequest.getFormatId(), osd.getFormatId());
    }

    private CreateNewVersionRequest createBunnyOsdAndReturnVersionRequest() throws IOException {
        var toh = new TestObjectHolder(client, userId);
        Format imagePng = TestObjectHolder.formats.stream()
                .filter(f -> f.getName().equals("image.png")).findFirst().orElseThrow();
        File bun = getBunnyFile();
        toh.createOsdWithContent(toh.createRandomName(), imagePng, bun);
        assertEquals(bun.length(), toh.osd.getContentSize());
        CreateNewVersionRequest versionRequest = new CreateNewVersionRequest(toh.osd.getId());
        versionRequest.setFormatId(imagePng.getId());
        return versionRequest;
    }

    private File getBunnyFile() {
        return new File("data/cinnamon-bun.png");
    }

}
