package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.Meta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
        @NotEmpty List<@Valid Meta> metas) implements ApiRequest<CreateMetaRequest> {

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

    /**
     * Metadata is attached to an object that already exists, so the objectId is required here - unlike
     * in createOsd and createFolder, which carry their metasets as Meta objects too but create the
     * object together with them.
     */
    @Override
    public Class<?>[] validationGroups() {
        return new Class<?>[]{Meta.OnExistingObject.class};
    }

    public Optional<CreateMetaRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<CreateMetaRequest>> examples() {
        return List.of(new CreateMetaRequest(32L, "<xml>some meta</xml>", 3L),
                new CreateMetaRequest(40L, "<meta>metadata</meta>", 10L));
    }
}
