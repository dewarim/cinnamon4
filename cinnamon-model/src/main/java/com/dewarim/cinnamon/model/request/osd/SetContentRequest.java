package com.dewarim.cinnamon.model.request.osd;

import java.util.Optional;

public class SetContentRequest {

    private Long id;
    private Long formatId;

    public SetContentRequest() {
    }

    public SetContentRequest(Long id, Long formatId) {
        this.id = id;
        this.formatId = formatId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    public Optional<SetContentRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    private boolean validated() {
        return id != null && id > 0 &&
                formatId != null && formatId > 0;
    }

    @Override
    public String toString() {
        return "SetContentRequest{" +
                "id=" + id +
                ", formatId=" + formatId +
                '}';
    }
}
