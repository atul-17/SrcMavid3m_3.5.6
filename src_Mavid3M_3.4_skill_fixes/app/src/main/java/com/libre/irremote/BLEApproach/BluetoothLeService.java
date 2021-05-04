package com.libre.irremote.BLEApproach;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import android.os.Looper;
import android.util.Log;


import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.libresdk.Util.LibreLogger;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.libre.irremote.MavidApplication.betweenDisconnectedCount;
import static com.libre.irremote.MavidApplication.disconnectedCount;


public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BleConnectionStatus connectionStatus;
    private BleReadInterface bleReadInterface;
    boolean gatt_status_133 = false;
    public BluetoothManager mBluetoothManager;
    public static BluetoothAdapter mBluetoothAdapter;
    public String mBluetoothDeviceAddress;
    public static BluetoothGatt mBluetoothGatt;
    public int mConnectionState = STATE_DISCONNECTED;
    int finalPayloadLength;
    BluetoothLeScanner mBleScanner;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    StringBuilder dataBuilder = new StringBuilder();


    private HashMap<String, BluetoothGattCharacteristic> bluetoothDeviceHashMapGattChar = new HashMap<>();

    private ArrayList<BleConnectionStatus> mBleConnectionStatusList = new ArrayList<BleConnectionStatus>();


    public void addBLEServiceToApplicationInterfaceListener(BleConnectionStatus mBleConnectionStatus) {
        Log.d(TAG, "Add Ble Service To app Interface Listener.");
        if (!mBleConnectionStatusList.contains(mBleConnectionStatus))
            mBleConnectionStatusList.add(mBleConnectionStatus);
    }

    public void removeBLEServiceToApplicationInterfaceListener(BleConnectionStatus mBleConnectionStatus) {
        Log.d(TAG, "Remove Ble Service To app Interface Listener.");
        if (mBleConnectionStatusList.contains(mBleConnectionStatus))
            mBleConnectionStatusList.remove(mBleConnectionStatus);

        // fireOnBLEDisConnectionSuccess(mBleConnectionStatus.hashCode());


    }

    public void fireOnBLEConnectionSuccess(BluetoothGattCharacteristic status) {
        Log.d(TAG, "fire On BLE Conenction Success");
//        boolean isConnected;
//        if (isConnected) return;
//        if (isBluetoothAvailable && savedDevice != null) {
//            bluetoothService?.connect(savedDevice!!)
//            isConnected = true;
//        }
        for (BleConnectionStatus mListener : mBleConnectionStatusList) {
            mListener.onConnectionSuccess(status);
        }

    }

    public void fireOnBLEDisConnectionSuccess(int status) {
        //Log.d(TAG, "fire On BLE Disconenction Success");
        for (BleConnectionStatus mListener : mBleConnectionStatusList) {
            mListener.onDisconnectionSuccess(status);

            LibreLogger.d(this,"fire On BLE Conenction Success DISCONNECTED"+mBluetoothDeviceAddress);
        }

    }

    public void removelistener(BleConnectionStatus bleConnectionStatus) {
        removeBLEServiceToApplicationInterfaceListener(bleConnectionStatus);
    }


    // connection change and services discovered.
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, int newState) {

            final String intentAction;
            LibreLogger.d(this, "Connected to GATT client. Attempting for connection state change\n" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT client. Attempting to start service discovery");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestMtu(180);
                }
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                // broadcastUpdate(intentAction);
                //suma commenting to get ble timeout issue   fireOnBLEConnectionSuccess(bluetoothDeviceHashMapGattChar.get(mBluetoothDeviceAddress));

                BluetoothGattService service = gatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLEGattAttributes.MAVID_BLE_CHARACTERISTICS);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        Log.d("Bluetooth", "characteristics service\n" + characteristic);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEGattAttributes.MAVID_BLE_CHARACTERISTICS);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        }
                        putGattCharacteristic(characteristic, mBluetoothDeviceAddress);
                        if(mBluetoothGatt.getServices()!=null) {
                            fireOnBLEConnectionSuccess(characteristic);
                            LibreLogger.d(this,"suma in fire on BLE Connection Sucess ONE IF");

                        }
                        else{
                            LibreLogger.d(this,"suma in fire on BLE Connection Sucess ONE ELSE");

                        }
                       // mBluetoothGatt.getServices()
                    }
                }
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
//                Log.i(TAG, "Attempting to start service discovery:" +
                if(mBluetoothGatt!=null)
                    mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                if (status == 133) {
                    gatt_status_133 = true;

                    if (!MavidApplication.noReconnectionRuleToApply) {
                        betweenDisconnectedCount++;
                        if (betweenDisconnectedCount <= 4) {
                            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mBluetoothGatt = device.connectGatt(getApplicationContext(), true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                                LibreLogger.d(this, "suma in 133 disconnection inside if");
                            }
                            LibreLogger.d(this, "suma in 133 disconnection outside");
                            // handler.postDelayed(this, 1000);
                        } else {
                            LibreLogger.d(this, "suma in 133 disconnection outside else reached 5 attempts");
                            //   Toast.makeText(getApplicationContext(), "BLE DEVICE CONNECT FAILURE(133) ", Toast.LENGTH_SHORT).show();
                            broadcastUpdate(intentAction);
                            fireOnBLEDisConnectionSuccess(status);
                        }
                    }

                } else {
                    mConnectionState = STATE_DISCONNECTED;
                    LibreLogger.d(this, "Suma in getting error state turning disconnected");
                    Log.i(TAG, "Disconnected from GATT server BLE Services.");

                    disconnectedCount++;
                    if (disconnectedCount < 5) {
                        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mBluetoothGatt = device.connectGatt(getApplicationContext(), true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                            LibreLogger.d(this, "suma in disconnection inside if");

                        }
                        broadcastUpdate(intentAction);
                        fireOnBLEDisConnectionSuccess(status);
                        LibreLogger.d(this, "suma in disconnection outside");

                        // handler.postDelayed(this, 1000);
                    } else {
                        LibreLogger.d(this, "suma in disconnection outside else reached 5 attempts");
                        broadcastUpdate(intentAction);
                        fireOnBLEDisConnectionSuccess(status);
                    }

                }

            }

        }

        public void hexByteToString(byte[] data) {
            String hex = Arrays.toString(data);
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < hex.length(); i += 2) {
                String str = hex.substring(i, i + 2);
                output.append((char) Integer.parseInt(str, 16));
            }
            System.out.println(output.toString().trim());
            LibreLogger.d(this, "BleCommunication in suma ble state turning get SSID String" + output.toString().trim());
        }

        public String byteToHexString(byte[] data) {
            StringBuilder res = new StringBuilder(data.length * 2);
            int lower4 = 0x0F; //mask used to get lowest 4 bits of a byte
            for (int i = 0; i < data.length; i++) {
                int higher = (data[i] >> 4);
                int lower = (data[i] & lower4);
                if (higher < 10) res.append((char) ('0' + higher));
                else res.append((char) ('A' + higher - 10));
                if (lower < 10) res.append((char) ('0' + lower));
                else res.append((char) ('A' + lower - 10));
                res.append(' '); //remove this if you don't want spaces between bytes
            }
            return res.toString();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean connected = false;

                BluetoothGattService service = gatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLEGattAttributes.MAVID_BLE_CHARACTERISTICS);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    gatt.setCharacteristicNotification(characteristic, true);
                    Log.d("Bluetooth", "characteristics service\n" + characteristic);

                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEGattAttributes.MAVID_BLE_CHARACTERISTICS);
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        connected = gatt.writeDescriptor(descriptor);
                        Log.d("Bluetooth", "characteristics service connected device\n" + connected);

                    }
