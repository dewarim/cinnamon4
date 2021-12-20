package com.dewarim.cinnamon.provider.state;

import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateProvider;
import com.dewarim.cinnamon.lifecycle.FailState;


public class FailStateProvider implements StateProvider {

    @Override
    public String getName() {
        return FailState.class.getName();
    }

    @Override
    public State getState() {
        return new FailState();
    }
}
