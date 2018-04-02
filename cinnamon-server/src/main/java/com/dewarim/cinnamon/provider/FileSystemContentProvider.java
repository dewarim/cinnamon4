package com.dewarim.cinnamon.provider;

import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.configuration.ServerConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static com.dewarim.cinnamon.Constants.DATA_ROOT_PATH_PROPERTY_NAME;

public class FileSystemContentProvider implements ContentProvider {

    private String dataRootPath;
    
    @Override
    public void initialize(Properties properties) {
        dataRootPath = properties.getProperty(DATA_ROOT_PATH_PROPERTY_NAME, new ServerConfig().getDataRoot());        
    }

    @Override
    public InputStream getContentInputStream(ContentMetadata metadata) {
        
        return null;
    }

    @Override
    public Long writeContentOutputStream(ContentMetadata metadata, OutputStream outputStream) {
        return null;
    }
}
