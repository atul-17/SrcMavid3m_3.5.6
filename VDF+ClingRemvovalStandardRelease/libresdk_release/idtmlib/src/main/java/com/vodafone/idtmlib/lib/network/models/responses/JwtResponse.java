package com.vodafone.idtmlib.lib.network.models.responses;

import com.vodafone.idtmlib.lib.network.Response;

public class JwtResponse extends Response {
    private String context;
    private String data;

    public JwtResponse() {

    }

    public String getContext() {
        return context;
    }

    public String getData() {
        return data;
    }
}
