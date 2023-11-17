package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listLanguageRequest")
public class ListLanguageRequest extends DefaultListRequest implements ListRequest<Language>, ApiRequest<ListLanguageRequest> {
    @Override
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }

    @Override
    public List<ApiRequest<ListLanguageRequest>> examples() {
        return List.of(new ListLanguageRequest());
    }
}
