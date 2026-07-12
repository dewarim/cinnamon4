package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteLanguageRequest")
public record DeleteLanguageRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Language>, ApiRequest<DeleteLanguageRequest> {

    public DeleteLanguageRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteLanguageRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteLanguageRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteLanguageRequest>> examples() {
        return List.of(new DeleteLanguageRequest(999L));
    }
}
