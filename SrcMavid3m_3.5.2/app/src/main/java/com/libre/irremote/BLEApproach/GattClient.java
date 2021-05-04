package com.libre.irremote.BLEApproach;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.libre.irremote.MavidApplication;
import com.libre.libresdk.Util.LibreLogger;

import static android.content.Context.BLUETOOTH_SERVICE;
import static com.libre.irremote.BLEApproach.BLEGattAttributes.MAVID_BLE_CHARACTERISTICS;
import static com.libre.irremote.BLEApproach.BLEGattAttributes.MAVID_BLE_SERVICE;

public class GattClient {

    private static final String TAG = GattClient.class.getSimpleName();

    public interface OnCounterReadListener {
        void onCounterRead(byte[] value);

        void onConnected(boolean success);
    }

    private Context mContext;
    private OnCounterReadListener mListener;
    private String mDeviceAddress;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange( BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT client. Attempting to start service discovery");
//                new Handler(Looper.getMainLooper()) {
//                    gatt.discoverServices();
//                };
                //suma changes
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        boolean ans = mBluetoothGatt.discoverServices();
                        Log.d(TAG, "Discover Services started: " + ans);
                    }
                });
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT client in GATTCLIENT");
                mListener.onConnected(false);
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean connected = false;

                BluetoothGattService service = gatt.getService(MAVID_BLE_SERVICE);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(MAVID_BLE_CHARACTERISTICS);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);

                       /* BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DESCRIPTOR_CONFIG);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            connected = gatt.writeDescriptor(descriptor);
                        }*/
                    }
                }
                mListener.onConnected(connected);
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            readCounterCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            readCounterCharacteristic(characteristic);
            String str = "Hi";
            byte[] byteArr = str.getBytes();
          // WriteValueInternal(byteArr,characteristic,gatt );
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
//            if (status == GattStatus.Success) {
                Log.d(TAG,"Writing was successfullOne" + status);
//            } else {
//                var errorCode = status;
//                //Process error...
////            }
        }



        private void readCounterCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (MAVID_BLE_CHARACTERISTICS.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                int value = Ints.fromByteArray(data);
                Log.d("Bluetooth","read data length"+value);
                mListener.onCounterRead(data);
            }
        }


        private void WriteValueInternal(byte[] buffer, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
            //Set value that will be written
            characteristic.setValue(buffer);
            //Set writing type
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
           // gatt.writeCharacteristic(characteristic);
        }
    };

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startClient();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopClient();
                    LibreLogger.d(this,"BleCommunication in suma ble state in state off stop client");
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
    };

    public void onCreate(Context context, OnCounterReadListener listener) throws RuntimeException {
        mContext = context;
        mListener = listener;
//        mDeviceAddress = deviceAddress;

        mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!checkBluetoothSupport(mBluetoothAdapter)) {
            throw new RuntimeException("GATT client requires Bluetooth support");
        }

        // Register for system Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothReceiver, filter);
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is currently disabled... enabling");
            mBluetoothAdapter.enable();
        } else {
            Log.d(TAG, "Bluetooth enabled... starting client");
            startClient();
        }
    }

    public void onDestroy() {
        mListener = null;

        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            stopClient();
            LibreLogger.d(this,"BleCommunication in suma ble state in state off stop client");

        }

        mContext.unregisterReceiver(mBluetoothReceiver);
    }

    public void writeInteractor(byte[] b) {
        BluetoothGattCharacteristic interactor = mBluetoothGatt
                .getService(MAVID_BLE_SERVICE)
                .getCharacteristic(MAVID_BLE_CHARACTERISTICS);
        interactor.setValue(b);
        mBluetoothGatt.writeCharacteristic(interactor);
    }

    public void writeInteractor(String b) {
        BluetoothGattCharacteristic interactor = mBluetoothGatt
                .getService(MAVID_BLE_SERVICE)
                .getCharacteristic(MAVID_BLE_CHARACTERISTICS);
        interactor.setValue(b);
        mBluetoothGatt.writeCharacteristic(interactor);
    }

    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    private void startClient() {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(MavidApplication.mDeviceAddress);
        mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, mGattCallback);

        if (mBluetoothGatt == null) {
            Log.d(TAG, "Unable to create GATT client");
            return;
        }
    }

    private void stopClient() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter = null;
        }
    }
}
