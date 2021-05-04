package com.vodafone.idtmlib.lib.dagger;


import com.vodafone.idtmlib.EnvironmentType;
import com.vodafone.idtmlib.lib.network.Environment;
import com.vodafone.idtmlib.lib.utils.Device;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EnvironmentModule {
    private EnvironmentType environmentType;

    public EnvironmentModule(EnvironmentType environmentType) {
        this.environmentType = environmentType;
    }

    @Singleton
    @Provides
    Environment provideEnvironment(Device device) {
        return new Environment(device, environmentType);
    }
}
