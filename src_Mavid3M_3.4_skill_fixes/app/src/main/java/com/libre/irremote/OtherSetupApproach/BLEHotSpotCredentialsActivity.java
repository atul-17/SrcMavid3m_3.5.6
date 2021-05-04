package com.libre.irremote.OtherSetupApproach;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.danimahardhika.cafebar.CafeBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.libre.irremote.BLEApproach.BLEEvntBus;
import com.libre.irremote.BLEApproach.BleCommunication;
import com.libre.irremote.BLEApproach.BleWriteInterface;
import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.alexa_signin.AlexaSignInActivity;
import com.libre.irremote.alexa_signin.AlexaThingsToTryDoneActivity;
import com.libre.libresdk.Constants.BundleConstants;
import com.libre.libresdk.Exceptions.WifiException;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.Discovery.Listeners.DeviceListener;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListener;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.models.ModelSaveHotSpotName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import static com.libre.irremote.Constants.Constants.CONFIGURED_DEVICE_FOUND;
import static com.libre.irremote.Constants.Constants.WIFI_CONNECTING_NOTIFICATION;
import static com.libre.irremote.MavidApplication.hotSpotAlexaCalled;


//BLE Hotspot configuration
public class BLEHotSpotCredentialsActivity extends BaseActivity implements View.OnClickListener, BleWriteInterface {

    private EditText hotspotTxt, passphraseTxt;
    private int PERMISSION_ACCESS_COARSE_LOCATION = 0x100;
    private final int CONNECT_TO_SAC = 0x300;
    private Button btnNext;
    int configuratonfailedIndexBle;
    String configuratonfailedStringBle;


    private int security = 8;

    private final int MSEARCH_TIMEOUT_SEARCH = 2000;
    private Handler mTaskHandlerForSendingMSearch = new Handler();
    private Handler mTaskHandlerForSendingMSearch1 = new Handler();
    int count = 0;
    Handler handler;
    AlertDialog.Builder builder;
    public boolean mBackgroundMSearchStoppedDeviceFound = false;
    private String lookFor;
    private String mSACConfiguredIpAddress = "", getAlexaLoginStatus = "";
    private boolean isHotSpotConnected = true;
    ProgressDialog mProgressDialog;
    Handler myHandler;
    private ImageView back;
    static boolean isActivityActive = false;
    NetworkInfo wifiCheck;
    String asset;

    CafeBar cafeBar;
    String ConfigurationFailedString, finalConnecting, finalConnectedArray, finalConnectedString, readWifiStatusString, FilteredfinalIpAddress, filteredAlexaLogin;
    int configuratonfailedIndex, finalConnectingIndex, finalConnectedIndex, readWifiStatusIndexConfiguration, finalIpIndez, alexaLoginIndex;

    private Dialog alert;

    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    String hotSpotNameString;

    List<ModelSaveHotSpotName> modelSaveHotSpotNameList = new ArrayList<>();


    boolean isRefreshedAlexaToken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hot_spot_credentials_activity);
        disableNetworkChangeCallBack();
        disableNetworkOffCallBack();
        initWidgets();
        sendMSearchInIntervalOfTime();
        waitForDiscovery();
        hotSpotAlexaCalled = false;
        isActivityActive = true;
        LibreLogger.d(this, "hotspot credential oncreate");


       // EventBus.getDefault().register(this);





//        ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        wifiCheck= connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//        if (!wifiCheck.isConnected()) {
//            Log.d("WifiConnected","connectivity services not connected");
//            if (!isMobileDataEnabled()) {
//                //Mobile data is enabled and do whatever you want here
//                LibreLogger.d(this, "Mobile data On Do Something");
//                MavidApplication.mobileDataEnabled = true;
//                mobileDataOnOff(BLEHotSpotCredentialsActivity.this);
//            } else {
//                LibreLogger.d(this, "suma mobile data disabled here");
//                MavidApplication.mobileDataEnabled = false;
//                //Mobile data is disabled here
//            }
//        }
//        else{
////            if (!isMobileDataEnabled()) {
////                //Mobile data is enabled and do whatever you want here
////                LibreLogger.d(this, "Mobile data On Do Something");
////                MavidApplication.mobileDataEnabled = true;
////                mobileDataOnOff(BLEHotSpotCredentialsActivity.this);
////            } else {
//                LibreLogger.d(this, "suma mobile data disabled here");
//                MavidApplication.mobileDataEnabled = false;
//                //Mobile data is disabled here
//            //}
//            Log.d("WifiConnected","connectivity services connected");
//
//        }

//        checkWifiON();
//        if (isMobileDataEnabled()) {
//            //Mobile data is enabled and do whatever you want here
//            LibreLogger.d(this, "Mobile data On Do Something");
//            MavidApplication.mobileDataEnabled = true;
//            mobileDataOnOff(BLEHotSpotCredentialsActivity.this);
//        } else {
//            LibreLogger.d(this, "suma mobile data disabled here");
//            MavidApplication.mobileDataEnabled = false;
//            //Mobile data is disabled here
//        }

    }

    //    private final int MSEARCH_TIMEOUT_SEARCH = 2000;
