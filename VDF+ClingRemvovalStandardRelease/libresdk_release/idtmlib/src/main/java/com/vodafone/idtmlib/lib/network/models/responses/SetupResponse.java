package com.vodafone.idtmlib.lib.network.models.responses;

import com.vodafone.idtmlib.lib.network.Response;
import com.vodafone.idtmlib.lib.network.models.Certificate;

import java.util.List;

public class SetupResponse extends Response {
    private String sdkId;
    private String symmetricKey;
    private long keyExpDate;
    private String refreshState;
    private List<Certificate> certificates;

    public String getSdkId() {
        return sdkId;
    }

    public String getSymmetricKey() {
        return symmetricKey;
    }

    public long getKeyExpDate() {
        return keyExpDate;
    }

    public String getRefreshState() {
        return refreshState;
    }

    public List<Certificate> getCertificates() {
        return certificates;
    }
}
