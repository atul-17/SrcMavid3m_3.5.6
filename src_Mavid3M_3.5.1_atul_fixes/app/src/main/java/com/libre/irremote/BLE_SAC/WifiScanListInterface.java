package com.libre.irremote.BLE_SAC;

import com.libre.irremote.models.ScanListData;

import java.util.ArrayList;

public interface WifiScanListInterface {
    void onScanListRecievedFromWifiManager(ArrayList<ScanListData> scanListData);
}
