package com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils;

import java.io.Serializable;

/**
 * Created by bhargav on 2/2/18.
 */

public class DeviceInfo implements Serializable {
    private String ipAddress;
    private String friendlyName;
    private String fwVersion;
    private String A2dpTypeValue="";
    private String fw_url="";
    private String updatedFwVersion="";
    private String batteryValue;
    private String batteryStatus;
    private String fwProgressValue="",bslInfobeforeSplit="",bslProgressvalue="",bslInfoAfterSplit,bslOldValue,bslNewValue;

    private String USN;//mac id

    public String getUSN() {
        return USN;
    }

    public void setUSN(String USN) {
        this.USN = USN;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public DeviceInfo(String ipAddress, String friendlyName,String usn){
        this.ipAddress = ipAddress;
        this.friendlyName = friendlyName;
        this.USN = usn;
    }
    public String getIpAddress() {
        return ipAddress;
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
    public void setA2dpTypeValue(String A2dpTypeValue){
        this.A2dpTypeValue=A2dpTypeValue;
    }
    public String getA2dpTypeValue(){
        return A2dpTypeValue;
    }
    public void setFw_url(String fw_url){
        this.fw_url=fw_url;
    }
    public String getFw_url(){
        return fw_url;
    }

    public String getUpdatedFwVersion() {
        return updatedFwVersion;
    }

    public void setUpdatedFwVersion(String updatedFwVersion) {
        this.updatedFwVersion = updatedFwVersion;
    }

    public void setBslInfoBeforeSplit(String bslInfobeforeSplit) {
        this.bslInfobeforeSplit = bslInfobeforeSplit;
    }
    public String getBslInfoBeforeSplit(){
        return bslInfobeforeSplit;
    }
    public void setBslInfoAfterSplit(String bslInfoAfterSplit){
        this.bslInfoAfterSplit=bslInfoAfterSplit;
    }
    public String getBslInfoAfterSplit(){
        return bslInfoAfterSplit;
    }
    public void setBslProgressvalue(String bslProgressvalue){
        this.bslProgressvalue=bslProgressvalue;
    }
    public String getBslProgressvalue(){
        return bslProgressvalue;
    }
    public String getBatteryValue() {
        return batteryValue;
    }
    public void setBslOldValue(String bslOldValue){
        this.bslOldValue=bslOldValue;
    }
    public String getBslOldValue(){
        return bslOldValue;
    }
    public void setBslNewValue(String bslNewValue){
        this.bslNewValue=bslNewValue;
    }
    public String getBslNewValue(){
        return bslNewValue;
    }
    public void setBatteryValue(String batteryValue) {
        this.batteryValue = batteryValue;
    }
    public String getFwProgressValue() {
        return fwProgressValue;
    }

    public void setFwProgressValue(String fwProgressValue) {
        this.fwProgressValue = fwProgressValue;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", friendlyName='" + friendlyName + '\'' +
                ", fwVersion='" + fwVersion + '\'' +
                ", A2dpTypeValue='" + A2dpTypeValue + '\'' +
                ", fw_url='" + fw_url + '\'' +
                ", updatedFwVersion='" + updatedFwVersion + '\'' +
                ", batteryValue='" + batteryValue + '\'' +
                ", batteryStatus='" + batteryStatus + '\'' +
                ", fwProgressValue='" + fwProgressValue + '\'' +
                ", bslInfobeforeSplit='" + bslInfobeforeSplit + '\'' +
                ", bslProgressvalue='" + bslProgressvalue + '\'' +
                ", bslInfoAfterSplit='" + bslInfoAfterSplit + '\'' +
                ", bslOldValue='" + bslOldValue + '\'' +
                ", bslNewValue='" + bslNewValue + '\'' +
                ", USN='" + USN + '\'' +
                '}';
    }
}
