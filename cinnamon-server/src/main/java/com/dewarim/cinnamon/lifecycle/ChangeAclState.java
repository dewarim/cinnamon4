package com.dewarim.cinnamon.lifecycle;

import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.model.ObjectSystemData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChangeAclState implements State {

    private final static Logger log = LogManager.getLogger(ChangeAclState.class);

    private List<State> states = new ArrayList<>();

    @Override
    public List<State> getExitStates(CinnamonObject osd) {
        return states;
    }

    @Override
    public StateChangeResult checkEnteringObject(CinnamonObject osd, LifecycleStateConfig config) {
        // currently, you can change to the ACL state from any other state.
        return new StateChangeResult(true);
    }

    @Override
    public StateChangeResult enter(CinnamonObject osd, LifecycleStateConfig config) {
        log.debug("osd " + osd.getId() + " entered ChangeAclState.");
        List<String> aclNames = config.getPropertyValues("aclName");
        if(aclNames.size() != 1){
            return new StateChangeResult(false, Collections.singletonList("ChangeAclState: failed to enter state - need single acl name param in config."));
        }
        String aclName = aclNames.get(0);
        AclDao aclDao = new AclDao();
        Acl acl = aclDao.getAclByName(aclName);
        if (acl == null) {
            return new StateChangeResult(false, Arrays.asList("error.acl.not_found", aclName));
        }
        log.debug("Setting acl from " + osd.getAclId() + " to " + acl.getId());
        OsdDao osdDao = new OsdDao();
        osd.setAclId(acl.getId());
        osdDao.updateOsd((ObjectSystemData) osd);
        return new StateChangeResult(true);
    }

    @Override
    public void exit(CinnamonObject osd, State nextState, LifecycleStateConfig config) {
        log.debug("osd " + osd.getId() + " left ChangeAclState.");
    }

    @Override
    public void setExitStates(List<State> states) {
        this.states=states;
    }
}
