package com.dewarim.cinnamon.lifecycle;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateChangeResult;
import com.dewarim.cinnamon.dao.AclDao;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.provider.StateProviderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChangeAclState implements State {

    private final static Logger log = LogManager.getLogger(ChangeAclState.class);

    @Override
    public List<State> getNextStates(CinnamonObject osd, LifecycleStateConfig config) {
        return StateProviderService.getInstance().getNextStateImplementationsByConfiguration(config);
    }

    @Override
    public StateChangeResult enter(CinnamonObject osd, LifecycleStateConfig config) {
        log.debug("osd {} entered ChangeAclState.", osd.getId());
        List<String> aclNames = config.getPropertyValues("aclName");
        if(aclNames.size() != 1){
            return new StateChangeResult(false, Collections.singletonList("ChangeAclState: failed to enter state - need single acl name param in config."));
        }
        String aclName = aclNames.getFirst();
        AclDao aclDao = new AclDao();
        log.debug("looking up acl by name: {}", aclName);
        Acl acl = aclDao.getAclByName(aclName).orElse(null);
        if (acl == null) {
            return new StateChangeResult(false, Arrays.asList(ErrorCode.ACL_NOT_FOUND.getCode(), aclName));
        }
        log.debug("Setting acl from {} to {}", osd.getAclId(), acl.getId());
        OsdDao osdDao = new OsdDao();
        osd.setAclId(acl.getId());
        osdDao.updateOsd((ObjectSystemData) osd, false);
        return new StateChangeResult(true);
    }

    @Override
    public StateChangeResult exit(CinnamonObject osd, State nextState, LifecycleStateConfig config) {
        log.debug("osd {} left ChangeAclState.", osd.getId());
        return new StateChangeResult(true);
    }

}
