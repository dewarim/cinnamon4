package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("updateMetaRequest")
public record UpdateMetaRequest(
        @JacksonXmlElementWrapper(localName = "metasets")
        @JacksonXmlProperty(localName = "metaset")
        List<Meta> metas) implements ApiRequest<UpdateMetaRequest>, UpdateRequest<Meta> {

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
    public boolean validated() {
        return Objects.nonNull(metas) && !metas.isEmpty() &&
                metas.stream().allMatch(meta ->
                        meta.getId() != null && meta.getId() > 0 &&
                                meta.getObjectId() != null && meta.getObjectId() > 0
                                && (meta.getTypeId() != null && meta.getTypeId() > 0)
                                && (meta.getContent() != null));
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
