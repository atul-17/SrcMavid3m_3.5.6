package com.libre.irremote.models;

public class ModelBlutoothDevices {
    public String deviceName;
    public String macAddress;
    public Boolean isPaired = false;

    public ModelBlutoothDevices(String deviceName, String macAddress, Boolean isPaired) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.isPaired = isPaired;
    }
}