//    private Handler mTaskHandlerForSendingMSearch = new Handler();
//    public boolean mBackgroundMSearchStoppedDeviceFound = false;
//    private Runnable mMyTaskRunnableForMSearch = new Runnable() {
//        @Override
//        public void run() {
//            LibreLogger.d(this, "My task is Sending 1 Minute Once M-Search");
//            /* do what you need to do */
//            if (mBackgroundMSearchStoppedDeviceFound)
//                return;
//
//            try {
//                LibreMavidHelper.advertise();
//            } catch (WrongStepCallException e) {
//                e.printStackTrace();
//            }
//            /* and here comes the "trick" */
//            mTaskHandlerForSendingMSearch.postDelayed(this, MSEARCH_TIMEOUT_SEARCH);
//        }
//    };
//    private void sendMSearchInIntervalOfTime() {
//        mHandler.sendEmptyMessageDelayed(Constants.SEARCHING_FOR_DEVICE,500);
//        mTaskHandlerForSendingMSearch.postDelayed(mMyTaskRunnableForMSearch, MSEARCH_TIMEOUT_SEARCH);
//
//    }


    private boolean isMobileDataEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm1 = (ConnectivityManager) BLEHotSpotCredentialsActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info1 = cm1.getActiveNetworkInfo();
        if (info1 != null) {
            if (info1.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    Class cmClass = Class.forName(cm1.getClass().getName());
                    Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                    method.setAccessible(true);
                    mobileDataEnabled = (Boolean) method.invoke(cm1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return mobileDataEnabled;
    }

    private void initWidgets() {

        hotspotTxt = findViewById(R.id.hotspotTxt);
        passphraseTxt = findViewById(R.id.passphraseTxt);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
//        passwordVisibility = findViewById(R.id.passwordVisibility);
        passphraseTxt.setTransformationMethod(new PasswordTransformationMethod());
//        passwordVisibility.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggleVisibility(passwordVisibility);
//            }
//        });

        back = findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void toggleVisibility(TextView passwordVisibility) {
        if (isTextVisible(passwordVisibility)) {
            //change button text to Hide
            passwordVisibility.setText(getResources().getString(R.string.hide));
            //hide password
            passphraseTxt.setTransformationMethod(null);
        } else {
            //change button text to Show
            passwordVisibility.setText(getResources().getString(R.string.show));
            //show password
            passphraseTxt.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    public boolean isTextVisible(TextView textView) {
        return textView.getText().toString().equalsIgnoreCase(getResources().getString(R.string.show));
    }

    private boolean validate() {

        if (hotspotTxt.getText().toString().length() == 0) {
            buildSnackBar("Please enter Hotspot Name!");
//                Toast.makeText(BLEHotSpotCredentialsActivity.this, "Please enter Hotspot Name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passphraseTxt.getText().toString().length() == 0) {
            buildSnackBar("Please enter Password!");
//                Toast.makeText(BLEHotSpotCredentialsActivity.this, "Please enter Password!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passphraseTxt.getText().length() > 64) {
            buildSnackBar("Password should be less than 64 characters!");
//                Toast.makeText(BLEHotSpotCredentialsActivity.this, "Password should be less than 64 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passphraseTxt.getText().toString().length() < 8) {
            buildSnackBar("Password should be of minimum 8 characters!");
//                Toast.makeText(BLEHotSpotCredentialsActivity.this, "Password should be of minimum 8 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(BLEHotSpotCredentialsActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    public void getListOfConnectedDevice() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedReader br = null;
                boolean isFirstLine = true;

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        String[] splitted = line.split(" +");
                        Log.d("suma in mac", "ip" + " : "
                                + splitted[0]);
                        if (splitted != null && splitted.length >= 4) {

                            String ipAddress = splitted[0];
                            String macAddress = splitted[3];

                            boolean isReachable = InetAddress.getByName(
                                    splitted[0]).isReachable(500);  // this is network call so we cant do that on UI thread, so i take background thread.
                            if (isReachable) {
                                Log.d("suma in mac\n", ipAddress + " : "
                                        + macAddress);
                            }

                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void checkWifiON() {
        ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifiCheck.isConnected()) {
            Log.d("WifiConnected", "connectivity services not connected");
        } else {
            Log.d("WifiConnected", "connectivity services connected");

        }

    }

    public int getClientList1() {
        int macCount = 0;
        BufferedReader br = null;
        String flushCmd = "sh ip -s -s neigh flush all";
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(flushCmd, null, new File("/proc/net"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null) {
                    // Basic sanity check
                    String mac = splitted[3];
                    Log.d("suma in mac", "check mac address\n" + mac);
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
               /* ClientList.add("Client(" + macCount + ")");
                IpAddr.add(splitted[0]);
                HWAddr.add(splitted[3]);
                Device.add(splitted[5]);*/
                        Log.d("Mac : 0 ", "check mac\n" + mac + " IP Address :0 " + splitted[0]);
                        System.out.println("Mac_Count  " + macCount + " MAC_ADDRESS  " + mac);

                        buildSnackBar("Mac_Count  " + macCount + "   MAC_ADDRESS  " + mac);
//                        Toast.makeText(
//                                getApplicationContext(),
//                                "Mac_Count  " + macCount + "   MAC_ADDRESS  "
//                                        + mac, Toast.LENGTH_SHORT).show();

                    }
           /* for (int i = 0; i < splitted.length; i++)
                System.out.println("Addressssssss     "+ splitted[i]);*/

                }
            }
        } catch (Exception e) {

        }
        return macCount;
    }


    private int countNumMac() {
        int macCount = 0;
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader("/proc/net/arp"), 1024);
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                    }
                    getMacAddress(mac);
                    Log.d("suma in mac", "splitted mac device\n" + getMacAddress(mac));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (macCount == 0)
            return 0;
        else
            return macCount - 1; //One MAC address entry will be for the host.
    }


    public void getClientList() {


        try {
            ArrayList<String> args = new ArrayList<>(Arrays.asList("ip", "neigh"));
            ProcessBuilder cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            int macCount = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String line; (line =reader.readLine()) != null; ) {
                String[] split = line.split(" +");
                if (split.length > 4 && split[0].matches("([0-9]{1,3}\\.){3}[0-9]{1,3}")) {
                    String ipAddress = split[0];
                    String mac = split[4];
                    Log.v("BLEHotspot", "Mac : Outside If " + mac);
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                        isHotSpotConnected = false;
                        lookFor = getMacAddress(mac);
                        Log.v("BLEHotspot", lookFor);
                        buildSnackBar("Mac_Count  " + macCount + "   MAC_ADDRESS  " + mac);
                        waitForDiscovery();
                    }
                }
            }
            reader.close();
            process.destroy();
        } catch (Exception e) {
            Log.d("atul_hotspot_excep", e.toString());
            e.printStackTrace();
        }

    }

    public String getMacAddress(String mac) {
        String newMac = mac.replace(":", "");
        Log.d("splitted mac device \n", "Mac : \n" + newMac + " IP Address :\n" + newMac);
        newMac = newMac.substring(newMac.length() - 6);
        return newMac;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LibreLogger.d(this, "hotspot credential onresume");
        Log.d("suma in mac", "receiver in onresume hotspot activity");
        EventBus.getDefault().register(this);
        //check if ACCESS_COARSE_LOCATION permission is allowed
       /* if (isLocationPermissionEnabled()) {
            afterPermit();
        } else {
            askLocationPermission();
        }*/

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNext:
                if (validate()) {
                    ShowHotSpotConnecting();

                }
                break;
        }

    }

    private void sendMSearchInIntervalOfTime() {
        mHandler.sendEmptyMessageDelayed(Constants.SEARCHING_FOR_DEVICE, 6000);
        mTaskHandlerForSendingMSearch.postDelayed(mMyTaskRunnableForMSearch, MSEARCH_TIMEOUT_SEARCH);
        Log.d("MavidCommunication", "My task is Sending 1 Minute Once M-Search msearch interval of time");
    }

    private Runnable mMyTaskRunnableForMSearch = new Runnable() {
        @Override
        public void run() {
            LibreLogger.d(this, "My task is Sending 1 Minute Once M-Search");
            /* do what you need to do */
            if (mBackgroundMSearchStoppedDeviceFound)
                return;

            try {
                LibreMavidHelper.advertiseWithIp(mSACConfiguredIpAddress,BLEHotSpotCredentialsActivity.this);
            } catch (WrongStepCallException e) {
                e.printStackTrace();
            }
            /* and here comes the "trick" */
            mTaskHandlerForSendingMSearch.postDelayed(this, MSEARCH_TIMEOUT_SEARCH);
        }
    };
