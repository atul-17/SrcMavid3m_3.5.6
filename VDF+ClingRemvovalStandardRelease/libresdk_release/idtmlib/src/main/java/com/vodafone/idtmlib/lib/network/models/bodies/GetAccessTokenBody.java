package com.vodafone.idtmlib.lib.network.models.bodies;

import com.vodafone.idtmlib.lib.utils.Device;

public class GetAccessTokenBody {
    private String sdkId;
    private String deviceOSVersion;
    private String nonce;
    private String code;
    private String redirectUri;

    public GetAccessTokenBody(String sdkId, String nonce, String code, String redirectUri) {
        this.sdkId = sdkId;
        this.deviceOSVersion = Device.getOSVersion();
        this.nonce = nonce;
        this.code = code;
        this.redirectUri = redirectUri;
    }
}
