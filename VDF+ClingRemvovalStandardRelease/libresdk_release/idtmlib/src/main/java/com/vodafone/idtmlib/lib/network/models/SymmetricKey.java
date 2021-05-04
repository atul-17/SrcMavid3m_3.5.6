package com.vodafone.idtmlib.lib.network.models;

import android.text.TextUtils;

public class SymmetricKey {
    private String kty;
    private String k;

    public String getKty() {
        return kty;
    }

    public String getK() {
        return k;
    }

    public boolean isValid() {
        return TextUtils.equals("oct", kty) && !TextUtils.isEmpty(k);
    }
}
