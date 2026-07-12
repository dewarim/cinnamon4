package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateUiLanguageRequest")
public record UpdateUiLanguageRequest(
        @JacksonXmlElementWrapper(localName = "uiLanguages")
        @JacksonXmlProperty(localName = "uiLanguage")
        List<UiLanguage> uiLanguages) implements UpdateRequest<UiLanguage>, ApiRequest<UpdateUiLanguageRequest> {

    public UpdateUiLanguageRequest {
        if (uiLanguages == null) {
            uiLanguages = new ArrayList<>();
        }
    }

    public UpdateUiLanguageRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new UiLanguage(id, name))));
    }

    @Override
    public List<UiLanguage> list() {
        return uiLanguages;
    }

    @Override
    public boolean validated() {
        return uiLanguages.stream().allMatch(uiLanguage ->
                uiLanguage != null && uiLanguage.getIsoCode() != null && !uiLanguage.getIsoCode().trim().isEmpty()
                        && uiLanguage.getId() != null && uiLanguage.getId() > 0);
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }

    @Override
    public List<ApiRequest<UpdateUiLanguageRequest>> examples() {
        return List.of(new UpdateUiLanguageRequest(List.of(new UiLanguage(69L, "FR"), new UiLanguage(96L, "GR"))));
    }
}
