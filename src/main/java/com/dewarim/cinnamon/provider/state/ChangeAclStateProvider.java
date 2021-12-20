package com.dewarim.cinnamon.provider.state;

import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateProvider;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;

public class ChangeAclStateProvider implements StateProvider {

    @Override
    public String getName() {
        return ChangeAclState.class.getName();
    }

    @Override
    public State getState() {
        return new ChangeAclState();
    }
}
