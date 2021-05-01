package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListUiLanguageRequest extends DefaultListRequest implements ListRequest<UiLanguage> {

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }
}
