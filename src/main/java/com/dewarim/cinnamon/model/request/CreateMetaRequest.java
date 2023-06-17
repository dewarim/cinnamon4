package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "createMetaRequest")
public class CreateMetaRequest implements ApiRequest<Meta> {

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "metaset")
    private List<Meta> metas = new ArrayList<>();

    public CreateMetaRequest() {
    }

    public CreateMetaRequest(Long id, String content, Long typeId) {
        metas.add(new Meta(id, typeId, content));
    }

    public List<Meta> getMetas() {
        return metas;
    }

    public void setMetas(List<Meta> metas) {
        this.metas = metas;
    }

    private boolean validated() {
        if (metas == null || metas.isEmpty()) {
            return false;
        }
        return metas.stream().allMatch(meta ->
                meta.getObjectId() != null && meta.getObjectId() > 0
                        && (meta.getTypeId() != null && meta.getTypeId() > 0)
                        && (meta.getContent() != null)
        );
    }

    public Optional<CreateMetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "CreateMetaRequest{" +
                "metas=" + metas +
                '}';
    }

    @Override
    public List<ApiRequest<Meta>> examples() {
        return List.of(new CreateMetaRequest(32L, "<xml>some meta</xml>", 3L),
                new CreateMetaRequest(40L, "<meta>metadata</meta>", 10L));
    }
}