//    private Runnable mMyTaskRunnableForMSearch1 = new Runnable() {
//        @Override
//        public void run() {
//            LibreLogger.d(this, "My task is Sending 1 Minute Once M-Search");
//            /* do what you need to do */
//           mHandler.sendEmptyMessage(WIFI_CONNECTING_NOTIFICATION);
//            /* and here comes the "trick" */
//            mTaskHandlerForSendingMSearch1.postDelayed(this, MSEARCH_TIMEOUT_SEARCH);
//        }
//    };
//
//    private void sendReadWifiStatus() {
//        mHandler.sendEmptyMessageDelayed(Constants.READ_WIFI_IN_INTERVALS, 20000);
//        mTaskHandlerForSendingMSearch.postDelayed(mMyTaskRunnableForMSearch1, 20000);
//        LibreLogger.d(this, "suma in read wifi status intervals method");
//    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LibreLogger.d(this, "Handler Message Got " + msg.toString());
            if (msg.what == Constants.SEARCHING_FOR_DEVICE) {
                if (mHandler.hasMessages(Constants.HTTP_POST_SOMETHINGWRONG)) {
                    mHandler.removeMessages(Constants.HTTP_POST_SOMETHINGWRONG);
                }
                mHandler.sendEmptyMessageDelayed(Constants.TIMEOUT_FOR_SEARCHING_DEVICE, 30000);
                LibreLogger.d(this, "suma in connectTowifi searchingfordevice");

            }
            else if (msg.what == CONFIGURED_DEVICE_FOUND) {
                mHandler.removeCallbacksAndMessages(null);
                //stopMsearch
                mBackgroundMSearchStoppedDeviceFound = true;
                mTaskHandlerForSendingMSearch.removeCallbacks(mMyTaskRunnableForMSearch);
                /*
                enableNetworkCallbacks();*/
                closeLoader();
                LibreLogger.d(this, "suma in wifi connecting close loader configuredevice found");

                // MavidApplication.isSacConfiguredSuccessfully=true;
                Log.d("HotSpot", "connectTowifi devicefound");
                if (!hotSpotAlexaCalled) {
                    Log.d("MavidCommunication", "suma in hotspot credential from active network in status configure device found\n" + mSACConfiguredIpAddress);
                }

            } else if (msg.what == Constants.HOTSPOT_CONNECTION_TIMEOUT) {
                closeLoader();
                LibreLogger.d(this, "suma in wifi connecting timeout");

                //ShowAlertDynamicallyGoingToHomeScreen(getResources().getString(R.string.enableHSFail),getResources().getString(R.string.enableHSWaitFAil));

            } else if (msg.what == WIFI_CONNECTING_NOTIFICATION) {
                LibreLogger.d(this, "wifi timer for every 30seconds in wifi connecting HANDLER\n");

                LibreLogger.d(this, "suma in read wifi status intervals connecting timeout for 30 seconds ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
                        BleCommunication.writeInteractorReadWifiStatus();
                        LibreLogger.d(this, "wifi timer for every 30seconds write interactor reas wifi status");
                        if (MavidApplication.readWifiStatus == true) {
                            LibreLogger.d(this, "suma in wifi connecting write interactor reas wifi status success");
                        } else {
                            LibreLogger.d(this, "suma in wifi connecting write interactor reas wifi status failure");


                        }
                    }
                });
                if (count == 5 || MavidApplication.readWifiStatusNotification) {
                    closeLoader();
                    LibreLogger.d(this, "wifi timer for every 30seconds getting count \n");
                    if (!BLEHotSpotCredentialsActivity.this.isFinishing()) {
                        ShowAlertDynamicallyGoingToHomeScreen("Configuration failed",
                                " Please make sure your speaker is blinking multiple colours and then try again.", BLEHotSpotCredentialsActivity.this);
                    }
                }
            } else if (msg.what == Constants.WIFI_CONNECTED_NOTIFICATION) {
                LibreLogger.d(this, "suma in wifi connecting event send empty msg delayed for 30 seconds two ");

            }

        }
    };

    @Override
    protected void onDestroy() {
        try {
            if (mTaskHandlerForSendingMSearch.hasMessages(MSEARCH_TIMEOUT_SEARCH)) {
                mTaskHandlerForSendingMSearch.removeMessages(MSEARCH_TIMEOUT_SEARCH);
            }
            mTaskHandlerForSendingMSearch.removeCallbacks(mMyTaskRunnableForMSearch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.removeMessages(CONFIGURED_DEVICE_FOUND);
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    private void waitForDiscovery() {
        try {
            MavidApplication.setDeviceListener(new DeviceListener() {
                @Override
                public void newDeviceFound(final DeviceInfo deviceInfo) {
                    Log.d("MavidCommunication", "Newdevice found in after sending hotspot" + deviceInfo.getFriendlyName() + "get IP address\n" + deviceInfo.getIpAddress());

                    if (deviceInfo != null) {
                        if (isThisDeviceLookingFor(deviceInfo)) {

                            LibreLogger.d(this, "suma in wifi connecting close loader inside lookingfor device");

                            LibreLogger.d(this, "Hurray !! New Device Found and same as Configured Device"
                                    + deviceInfo.getFriendlyName() + "get IP\n" + deviceInfo.getIpAddress());
                            mSACConfiguredIpAddress = deviceInfo.getIpAddress();

                            Log.d("atul_ble_hotspot", deviceInfo.getFriendlyName());
                            saveConnectedHotSpotName(new ModelSaveHotSpotName(deviceInfo.getFriendlyName(), hotSpotNameString));

                            if (!hotSpotAlexaCalled) {
                                LibreLogger.d(this, "suma in wifi connecting event alexa login three\n");

                            }
                            LibreLogger.d("MavidCommunication", "suma in hotspot credential alexa login  from active network mSacConfigured\n" + mSACConfiguredIpAddress + "get device info IP address\n" + deviceInfo.getIpAddress());
                            {
                                /*changes specific to alexa check*/
                                if (mSACConfiguredIpAddress != null && !mSACConfiguredIpAddress.isEmpty()) {
                                    if (mHandler.hasMessages(Constants.ALEXA_CHECK_TIMEOUT)) {
                                        mHandler.removeMessages(Constants.ALEXA_CHECK_TIMEOUT);
                                    }
                                    LibreLogger.d(this,"conf device found bleHotspot");
                                    mHandler.sendEmptyMessage(CONFIGURED_DEVICE_FOUND);
                                }

                                if (!isRefreshedAlexaToken) {
                                    isRefreshedAlexaToken = true;
                                    checkForAlexaToken(mSACConfiguredIpAddress, getAlexaLoginStatus, null);
                                }
                            }
                        }
                    }

                }

                @Override
                public void deviceGotRemoved(DeviceInfo deviceInfo) {

                }

                @Override
                public void deviceDataReceived(MessageInfo messageInfo) {

                }

                @Override
                public void failures(Exception e) {

                }

                @Override
                public void checkFirmwareInfo(DeviceInfo deviceInfo) {

                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isThisDeviceLookingFor(DeviceInfo deviceInfo) {
        try {
            String lookForDevice = null;
            if (deviceInfo != null) {

                String deviceName = deviceInfo.getFriendlyName();
                deviceName = deviceName.substring(deviceName.length() - 5);
                String amr = deviceName.substring(0, deviceName.length() - 1);
                lookForDevice = MavidApplication.lookFor.substring(MavidApplication.lookFor.length() - 5);
                //}
                return lookForDevice.contains(amr);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void ShowHotSpotConnecting() {

        if (alert == null) {

            alert = new Dialog(BLEHotSpotCredentialsActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText(getResources().getString(R.string.enableHS));

        tv_alert_message.setText(getResources().getString(R.string.enableHSWait));

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert.dismiss();
                        alert = null;
                        showLoader(getResources().getString(R.string.notice), getResources().getString(R.string.waitingHotSpot));

                    }
                });
                //  mHandler.sendEmptyMessageDelayed(Constants.HOTSPOT_CONNECTION_TIMEOUT, 80000);
                final Bundle sacParams = new Bundle();
                sacParams.putString(BundleConstants.SACConstants.SSID
                        , hotspotTxt.getText().toString().trim());
                sacParams.putString(BundleConstants.SACConstants.PASSPHRASE
                        , passphraseTxt.getText().toString());
                sacParams.putInt(BundleConstants.SACConstants.NETWORK_TYPE
                        , security);

                LibreMavidHelper.configureBLE(sacParams, new SACListener() {
                    @Override
                    public void success() {
//                            updateResponseUI("Credentials succesfully posted. Please restart the app.");
//                            doNext(sacParams);
                    }

                    @Override
                    public void failure(String message) {
                        updateResponseUI(message);
                    }

                    @Override
                    public void successBLE(byte[] b) {
                        BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
                        BleCommunication.writeInteractor(b);
                        Log.v("BleCommunication", "suma in bleCommunication writting Sac Success BLE");

                    }
                });
            }
        });

        alert.show();

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(true);
//        builder.setTitle(getResources().getString(R.string.enableHS));
//        builder.setMessage(getResources().getString(R.string.enableHSWait));
//        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //dialog.dismiss();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showLoader(getResources().getString(R.string.notice), getResources().getString(R.string.waitingHotSpot));
//
//                    }
//                });
//                //  mHandler.sendEmptyMessageDelayed(Constants.HOTSPOT_CONNECTION_TIMEOUT, 80000);
//                final Bundle sacParams = new Bundle();
//                sacParams.putString(BundleConstants.SACConstants.SSID
//                        , hotspotTxt.getText().toString().trim());
//                sacParams.putString(BundleConstants.SACConstants.PASSPHRASE
//                        , passphraseTxt.getText().toString());
//                sacParams.putInt(BundleConstants.SACConstants.NETWORK_TYPE
//                        , security);
//
//                LibreMavidHelper.configureBLE(sacParams, new SACListener() {
//                    @Override
//                    public void success() {
////                            updateResponseUI("Credentials succesfully posted. Please restart the app.");
////                            doNext(sacParams);
//                    }
//
//                    @Override
//                    public void failure(String message) {
//                        updateResponseUI(message);
//                    }
//
//                    @Override
//                    public void successBLE(byte[] b) {
//                        BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
//                        BleCommunication.writeInteractor(b);
//                        LibreLogger.d("BleCommunication", "suma in bleCommunication writting Sac Success BLE");
//
//                    }
//                });
//            }
//        });
//        AlertDialog alert = builder.create();
//        alert.show();
    }

    @Subscribe
    public void onEvent(BLEEvntBus event) {
        byte[] value = event.message;

        int response = getDataLength(value);
        Log.v("BLEHotspot", "Value received getting my response\n " + response);

        //getDataLengthString(value);
        LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value\n" + response);

        byte[] value1 = event.message;
        // String asset = new String(value, StandardCharsets.UTF_8);
        // LibreLogger.d(this,"suma in gettin index value getting string value\n"+value+"string\n"+asset);

//        LibreLogger.d(this, "suma in wifi connecting event send empty msg delayed get response \n " + value[5]);

        if (response == 0) {
            // closeLoader();
            Log.d("Hotspot", "credentials ok" + value);
            if (isActivityActive) {
                updateResponseUI("Credentials succesfully posted.");
                Log.d("MavidCommunication", "response 0");
            }
//                doNext(sacParams);
            while (isHotSpotConnected) {
                getClientList();
                //getIps();
            }
            // showLoader("","waiting for device response...");
            sendMSearchInIntervalOfTime();
            MavidApplication.credentialSent = true;
        }
        if (response == 1) {
            //  closeLoader();
            LibreLogger.d(this, "suma in wifi connecting close loader 1");

            if (isActivityActive) {
                updateResponseUI("Invalid credentials");
            }
            Log.d("Hotspot", "invalid credentials" + value);
        }

        if (response == 4) {
            closeLoader();
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    count++;
                    if (count < 6) {
                        mHandler.sendEmptyMessage(WIFI_CONNECTING_NOTIFICATION);
                        LibreLogger.d(this, "wifi timer for every 30seconds" + count);
                        handler.postDelayed(this, 30000);
                    }
                }
            }, 30000);

            if (value[2] == 0 && value1.length > 6) {
                LibreLogger.d(this, "suma in data length\n" + value1.length);
                for (int i = 5; i < value.length - 5; i++) {
                    Log.v("BLEHotspot", "suma in gettin index value" + value[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        finalConnecting = new String(value, StandardCharsets.UTF_8);
                    }
                    finalConnectingIndex = finalConnecting.indexOf("Connecting");
                    Log.v("BLEHotspot", "suma wifi connecting only splitted array string\n" + finalConnecting.substring(finalConnectingIndex));

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoader("Please Wait...", "" + finalConnecting.substring(finalConnectingIndex));

                    }
                });
            }
        }
        if (response == 5) {
            mHandler.removeCallbacksAndMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);

            LibreLogger.d(this, "suma in wifi connecting close loader 5");
            if (value[2] == 0 && value.length > 6) {
                LibreLogger.d(this, "suma in data length\n" + value1.length);
                for (int i = 5; i < value.length - 5; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        finalConnectedArray = new String(value, StandardCharsets.UTF_8);
                    }

                    hotSpotNameString = finalConnectedArray;


                    finalConnectedIndex = finalConnectedArray.indexOf("Connected");


                    finalConnectedString = finalConnectedArray.substring(finalConnectedIndex);

                    hotSpotNameString = hotSpotNameString.substring(hotSpotNameString.lastIndexOf(" ") + 1);

                    //save the hotspotName with along with friendly name


                    Log.v("BLEHotspot", "hotSpot_name " + hotSpotNameString.substring(hotSpotNameString.lastIndexOf(" ") + 1));


                    Log.v("BLEHotspot", "wifi timer for every 30seconds connected status\n" + finalConnectedArray.substring(finalConnectedIndex));


                }
            }
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(),""+finalConnectedArray.substring(finalConnectedIndex),Toast.LENGTH_SHORT).show();
//
//                }
//            });
            //   Toast.makeText(this,""+finalConnectedArray.substring(finalConnectedIndex),Toast.LENGTH_SHORT).show();

//            startActivity(new Intent(BLEHotSpotCredentialsActivity.this, OSHotSpotSuccessAlexaLogin.class).putExtra("IPADDRESS", mSACConfiguredIpAddress));
//            LibreLogger.d(this,"suma in wifi connecting event alexa login one\n");

            LibreLogger.d(this, "suma in wifi connecting event send empty msg delayed for 30 seconds  got connected status alexa login\n");
            sendMSearchInIntervalOfTime();
            waitForDiscovery();
            try {
                LibreMavidHelper.advertise();
                LibreLogger.d(this,"suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery mavid blehotspot");

            } catch (WrongStepCallException e) {
                e.printStackTrace();
            }
            BleCommunication.writeInteractorDeviceStatus();

//            //StopSAC
//            BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
//            BleCommunication.writeInteractorStopSac();

//            Toast.makeText(this,""+finalConnectedArray.substring(finalConnectedIndex),Toast.LENGTH_SHORT).show();

//            if(!hotSpotAlexaCalled) {
//                startActivity(new Intent(BLEHotSpotCredentialsActivity.this, OSHotSpotSuccessAlexaLogin.class).putExtra("IPADDRESS", mSACConfiguredIpAddress));
//                Log.d("MavidCommunication","suma in hotspot credential from active network in status configure device found\n"+mSACConfiguredIpAddress);
//
//                finish();
//            }
//            //StopSAC
//            BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
//            BleCommunication.writeInteractorStopSac();
//            Log.d("Hotspot", "wifi conneted" + value);


            //saving ssid name
        }
//        if(MavidApplication.readWifiStatus=false){
//            LibreLogger.d("BleCommunication","suma in wifi connecting event in read wifi status check");
//            BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
//            BleCommunication.writeInteractorReadWifiStatus();
//        }

          /*  if (value[2] == 1) {
                Log.d("Hotspot", "scanlist");
            } else {
                int response = getDataLength(value);
                Log.d("Hotspot", "get data" + response);
                if (response == 0) {
                    Log.d("Hotspot", "credentials ok" + value);
                    updateResponseUI("Credentials succesfully posted. Please restart the app.");
//                doNext(sacParams);
                    while (isHotSpotConnected) {
                        getClientList();
                    }
                    sendMSearchInIntervalOfTime();

                } else if (response == 1) {
                    closeLoader();
                    updateResponseUI("Invalid credentials");
                    Log.d("Hotspot", "invalid credentials" + value);
                }
            }*/
        if (response == 16) {
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC connection wifi failed");
            //ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..","Please put the device to setup mode");
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    LibreLogger.d(this, "suma in gettin index value" + value[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        configuratonfailedStringBle = new String(value, StandardCharsets.UTF_8);
                    }
                    configuratonfailedIndexBle = configuratonfailedStringBle.indexOf("Configuration");
                    String failedString = configuratonfailedStringBle.substring(configuratonfailedIndexBle);
                    LibreLogger.d(this, "suma in configuration failed 16 final string\n" + failedString);
                    handler.removeCallbacksAndMessages(WIFI_CONNECTING_NOTIFICATION);
                    handler.removeCallbacksAndMessages(null);
                    ShowAlertDynamicallyGoingToHomeScreen("Configuration Failed", failedString, BLEHotSpotCredentialsActivity.this);

                }
            }
        }
        if (response == 12) {
            closeLoader();
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value 12 NOTIFY\n" + response);
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    LibreLogger.d(this, "suma in gettin index value hotspot\n" + value[i]);
                    readWifiStatusString = new String(value, StandardCharsets.UTF_8);
                    readWifiStatusIndexConfiguration = readWifiStatusString.indexOf("Con");
                    LibreLogger.d(this, "suma in gettin read wifi status value readwifi status\n" + readWifiStatusString.substring(readWifiStatusIndexConfiguration));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoader("Please Wait...", "" + readWifiStatusString.substring(readWifiStatusIndexConfiguration));
                    }
                });
                MavidApplication.readWifiStatusNotificationBLE = true;
                LibreLogger.d(this, "c\n");

            }
        }
        if (response == 13) {
            closeLoader();
//            ShowAlertDynamicallyGoingToHomeScreen("Sac Not Allowed...",
//                    "Please make sure your device is in setup mode", BLEHotSpotCredentialsActivity.this);
        }
        if (response == 17) {
            mHandler.removeMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);
            LibreLogger.d(this, "Getting the ble Status suma in gettin response value Read Device status");
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    LibreLogger.d(this, "suma in gettin index value in 17\n" + value[i]);
                    String deviceStatus = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        deviceStatus = new String(value, StandardCharsets.UTF_8);
                    }
                    Log.v("BLEHotspot", "Getting the ble Status getting device status\n" + deviceStatus);

                    finalIpIndez = deviceStatus.indexOf("IpAdd:");
                    LibreLogger.d(this, "Getting the ble Status getting ipAddress\n" + deviceStatus.substring(finalIpIndez));
                    FilteredfinalIpAddress = deviceStatus.substring(finalIpIndez);
                    String[] splittedIpValue = FilteredfinalIpAddress.split(":");
                    mSACConfiguredIpAddress = splittedIpValue[1];
                    MavidApplication.broadCastAddress = mSACConfiguredIpAddress;
                    LibreLogger.d(this, "Getting the ble Status getting ipAddress index 1\n" + splittedIpValue[1] + "mSacConfiguredIp" + mSACConfiguredIpAddress);

                    alexaLoginIndex = deviceStatus.indexOf("AlexaLogin");
                    filteredAlexaLogin = deviceStatus.substring(alexaLoginIndex);
                    String[] splittedAlexaLogin = filteredAlexaLogin.split(":");
                    getAlexaLoginStatus = splittedAlexaLogin[1];
                    LibreLogger.d(this, "Hotspot Credential Getting the ble Status getting AlexaLogin\n" + splittedAlexaLogin[1] + "login status\n" + getAlexaLoginStatus);

                    LibreLogger.d(this,"suma in getting stop sac activity mavid ble hotspot  17 ");


