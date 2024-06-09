package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class FormatWrapper extends BaseResponse implements Wrapper<Format>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "formats")
    @JacksonXmlProperty(localName = "format")
    List<Format> formats = new ArrayList<>();

    public List<Format> getFormats() {
        return formats;
    }

    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    @Override
    public List<Format> list() {
        return getFormats();
    }

    @Override
    public Wrapper<Format> setList(List<Format> formats) {
        setFormats(formats);
        return this;
    }

    @Override
    public List<Object> examples() {
        FormatWrapper formatWrapper = new FormatWrapper();
        Format        format        = new Format("text/adoc", "adoc", "AsciiDoc", 1L, IndexMode.PLAIN_TEXT);
        format.setId(50L);
        formatWrapper.getFormats().add(format);
        return List.of(formatWrapper);
    }
}
