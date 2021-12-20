package com.dewarim.cinnamon.provider.state;

import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateProvider;
import com.dewarim.cinnamon.lifecycle.NopState;


public class NopStateProvider implements StateProvider {

    @Override
    public String getName() {
        return NopState.class.getName();
    }

    @Override
    public State getState() {
        return new NopState();
    }
}
