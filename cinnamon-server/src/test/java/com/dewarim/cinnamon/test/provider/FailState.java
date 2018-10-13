package com.dewarim.cinnamon.test.provider;

import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Does nothing, accepts exiting to and entering from every other state.
 * Useful for lifecycles whose states are mostly declarative instead of functional.
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
        log.debug("OSD {} tries to leave FailStatee.", osd.getId());
        return new StateChangeResult(false, Collections.singletonList("Failed to leave FailState."));
    }

    @Override
    public List<State> getExitStates(CinnamonObject osd) {
        return Collections.emptyList();
    }

}