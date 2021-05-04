package com.libre.irremote.utility.FirmwareClasses;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by suma on 2/8/18.
 */
public class FirmwareUpdateHashmap {

    public static LinkedHashMap<String, UpdatedMavidNodes> updateHashMap = new LinkedHashMap<>();
    private String ipAddress;
    private UpdatedMavidNodes updateNode;
    private ArrayList<UpdatedMavidNodes> updateNodeList;
    static FirmwareUpdateHashmap updateHashMapInstance;
    private FirmwareUpdateHashmap() {

    }

    public static FirmwareUpdateHashmap getInstance(){
        if (updateHashMapInstance==null){
            updateHashMapInstance= new FirmwareUpdateHashmap();
        }
        return updateHashMapInstance;
    }

    public void CreateUpdatedHashMap(String ipAddress, UpdatedMavidNodes updateNode) {
        updateHashMap.put(ipAddress, updateNode);

    }

    public boolean isDeviceAvailableForUpdate(){
        for (String key : updateHashMap.keySet()){
            if (updateHashMap.get(key).isFirmwareUpdateNeeded()){
                return true;
            }
        }
        return false;
    }
    public boolean checkIfNodeAlreadyPresentinList(String mNodeIp) {
        return updateHashMap.containsKey(mNodeIp);
    }

    public void updateFWStatusForAlreadyExistingNode(String mNodeIp) {

//        updateHashMap.get(mNodeIp).setFirmwareStatus(2);


    }

    public Integer hashMapSize() {
        return updateHashMap.size();
    }

    public UpdatedMavidNodes getUpdateNode(String ipAddress){
        return updateHashMap.get(ipAddress);
    }

    public UpdatedMavidNodes getEachUpdatedNode(Integer pos) {

        updateNodeList = new ArrayList<UpdatedMavidNodes>();
        updateNodeList.clear();
        for (HashMap.Entry entry : updateHashMap.entrySet()) {
            Log.d("FirmwareUpdate", "hashmap key: " + entry.getKey() + " hashmap value: " + entry.getValue());
        }

        for (UpdatedMavidNodes listData : updateHashMap.values()) {
            updateNodeList.add(listData);
        }

        return updateNodeList.get(pos);
    }

}
