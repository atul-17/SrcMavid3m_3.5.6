package com.libre.irremote.utility;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class BusLeScanBluetoothDevicesEventProgress  {

    public static class BusScanBluetoothDevices implements Serializable{
        private BluetoothDevice bluetoothDevice;

        public BluetoothDevice getBluetoothDevice() {
            return bluetoothDevice;
        }

        public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
        }
    }
}
