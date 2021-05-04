package com.libre.irremote.BluetoothActivities;


public class MavidSourceDeviceInfo {
    private String ipAddress;
    private String friendlyName;
    private String fwVersion,DeviceId;

    private String connectionStatus  = "";

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    private String updatedFwVersion = "";

    public MavidSourceDeviceInfo(String DeviceId, String friendlyName) {
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
}