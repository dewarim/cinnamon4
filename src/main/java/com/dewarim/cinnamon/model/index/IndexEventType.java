package com.dewarim.cinnamon.model.index;

/**
 * What kind of problem an index event records. There is deliberately no TIKA value: a document Tika
 * could not extract metadata from is indexed all the same, just without that metadata, and is
 * recorded per document in tika_failures - with its attempt count, and cleared once an extraction
 * succeeds - rather than as an event here. The value existed from the start and was written by
 * nothing, which made admin/listIndexEvents look as though it could answer a question it cannot.
 */
public enum IndexEventType {
    /**
     * The content of the object could not be read, for example because the content store is not
     * reachable or the file is gone.
     */
    IO,
    /**
     * Lucene refused the document.
     */
    LUCENE,
    /**
     * A single item the indexer could not handle.
     */
    GENERIC,
    /**
     * When any uncaught exception is thrown during indexing.
     */
    ERROR
}