//                        connectionStatus.onConnectionSuccess(true);
                    putGattCharacteristic(characteristic, mBluetoothDeviceAddress);
                    if(mBluetoothGatt.getServices()!=null) {
                        fireOnBLEConnectionSuccess(characteristic);
                        LibreLogger.d(this,"suma in fire on BLE Connection Sucess TWO IF");

                    }
                    else{
                        LibreLogger.d(this,"suma in fire on BLE Connection Sucess TWO ELSE");

                    }
                  //  fireOnBLEConnectionSuccess(characteristic);

                  //  LibreLogger.d(this,"suma in fire on BLE Connection Sucess TWO");

                    LibreLogger.d(this, " FireONBLEConnection Sucess Connectedfrom GATT server on connection success set value");

                }
//                mListener.onConnected(connected);
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        private void putGattCharacteristic(BluetoothGattCharacteristic gattCharacteristic, String btMacAddress) {
            Log.d(TAG, "putGattCharacteristic " + btMacAddress + " Id " + gattCharacteristic.hashCode());
            if (!bluetoothDeviceHashMapGattChar.containsKey(btMacAddress)) {
                Log.d(TAG, "putGattCharacteristic " + btMacAddress + " Id " + "is it removing");
                bluetoothDeviceHashMapGattChar.remove(btMacAddress);
            }
            bluetoothDeviceHashMapGattChar.put(btMacAddress, gattCharacteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bleeeeee", "call onMtuChanged");
                gatt.discoverServices();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCounterCharacteristic(characteristic);
                LibreLogger.d(this, "on read characteristics step1");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            readCounterCharacteristic(characteristic);
            String str = "Hi";
            LibreLogger.d(this, "on read characteristics step2 characteristics\n" + characteristic.toString());
            LibreLogger.d(this, "Writing was successfullTwo read value" + "characteristics\n" + characteristic.toString());

            byte[] byteArr = str.getBytes();
//            WriteValueInternal(byteArr,characteristic,gatt );
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            /*if (DESCRIPTOR_CONFIG.equals(descriptor.getUuid())) {
                BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_COUNTER_UUID);
                gatt.readCharacteristic(characteristic);
            }*/
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);


            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean connected = false;

                BluetoothGattService service = gatt.getService(BLEGattAttributes.MAVID_BLE_SERVICE);
                if (service != null) {
                    characteristic = service.getCharacteristic(BLEGattAttributes.MAVID_BLE_CHARACTERISTICS);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEGattAttributes.MAVID_BLE_CHARACTERISTICS);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

                            connected = gatt.writeDescriptor(descriptor);
                        }
                        // connectionStatus.onConnectionSuccess(true);

                    }
                }
