package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("createMetaRequest")
public record CreateMetaRequest(
        @JacksonXmlElementWrapper(localName = "metasets")
        @JacksonXmlProperty(localName = "metaset")
        List<Meta> metas) implements ApiRequest<CreateMetaRequest> {

    public CreateMetaRequest {
        if (metas == null) {
            metas = new ArrayList<>();
        }
    }

    public CreateMetaRequest() {
        this(new ArrayList<>());
    }

    public CreateMetaRequest(Long id, String content, Long typeId) {
        this(new ArrayList<>(List.of(new Meta(id, typeId, content))));
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
    public List<ApiRequest<CreateMetaRequest>> examples() {
        return List.of(new CreateMetaRequest(32L, "<xml>some meta</xml>", 3L),
                new CreateMetaRequest(40L, "<meta>metadata</meta>", 10L));
    }
}
