package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.model.UiLanguage;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class UiLanguageWrapper implements Wrapper<UiLanguage>{

    @JacksonXmlElementWrapper(localName = "uiLanguages")
    @JacksonXmlProperty(localName = "uiLanguage")
    List<UiLanguage> uiLanguages = new ArrayList<>();

    public List<UiLanguage> getUiLanguages() {
        return uiLanguages;
    }

    public void setUiLanguages(List<UiLanguage> uiLanguages) {
        this.uiLanguages = uiLanguages;
    }

    @Override
    public List<UiLanguage> list() {
        return getUiLanguages();
    }

    @Override
    public Wrapper<UiLanguage> setList(List<UiLanguage> uiLanguages) {
        setUiLanguages(uiLanguages);
        return this;
    }
}
