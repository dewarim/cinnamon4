package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateLanguageRequest")
public record UpdateLanguageRequest(
        @JacksonXmlElementWrapper(localName = "languages")
        @JacksonXmlProperty(localName = "language")
        @NotEmpty List<@Valid Language> languages) implements UpdateRequest<Language>, ApiRequest<UpdateLanguageRequest> {

    public UpdateLanguageRequest {
        if (languages == null) {
            languages = new ArrayList<>();
        }
    }

    public UpdateLanguageRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new Language(id, name))));
    }

    @Override
    public List<Language> list() {
        return languages;
    }

    @Override
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }

    @Override
    public List<ApiRequest<UpdateLanguageRequest>> examples() {
        return List.of(new UpdateLanguageRequest(53L, "new-isoCode-for-language"));
    }
}
