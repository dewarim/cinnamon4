package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("createNewVersionRequest")
public record CreateNewVersionRequest(
        @NotNull @Positive Long id,
        @JacksonXmlElementWrapper(localName = "metaRequests")
        @JacksonXmlProperty(localName = "metaRequest")
        List<@Valid Metadata> metaRequests,
        @Positive Long formatId) implements ApiRequest<CreateNewVersionRequest> {

    public CreateNewVersionRequest {
        if (metaRequests == null) {
            metaRequests = new ArrayList<>();
        }
    }

    public CreateNewVersionRequest(Long id) {
        this(id, new ArrayList<>(), null);
    }

    public boolean hasMetaRequests() {
        return metaRequests != null && !metaRequests.isEmpty();
    }

    public Optional<CreateNewVersionRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    public static class Metadata {
        // Request validation, checked when a request cascades into this class with @Valid.
        @NotNull
        private String content;
        @NotNull
        @Positive
        private Long   typeId;

        public Metadata() {
        }

        public Metadata(String content, Long typeId) {
            this.content = content;
            this.typeId = typeId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getTypeId() {
            return typeId;
        }

        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }
    }

    @Override
    public List<ApiRequest<CreateNewVersionRequest>> examples() {
        return List.of(
                new CreateNewVersionRequest(5L, List.of(new Metadata("<xml>new metadata</xml>", 1L)), 4L),
                new CreateNewVersionRequest(6L));
    }
}
