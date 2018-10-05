package com.dewarim.cinnamon.api.lifecycle;

import com.dewarim.cinnamon.api.CinnamonObject;

import java.util.List;

public interface State {

    List<State> getExitStates(CinnamonObject osd);

    void setExitStates(List<State> states);

    StateChangeResult checkEnteringObject(CinnamonObject osd, LifecycleStateConfig config);

    StateChangeResult enter(CinnamonObject osd, LifecycleStateConfig config);

    void exit(CinnamonObject osd, State nextState, LifecycleStateConfig config);

}
