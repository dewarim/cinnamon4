package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteChangeTriggerRequest")
public record DeleteChangeTriggerRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<ChangeTrigger>, ApiRequest<DeleteChangeTriggerRequest> {

    public DeleteChangeTriggerRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteChangeTriggerRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteChangeTriggerRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteChangeTriggerRequest>> examples() {
        return List.of(new DeleteChangeTriggerRequest(77L));
    }
}
