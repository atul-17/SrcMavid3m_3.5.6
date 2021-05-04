package com.libre.irremote.models;

import com.android.volley.VolleyError;

public class AuthURLRepoModel {
    private String authUrl;
    private VolleyError volleyError;

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public VolleyError getVolleyError() {
        return volleyError;
    }

    public void setVolleyError(VolleyError volleyError) {
        this.volleyError = volleyError;
    }
}
