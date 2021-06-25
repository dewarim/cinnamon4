package com.dewarim.cinnamon.provider.state;

import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateProvider;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.provider.DefaultStateProvider;

public class ChangeAclStateProvider implements StateProvider {

    @Override
    public String getName() {
        return DefaultStateProvider.CHANGE_ACL_STATE.getClassName();
    }

    @Override
    public State getState() {
        return new ChangeAclState();
    }
}
