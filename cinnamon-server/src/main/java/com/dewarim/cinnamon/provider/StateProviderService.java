package com.dewarim.cinnamon.provider;

import com.dewarim.cinnamon.api.lifecycle.StateProvider;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class StateProviderService {

    private        ServiceLoader<StateProvider> serviceLoader;
    private static StateProviderService         stateProviderService;

    private StateProviderService(){
        serviceLoader = ServiceLoader.load(StateProvider.class);
    }

    public static synchronized StateProviderService getInstance(){
        if(stateProviderService == null){
            stateProviderService = new StateProviderService();
        }
        return stateProviderService;
    }

    public StateProvider getStateProvider(String name){
        StateProvider stateProvider = null;
        try {
            Iterator<StateProvider> providers = serviceLoader.iterator();
            while (stateProvider == null && providers.hasNext()) {
                stateProvider = providers.next();
                if (stateProvider.getName().equals(name)) {
                    return stateProvider;
                }
            }
            throw new IllegalStateException("Found no valid lifecycle state implementation provider for " + name);
        } catch (ServiceConfigurationError e) {
            throw new RuntimeException("Failed to find a valid lifecycle state implementation provider for " + name, e);
        }
    }

}
