package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.trigger.TriggerResult;
import com.dewarim.cinnamon.dao.ChangeTriggerDao;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.UserAccount;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ChangeTriggerFilter extends HttpFilter {

    private static final Logger log = LogManager.getLogger(ChangeTriggerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        ChangeTriggerDao changeTriggerDao = new ChangeTriggerDao();

        UserAccount user = RequestScope.getCurrentUser();

        UrlMapping mapping = UrlMapping.getByPath(cinnamonRequest.getRequestURI());
        List<ChangeTrigger> triggers = changeTriggerDao.findApplicableTriggers(mapping);
        List<ChangeTrigger> preTriggers = triggers.stream().filter(ChangeTrigger::isPreTrigger)
                .sorted(Comparator.comparingLong(ChangeTrigger::getRanking)).toList();
        log.debug("Found {} preTriggers to execute for {}.", preTriggers.size(), mapping.getPath());
        boolean isDebugEnabled = CinnamonServer.isDebugEnabled();
        if (triggers.size() > 0 || isDebugEnabled) {
            boolean doCopyFileContent = triggers.stream().anyMatch(ChangeTrigger::isCopyFileContent);
            log.debug("Found change trigger that requires copyFileContent: {}", doCopyFileContent);
            if (isDebugEnabled) {
                log.debug("copy request for debugging");
                doCopyFileContent = true;
            }
            cinnamonRequest.copyInputStream(doCopyFileContent);
        }

        try {
            if (activeChangeTriggerFilter(user, cinnamonResponse)) {
                // do pre triggers:
                for (ChangeTrigger trigger : preTriggers) {
                    log.debug("Calling changeTrigger {}", trigger.getName());
                    TriggerResult result = trigger.getTriggerType().trigger.executePreCommand(trigger, cinnamonRequest, cinnamonResponse);
                    log.debug("Result of trigger call: {} ", result);
                    if (result == TriggerResult.STOP) {
                        throw ErrorCode.REQUEST_DENIED_BY_CHANGE_TRIGGER.exception();
                    }
                }
            }
            log.debug("After pre-trigger: hand request over to servlet");
            // hand over to servlet:
            chain.doFilter(cinnamonRequest, cinnamonResponse);
            log.debug("After servlet: continue with post-triggers");

            if (!activeChangeTriggerFilter(user, cinnamonResponse)) {
                return;
            }
            // do post triggers:
            List<ChangeTrigger> postTriggers = triggers.stream().filter(ChangeTrigger::isPostTrigger)
                    .sorted(Comparator.comparingLong(ChangeTrigger::getRanking)).toList();
            log.debug("Found {} postTriggers to execute.", postTriggers.size());
            for (ChangeTrigger trigger : postTriggers) {
                log.debug("Calling changeTrigger {}", trigger.getName());
                TriggerResult result = trigger.getTriggerType().trigger.executePostCommand(trigger, cinnamonRequest, cinnamonResponse);
                log.debug("Result of trigger call: {} ", result);
                if (result == TriggerResult.STOP) {
                    throw ErrorCode.REQUEST_DENIED_BY_CHANGE_TRIGGER.exception();
                }

            }
        } finally {
            cinnamonRequest.deleteTempFile();
        }
    }

    public static boolean activeChangeTriggerFilter(UserAccount user, CinnamonResponse cinnamonResponse) {
        UserAccount ua = user == null ? cinnamonResponse.getUser() : user;
        if (ua == null) {
            log.warn("Could not find any user associated with response, should not activate CT.");
            return false;
        }
        if (ua.isActivateTriggers()) {
            log.debug("User {} activates changeTriggers", ua);
            return true;
        }
        else {
            log.debug("User {} does not activate changeTriggers", ua);
            return false;
        }
    }


}
