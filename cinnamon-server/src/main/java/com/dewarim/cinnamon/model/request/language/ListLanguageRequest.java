package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListLanguageRequest extends DefaultListRequest implements ListRequest<Language> {
    @Override
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }
}
