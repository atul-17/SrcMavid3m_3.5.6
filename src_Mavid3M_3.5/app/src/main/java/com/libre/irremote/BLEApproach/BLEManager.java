package com.libre.irremote.BLEApproach;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;


public class BLEManager {

    private static BluetoothAdapter mBluetoothAdapter;
    private static BLEManager bleManager = null;
    private static Context context;


    private BLEManager() {

    }

    public static BLEManager getInstance(Context ct) {
        if (bleManager == null) {
            bleManager = new BLEManager();
            context = ct;
            BluetoothManager bluetoothManager =
                    (BluetoothManager) ct.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bleManager;
    }

    public boolean isBluetoothEnabled() {
        if (mBluetoothAdapter.isEnabled()) {
            return true;
        }
        return false;
    }


    public  boolean isBTAdapterExist(){
        if(mBluetoothAdapter==null){
            return false;
        }
        return true;
    }



}
