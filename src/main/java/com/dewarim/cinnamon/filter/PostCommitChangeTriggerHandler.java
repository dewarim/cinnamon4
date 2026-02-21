package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.trigger.TriggerResult;
import com.dewarim.cinnamon.dao.ChangeTriggerDao;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.dewarim.cinnamon.filter.ChangeTriggerFilter.activeChangeTriggerFilter;

public class PostCommitChangeTriggerHandler {

    private static final Logger log = LogManager.getLogger(PostCommitChangeTriggerHandler.class);

    public void executeTriggers(CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse) {
        log.debug("After servlet: continue with post-commit-change-triggers");

        UserAccount user = RequestScope.getCurrentUser();
        if (!activeChangeTriggerFilter(user, cinnamonResponse)) {
            return;
        }

        // load triggers (later: cache them)
        UrlMapping mapping = UrlMapping.getByPath(cinnamonRequest.getRequestURI());
        List<ChangeTrigger> triggers = new ChangeTriggerDao().list().stream()
                .filter(changeTrigger -> {
                            if (mapping.getServlet().equals(changeTrigger.getController()) &&
                                    changeTrigger.getAction().equals(mapping.getAction()) &&
                                    changeTrigger.isPostCommitTrigger() &&
                                    changeTrigger.isActive()) {
                                log.debug("Found post commit change trigger {} for {}", changeTrigger.getName(), mapping.getPath());
                                return true;
                            }
                            else {
                                log.debug("Change trigger {} does not apply to {}", changeTrigger.getName(), mapping.getPath());
                                return false;
                            }
                        }
                ).toList();
        log.debug("Found {} postCommitChangeTriggers to execute.", triggers.size());
        // do post triggers:
        for (ChangeTrigger trigger : triggers) {
            log.debug("Calling changeTrigger {}", trigger.getName());
            TriggerResult result = trigger.getTriggerType().trigger.executePostCommand(trigger, cinnamonRequest, cinnamonResponse);
            log.debug("Result of trigger call: {} ", result);
        }
    }

}
