package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.application.trigger.MicroserviceChangeTrigger;
import com.dewarim.cinnamon.application.trigger.Trigger;

public enum ChangeTriggerType {

    MICROSERVICE(new MicroserviceChangeTrigger());

    public final Trigger trigger;

    ChangeTriggerType(Trigger trigger) {
        this.trigger = trigger;
    }


}
