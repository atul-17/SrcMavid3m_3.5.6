package com.libre.irremote.OtherSetupApproach;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.danimahardhika.cafebar.CafeBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;

import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.libresdk.Constants.BundleConstants;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.Discovery.Listeners.DeviceListener;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListener;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.models.ModelSaveHotSpotName;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.libre.irremote.Constants.Constants.CONFIGURED_DEVICE_FOUND;

public class WifiHotSpotCredentialsActivity extends BaseActivity implements View.OnClickListener {

    private TextInputEditText hotspotTxt, passphraseTxt;
    private int PERMISSION_ACCESS_COARSE_LOCATION = 0x100;
    private final int CONNECT_TO_SAC = 0x300;
    private Button btnNext;
    private Dialog alert;
    private int security = 4;


    private final int MSEARCH_TIMEOUT_SEARCH = 2000;
    private Handler mTaskHandlerForSendingMSearch = new Handler();
    public boolean mBackgroundMSearchStoppedDeviceFound = false;
    private String lookFor;
    private String mSACConfiguredIpAddress = "";
    private boolean isHotSpotConnected = true;
    ProgressDialog mProgressDialog;
    private ImageView back;

    CafeBar cafeBar;

    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    List<ModelSaveHotSpotName> modelSaveHotSpotNameList = new ArrayList<>();


    String ipAddress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hot_spot_credentials_activity);
        disableNetworkChangeCallBack();
        disableNetworkOffCallBack();
        initWidgets();

        MavidApplication.hotSpotAlexaCalled = false;
    }

    private void initWidgets() {

        hotspotTxt = (TextInputEditText) findViewById(R.id.hotspotTxt);
        passphraseTxt = (TextInputEditText) findViewById(R.id.passphraseTxt);
        btnNext = (Button) findViewById(R.id.btnNext);
        back = (ImageView) findViewById(R.id.iv_back);
        btnNext.setOnClickListener(this);
        passphraseTxt.setTransformationMethod(new PasswordTransformationMethod());
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
        if (textView.getText().toString().equalsIgnoreCase(getResources().getString(R.string.show))) {
            return true;
        }
        return false;
    }

    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(WifiHotSpotCredentialsActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    private boolean validate() {
        if (hotspotTxt.getText() != null) {
            if (hotspotTxt.getText().toString().length() == 0) {
                buildSnackBar("Please enter Hotspot Name!");
                return false;
            }
        }

        if (passphraseTxt.getText() != null) {

            if (passphraseTxt.getText().toString().length() == 0) {
                buildSnackBar("Please enter Password!");
                return false;
            }

            if (passphraseTxt.getText().length() > 64) {
                buildSnackBar("Password should be less than 64 characters!");
                return false;
            }

            if (passphraseTxt.getText().toString().length() < 8) {
                buildSnackBar("Password should be of minimum 8 characters!");
                return false;
            }
        }

        return true;
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

            Iterator<ModelSaveHotSpotName>modelSaveHotSpotNameIterator = modelSaveHotSpotNameList.iterator();

            if (modelSaveHotSpotNameIterator.hasNext()) {

                ModelSaveHotSpotName savedHotSpotName = modelSaveHotSpotNameIterator.next();

                if (savedHotSpotName.friendlyName.equals(modelSaveHotSpotName.friendlyName)
                        || savedHotSpotName.friendlyName.contains(modelSaveHotSpotName.friendlyName)) {
                    //update if the device already present
                    Log.d("atul_sveDevceHotSptNme", "updated");
                    savedHotSpotName.hotSpotName = modelSaveHotSpotName.hotSpotName;
                    mHandler.removeMessages(Constants.HOTSPOT_CONNECTION_TIMEOUT);
                } else {
                    //add a new device to the list
                    Log.d("atul_sveDevceHotSptNme", "added to the list");

                    modelSaveHotSpotNameList.add(modelSaveHotSpotName);
                }
            }
        } else {
            //adding the first device
            modelSaveHotSpotNameList.add(modelSaveHotSpotName);
        }

        return modelSaveHotSpotNameList;
    }

    public void getClientList1() {
        int macCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null) {
                    // Basic sanity check
                    String mac = splitted[3];
                    LibreLogger.d(this, "Mac : Outside If " + mac);
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                   /* ClientList.add("Client(" + macCount + ")");
                    IpAddr.add(splitted[0]);
                    HWAddr.add(splitted[3]);
                    Device.add(splitted[5]);*/
                        isHotSpotConnected = false;
                        LibreLogger.d(this, "Mac : 0" + mac + " IP Address :0 " + splitted[0]);
                        LibreLogger.d(this, "Mac_Count  " + macCount + " MAC_ADDRESS  " + getMacAddress(mac));
                        lookFor = getMacAddress(mac);

                        //mSACConfiguredIpAddress=splitted[0];

                        /*Toast.makeText(getApplicationContext(),"Mac_Count  " + macCount + "   MAC_ADDRESS  "
                                        + mac, Toast.LENGTH_SHORT).show();*/

                    }
               /* for (int i = 0; i < splitted.length; i++)
                    System.out.println("Addressssssss     "+ splitted[i]);*/

                }
            }
        } catch (Exception e) {

        }
    }

