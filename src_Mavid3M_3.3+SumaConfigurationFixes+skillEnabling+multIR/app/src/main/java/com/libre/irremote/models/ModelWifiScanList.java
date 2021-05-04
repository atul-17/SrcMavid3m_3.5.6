package com.libre.irremote.models;

public class ModelWifiScanList {
    private String ssid;
    private String security;
    private String rssi;

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public ModelWifiScanList(String ssid, String security, String rssi) {
        this.ssid = ssid;
        this.security = security;
        this.rssi = rssi;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }
}