//                mListener.onConnected(connected);
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void readCounterCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (BLEGattAttributes.MAVID_BLE_CHARACTERISTICS.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                //hexByteToString(data);
                Log.d("Bluetooth", "data  length:\n " + data.length + "real data value" + data.getClass().getName() + "characteristics\n" + characteristic.getUuid());

                try {
                    bleReadInterface.onReadSuccess(data);

                    int response2 = getDataLength(data);
                    for (int i = 0; i < data.length; i++) {

                    }
                    LibreLogger.d(this, "suma in ble configure activity get the value\n" + response2);
                    if (response2 == 14) {
                        MavidApplication.bleSACTimeout = true;

                    } else {
                        MavidApplication.bleSACTimeout = false;
                    }

                    BLEEvntBus evntBus = new BLEEvntBus(data);
                    baos.write(data);
                    dataBuilder.append(new String(data));
                    EventBus.getDefault().post(evntBus);


                    int finalPayloadLength = getDataLength(data);

                    if (finalPayloadLength == 10) {
                        Log.v("EventBusService", "Ecevt Service:   " + baos.toString());
                        baos.reset();
                        baos.flush();

                        Log.v("EventBusService", "Data is ::::   " + dataBuilder.toString());
                        dataBuilder.setLength(0);
                    }

                    int response = getDataLength(data);

                    bytesToHex(data);
                    String hex = "75546f7272656e745c436f6d706c657465645c6e667375635f6f73745f62795f6d757374616e675c50656e64756c756d2d392c303030204d696c65732e6d7033006d7033006d7033004472756d202620426173730050656e64756c756d00496e2053696c69636f00496e2053696c69636f2a3b2a0050656e64756c756d0050656e64756c756d496e2053696c69636f303038004472756d2026204261737350656e64756c756d496e2053696c69636f30303800392c303030204d696c6573203c4d757374616e673e50656e64756c756d496e2053696c69636f3030380050656e64756c756d50656e64756c756d496e2053696c69636f303038004d50330000";
                    String hex1 = bytesToHex(data);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < hex1.length(); i += 2) {
                        String str = hex1.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str, 16));
                    }
                    System.out.println(output);
                    LibreLogger.d(this, "Swetha BT and SUMA IN SSID in read response dataValue READ DATA\n" + bytesToHex(data));
                    LibreLogger.d(this, "Swetha BT and SUMA IN SSID in read response dataValue conversion\n" + output);

                    String finalSsidList = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                               finalSsidList = new String(baos.toByteArray(), StandardCharsets.UTF_8);
//                               String input = "test string (67hghjgigliugliugliug)";
//                               input = finalSsidList.substring(finalSsidList.indexOf(" {\"Items\"")+1, finalSsidList.lastIndexOf("]}"));
//                               System.out.println(input);
//                               LibreLogger.d(this, "Swetha BT and SUMA IN SSID in READ DATA crosscheck filter\n"+input);
//                               LibreLogger.d(this, "Swetha BT and SUMA IN SSID in READ DATA while read filter\n"+finalSsidList.lastIndexOf("{\"Items\""));
//                               LibreLogger.d(this, "Swetha BT and SUMA IN SSID in READ DATA while read filter final\n"+finalSsidList.lastIndexOf("{\"Items\""));

                        if (getDataLength(data) != 10 /*&& value[2] != 1*/) {
                            MavidApplication.baos = new ByteArrayOutputStream();
                            MavidApplication.baos.write(data);
                            String sumaSSID = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                sumaSSID = new String(MavidApplication.baos.toByteArray(), StandardCharsets.UTF_8);
                                LibreLogger.d(this, "Swetha BT and SUMA IN SSID in READ DATA while read filter inside check\n" + sumaSSID);

                            }
