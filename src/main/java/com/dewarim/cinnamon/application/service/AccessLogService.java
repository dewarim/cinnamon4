package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.dao.event.AccessLogDao;
import com.dewarim.cinnamon.model.event.AccessLogEntry;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AccessLogService {
    private static final Logger log = LogManager.getLogger(AccessLogService.class);

    private static final ObjectMapper mapper       = new XmlMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final        AccessLogDao accessLogDao = new AccessLogDao();

    public void addEntry(CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse, CinnamonErrorWrapper errorWrapper, ErrorCode errorCode, String errorMessage, Long userId) {
        AccessLogEntry accessLogEntry;
        try {
            accessLogEntry = new AccessLogEntry(cinnamonRequest, cinnamonResponse,
                    mapper.writeValueAsString(errorWrapper),
                    errorCode, errorMessage, userId);
            accessLogDao.insert(accessLogEntry);
        } catch (IOException e) {
            throw new CinnamonException("Error logging to DB failed:", e);
        }
        log.debug("AccessLogEntry: {}", accessLogEntry);
    }

    public void truncateLogIfNecessary() {
        int rowCount = accessLogDao.count();
        if(rowCount > CinnamonServer.getConfig().getLoggingConfig().getTruncateLogAfterRow()) {
            accessLogDao.truncate(CinnamonServer.getConfig().getLoggingConfig().getTruncateTableBy());
        }
    }
}
