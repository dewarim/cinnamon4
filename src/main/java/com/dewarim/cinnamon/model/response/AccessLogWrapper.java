package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.event.AccessLogEntry;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("cinnamon")
public class AccessLogWrapper extends BaseResponse implements Wrapper<AccessLogEntry>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "accessLogEntries")
    @JacksonXmlProperty(localName = "accessLogEntry")
    List<AccessLogEntry> accessLogEntries = new ArrayList<>();

    public AccessLogWrapper() {
    }

    public AccessLogWrapper(List<AccessLogEntry> accessLogEntries) {
        this.accessLogEntries = accessLogEntries;
    }

    public List<AccessLogEntry> getAccessLogEntries() {
        return accessLogEntries;
    }

    public void setAccessLogEntries(List<AccessLogEntry> accessLogEntries) {
        this.accessLogEntries = accessLogEntries;
    }

    @Override
    public List<AccessLogEntry> list() {
        return getAccessLogEntries();
    }

    @Override
    public Wrapper<AccessLogEntry> setList(List<AccessLogEntry> accessLogEntries) {
        setAccessLogEntries(accessLogEntries);
        return this;
    }

    @Override
    public List<Object> examples() {
        AccessLogEntry entry = new AccessLogEntry();
        entry.setId(9L);
        entry.setUrl("/api/osd/getObjectsById");
        entry.setRequest("<getObjectsByIdRequest><ids>42</ids></getObjectsByIdRequest>");
        entry.setResponse("");
        entry.setErrorCode(ErrorCode.OBJECT_NOT_FOUND);
        entry.setErrorMessage("object not found");
        entry.setUserId(2L);
        // the example has to be the same on every build, so no LocalDateTime.now() here:
        entry.setCreated(LOCAL_DATE_TIME_EXAMPLE);
        return List.of(new AccessLogWrapper(List.of(entry)));
    }
}
