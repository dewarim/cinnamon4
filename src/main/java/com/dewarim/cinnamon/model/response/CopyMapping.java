package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Objects;

/**
 * Which source object a copy was made from, as reported by {@code OSD__COPY}.
 * <p>
 * A copy carries no reference to its source - {@code predecessorId} and {@code rootId} are
 * deliberately cleared, since a copy starts a version tree of its own - and the order of the
 * copies in the response need not match the order of the ids in the request, because the
 * sources are loaded by id list. Without this mapping a client that has to touch each copy
 * afterwards, for instance to undo the {@code Copy_} prefix or to rebuild relations, can only
 * copy one object per request.
 */
@JsonRootName("copy")
public class CopyMapping {

    private Long sourceId;
    private Long copyId;

    public CopyMapping() {
    }

    public CopyMapping(Long sourceId, Long copyId) {
        this.sourceId = sourceId;
        this.copyId = copyId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getCopyId() {
        return copyId;
    }

    public void setCopyId(Long copyId) {
        this.copyId = copyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyMapping that)) {
            return false;
        }
        return Objects.equals(sourceId, that.sourceId) && Objects.equals(copyId, that.copyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, copyId);
    }

    @Override
    public String toString() {
        return "CopyMapping{sourceId=" + sourceId + ", copyId=" + copyId + "}";
    }
}
