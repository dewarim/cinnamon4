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
 * Does nothing, accepts exiting to and entering from every other state.
 * Useful for lifecycles whose states are mostly declarative instead of functional.
 */
public class NopState implements State {

    private final static Logger log = LogManager.getLogger(NopState.class);

    @Override
    public StateChangeResult checkEnteringObject(CinnamonObject osd, LifecycleStateConfig config) {
        return new StateChangeResult(true);
    }

    @Override
    public StateChangeResult enter(CinnamonObject osd, LifecycleStateConfig config) {
        log.debug("OSD {} entered NopState",osd.getId());
        return new StateChangeResult(true);
    }

    @Override
    public void exit(CinnamonObject osd, State nextState, LifecycleStateConfig config) {
        log.debug("OSD {} left NopState.", osd.getId());
    }

    @Override
    public List<State> getExitStates(CinnamonObject osd) {
        return Collections.emptyList();
    }

    @Override
    public void setExitStates(List<State> states) {

    }
}