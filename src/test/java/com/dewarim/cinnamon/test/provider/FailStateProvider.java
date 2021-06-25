package com.dewarim.cinnamon.test.provider;

import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateProvider;


public class FailStateProvider implements StateProvider {

    @Override
    public String getName() {
        return "FailState";
    }

    @Override
    public State getState() {
        return new FailState();
    }
}
