package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "createNewVersionRequest")
public class CreateNewVersionRequest implements ApiRequest<CreateNewVersionRequest> {

    private Long           id;
    @JacksonXmlElementWrapper(localName = "metaRequests")
    @JacksonXmlProperty(localName = "metaRequest")
    private List<Metadata> metaRequests = new ArrayList<>();
    private Long           formatId;

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

    public boolean hasMetaRequests() {
        return metaRequests != null && metaRequests.size() > 0;
    }

    private boolean validated() {
        if (metaRequests != null &&
                metaRequests.size() > 0 &&
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
        CreateNewVersionRequest createNewVersionRequest = new CreateNewVersionRequest(5L);
        createNewVersionRequest.setMetaRequests(List.of(new Metadata("<xml>new metadata</xml>", 1L)));
        createNewVersionRequest.setFormatId(4L);
        return List.of(createNewVersionRequest, new CreateNewVersionRequest(6L));
    }
}
