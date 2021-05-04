package com.libre.irremote.utility.FirmwareClasses;

import com.libre.irremote.MavidApplication;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.Util.LibreLogger;

/**
 * Created by suma on 2/8/18.
 */
public class CheckFirmwareInfoClass {
    DeviceInfo deviceInfo;
    String latestFirmwareVersionAvailable;
    private XmlParser myXml;
    String oldFirmWareVersion,newFirmWareVersion,oldBslVersion,newBslVersion;
    // String latestFirmwareVersionAvailable1;

    public CheckFirmwareInfoClass(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    public void setLatestFirmwareVersionAvailable(String fw){
        latestFirmwareVersionAvailable = fw;
    }
    private String getLatestFirmwareVersionAvailable(){
        return latestFirmwareVersionAvailable;
    }

    private void splitOldFirmware(String ipadress) {
        try{
        LibreLogger.d(this,"checkfirmwareclass suma getting oldfirmware1"+deviceInfo.getFwVersion()+"device friendly Name\n"+deviceInfo.getFriendlyName());
        if (deviceInfo != null && deviceInfo.getFwVersion() != null) {
            String dutExistingFirmware = deviceInfo.getFwVersion();
            final String[] splitold = dutExistingFirmware.split("\\.");
            LibreLogger.d(this, "checkfirmwareclass suma getting oldfirmware3 \n " + splitold[2] + "friendlyName\n" + deviceInfo.getFriendlyName());
            oldFirmWareVersion = splitold[2];
//            LibreLogger.d(this,"checkfirmwareclass suma getting oldfirmware3 \n "+oldFirmWareVersion+"friendlyName\n"+deviceInfo.getFriendlyName());
        }
        }
        catch (Exception e){
            MavidApplication.urlFailure=false;
            LibreLogger.d(this, "exception while parsing the XML 5 check suma" + e.toString());
            MavidApplication.fwupdatecheckPrivateBuild=true;
            LibreLogger.d(this,"checkfirmwareclass suma getting oldfirmware exception\n"+e);
            e.printStackTrace();
        }
    }

    private void splitOldBslVersion(String ipadress){
        LibreLogger.d(this,"checkfirmwareclass suma getting oldbslfirmware"+deviceInfo.getBslInfoBeforeSplit());
        if (deviceInfo != null && deviceInfo.getBslInfoBeforeSplit() != null) {
            String mavidBslINfoBeforeSplit = deviceInfo.getBslInfoBeforeSplit();
             LibreLogger.d(this,"checkfirmwareclass suma getting oldbslfirmware2"+mavidBslINfoBeforeSplit);
            final String[] splitold =
                    mavidBslINfoBeforeSplit.split("\\.");
            oldBslVersion= splitold[2];
            deviceInfo.setBslOldValue(oldBslVersion);

        }
    }

    private void splitNewBslFirmwareLogic(String ipadress) {
        LibreLogger.d(this,"checkfirmwareclass suma getting newbslfirmware1"+deviceInfo.getBslInfoAfterSplit()+"friendlyName\n"+deviceInfo.getFriendlyName());
        if (deviceInfo != null && deviceInfo.getBslInfoAfterSplit() != null) {
            String mavidNewBslVersion = deviceInfo.getBslInfoAfterSplit();
            // LibreLogger.d(this,"checkfirmwareclass suma getting newbslfirmware2"+mavidNewBslVersion);
            final String[] splitnewbsl = mavidNewBslVersion.split("\\.");
            newBslVersion= splitnewbsl[2];
            deviceInfo.setBslNewValue(newBslVersion);
            // LibreLogger.d(this,"checkfirmwareclass suma getting newbslfirmware3\n "+newBslVersion);
        }
    }
    private void splitNewFirmwareLogic(String ipadress) {
        LibreLogger.d(this,"checkfirmwareclass suma getting newfirmware1"+deviceInfo.getUpdatedFwVersion());
        if (deviceInfo != null && deviceInfo.getUpdatedFwVersion() != null) {
            String dutExistingnewFirmware = deviceInfo.getUpdatedFwVersion();
            //LibreLogger.d(this,"checkfirmwareclass suma getting newfirmware2"+dutExistingnewFirmware);
            final String[] splitold = dutExistingnewFirmware.split("\\.");
            newFirmWareVersion= splitold[2];
            // LibreLogger.d(this,"checkfirmwareclass suma getting newfirmware3\n "+newFirmWareVersion);
        }
    }

    public boolean checkFirmwareUpdateButtonEnableorDisable(String ipAddress){
        splitOldFirmware(ipAddress);
        LibreLogger.d(this, "suma in response device list suma1 \n" +  "device ip" + deviceInfo.getFriendlyName()+"device fw\n"+deviceInfo.getFwVersion());

        splitNewFirmwareLogic(ipAddress);
        splitOldBslVersion(ipAddress);
        splitNewBslFirmwareLogic(ipAddress);

        return oldBslVersion != null && (oldBslVersion.equals("PRIVATE_BUILD"))
                ||Integer.parseInt(oldBslVersion) >=Integer.parseInt(newBslVersion)&&Integer.parseInt(oldFirmWareVersion) >=Integer.parseInt(newFirmWareVersion);

    }
    public boolean checkIfFirmwareUpdateNeeded(final String ipadress) {
        splitOldFirmware(ipadress);
        LibreLogger.d(this, "suma in response device list suma2 \n" +  "device ip" + deviceInfo.getFriendlyName()+"device fw\n"+deviceInfo.getFwVersion());
        splitNewFirmwareLogic(ipadress);
        splitOldBslVersion(ipadress);
        splitNewBslFirmwareLogic(ipadress);
        LibreLogger.d(this, "splitted old fw version final check old \n " + oldFirmWareVersion);
        LibreLogger.d(this, "splitted old fw version final check new\n " + newFirmWareVersion);

        LibreLogger.d(this, "splitted bsl version final check old checkfirmwareinfo class \n " + oldBslVersion);

        LibreLogger.d(this, "splitted bsl version final check new checkfirmwareinfo class\n " + newBslVersion);
        return Integer.parseInt(oldBslVersion) <Integer.parseInt(newBslVersion)/*||Integer.parseInt(oldFirmWareVersion) <Integer.parseInt(newFirmWareVersion)*/;

    }

}
