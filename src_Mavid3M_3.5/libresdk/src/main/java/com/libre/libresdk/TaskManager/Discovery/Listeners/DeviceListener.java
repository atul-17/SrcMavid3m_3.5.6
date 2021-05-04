package com.libre.libresdk.TaskManager.Discovery.Listeners;

import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;

/**
 * Created by bhargav on 2/2/18.
 */

public interface DeviceListener {
    public void newDeviceFound(DeviceInfo deviceInfo);
    public void deviceGotRemoved(DeviceInfo deviceInfo);
    public void deviceDataReceived(MessageInfo messageInfo);
    public void failures(Exception e);
    public void checkFirmwareInfo(DeviceInfo deviceInfo);

}
