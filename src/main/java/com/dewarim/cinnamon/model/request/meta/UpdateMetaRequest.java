package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "updateMetaRequest")
public class UpdateMetaRequest implements ApiRequest<UpdateMetaRequest>, UpdateRequest<Meta> {

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "metaset")
    private List<Meta> metas = new ArrayList<>();

    public UpdateMetaRequest() {
    }

    public UpdateMetaRequest(List<Meta> metas) {
        this.metas = metas;
    }

    @Override
    public List<Meta> list() {
        return metas;
    }

    public List<Meta> getMetas() {
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
    public String toString() {
        return "UpdateMetaRequest{" +
                "metas=" + metas +
                '}';
    }

    @Override
    public List<ApiRequest<UpdateMetaRequest>> examples() {
        Meta meta = new Meta(1L, 2L, "meta content update");
        meta.setId(123L);
        return List.of(new UpdateMetaRequest(List.of(meta)));
    }
}
