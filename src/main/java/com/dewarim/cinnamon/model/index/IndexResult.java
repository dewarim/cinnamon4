package com.dewarim.cinnamon.model.index;

public enum IndexResult {
    SUCCESS,
    /**
     * The indexing of a single object has failed (can be repeated later), for example if we try to parse an XML file and
     * the XML is not well-formed.
     * Increase fail counter on this index job by 1.
     */
    FAILED,
    /**
     * An I/O error occurred, for example if out of disk space or Lucene failed to write the index for some reason.
     * In that case, we try to roll back the index changes.
     */
    ERROR,
    IGNORE
}
