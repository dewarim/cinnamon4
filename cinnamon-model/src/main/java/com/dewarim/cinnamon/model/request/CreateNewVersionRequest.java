package com.dewarim.cinnamon.model.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateNewVersionRequest {

    private Long           id;
    private List<Metadata> metaRequests = new ArrayList<>();
    private Long         formatId;

    public CreateNewVersionRequest() {
    }

    public CreateNewVersionRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Metadata> getMetaRequests() {
        return metaRequests;
    }

    public void setMetaRequests(List<Metadata> metaRequests) {
        this.metaRequests = metaRequests;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    private boolean validated() {
        if (metaRequests.size() > 0 && metaRequests.stream().filter(Metadata::validated).count() != metaRequests.size()) {
            return false;
        }

        return (formatId == null || formatId > 0) &&  id != null && id > 0;
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
        private String typeName;

        private boolean validated() {
            return ((typeId != null && typeId > 0) || (typeName != null && !typeName.trim().isEmpty()))
                    && (content != null);
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

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }
}