//

                        }
//                               // strips off all non-ASCII characters
//                               finalSsidList = finalSsidList.replaceAll("[^\\x00-\\x7F]", "");
//                               LibreLogger.d(this, "SUMA IN SSID in read  response data READING DATA NONASCII\n"+finalSsidList.trim());
//
//                               // erases all the ASCII control characters
//                               finalSsidList = finalSsidList.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
//                               LibreLogger.d(this, "SUMA IN SSID in read  response data READING DATA ASCII CONTROL CHARSXC\n"+finalSsidList.trim());
//
//                               // removes non-printable characters from Unicode
//                               finalSsidList = finalSsidList.replaceAll("\\p{C}", "");
//                               LibreLogger.d(this, "SUMA IN SSID in read  response data READING DATA UNICODE CHARScj\n"+finalSsidList.trim());

                        //  LibreLogger.d(this, "SUMA IN SSID in read  response reading VALUE BYTEARRAY\n"+ Arrays.toString(baos.toByteArray()));

                        //  LibreLogger.d(this, "SUMA IN SSID in read  response reading VALUE\n"+finalSsidList);

                    }
                    //getIntentParams(finalSsidList);
                    //suma
                    //   d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next response scanlist getdatalengthvalue! 10" + response);
                    //  }
                    //  }


//                   String finalSsidList = null,finalSsidList1 = null;
//                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                       finalSsidList = new String(data, StandardCharsets.ISO_8859_1);
//                   }
                    // if(finalSsidList.equals(""))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                       finalSsidList = new String(data, StandardCharsets.ISO_8859_1);
                        //  finalSsidList1 = new String(finalSsidList.getBytes(), StandardCharsets.UTF_8);
                        Log.d("READ SCAN BLE DATA", "SUMA IN SSID in read response junk remove ISO\n" + finalSsidList);
                        //   Log.d("READ SCAN BLE DATA", "SUMA IN SSID in read response junk remove UTF8\n" + finalSsidList1);

//                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                           String encodeBytes = Base64.getEncoder().encodeToString(finalSsidList.getBytes());
//                       }
                        if (getDataLength(data) != 10 /*&& value[2] != 1*/) {
                            ByteArrayOutputStream baos_SUMA = new ByteArrayOutputStream();
                            baos_SUMA.write(data);
                            // getSSIDList();
                            String finalSsidListSUMA = null;
                            finalSsidListSUMA = new String(baos_SUMA.toByteArray(), StandardCharsets.UTF_8);
                            LibreLogger.d(this, "suma in ssid read response data value" + finalSsidListSUMA);
                            //suma
                            //   d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next response scanlist getdatalengthvalue! 10" + response);
                        }


                        // "When I do a getbytes(encoding) and "

//                       System.Text.Encoding iso_8859_1 =finalSsidList.Encoding.GetEncoding("iso-8859-1");
//                       System.Text.Encoding utf_8 = System.Text.Encoding.UTF8;
//
//                       // Unicode string.
//                       string s_unicode = "abcéabc";
//
//                       // Convert to ISO-8859-1 bytes.
//                       byte[] isoBytes = iso_8859_1.GetBytes(s_unicode);
//
//                       // Convert to UTF-8.
//                       byte[] utf8Bytes = System.Text.Encoding.Convert(iso_8859_1, utf_8, isoBytes);


                        // String finalSUMAString= new String(finalSsidList("ISO-8859-1"), "UTF-8");

                        //  finalConnectedArray = new String(value, "UTF-8");
