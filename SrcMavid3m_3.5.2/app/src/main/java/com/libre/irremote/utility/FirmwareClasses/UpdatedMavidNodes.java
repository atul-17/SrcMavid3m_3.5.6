package com.libre.irremote.utility.FirmwareClasses;
/**
 * Created by suma on 12/9/18.
 */
public class UpdatedMavidNodes {

    private String friendlyname;
    private String fwVersion = "";
    private String nodeaddress;
    private Integer fwState;
    private String USN;
    private boolean isFirmwareUpdateNeeded,FwUpdateNeededBtnEnable;

    public boolean isFirmwareUpdateNeeded() {
        return isFirmwareUpdateNeeded;
    }

    public void setFirmwareUpdateNeeded(boolean firmwareUpdateNeeded) {
        isFirmwareUpdateNeeded = firmwareUpdateNeeded;
    }

    public boolean isFwUpdateNeededBtnEnableCheck() {
        return FwUpdateNeededBtnEnable;
    }

    public void setFwUpdateNeededBtnEnable(boolean FwUpdateNeededBtnEnable) {
        this.FwUpdateNeededBtnEnable = FwUpdateNeededBtnEnable;
    }
    public String getFriendlyname() {
        return friendlyname;
    }

    public void setFriendlyname(String friendlyname) {
        this.friendlyname = friendlyname;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public String getUSN() {
        return USN;
    }

    public void setUSN(String USN) {
        this.USN = USN;
    }
}

