package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateLanguageRequest")
public record UpdateLanguageRequest(
        @JacksonXmlElementWrapper(localName = "languages")
        @JacksonXmlProperty(localName = "language")
        List<Language> languages) implements UpdateRequest<Language>, ApiRequest<UpdateLanguageRequest> {

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
    public boolean validated() {
        return languages.stream().allMatch(language ->
                language != null && language.getIsoCode() != null && !language.getIsoCode().trim().isEmpty()
                        && language.getId() != null && language.getId() > 0);
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
