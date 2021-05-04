package com.vodafone.idtmlib.lib.ui.dagger;


import com.vodafone.idtmlib.lib.ui.IdGatewayActivity;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = IdGatewayActivityModule.class)
public interface IdGatewayActivityComponent {
    void inject(IdGatewayActivity activity);
}
