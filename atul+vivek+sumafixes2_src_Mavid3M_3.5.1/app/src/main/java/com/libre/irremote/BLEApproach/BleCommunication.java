package com.libre.irremote.BLEApproach;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.libre.irremote.MavidApplication;
import com.libre.libresdk.Util.LibreLogger;

import static com.libre.irremote.BLEApproach.BLEGattAttributes.MAVID_BLE_CHARACTERISTICS;
import static com.libre.irremote.BLEApproach.BluetoothLeService.mBluetoothGatt;

public class BleCommunication {

    static BleWriteInterface bleWriteInterface;

    public BleCommunication(BleWriteInterface bleWriteInterface) {

        this.bleWriteInterface = bleWriteInterface;

    }
//"ReadDeviceStatus

    public static void writeInteractor() {
        try {
            if (mBluetoothGatt.getServices() != null) {
                LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has some Value");

                try {
                    Log.v("SacTesting", "start sac is getting called");
                    if (mBluetoothGatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE) != null) {
                        MavidApplication.BLEServiceisNullScanREQ = false;
                        LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has some value" + mBluetoothGatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE));
                        BluetoothGattCharacteristic interactor = mBluetoothGatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE).getCharacteristic(MAVID_BLE_CHARACTERISTICS);
                        if (interactor != null) {
                            interactor.setValue("ScanReq");

                            interactor.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

                            mBluetoothGatt.connect();
                            mBluetoothGatt.writeCharacteristic(interactor);
                            bleWriteInterface.onWriteSuccess();
                            BluetoothDevice device = mBluetoothGatt.getDevice();
                            int bondState = device.getBondState();
                            Log.d("SCANREQ", "SUMA IN SCANREQ write interactor Success" + bondState);
                            Log.v("SacTesting", "start sac command sent" + bondState);
                        }
                    } else {
                        Log.v("SacTesting", "start sac command sent:----else block because it is null");
                        LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has null value" + mBluetoothGatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE));
                        MavidApplication.BLEServiceisNullScanREQ = true;
                        // startActivity(njew Intent(BaseActivity.this, BLEScanActivity.class));
                    }

                } catch (NullPointerException e) {
                    Log.v("SacTesting", "start sac command sent:----Exception Block" + e);
                    e.printStackTrace();
                }
            } else {
                Log.v("SacTesting", "start sac command sent:----BLE SERVICE VALUE has NULL value");
                LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has NULL value");
            }
        } catch (NullPointerException e) {
            Log.v("SacTesting", "start sac command sent:----BLE SERVICE VALUE has NULL value 222" + e);

            LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has NULL POINTER EXC");
            MavidApplication.BLEServiceisNullScanREQ = true;

        }

    }

    public static void writeInteractorDeviceStatus() {
        BluetoothGattCharacteristic interactor = mBluetoothGatt
                .getService(BLEGattAttributes.MAVID_BLE_SERVICE)
                .getCharacteristic(MAVID_BLE_CHARACTERISTICS);
        interactor.setValue("ReadDeviceStatus");
        //interactor.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        if ((interactor.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0
                && (interactor.getProperties() &
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
            Log.d("Bluetooth", "write interactor if");
        } else {
            Log.d("Bluetooth", "write interactor else");
            //interactor.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            BluetoothLeService.mBluetoothGatt.connect();

            mBluetoothGatt.writeCharacteristic(interactor);
            bleWriteInterface.onWriteSuccess();
            Log.v("SacTesting", "start sac command sent:----Read Device Status:::");
        }

    }

    public static void writeInteractorStopSac() {
        try {
            Log.v("SacTesting", "start sac command sent:----Calling Stop SAC...");
            BluetoothGattCharacteristic interactor = mBluetoothGatt
                    .getService(BLEGattAttributes.MAVID_BLE_SERVICE)
                    .getCharacteristic(MAVID_BLE_CHARACTERISTICS);
            interactor.setValue("StopSAC");
            interactor.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(interactor);
            bleWriteInterface.onWriteSuccess();
            LibreLogger.d("BleCommunication", "suma in wifi connecting event in stopsac successs");
            Log.v("SacTesting", "start sac command sent:----Calling Stop SAC... Success...");
        } catch (NullPointerException e) {
            Log.v("SacTesting", "start sac command sent:----Calling Stop SAC... Exception..." + e);
            e.printStackTrace();
        }
    }

    public static void writeInteractor(byte[] b) {

        BluetoothGattCharacteristic interactor = mBluetoothGatt
                .getService(BLEGattAttributes.MAVID_BLE_SERVICE)
                .getCharacteristic(MAVID_BLE_CHARACTERISTICS);

        interactor.setValue(b);

        mBluetoothGatt.writeCharacteristic(interactor);
        bleWriteInterface.onWriteSuccess();
        LibreLogger.d("BleCommunication", "suma in bleCommunication writting Sac");
    }

    public static void writeInteractorWifiConnected() {
        BluetoothGattCharacteristic interactor = mBluetoothGatt
                .getService(BLEGattAttributes.MAVID_BLE_SERVICE)
                .getCharacteristic(MAVID_BLE_CHARACTERISTICS);
        interactor.setValue("Wifi_Conenction");
        interactor.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

        mBluetoothGatt.writeCharacteristic(interactor);
        if (interactor == null) {
            bleWriteInterface.onWriteFailure();
        } else {
            bleWriteInterface.onWriteSuccess();
        }
    }

    //ReadWIFIStatus
    public static void writeInteractorReadWifiStatus() {
        BluetoothGattCharacteristic interactor = mBluetoothGatt
                .getService(BLEGattAttributes.MAVID_BLE_SERVICE)
                .getCharacteristic(MAVID_BLE_CHARACTERISTICS);
        interactor.setValue("ReadWIFIStatus");
        interactor.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

        mBluetoothGatt.writeCharacteristic(interactor);
        bleWriteInterface.onWriteSuccess();
        MavidApplication.readWifiStatus = true;
        LibreLogger.d("BleCommunication", "suma in wifi connecting event in read wifi status success");
        Log.v("SacTesting", "start sac command sent:----... writeInteractorReadWifiStatus...");
    }
}
