package com.dewarim.cinnamon.api.content;

public interface ContentMetadata {

    /**
     * @return the id of the OSD.
     */
    Long getId();

    /**
     * @return name of the content object
     */
    String getName();

    /**
     * getContentSize reports the length in bytes of content of an object from the point when the content
     * was initially transferred to the repository. Useful for static content stored on disc
     * Less useful for dynamically created content.
     *
     * @return length of content on disc as seen when writing data to the content provider.
     */
    Long getContentSize();

    /**
     * @return SHA256 hash value of the content
     */
    String getContentHash();

    /**
     * @return a file path or a URL of where to locate the file content.
     */
    String getContentPath();

    void setContentHash(String contentHash);

    void setContentPath(String contentPath);

    void setContentSize(Long size);

}
