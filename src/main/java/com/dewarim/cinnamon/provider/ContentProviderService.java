package com.dewarim.cinnamon.provider;

import com.dewarim.cinnamon.api.content.ContentProvider;



public class ContentProviderService {


    public final FileSystemContentProvider fileSystemContentProvider;

    public ContentProviderService() {
        fileSystemContentProvider = new FileSystemContentProvider();
    }

    public ContentProvider getContentProvider(String name) {
        switch (name) {
            case "FILE_SYSTEM" -> {
                return fileSystemContentProvider;
            }
            default -> throw new IllegalStateException("Unsupported content provider: " + name);
        }
    }

}
