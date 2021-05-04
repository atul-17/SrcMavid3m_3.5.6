package com.libre.irremote.models;

public class ModelDeviceState {

    private String ssid;
    private String profile;


    public ModelDeviceState(String ssid, String profile) {
        this.ssid = ssid;
        this.profile = profile;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
