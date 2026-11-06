package com.dewarim.cinnamon.model.index;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One OSD whose Tika extraction was rejected by the Tika server, or whose content could not be
 * read. Rows are created by TikaService and removed again as soon as an extraction succeeds, so
 * the table is a list of the documents that currently have no usable Tika metadata.
 * <p>
 * A row is only written for failures that say something about this document. A Tika server that
 * cannot be reached at all is not recorded: it would produce a row per pending OSD and say nothing
 * about any of them, and the extraction is retried on the next pass anyway.
 */
public class TikaFailure {

    private Long    osdId;
    private Integer attempts;

    // as everywhere else in the API: without this, the value goes out as an array of numbers and
    // the client cannot read it back (see ObjectSystemData.created).
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAttempt;

    private String lastError;

    public TikaFailure() {
    }

    public TikaFailure(Long osdId, Integer attempts, LocalDateTime lastAttempt, String lastError) {
        this.osdId       = osdId;
        this.attempts    = attempts;
        this.lastAttempt = lastAttempt;
        this.lastError   = lastError;
    }

    public Long getOsdId() {
        return osdId;
    }

    public void setOsdId(Long osdId) {
        this.osdId = osdId;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(LocalDateTime lastAttempt) {
        this.lastAttempt = lastAttempt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TikaFailure that = (TikaFailure) o;
        return Objects.equals(osdId, that.osdId) && Objects.equals(attempts, that.attempts)
                && Objects.equals(lastError, that.lastError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(osdId);
    }

    @Override
    public String toString() {
        return "TikaFailure{" +
                "osdId=" + osdId +
                ", attempts=" + attempts +
                ", lastAttempt=" + lastAttempt +
                ", lastError='" + lastError + '\'' +
                '}';
    }
}
