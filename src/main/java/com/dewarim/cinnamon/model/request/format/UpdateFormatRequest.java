package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "updateFormatRequest")
public class UpdateFormatRequest implements UpdateRequest<Format>, ApiRequest<UpdateFormatRequest> {

    @JacksonXmlElementWrapper(localName = "formats")
    @JacksonXmlProperty(localName = "format")
    private List<Format> formats = new ArrayList<>();

    @Override
    public List<Format> list() {
        return formats;
    }

    public UpdateFormatRequest() {
    }

    public UpdateFormatRequest(List<Format> formats) {
        this.formats = formats;
    }

    public List<Format> getFormats() {
        return formats;
    }

    @Override
    public boolean validated() {
        if(formats == null || formats.isEmpty()){
            return false;
        }
        return formats.stream().noneMatch(format ->
                Objects.isNull(format) ||
                        Objects.isNull(format.getId()) ||
                        Objects.isNull(format.getName()) ||
                        Objects.isNull(format.getContentType()) ||
                        Objects.isNull(format.getExtension()) ||
                        Objects.isNull(format.getDefaultObjectTypeId()) ||
                        Objects.isNull(format.getIndexMode()) ||
                        format.getId() < 1 ||
                        format.getName().isBlank() ||
                        format.getContentType().isBlank() ||
                        format.getExtension().isBlank() ||
                        format.getDefaultObjectTypeId() < 1);
    }

    @Override
    public Wrapper<Format> fetchResponseWrapper() {
        return new FormatWrapper();
    }

    @Override
    public List<ApiRequest<UpdateFormatRequest>> examples() {
        return List.of(new UpdateFormatRequest(List.of(new Format("text/plain", "txt", "text", 2L, IndexMode.PLAIN_TEXT))));
    }
}
