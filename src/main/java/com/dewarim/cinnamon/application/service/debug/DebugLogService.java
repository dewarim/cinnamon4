package com.dewarim.cinnamon.application.service.debug;

import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class DebugLogService {

    private static final Logger log = LogManager.getLogger(DebugLogService.class);

    private static final AtomicLong actionCounter = new AtomicLong();

    private static final ThreadLocal<DebugSession> debugSessionThreadLocal = ThreadLocal.withInitial(
            () -> new DebugSession(ThreadLocalSqlSession.getCurrentUser(), actionCounter.incrementAndGet()));

    public static DebugSession getDebugSession() {
        return debugSessionThreadLocal.get();
    }

    public static void log(String message, Object object){
        getDebugSession().log(message, object);
    }

    public static void stop(){
        debugSessionThreadLocal.remove();
    }
}
