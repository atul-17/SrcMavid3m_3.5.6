package com.vodafone.idtmlib.lib.network.models.bodies;

import com.vodafone.idtmlib.lib.utils.Device;

public class RefreshAccessTokenBody {
    private String sdkId;
    private String deviceOSVersion;
    private String refreshToken;

    public RefreshAccessTokenBody(String sdkId, String refreshToken) {
        this.sdkId = sdkId;
        this.deviceOSVersion = Device.getOSVersion();
        this.refreshToken = refreshToken;
    }
}
