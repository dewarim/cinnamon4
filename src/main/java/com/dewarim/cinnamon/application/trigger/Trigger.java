package com.dewarim.cinnamon.application.trigger;

import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.model.ChangeTrigger;

public interface Trigger {


    TriggerResult executePreCommand(ChangeTrigger changeTrigger, CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse);
    TriggerResult executePostCommand(ChangeTrigger changeTrigger, CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse);


}
