package com.dewarim.cinnamon.api.content;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public interface ContentProvider {

    String getName();

    InputStream getContentStream(ContentMetadata metadata) throws IOException;

    /**
     * @param metadata    object metadata (id,name etc) - which may be useful for storage (for example: URLs)
     * @param inputStream data to be stored
     * @return new ContentMetadata with unique contentPath which can be used to retrieve the data
     * by using getContentStream. Also: contentHash and contentSize.
     */
    ContentMetadata writeContentStream(ContentMetadata metadata, FileInputStream inputStream) throws IOException;

}
