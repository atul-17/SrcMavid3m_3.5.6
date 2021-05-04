package com.vodafone.idtmlib.lib.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lorenzo on 08/01/18.
 */

public class Certificate {
    private String domain;
    @SerializedName("hash") private String[] hashes;

    public Certificate(String domain, String[] hashes) {
        this.domain = domain;
        this.hashes = hashes;
    }

    public String getDomain() {
        return domain;
    }

    public String[] getHashes() {
        return hashes;
    }
}
