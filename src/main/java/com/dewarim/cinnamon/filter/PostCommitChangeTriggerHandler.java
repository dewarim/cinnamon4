package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.trigger.TriggerResult;
import com.dewarim.cinnamon.dao.ChangeTriggerDao;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PostCommitChangeTriggerHandler  {

    private static final Logger log = LogManager.getLogger(PostCommitChangeTriggerHandler.class);

    public void executeTriggers(CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse)  {
        log.debug("After servlet: continue with post-commit-change-triggers");

        UserAccount user = ThreadLocalSqlSession.getCurrentUser();
        if(user == null) {
            // try and fetch connect() user
            user = cinnamonResponse.getUser();
        }
        if(user != null && !user.isActivateTriggers()){
            log.debug("User {} does not have activate triggers", user.getUsername());
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

        // do post triggers:
        for (ChangeTrigger trigger : triggers) {
            log.debug("Calling changeTrigger {}", trigger.getName());
            TriggerResult result = trigger.getTriggerType().trigger.executePostCommand(trigger, cinnamonRequest, cinnamonResponse);
            log.debug("Result of trigger call: {} ", result);
        }
    }

}