//    public void getClientList() {
//        int macCount = 0;
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader("/proc/net/arp"));
//            Log.d("Hotspot", String.valueOf(br));
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] splitted = line.split(" +");
//                if (splitted != null) {
//                    // Basic sanity check
//                    String mac = splitted[3];
//                    LibreLogger.d(this, "Mac : Outside If " + mac);
//                    if (mac.matches("..:..:..:..:..:..")) {
//                        macCount++;
//                   /* ClientList.add("Client(" + macCount + ")");
//                    IpAddr.add(splitted[0]);
//                    HWAddr.add(splitted[3]);
//                    Device.add(splitted[5]);*/
//                        isHotSpotConnected = false;
//                        Log.d("Hotspot", "Mac : " + mac + " IP Address : " + splitted[0]);
//                        Log.d("Hotspot", "Mac_Count  " + macCount + " MAC_ADDRESS  " + getMacAddress(mac));
//                        lookFor = getMacAddress(mac);
//                        Log.d("atul_hotspot", lookFor);
//                        buildSnackBar("Mac_Count  " + macCount + "   MAC_ADDRESS  " + mac);
////
//                        waitForDiscovery();
////                        Toast.makeText(
////                                getApplicationContext(),
////                                "Mac_Count  " + macCount + "   MAC_ADDRESS  "
////                                        + mac, Toast.LENGTH_SHORT).show();
//
//                    }
//               /* for (int i = 0; i < splitted.length; i++)
//                    System.out.println("Addressssssss     "+ splitted[i]);*/
//
//                }
//            }
//        } catch (Exception e) {
//            buildSnackBar(e.toString());
//            Log.d("atul_hotspot", e.toString());
//        }
//    }

    private void getClientList() {
        try {
            ArrayList<String> args = new ArrayList<>(Arrays.asList("ip", "neigh"));
            ProcessBuilder cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            int macCount = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for(String line; (line =reader.readLine()) != null; ) {
                String[] split = line.split(" +");
                if (split.length > 4 && split[0].matches("([0-9]{1,3}\\.){3}[0-9]{1,3}")) {
                    ipAddress = split[0];
                    String mac = split[4];
                    Log.d("atul_hotspot", "Mac : Outside If " + mac);
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                        isHotSpotConnected = false;
                        lookFor = getMacAddress(mac);
                        Log.d("atul_hotspot", lookFor);
                        buildSnackBar("Mac_Count  " + macCount + "   MAC_ADDRESS  " + mac);
                        sendMSearchInIntervalOfTime();

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
        Log.d("Hotspot-newMac", newMac);
        newMac = newMac.substring(newMac.length() - 6);
        Log.d("Hotspot-", newMac);
        return newMac;
    }


    @Override
    protected void onResume() {
        super.onResume();
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
        mHandler.sendEmptyMessageDelayed(Constants.SEARCHING_FOR_DEVICE, 500);
        mTaskHandlerForSendingMSearch.postDelayed(mMyTaskRunnableForMSearch, MSEARCH_TIMEOUT_SEARCH);
        waitForDiscovery();


    }

    private Runnable mMyTaskRunnableForMSearch = new Runnable() {
        @Override
        public void run() {
            LibreLogger.d(this, "My task is Sending 1 Minute Once M-Search");
            /* do what you need to do */
            if (mBackgroundMSearchStoppedDeviceFound)
                return;
            try {
                LibreMavidHelper.advertise();
            } catch (WrongStepCallException e) {
                e.printStackTrace();
            }
            /* and here comes the "trick" */
            mTaskHandlerForSendingMSearch.postDelayed(this, MSEARCH_TIMEOUT_SEARCH);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LibreLogger.d(this, "Handler Message Got " + msg.toString());


            if (msg.what == Constants.SEARCHING_FOR_DEVICE) {
                if (mHandler.hasMessages(Constants.HTTP_POST_SOMETHINGWRONG)) {
                    mHandler.removeMessages(Constants.HTTP_POST_SOMETHINGWRONG);
                }
//                updateConnectionStatus(getResources().getString(R.string.searchingForDevice));
                mHandler.sendEmptyMessageDelayed(Constants.TIMEOUT_FOR_SEARCHING_DEVICE, 80000);
                LibreLogger.d(this, "suma in connectTowifi searchingfordevice");

            } else if (msg.what == Constants.CONFIGURED_DEVICE_FOUND) {
                mHandler.removeCallbacksAndMessages(null);
                //stopMsearch
                mBackgroundMSearchStoppedDeviceFound = true;
                mTaskHandlerForSendingMSearch.removeCallbacks(mMyTaskRunnableForMSearch);
                /*
                enableNetworkCallbacks();*/
                closeLoader();
                // MavidApplication.isSacConfiguredSuccessfully=true;
                Log.d("HotSpot", "connectTowifi devicefound");

            } else if (msg.what == Constants.HOTSPOT_CONNECTION_TIMEOUT) {
                closeLoader();
                ShowHotSpotFailed();
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
    }

    private void waitForDiscovery() {
        try {
            MavidApplication.setDeviceListener(new DeviceListener() {
                @Override
                public void newDeviceFound(final DeviceInfo deviceInfo) {
                    Log.d("HotSpot+atul", "Newdevice found in after sending hotspot" + deviceInfo.getFriendlyName());

                    if (deviceInfo != null) {
                        if (isThisDeviceLookingFor(deviceInfo)) {
                            if (hotspotTxt.getText() != null) {
                                saveConnectedHotSpotName(new ModelSaveHotSpotName(deviceInfo.getFriendlyName(), hotspotTxt.getText().toString()));
                            }
                            LibreLogger.d(this, "Hurray !! New Device Found and same as Configured Device"
                                    + deviceInfo.getFriendlyName());
                            mSACConfiguredIpAddress = deviceInfo.getIpAddress();
                            {
                                /*changes specific to alexa check*/
                                if (mSACConfiguredIpAddress != null && !mSACConfiguredIpAddress.isEmpty()) {
                                    if (mHandler.hasMessages(Constants.ALEXA_CHECK_TIMEOUT)) {
                                        mHandler.removeMessages(Constants.ALEXA_CHECK_TIMEOUT);
                                    }
                                    mHandler.sendEmptyMessage(Constants.CONFIGURED_DEVICE_FOUND);

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
        if (deviceInfo != null) {
            String deviceName = deviceInfo.getFriendlyName();
            deviceName = deviceName.substring(deviceName.length() - 6);
            Log.d("Hotspot", "deviceName: " + deviceName + " lookFor: " + lookFor);
            if (deviceName.equalsIgnoreCase(lookFor)) {
                return true;
            }
        }
        return false;
    }

    private void ShowHotSpotFailed() {

        if (alert == null) {

            alert = new Dialog(WifiHotSpotCredentialsActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText(getResources().getString(R.string.enableHSFail));

        tv_alert_message.setText(getResources().getString(R.string.enableHSWaitFAil));

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                alert = null;
                startActivity(new Intent(WifiHotSpotCredentialsActivity.this, MavidHomeTabsActivity.class));
                finish();
            }
        });

        alert.show();

    }

    private void ShowHotSpotConnecting() {

        if (alert == null) {

            alert = new Dialog(WifiHotSpotCredentialsActivity.this);

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
                alert.dismiss();
                alert = null;
                showLoader(getResources().getString(R.string.notice), getResources().getString(R.string.waitingHotSpot));
                mHandler.sendEmptyMessageDelayed(Constants.HOTSPOT_CONNECTION_TIMEOUT, 120000);
                final Bundle sacParams = new Bundle();
                sacParams.putString(BundleConstants.SACConstants.SSID
                        , hotspotTxt.getText().toString().trim());
                sacParams.putString(BundleConstants.SACConstants.PASSPHRASE
                        , passphraseTxt.getText().toString());
                sacParams.putInt(BundleConstants.SACConstants.NETWORK_TYPE
                        , security);
                LibreMavidHelper.configure(sacParams, new SACListener() {
                    @Override
                    public void success() {
                        LibreLogger.d(this, "Credentials succesfully posted.");
//                    doNext(sacParams);

                        final Timer timer = new Timer();
                        TimerTask doAsynchronousTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (isHotSpotConnected) {
                                    Log.d("atul_hotspot", "clientList called");
                                    getClientList();
                                } else {
                                    timer.cancel();
                                    //update the old conncected network

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            saveConnectedHotSpotName(new ModelSaveHotSpotName(lookFor, "Personnel Hotspot"));
                                        }
                                    });
                                }
                            }
                        };
                        timer.schedule(doAsynchronousTask, 0, 100); //execute in every 100

                        sendMSearchInIntervalOfTime();
                    }

                    @Override
                    public void failure(String message) {
//                    updateResponseUI(message);
                        LibreLogger.d(this, "Credentials  failed: " + message);
                    }

                    @Override
                    public void successBLE(byte[] b) {

                    }
                });
            }
        });

        alert.show();
    }
}
