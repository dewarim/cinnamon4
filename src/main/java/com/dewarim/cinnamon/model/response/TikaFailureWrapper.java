package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.index.TikaFailure;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("cinnamon")
public class TikaFailureWrapper extends BaseResponse implements Wrapper<TikaFailure>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "tikaFailures")
    @JacksonXmlProperty(localName = "tikaFailure")
    List<TikaFailure> tikaFailures = new ArrayList<>();

    public TikaFailureWrapper() {
    }

    public TikaFailureWrapper(List<TikaFailure> tikaFailures) {
        this.tikaFailures = tikaFailures;
    }

    public List<TikaFailure> getTikaFailures() {
        return tikaFailures;
    }

    public void setTikaFailures(List<TikaFailure> tikaFailures) {
        this.tikaFailures = tikaFailures;
    }

    @Override
    public List<TikaFailure> list() {
        return getTikaFailures();
    }

    @Override
    public Wrapper<TikaFailure> setList(List<TikaFailure> tikaFailures) {
        setTikaFailures(tikaFailures);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new TikaFailureWrapper(List.of(new TikaFailure(143L, 3, LOCAL_DATE_TIME_EXAMPLE,
                "Tika answered with HTTP status 422"))));
    }
}