//                    Intent intent = new Intent(BLEHotSpotCredentialsActivity.this, BLEYesAlexaLoginNoHomeScreen.class);
//                    intent.putExtra("IPADDRESS", mSACConfiguredIpAddress);
//                    intent.putExtra(Constants.INTENTS.ALEXA_LOGIN_STATUS,getAlexaLoginStatus);
//                    startActivity(intent);

                }
            }
            sendMSearchInIntervalOfTime();
            waitForDiscovery();

          //  BleCommunication.writeInteractorStopSac();
        }
        if (response == 14) {
            closeLoader();
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC timeout dialogue");
            //StopSAC
//            BleCommunication bleCommunication = new BleCommunication(BLEHotSpotCredentialsActivity.this);
//            BleCommunication.writeInteractorStopSac();
            LibreLogger.d(this,"suma in getting stop sac activity mavid ble hotspot credential 14 ");
            ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..",
                    "Please put the device to setup mode", BLEHotSpotCredentialsActivity.this);
        }

        if (response == 15) {
            closeLoader();
            mHandler.removeCallbacksAndMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);
            if (value[2] == 0 && value1.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    LibreLogger.d(this, "suma in gettin index value" + value[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        ConfigurationFailedString = new String(value, StandardCharsets.UTF_8);
                    }
                    configuratonfailedIndex = ConfigurationFailedString.indexOf("Configuration");
                    LibreLogger.d(this, "suma in gettin index value configuration failed\n" + ConfigurationFailedString);

                }
                MavidApplication.readWifiStatusNotification = true;
                LibreLogger.d(this, "suma in gettin index value configuration failed outside loop 15\n" + ConfigurationFailedString.substring(configuratonfailedIndex));
                ShowAlertDynamicallyGoingToHomeScreen(ConfigurationFailedString.substring(configuratonfailedIndex),
                        "Please make sure Credential entered is correct and Try Again!", BLEHotSpotCredentialsActivity.this);
               // BleCommunication.writeInteractorStopSac();
                LibreLogger.d(this,"suma in getting stop sac activity mavid ble hotspot credential 15");

            }
        }
    }


    private void checkForAlexaToken(String mSACConfiguredIpAddress, final String alexaLoginStatus, Network network) {
        LibreMavidHelper.askRefreshTokenWithNetwork(mSACConfiguredIpAddress, new CommandStatusListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                String messages = messageInfo.getMessage();
                Log.d("atul_after_alexa_token", alexaLoginStatus);
                closeLoader();
                Log.d("DeviceManager", " got alexa token " + messages);
                handleAlexaRefreshTokenStatus(messageInfo.getIpAddressOfSender(), messageInfo.getMessage(), alexaLoginStatus);

            }

            @Override
            public void failure(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoader();
                        showConfigurationSucessfulMessage();
                        Log.d("alexa_ex", e.toString());
                    }
                });
            }

            @Override
            public void success() {

            }
        }, network);
    }


    public void showConfigurationSucessfulMessage() {
        if (!BLEHotSpotCredentialsActivity.this.isFinishing()) {

            alert = new Dialog(BLEHotSpotCredentialsActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);


            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);


            tv_alert_title.setText("");

            tv_alert_message.setText("Configuration Successful");

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    Intent ssid = new Intent(BLEHotSpotCredentialsActivity.this,
                            MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(ssid);
                    finish();
                }
            });

            alert.show();
        }

    }

    private void handleAlexaRefreshTokenStatus(String current_ipaddress, String refreshToken, String alexaLoginStatus) {

        if (refreshToken != null && !refreshToken.isEmpty() && alexaLoginStatus != null && alexaLoginStatus.contains("Yes")) {
            /*not logged in*/
            Intent i = new Intent(BLEHotSpotCredentialsActivity.this, AlexaThingsToTryDoneActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("speakerIpaddress", current_ipaddress);
            i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(i);
            finish();
        } else {
            Intent newIntent = new Intent(BLEHotSpotCredentialsActivity.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("speakerIpaddress", current_ipaddress);
            newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(newIntent);
            finish();
        }
    }

    public void saveConnectedHotSpotName(ModelSaveHotSpotName modelSaveHotSpotName) {
        SharedPreferences mPrefs = getSharedPreferences("Mavid", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String hotspotNamesListString = mPrefs.getString("hotspotNamesList", "");

        Type type = new TypeToken<List<ModelSaveHotSpotName>>() {
        }.getType();

        if (hotspotNamesListString != null) {
            if (!hotspotNamesListString.isEmpty()) {
                modelSaveHotSpotNameList = gson.fromJson(hotspotNamesListString, type);
            } else {
                modelSaveHotSpotNameList = new ArrayList<>();
            }
        }
        modelSaveHotSpotNameList = saveDeviceHotSpotName(modelSaveHotSpotNameList, modelSaveHotSpotName);

        SharedPreferences.Editor prefsEditor = mPrefs.edit();

        String json = gson.toJson(modelSaveHotSpotNameList);
        prefsEditor.putString("hotspotNamesList", json);
        prefsEditor.apply();

    }

    public List<ModelSaveHotSpotName> saveDeviceHotSpotName(List<ModelSaveHotSpotName> modelSaveHotSpotNameList, ModelSaveHotSpotName modelSaveHotSpotName) {
        if (modelSaveHotSpotNameList.size() > 0) {

            Iterator<ModelSaveHotSpotName> modelSaveHotSpotNameIterator = modelSaveHotSpotNameList.iterator();

            if (modelSaveHotSpotNameIterator.hasNext()) {

                ModelSaveHotSpotName savedHotSpotName = modelSaveHotSpotNameIterator.next();

                if (savedHotSpotName.friendlyName.equals(modelSaveHotSpotName.friendlyName)
                        || savedHotSpotName.friendlyName.contains(modelSaveHotSpotName.friendlyName)) {
                    //update if the device already present
                    Log.d("atul_ble_hotspot_cred", "updated");
                    savedHotSpotName.hotSpotName = modelSaveHotSpotName.hotSpotName;
                } else {
                    //add a new device to the list
                    modelSaveHotSpotNameList.add(modelSaveHotSpotName);
                    Log.d("atul_ble_hotspot_cred", "added");
                }
            }
        } else {
            //adding the first device
            modelSaveHotSpotNameList.add(modelSaveHotSpotName);
        }
        return modelSaveHotSpotNameList;
    }


    public void getDataLengthString(byte[] buf) {

//        //from 5 th idex buflength-5;
//
//        for (int i = 5; i < buf.length-5; i++){
//            LibreLogger.d(this,"suma in gettin index value in getDataLength\n"+buf[i]);
//
//        }
//
////        for (int i = 5; i < buf.length-1; i++){
////            LibreLogger.d(this,"Hotspot value suma in gettin index value in getDataLength second\n"+buf[i]+"getting data value 5th"+buf[5]);
////        }
////       buf[5]-buf.length
////        return String;

        for (int i = 5; i < buf.length - 5; i++) {
            LibreLogger.d(this, "suma in gettin index value" + buf[i]);
            String asset = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                asset = new String(buf, StandardCharsets.UTF_8);
            }
            //  LibreLogger.d(this,"Getting the HotSpot Status suma in gettin index value asset main\n"+asset);

//                int configuratonfailed=asset.indexOf("Configuration");
//                asset.substring(configuratonfailed);
//                LibreLogger.d(this,"Getting the HotSpot Status suma in gettin index value getting sac failed\n"+asset.substring(configuratonfailed));

            int idx = asset.indexOf("Connecting");
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin index value getting string value\n" + asset.substring(idx));


        }
    }

    public static void getIps() {

        ArrayList<String> ips = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
        } catch (FileNotFoundException e) {
            Log.d("suma in mac", "file not found", e);
        }
        String line;
        try {
            if (br != null) {
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" +");
                    LibreLogger.d("suma in mac", "hotspot macaddress splitted string\n" + splitted[0] + "splitted 1" + splitted[1] + "splitted 3" + splitted[3]);

                    if (splitted.length >= 4
                            && ((splitted[2].equals("0x2")) || (splitted[2]
                            .equals("0x0")))) {
                        String mac = splitted[3];
                        Log.v("BLEHotspot", "hotspot macaddress\n" + mac);

                        if (!mac.matches("00:00:00:00:00:00")) {
                            ips.add(splitted[0]);
                            Log.v("BLEHotspot", "hotspot macaddress splitted\n" + ips.add(splitted[0]));

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(ips);
        Log.d("suma in mac", "mac ip address found" + ips);

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                        Log.d("suma in mac", "mac ip address found" + inetAddress.getHostAddress() + "one more\n" + inetAddress.getHostName() + "get two\n" + inetAddress.getAddress());

                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    private void updateResponseUI(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //closeLoader();
                if (isActivityActive) {
                    buildSnackBar("" + message);
//                    Toast.makeText(BLEHotSpotCredentialsActivity.this, "" + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public int getDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1 << 8 | b2 & 0xFF);

        LibreLogger.d(this, "Data length is returned as s" + s);
        return s;
    }

    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onWriteFailure() {

    }


    public String getIPAddress() throws WifiException {
        try {
            InetAddress mAddress = getLocalV4Address(getActiveNetworkInterface());
            String ipAddress = mAddress.getHostAddress();
            return ipAddress;
        } catch (Exception e) {
            WifiException wifiException = new WifiException("Getting invalid IPAddress. Check if wifi is enabled");
            wifiException.setStackTrace(e.getStackTrace());
            throw wifiException;
        }
    }

    public static NetworkInterface getActiveNetworkInterface() throws SocketException {

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
            Log.d("ip check", " in getActiveInterface " + iface.getName());

            /* Check if we have a non-local address. If so, this is the active
             * interface.
             *
             * This isn't a perfect heuristic: I have devices which this will
             * still detect the wrong interface on, but it will handle the
             * common cases of wifi-only and Ethernet-only.
             */
            //for wlan0 interface
            if ((iface.getName().startsWith("w") && iface.isUp())
                    //for softAP interface
                    || (iface.getName().startsWith("sof") && iface.isUp())
                    //for p2p interface
                    || (iface.getName().startsWith("p") && iface.isUp())) {
                //this is a perfect hack for getting wifi alone

                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();

                    if (!(addr.isLoopbackAddress() || addr.isLinkLocalAddress())) {
                        Log.d("LSSDP", "DisplayName" + iface.getDisplayName() + "Name" + iface.getName() + "addr" + addr + " Host Address" + addr.getHostAddress());

                        return iface;
                    }
                }
            }
        }

        return null;
    }

    public static InetAddress getLocalV4Address(NetworkInterface netif) {


        Enumeration addrs;
        try {
            addrs = netif.getInetAddresses();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        while (addrs.hasMoreElements()) {
            InetAddress addr = (InetAddress) addrs.nextElement();
            if (addr instanceof Inet4Address && !addr.isLoopbackAddress())
                return addr;
        }
        return null;
    }


    @Override
    public void onStart() {
        super.onStart();
      //  EventBus.getDefault().register(this);
    }




    @Override
    public void onStop() {
        ///isActivityActive = false;
       // EventBus.getDefault().unregister(this);
        super.onStop();
    }


}
