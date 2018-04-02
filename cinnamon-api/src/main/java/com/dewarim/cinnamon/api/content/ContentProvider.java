package com.dewarim.cinnamon.api.content;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public interface ContentProvider {
    
    void initialize(Properties properties);
    
    InputStream getContentInputStream(ContentMetadata metadata);
    
    Long writeContentOutputStream(ContentMetadata metadata, OutputStream outputStream);
    
}
