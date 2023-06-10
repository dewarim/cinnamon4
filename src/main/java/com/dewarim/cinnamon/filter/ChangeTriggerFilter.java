package com.dewarim.cinnamon.filter;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.trigger.TriggerResult;
import com.dewarim.cinnamon.dao.ChangeTriggerDao;
import com.dewarim.cinnamon.model.ChangeTrigger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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

        // load triggers (later: cache them)
        UrlMapping mapping = UrlMapping.getByPath(cinnamonRequest.getRequestURI());
        List<ChangeTrigger> triggers = new ChangeTriggerDao().list().stream()
                .filter(changeTrigger ->
                        changeTrigger.getController().equals(mapping.getPath()) &&
                                changeTrigger.getAction().equals(mapping.getAction())).toList();
        List<ChangeTrigger> preTriggers = triggers.stream().filter(ChangeTrigger::isPreTrigger)
                .sorted(Comparator.comparingLong(ChangeTrigger::getRanking)).toList();
        List<ChangeTrigger> postTriggers = triggers.stream().filter(ChangeTrigger::isPostTrigger)
                .sorted(Comparator.comparingLong(ChangeTrigger::getRanking)).toList();

        if (triggers.size() > 0){
            boolean doCopyFileContent = triggers.stream().anyMatch(ChangeTrigger::isCopyFileContent);
            cinnamonRequest.copyInputStream(doCopyFileContent);
        }

        try {
            // do pre triggers:
            for (ChangeTrigger trigger : preTriggers) {
                TriggerResult result = trigger.getTriggerType().trigger.executePreCommand(trigger, cinnamonRequest, cinnamonResponse);
                if (result == TriggerResult.STOP) {
                    throw ErrorCode.REQUEST_DENIED_BY_CHANGE_TRIGGER.exception();
                }
            }

            // hand over to servlet:
            chain.doFilter(cinnamonRequest, cinnamonResponse);

            // do post triggers:
            for (ChangeTrigger trigger : postTriggers) {
                TriggerResult result = trigger.getTriggerType().trigger.executePostCommand(trigger, cinnamonRequest, cinnamonResponse);
                if (result == TriggerResult.STOP) {
                    throw ErrorCode.REQUEST_DENIED_BY_CHANGE_TRIGGER.exception();
                }
            }
        }
        finally {
            cinnamonRequest.deleteTempFile();
        }
    }

}
