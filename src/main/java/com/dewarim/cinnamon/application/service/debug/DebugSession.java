package com.dewarim.cinnamon.application.service.debug;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.model.UserAccount;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DebugSession {

    private static final Logger    log     = LogManager.getLogger(DebugSession.class);
    private static final XmlMapper xmlMapper;
    private static final byte[]    NEW_LINE = "\n".getBytes();

    static {
        xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private final String sessionId;

    public DebugSession(UserAccount currentUser, long actionCounter) {
        if (currentUser != null) {
            sessionId = currentUser.getName() + "-" + String.format("%08d", actionCounter);
        }
        else {
            sessionId = "unknown-" + actionCounter;
        }
    }

    public void start() {
        log.info("Starting debug session {}", sessionId);
    }

    public void stop() {
        log.info("Stopping debug session {}", sessionId);
    }

    public void log(String message, Object object) {
        if (!CinnamonServer.isDebugEnabled()) {
            // do not log if disabled
            return;
        }
        File logFile = new File(CinnamonServer.config.getDebugConfig().getDebugFolderPath(), sessionId + ".log");
        if (!logFile.exists()) {
            try {
                boolean createResult = logFile.createNewFile();
                if (!createResult) {
                    throw new CinnamonException("Unable to create debug log file");
                }
            } catch (IOException e) {
                throw new CinnamonException(e.getMessage(), e);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            fos.write(message.getBytes());
            fos.write(NEW_LINE);
            if (object instanceof String) {
                fos.write(((String) object).getBytes());
                fos.write(NEW_LINE);
            }
            else {
                fos.write(xmlMapper.writeValueAsBytes(object));
            }
            fos.write(NEW_LINE);
        } catch (IOException e) {
            throw new CinnamonException("Failed to write debug log: " + e.getMessage(), e);
        }
        log.debug("session: {} {}",sessionId, object);
    }
}
