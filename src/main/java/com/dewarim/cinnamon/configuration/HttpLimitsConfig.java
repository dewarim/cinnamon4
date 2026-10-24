package com.dewarim.cinnamon.configuration;

/**
 * Limits and information disclosure settings for the HTTP/HTTPS layer.
 * <p>
 * All sizes are in bytes. A value of 0 means "no limit" for the size fields.
 */
public class HttpLimitsConfig {

    /**
     * Maximum size of a non-multipart request body (XML/JSON API requests). These are buffered into memory,
     * so an unbounded body would allow a client to exhaust the heap. File content is uploaded as multipart
     * and governed by {@link #maxMultipartRequestSize} / {@link #maxMultipartFileSize} instead.
     */
    private long maxRequestBodySize = 64L * 1024 * 1024;

    /**
     * Maximum total size of a multipart request (OSD content upload). 0 = unlimited (Jetty default).
     */
    private long maxMultipartRequestSize = 0;

    /**
     * Maximum size of a single file within a multipart request. 0 = unlimited (Jetty default).
     */
    private long maxMultipartFileSize = 0;

    /**
     * Size threshold above which a multipart file part is written to disk instead of kept in memory.
     */
    private int multipartFileSizeThreshold = 1024 * 1024;

    /**
     * Whether to send the "Server: Jetty(version)" response header. Off by default to avoid disclosing
     * the exact server version.
     */
    private boolean sendServerVersion = false;

    /**
     * Whether error pages may contain Java stack traces and internal messages. Off by default.
     */
    private boolean showStackTraces = false;

    /**
     * Maximum size of the HTTP request header (Jetty default is 8192).
     */
    private int requestHeaderSize = 8192;

    public long getMaxRequestBodySize() {
        return maxRequestBodySize;
    }

    public void setMaxRequestBodySize(long maxRequestBodySize) {
        this.maxRequestBodySize = maxRequestBodySize;
    }

    public long getMaxMultipartRequestSize() {
        return maxMultipartRequestSize;
    }

    public void setMaxMultipartRequestSize(long maxMultipartRequestSize) {
        this.maxMultipartRequestSize = maxMultipartRequestSize;
    }

    public long getMaxMultipartFileSize() {
        return maxMultipartFileSize;
    }

    public void setMaxMultipartFileSize(long maxMultipartFileSize) {
        this.maxMultipartFileSize = maxMultipartFileSize;
    }

    public int getMultipartFileSizeThreshold() {
        return multipartFileSizeThreshold;
    }

    public void setMultipartFileSizeThreshold(int multipartFileSizeThreshold) {
        this.multipartFileSizeThreshold = multipartFileSizeThreshold;
    }

    public boolean isSendServerVersion() {
        return sendServerVersion;
    }

    public void setSendServerVersion(boolean sendServerVersion) {
        this.sendServerVersion = sendServerVersion;
    }

    public boolean isShowStackTraces() {
        return showStackTraces;
    }

    public void setShowStackTraces(boolean showStackTraces) {
        this.showStackTraces = showStackTraces;
    }

    public int getRequestHeaderSize() {
        return requestHeaderSize;
    }

    public void setRequestHeaderSize(int requestHeaderSize) {
        this.requestHeaderSize = requestHeaderSize;
    }

    @Override
    public String toString() {
        return "HttpLimitsConfig{" +
                "maxRequestBodySize=" + maxRequestBodySize +
                ", maxMultipartRequestSize=" + maxMultipartRequestSize +
                ", maxMultipartFileSize=" + maxMultipartFileSize +
                ", multipartFileSizeThreshold=" + multipartFileSizeThreshold +
                ", sendServerVersion=" + sendServerVersion +
                ", showStackTraces=" + showStackTraces +
                ", requestHeaderSize=" + requestHeaderSize +
                '}';
    }
}