//                       finalConnectedIndex = finalSsidList.indexOf("{\"Items\"");
//                       finalConnectedString = finalSsidList.substring(finalConnectedIndex);
//
//                       finalConnectedIndex1 = finalSsidList.indexOf("]}");
//                       finalConnectedString1 = finalSsidList.substring(finalConnectedIndex1);
//                       if(finalSsidList.startsWith("{\"Items\"") || finalSsidList.endsWith("]}")) {
//                           EventBus.getDefault().post(new BLEEvntBus(data));
//                           Log.d("READ SCAN BLE DATA", "SUMA IN SSID in read response check substring last ONE\n"+finalSsidList);
//                       }
//                      if(finalSsidList.contains(Character.UnicodeBlock.SPECIALS.toString())){
//                          Log.d("READ SCAN BLE DATA", "SUMA IN SSID in read response check substring last ONE\n"+finalSsidList.contains(Character.UnicodeBlock.SPECIALS.toString()));
//                      }
                        // Log.d("READ SCAN BLE DATA", "SUMA IN SSID in read response check substring last ONE\n" +finalConnectedString1+"first half\n" +finalConnectedString);

//                       sumaString = finalSsidList.replaceAll("��\u0001\u0001;", "");
//                       sumaString1 = finalSsidList.replaceAll("��", "");
//                       sumaString3 = finalSsidList.replaceAll("\uFFFD", "\"");

                        // Log.d("READ SCAN BLE DATA", "SUMA IN SSID in read response junk remove\n" + sumaString3);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Log.d("Bluetooth", "data  length exception:\n " + data.length + "real data value" + data.getClass().getName());
                }
//                mListener.onCounterRead(value);
            }
        }

    };

    public int getDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1 << 8 | b2 & 0xFF);

        LibreLogger.d("Bluetooth", "Data length is returned as s" + s + "buffer 4\n" + buf[4] + "buffer 3" + buf[3]);
        return s;
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public int getDataLengthSuma(byte[] buf) {
        short s = 0;
        int buf0Unsigned = buf[0] & 0xFF;
        int buf1Unsigned = buf[1] & 0xFF;
        int totalBuf0AndBuf1 = buf0Unsigned + buf1Unsigned;
        if (totalBuf0AndBuf1 == 342) {
            byte b1 = buf[3];
            byte b2 = buf[4];
            s = (short) (b1 << 8 | b2 & 0xFF);

            LibreLogger.d(this, "Data length is returned as BLEServices as s" + s + "get buff 0\n" + buf0Unsigned + "buff 1\n" + buf1Unsigned);
            return s;
        }
        return s;

    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind ");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (MavidApplication.isACLDisconnected) {
            LibreLogger.d(this, "SUMA IN SCANREQ BleCommunication in suma ble state disconnected unbind services BLEServices ");
        }
        close();
        return super.onUnbind(intent);
    }

    public final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize(BleConnectionStatus connectionStatus, BleReadInterface bleReadInterface) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        addBLEServiceToApplicationInterfaceListener(connectionStatus);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {

            Log.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        this.connectionStatus = connectionStatus;
        this.bleReadInterface = bleReadInterface;

        return true;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            LibreLogger.d(this, "BleCommunication in suma ble state turning existing conn");

            if (bluetoothDeviceHashMapGattChar.containsKey(address)) {
                Log.d(TAG, "connect");
                if(mBluetoothGatt.getServices()!=null) {
                    fireOnBLEConnectionSuccess(bluetoothDeviceHashMapGattChar.get(address));
                    LibreLogger.d(this,"suma in fire on BLE Connection Sucess THREE IF");

                }
                else{
                    LibreLogger.d(this,"suma in fire on BLE Connection Sucess THREE ELSE");

                }
            //    fireOnBLEConnectionSuccess(bluetoothDeviceHashMapGattChar.get(address));
             //   LibreLogger.d(this,"suma in fire on BLE Connection Sucess THREE");

                return true;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }


        mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        if (gatt_status_133) {
            Log.d(TAG, "Catch issue");


            LibreLogger.d("", "Suma in getting 133 error status inside connect method");
            mBluetoothGatt = device.connectGatt(this, true, mGattCallback, BluetoothDevice.TRANSPORT_LE);

            connect(address);
            gatt_status_133 = false;
        }


        return true;
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        sendBroadcast(intent);
    }

    private void ShowConnectionLost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("");
        builder.setMessage("Connection Lost to the Device");
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(getApplicationContext(), MavidHomeTabsActivity.class));
                // finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        //  mBluetoothGatt.disconnect();
        mBluetoothGatt = null;
        // Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();

    }

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            //System.out.println(b & 0xFF);
            // System.out.println(String.format("%02x", b));
            sb.append(String.format("%02x", b));


        }

        return sb.toString();

    }

    @TargetApi(21)
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (result.getDevice().getAddress().equals(mBluetoothDeviceAddress)) {
                    if (mBleScanner != null) {
                        mBleScanner.stopScan(mLeScanCallback);
                        mBleScanner = null;
                    }
                }
            }
        }
    };

    public BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                }
            };

}