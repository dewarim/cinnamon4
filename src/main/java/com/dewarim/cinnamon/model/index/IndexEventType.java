package com.dewarim.cinnamon.model.index;

public enum IndexEventType {
    IO,
    LUCENE,
    TIKA,
    GENERIC,
    /**
     * When any uncaught exception is thrown during indexing.
     */
    ERROR
}
