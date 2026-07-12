package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createLanguageRequest")
public record CreateLanguageRequest(
        @JacksonXmlElementWrapper(localName = "languages")
        @JacksonXmlProperty(localName = "language")
        List<Language> languages) implements CreateRequest<Language>, ApiRequest<Language> {

    public CreateLanguageRequest {
        if (languages == null) {
            languages = new ArrayList<>();
        }
    }

    public CreateLanguageRequest(String isoCode) {
        this(new ArrayList<>(List.of(new Language(isoCode))));
    }

    @Override
    public List<Language> list() {
        return languages;
    }

    @Override
    public boolean validated() {
        return languages.stream().noneMatch(language -> language == null ||
                language.getIsoCode() == null ||
                language.getIsoCode().trim().isEmpty());
    }

    @Override
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }

    @Override
    public List<ApiRequest<Language>> examples() {
        return List.of(new CreateLanguageRequest(List.of(
                new Language("en"), new Language("de"), new Language("fr"))
        ));
    }
}
