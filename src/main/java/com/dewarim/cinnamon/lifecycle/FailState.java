package com.dewarim.cinnamon.lifecycle;

import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Testing lifecycle state: will fail every time upon entering / leaving.
 */
public class FailState implements State {

    private final static Logger log = LogManager.getLogger(FailState.class);

    @Override
    public StateChangeResult enter(CinnamonObject osd, LifecycleStateConfig config) {
        log.debug("OSD {} entered Fail",osd.getId());
        return new StateChangeResult(false, Collections.singletonList("Failed to enter FailState."));
    }

    @Override
    public StateChangeResult exit(CinnamonObject osd, State nextState, LifecycleStateConfig config) {
        log.debug("OSD {} tries to leave FailState.", osd.getId());
        return new StateChangeResult(false, Collections.singletonList("Failed to leave FailState."));
    }

    @Override
    public List<State> getNextStates(CinnamonObject osd, LifecycleStateConfig config) {
        return Collections.emptyList();
    }

}