package com.libre.irremote.utility.DB;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import java.util.HashMap;

/**
 * Created by bhargav on 26/2/18.
 */

public class MavidNodes {
    private static MavidNodes mavidNodes;
    HashMap<String,DeviceInfo> deviceInfoHashMap = new HashMap<>();

    private MavidNodes(){

    }
    public static MavidNodes getInstance(){
        if (mavidNodes==null){
            mavidNodes = new MavidNodes();
        }
        return mavidNodes;
    }
    public void addToDeviceInfoMap(String ipAddress,DeviceInfo deviceInfo){
        deviceInfoHashMap.put(ipAddress,deviceInfo);
    }
    public DeviceInfo getDeviceInfoFromDB(String ipAddress){
        return deviceInfoHashMap.get(ipAddress);
    }
    public HashMap<String,DeviceInfo> getAllDeviceListMap(){
        return this.deviceInfoHashMap;
    }
    public void removeDevice(String ipAddress){
        this.deviceInfoHashMap.remove(ipAddress);
    }
    public void clearDeviceInfoMap(){
        this.deviceInfoHashMap.clear();
    }
}
