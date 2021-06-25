package com.dewarim.cinnamon.api.lifecycle;

import com.dewarim.cinnamon.api.CinnamonObject;

import java.util.List;

public interface State {

    List<State> getNextStates(CinnamonObject osd, LifecycleStateConfig config);

    StateChangeResult enter(CinnamonObject osd, LifecycleStateConfig config);

    StateChangeResult exit(CinnamonObject osd, State nextState, LifecycleStateConfig config);

}
