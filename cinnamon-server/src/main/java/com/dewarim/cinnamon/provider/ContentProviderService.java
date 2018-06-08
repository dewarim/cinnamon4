package com.dewarim.cinnamon.provider;

import com.dewarim.cinnamon.api.content.ContentProvider;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class ContentProviderService {

    private ServiceLoader<ContentProvider> serviceLoader;
    private static ContentProviderService contentProviderService;

    private ContentProviderService(){
        serviceLoader = ServiceLoader.load(ContentProvider.class);
    }

    public static synchronized ContentProviderService getInstance(){
        if(contentProviderService == null){
            contentProviderService = new ContentProviderService();
        }
        return contentProviderService;
    }

    public ContentProvider getContentProvider(String name){
        ContentProvider contentProvider = null;
        try {
            Iterator<ContentProvider> providers = serviceLoader.iterator();
            while (contentProvider == null && providers.hasNext()) {
                contentProvider = providers.next();
                if (contentProvider.getName().equals(name)) {
                    return contentProvider;
                }
            }
            throw new IllegalStateException("Found no valid content provider for " + name);
        } catch (ServiceConfigurationError e) {
            throw new RuntimeException("Failed to find a valid content provider for " + name, e);
        }
    }

}
