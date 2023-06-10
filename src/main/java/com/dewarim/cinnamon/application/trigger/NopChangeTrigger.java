package com.dewarim.cinnamon.application.trigger;

import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.model.ChangeTrigger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NopChangeTrigger implements Trigger{
    private static final Logger log = LogManager.getLogger(NopChangeTrigger.class);

    @Override
    public TriggerResult executePreCommand(ChangeTrigger changeTrigger, CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse) {
        log.debug("NopChangeTrigger executePreCommand");
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult executePostCommand(ChangeTrigger changeTrigger, CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse) {
        log.debug("NopChangeTrigger executePostCommand");
        return TriggerResult.CONTINUE;
    }
}
