package com.libre.irremote.BLEApproach;

import android.bluetooth.BluetoothGattCharacteristic;

public interface BleConnectionStatus {

    void onConnectionSuccess(BluetoothGattCharacteristic value);

    void onDisconnectionSuccess(int status);
}
