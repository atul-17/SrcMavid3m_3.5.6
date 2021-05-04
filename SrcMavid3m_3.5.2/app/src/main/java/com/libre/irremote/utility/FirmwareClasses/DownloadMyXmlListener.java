package com.libre.irremote.utility.FirmwareClasses;
/**
 * Created by suma on 2/8/18.
 */
public interface DownloadMyXmlListener {
    public void success(String fw_version, String bsl_version);
    public void failure(Exception e);
}
