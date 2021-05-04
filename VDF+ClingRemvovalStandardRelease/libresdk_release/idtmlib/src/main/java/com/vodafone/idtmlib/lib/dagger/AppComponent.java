package com.vodafone.idtmlib.lib.dagger;

import com.vodafone.idtmlib.IdtmLib;
import com.vodafone.idtmlib.lib.AutoRefreshTokenJobService;
import com.vodafone.idtmlib.lib.ui.dagger.IdGatewayActivityComponent;
import com.vodafone.idtmlib.lib.ui.dagger.IdGatewayActivityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        EnvironmentModule.class,
        NetworkModule.class,
        PrinterModule.class
})
public interface AppComponent {
    void inject(IdtmLib idtmLib);
    void inject(AutoRefreshTokenJobService service);

    IdGatewayActivityComponent plus(IdGatewayActivityModule module);
}
