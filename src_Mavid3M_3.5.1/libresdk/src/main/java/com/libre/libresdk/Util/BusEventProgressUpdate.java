package com.libre.libresdk.Util;


public class BusEventProgressUpdate {


    public static class SendFirmwareProgressEvents {
        private String otaStatus;
        private String blsStatus;
        private String deviceIp;

        private String a2dpStatus;
        private String a2dpDeviceName;
        private String a2dpDeviceUUID;
        private String connctedDeviceFriendlyName;

        public String getConnctedDeviceFriendlyName() {
            return connctedDeviceFriendlyName;
        }

        public void setConnctedDeviceFriendlyName(String connctedDeviceFriendlyName) {
            this.connctedDeviceFriendlyName = connctedDeviceFriendlyName;
        }

        public String getA2dpStatus() {
            return a2dpStatus;
        }

        public void setA2dpStatus(String a2dpStatus) {
            this.a2dpStatus = a2dpStatus;
        }

        public String getA2dpDeviceName() {
            return a2dpDeviceName;
        }

        public void setA2dpDeviceName(String a2dpDeviceName) {
            this.a2dpDeviceName = a2dpDeviceName;
        }

        public String getA2dpDeviceUUID() {
            return a2dpDeviceUUID;
        }

        public void setA2dpDeviceUUID(String a2dpDeviceUUID) {
            this.a2dpDeviceUUID = a2dpDeviceUUID;
        }
        public String getDeviceIp() {
            return deviceIp;
        }

        public void setDeviceIp(String deviceIp) {
            this.deviceIp = deviceIp;
        }

        public void sendOTAFwProgressValue(String otaStaus) {
            this.otaStatus = otaStaus;
        }

        public String getFwOTAProgressValue() {
            return otaStatus;
        }

        public void sendBLSProgressValeu(String blsStatus) {
            this.blsStatus = blsStatus;
        }

        public String getBLSProgressValue() {
            return blsStatus;
        }
    }


}