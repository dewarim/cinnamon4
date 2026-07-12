package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteUiLanguageRequest")
public record DeleteUiLanguageRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<UiLanguage>, ApiRequest<DeleteUiLanguageRequest> {

    public DeleteUiLanguageRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteUiLanguageRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteUiLanguageRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteUiLanguageRequest>> examples() {
        return List.of(new DeleteUiLanguageRequest(90L));
    }
}
