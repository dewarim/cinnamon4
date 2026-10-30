package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateFormatRequest")
public record UpdateFormatRequest(
        @JacksonXmlElementWrapper(localName = "formats")
        @JacksonXmlProperty(localName = "format")
        @NotEmpty List<@Valid Format> formats) implements UpdateRequest<Format>, ApiRequest<UpdateFormatRequest> {

    public UpdateFormatRequest {
        if (formats == null) {
            formats = new ArrayList<>();
        }
    }

    @Override
    public List<Format> list() {
        return formats;
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
