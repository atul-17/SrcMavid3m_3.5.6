package com.libre.irremote.BluetoothActivities;

public class BtSourceDeviceInfo {
    private String ipAddress;
    private String friendlyName;
    private String fwVersion,DeviceId,connectedStatus;


    public BtSourceDeviceInfo(String DeviceId, String friendlyName) {
        this.DeviceId = DeviceId;
        this.friendlyName = friendlyName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setDeviceID(String DeviceId) {
        this.DeviceId = DeviceId;
    }
    public String getDeviceID() {
        return DeviceId;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }


    public String getConnectedStatus() {
        return connectedStatus;
    }

    public void setConnectedStatus(String connectedStatus) {
        this.connectedStatus = connectedStatus;
    }

}