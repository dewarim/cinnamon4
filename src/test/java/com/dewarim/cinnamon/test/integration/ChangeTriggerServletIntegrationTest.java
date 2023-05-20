package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.ChangeTrigger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.model.ChangeTriggerType.MICROSERVICE;
import static org.junit.jupiter.api.Assertions.*;

public class ChangeTriggerServletIntegrationTest extends CinnamonIntegrationTest{
    private final ChangeTrigger changeTrigger = new ChangeTrigger(1L, "triggerThumbnailGenerator", "osd", "setContent", true,
            false, true, false, "<config><url>http://localhost:64888/createThumbnail</url></config>",
            MICROSERVICE, 100);
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
        assertEquals(changeTrigger.getTriggerType(),trigger.getTriggerType());
        assertEquals(changeTrigger.isPostTrigger(),trigger.isPostTrigger());
        assertEquals(changeTrigger.isPreTrigger(),trigger.isPreTrigger());
        assertEquals(changeTrigger.getConfig(),trigger.getConfig());
        assertEquals(changeTrigger.getAction(),trigger.getAction());
        assertEquals(changeTrigger.getController(),trigger.getController());
        assertEquals(changeTrigger.isActive(),trigger.isActive());
        assertEquals(changeTrigger.getRanking(),trigger.getRanking());
        assertEquals(changeTrigger.isCopyFileContent(),trigger.isCopyFileContent());
        assertNotNull(changeTrigger.getId());
    }

    @Test
    public void createChangeTriggerWithoutSuperuserStatus() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.createChangeTrigger(changeTrigger));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    // TODO: test changeTrigger-update + delete with/without superuser

}
