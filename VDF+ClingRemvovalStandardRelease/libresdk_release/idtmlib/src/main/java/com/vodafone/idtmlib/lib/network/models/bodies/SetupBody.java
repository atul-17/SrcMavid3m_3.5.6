package com.vodafone.idtmlib.lib.network.models.bodies;

import com.vodafone.idtmlib.lib.network.JwtHelper;
import com.vodafone.idtmlib.lib.network.models.PublicKey;
import com.vodafone.idtmlib.lib.utils.Device;

import java.security.interfaces.RSAPublicKey;

public class SetupBody {
    private String referenceId;
    private String deviceModel;
    private String deviceOSVersion;
    private String deviceType;
    private String proofId;
    private PublicKey publicKey;
    private String encAlg;
    private String encMethod;

    public SetupBody(String referenceId, String proofId, RSAPublicKey publicKey) {
        this.referenceId = referenceId;
        this.deviceModel = Device.getDeviceModel();
        this.deviceOSVersion = Device.getOSVersion();
        this.deviceType = "Android";
        this.proofId = proofId;
        this.publicKey = new PublicKey(publicKey);
        this.encAlg = JwtHelper.getRsaJweAlgorithm();
        this.encMethod = JwtHelper.getRsaJweMethod();
    }
}
