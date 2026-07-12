package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("createNewVersionRequest")
public record CreateNewVersionRequest(
        Long id,
        @JacksonXmlElementWrapper(localName = "metaRequests")
        @JacksonXmlProperty(localName = "metaRequest")
        List<Metadata> metaRequests,
        Long formatId) implements ApiRequest<CreateNewVersionRequest> {

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

    private boolean validated() {
        if (metaRequests != null &&
                !metaRequests.isEmpty() &&
                metaRequests.stream().filter(Metadata::validated).count() != metaRequests.size()) {
            return false;
        }
        return (formatId == null || formatId > 0) && id != null && id > 0;
    }

    public Optional<CreateNewVersionRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public static class Metadata {
        private String content;
        private Long   typeId;

        public Metadata() {
        }

        public Metadata(String content, Long typeId) {
            this.content = content;
            this.typeId = typeId;
        }

        private boolean validated() {
            return ((typeId != null && typeId > 0)) && (content != null);
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
