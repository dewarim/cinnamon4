package com.dewarim.cinnamon.model.request.osd;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class CopyTask {

    // Request validation, checked when a request cascades into this class with @Valid.
    @NotNull
    @Positive
    private Long       sourceOsdId;
    @NotNull
    @Positive
    private Long       targetOsdId;
    private boolean    copyContent;
    @JacksonXmlElementWrapper(localName = "metasetTypeIds")
    @JacksonXmlProperty(localName = "metasetTypeId")
    private List<@NotNull @Positive Long> metasetTypeIds = new ArrayList<>();

    public CopyTask() {
    }

    public CopyTask(Long sourceOsdId, Long targetOsdId, boolean copyContent, List<Long> metasetTypeIds) {
        this.sourceOsdId    = sourceOsdId;
        this.targetOsdId    = targetOsdId;
        this.copyContent    = copyContent;
        this.metasetTypeIds = metasetTypeIds;
    }

    public Long getSourceOsdId() {
        return sourceOsdId;
    }

    public Long getTargetOsdId() {
        return targetOsdId;
    }

    public boolean isCopyContent() {
        return copyContent;
    }

    public List<Long> getMetasetTypeIds() {
        return metasetTypeIds;
    }

    @Override
    public String toString() {
        return "CopyTask{" +
                "sourceOsdId=" + sourceOsdId +
                ", targetOsdId=" + targetOsdId +
                ", copyContent=" + copyContent +
                ", metasetTypeIds=" + metasetTypeIds +
                '}';
    }
}
