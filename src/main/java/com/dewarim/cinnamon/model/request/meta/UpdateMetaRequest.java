package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateMetaRequest")
public record UpdateMetaRequest(
        @JacksonXmlElementWrapper(localName = "metasets")
        @JacksonXmlProperty(localName = "metaset")
        @NotEmpty List<@Valid Meta> metas) implements ApiRequest<UpdateMetaRequest>, UpdateRequest<Meta> {

    public UpdateMetaRequest {
        if (metas == null) {
            metas = new ArrayList<>();
        }
    }

    @Override
    public List<Meta> list() {
        return metas;
    }

    @Override
    public Wrapper<Meta> fetchResponseWrapper() {
        return new MetaWrapper();
    }

    @Override
    public List<ApiRequest<UpdateMetaRequest>> examples() {
        Meta meta = new Meta(1L, 2L, "meta content update");
        meta.setId(123L);
        return List.of(new UpdateMetaRequest(List.of(meta)));
    }
}
