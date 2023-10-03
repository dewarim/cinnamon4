package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Request a list of config entries either by a list of ids or names
 */
@JacksonXmlRootElement(localName = "configEntryRequest")
public class ConfigEntryRequest implements ApiRequest {
    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private List<Long> ids = new ArrayList<>();

    public ConfigEntryRequest() {
    }


    public ConfigEntryRequest(Long id) {
        this.ids.add(id);
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean validated(){
        return (ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id != null && id > 0));
    }

    public Optional<ConfigEntryRequest> validateRequest(){
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Object> examples() {
        return List.of(new ConfigEntryRequest(123L), new ConfigEntryRequest(456L));
    }
}
