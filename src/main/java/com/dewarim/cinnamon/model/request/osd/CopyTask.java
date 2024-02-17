package com.dewarim.cinnamon.model.request.osd;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class CopyTask {

    private Long       sourceOsdId;
    private Long       targetOsdId;
    private boolean    copyContent;
    @JacksonXmlElementWrapper(localName = "metasetTypeIds")
    @JacksonXmlProperty(localName = "metasetTypeId")
    private List<Long> metasetTypeIds = new ArrayList<>();

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
