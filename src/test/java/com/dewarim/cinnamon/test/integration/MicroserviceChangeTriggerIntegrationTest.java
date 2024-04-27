package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.request.osd.CreateNewVersionRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.matchers.Times;

import java.io.File;
import java.io.IOException;

import static com.dewarim.cinnamon.model.ChangeTriggerType.MICROSERVICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {MicroserviceChangeTriggerIntegrationTest.MOCK_PORT})
public class MicroserviceChangeTriggerIntegrationTest extends CinnamonIntegrationTest {

    public final static int MOCK_PORT = 20001;

    private final static ChangeTrigger mctCreate  = new ChangeTrigger(1L, "create-ct", "osd", "create", true,
            false, true, false, "<config><remoteServer>http://localhost:" + MOCK_PORT + "/echo</remoteServer></config>",
            MICROSERVICE, 200, true);
    private final static ChangeTrigger mctVersion = new ChangeTrigger(1L, "version-ct", "osd", "version", true,
            false, true, false, "<config><remoteServer>http://localhost:" + MOCK_PORT + "/echo2</remoteServer></config>",
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
