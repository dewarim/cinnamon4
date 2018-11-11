package com.dewarim.cinnamon.provider;

import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.dewarim.cinnamon.api.lifecycle.State;
import com.dewarim.cinnamon.api.lifecycle.StateProvider;

import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class StateProviderService {

    private        ServiceLoader<StateProvider> serviceLoader;
    private static StateProviderService         stateProviderService;

    private StateProviderService() {
        serviceLoader = ServiceLoader.load(StateProvider.class);
    }

    public static synchronized StateProviderService getInstance() {
        if (stateProviderService == null) {
            stateProviderService = new StateProviderService();
        }
        return stateProviderService;
    }

    public StateProvider getStateProvider(String name) {
        try {
            for (StateProvider stateProvider : serviceLoader) {
                if (stateProvider.getName().equals(name)) {
                    return stateProvider;
                }
            }
            throw new IllegalStateException("Found no valid lifecycle state implementation provider for " + name);
        } catch (ServiceConfigurationError e) {
            throw new RuntimeException("Failed to find a valid lifecycle state implementation provider for " + name, e);
        }
    }

    public List<State> getNextStateImplementationsByConfiguration(LifecycleStateConfig config) {
        StateProviderService providerService = StateProviderService.getInstance();

        // note: this will throw an exception if the state provider is not found.
        return config.getNextStates().stream()
                .map(providerService::getStateProvider)
                .map(StateProvider::getState)
                .collect(Collectors.toList());

    }


}
