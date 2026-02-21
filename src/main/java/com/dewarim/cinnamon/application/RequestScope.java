package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.index.IndexJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle request scoped variables as thread local objects
 */
public class RequestScope {
    private static final Logger log = LogManager.getLogger(RequestScope.class);

    // UserAccount of the currently connected user.
    static final ThreadLocal<UserAccount> currentUser = ThreadLocal.withInitial(() -> null);

    public static UserAccount getCurrentUser(){
        return currentUser.get();
    }

    public static void setCurrentUser(UserAccount user){
        currentUser.set(user);
    }

    static final ThreadLocal<List<IndexJob>> indexJobs = ThreadLocal.withInitial(ArrayList::new);

    public static void addIndexJob(IndexJob job){
        indexJobs.get().add(job);
    }

    public static List<IndexJob> getIndexJobs(){
        return indexJobs.get();
    }

    public static void removeIndexJobs(){
        indexJobs.remove();
    }

    public static void clearThreadLocal(){
        log.debug("Clearing request scope thread locals");
        currentUser.remove();
        indexJobs.remove();
    }
}
