package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateFormatRequest implements CreateRequest<Format>, ApiRequest {

    private List<Format> formats = new ArrayList<>();

    public CreateFormatRequest() {
    }

    public CreateFormatRequest(List<Format> formats) {
        this.formats = formats;
    }

    public List<Format> getFormats() {
        return formats;
    }

    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    @Override
    public List<Format> list() {
        return formats;
    }

    @Override
    public boolean validated() {
        if (formats == null || formats.isEmpty()) {
            return false;
        }
        return formats.stream().noneMatch(format ->
                Objects.isNull(format) ||
                        Objects.isNull(format.getName()) ||
                        Objects.isNull(format.getContentType()) ||
                        Objects.isNull(format.getExtension()) ||
                        Objects.isNull(format.getDefaultObjectTypeId()) ||
                        format.getName().isBlank() ||
                        format.getContentType().isBlank() ||
                        format.getExtension().isBlank() ||
                        format.getDefaultObjectTypeId() < 1);
    }

    @Override
    public Wrapper<Format> fetchResponseWrapper() {
        return new FormatWrapper();
    }
}
