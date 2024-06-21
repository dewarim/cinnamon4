package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.response.ChangeTriggerResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static com.dewarim.cinnamon.filter.ChangeTriggerFilter.activeChangeTriggerFilter;
import static com.dewarim.cinnamon.model.ChangeTriggerType.MICROSERVICE;
import static com.dewarim.cinnamon.model.ChangeTriggerType.NOP_TRIGGER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ChangeTriggerServletIntegrationTest extends CinnamonIntegrationTest {
    private final ChangeTrigger changeTrigger = new ChangeTrigger(1L, "triggerThumbnailGenerator", "osd", "setContent", true,
            false, true, true, "<config><remoteServer>http://localhost:" + cinnamonTestPort + "/echo</remoteServer></config>",
            NOP_TRIGGER, 100, false);

    @Test
    public void list() throws IOException {
        List<ChangeTrigger> triggers = client.listChangeTriggers();
        assertNotNull(triggers);
        assertFalse(triggers.isEmpty());
        ChangeTrigger testTrigger = triggers.get(0);
        assertEquals(MICROSERVICE, testTrigger.getTriggerType());

    }

    @Test
    public void createChangeTriggerHappyPath() throws IOException {
        var trigger = adminClient.createChangeTrigger(changeTrigger);
        assertEquals(changeTrigger.getName(), trigger.getName());
        assertEquals(changeTrigger.getTriggerType(), trigger.getTriggerType());
        assertEquals(changeTrigger.isPostTrigger(), trigger.isPostTrigger());
        assertEquals(changeTrigger.isPreTrigger(), trigger.isPreTrigger());
        assertEquals(changeTrigger.getConfig(), trigger.getConfig());
        assertEquals(changeTrigger.getAction(), trigger.getAction());
        assertEquals(changeTrigger.getController(), trigger.getController());
        assertEquals(changeTrigger.isActive(), trigger.isActive());
        assertEquals(changeTrigger.getRanking(), trigger.getRanking());
        assertEquals(changeTrigger.isCopyFileContent(), trigger.isCopyFileContent());
        assertNotNull(changeTrigger.getId());
    }

    @Test
    public void createChangeTriggerWithoutSuperuserStatus() {
        assertClientError(() -> client.createChangeTrigger(changeTrigger), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    @Disabled("this is tested in MCT integration test (where we have a mocked server to respond)")
    public void nopTest() throws IOException {
        List<ChangeTriggerResponse> nops = client.changeTriggerNop(false);
        assertNotNull(nops);
    }

    // TODO: test changeTrigger-update + delete with/without superuser

    @Test
    public void ctActiveTest()  {

        UserAccount au = new UserAccount();
        au.setActivateTriggers(true);
        UserAccount pu = new UserAccount();
        pu.setActivateTriggers(false);
        CinnamonResponse response1 = new CinnamonResponse(mock(HttpServletResponse.class));
        CinnamonResponse response2 = new CinnamonResponse(mock(HttpServletResponse.class));

        assertTrue(activeChangeTriggerFilter(au, response1));
        assertFalse( activeChangeTriggerFilter(pu, response2));

        response1.setUser(au);
        response2.setUser(pu);
        assertTrue(activeChangeTriggerFilter(null, response1));
        assertFalse(activeChangeTriggerFilter(null, response2));

    }


}
