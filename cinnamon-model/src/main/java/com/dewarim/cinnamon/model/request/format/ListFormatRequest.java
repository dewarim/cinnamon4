package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListFormatRequest extends DefaultListRequest implements ListRequest<Format> {
    @Override
    public Wrapper<Format> fetchResponseWrapper() {
        return new FormatWrapper();
    }
}
