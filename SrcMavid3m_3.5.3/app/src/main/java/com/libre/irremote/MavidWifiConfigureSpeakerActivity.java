package com.libre.irremote;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.danimahardhika.cafebar.CafeBar;
import com.google.android.material.textview.MaterialTextView;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.OtherSetupApproach.BLEHotSpotCredentialsActivity;
import com.libre.irremote.alexa_signin.AlexaSignInActivity;
import com.libre.irremote.alexa_signin.AlexaThingsToTryDoneActivity;

import com.libre.irremote.R;
import com.libre.libresdk.Constants.BundleConstants;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.Discovery.Listeners.DeviceListener;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.Util.LibreLogger;

import java.util.Iterator;
import java.util.List;

import static com.libre.irremote.Constants.Constants.CONFIGURED_DEVICE_FOUND;
import static com.libre.irremote.Constants.Constants.SEARCHING_FOR_DEVICE;
import static java.lang.Thread.MAX_PRIORITY;

public class MavidWifiConfigureSpeakerActivity extends BaseActivity {

    private NetworkInfo wifiCheck;

    private Dialog alertWrongNetwork;

    private WifiManager mWifiManager;

    private String ssid, currentWifiSacParam;
    private String password;
    private String security;
    private String newMacAddress;
    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";

    boolean isConnectedStatusShown = false;
    String ipApaddress;
    private Dialog alert, wrongSSidAlert;

    private boolean alreadyDialogueShown = false;

    CafeBar cafeBar;

    private ConnectionReceiver connectionReceiver;

    boolean configuredeDeviceFound = false;

    boolean isAutoNetworkChangeShownOnce = false, callWaitForDiscoveryOnce = false;





//    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mavid_configure_speaker_activity);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        disableNetworkChangeCallBack();
        disableNetworkOffCallBack();
        getSACPARAMS();
        waitForDiscovery();


//        countDownTimer = new CountDownTimer(45000, 1000) {
//
//            public void onTick(long millisUntilFinished) {
//                //here you can have your logic to set text to edittext
//                Log.d("atul_countdown", String.valueOf(millisUntilFinished));
//            }
//
//            public void onFinish() {
//                Log.d("atul_countdown", "done");
//                //show a message
//                //configuration sucessful
////                showConfigurationSucessfulMessage();
//
//            }
//
//        }.start();
    }


    public void ShowAlertDynamicallyGoingWrongSSID(String title, String message, final AppCompatActivity appCompatActivity) {


        if (wrongSSidAlert == null) {


            wrongSSidAlert = new Dialog(appCompatActivity);

            wrongSSidAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            wrongSSidAlert.setContentView(R.layout.custom_single_button_layout);

            wrongSSidAlert.setCancelable(false);

            tv_alert_title = wrongSSidAlert.findViewById(R.id.tv_alert_title);

            tv_alert_message = wrongSSidAlert.findViewById(R.id.tv_alert_message);

            btn_ok = wrongSSidAlert.findViewById(R.id.btn_ok);


        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissWrongSSidAlerDialog();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                MavidApplication.clickedWrongNetwork = true;
                refreshDiscovery();
            }
        });

        if (!appCompatActivity.isFinishing()) {
            wrongSSidAlert.show();
        }
    }

    public void showConfigurationSucessfulMessage() {
        if (!MavidWifiConfigureSpeakerActivity.this.isFinishing()) {

            dismissWrongSSidAlerDialog();

            alert = new Dialog(MavidWifiConfigureSpeakerActivity.this);

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
                    Intent newIntent = new Intent(MavidWifiConfigureSpeakerActivity.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.putExtra("speakerIpaddress", MavidApplication.currentDeviceIP);
                    LibreLogger.d(this, "suma in get ipaddress SIGNIN_IP" + MavidApplication.currentDeviceIP);
                    newIntent.putExtra("fromActivity", MavidWifiConfigureSpeakerActivity.class.getSimpleName());
                    startActivity(newIntent);
                    finish();

                }
            });
            alert.show();
        }

    }

    public void getSACPARAMS() {
        if (getIntent().hasExtra(Constants.INTENTS.SAC_PARAMS)) {
            Bundle bundle = getIntent().getBundleExtra(Constants.INTENTS.SAC_PARAMS);
            ssid = bundle.getString(BundleConstants.SACConstants.SSID);
            password = bundle.getString(BundleConstants.SACConstants.PASSPHRASE);
            LibreLogger.d(this, "SUMA in OTHER SETUP OPTION SSID" + ssid + "PASSWORD\n" + password);
            int sec = bundle.getInt(BundleConstants.SACConstants.NETWORK_TYPE);
            if (sec == 0) {
                security = OPEN;
            } else {
                security = PSK;
            }
            newMacAddress = bundle.getString(Constants.INTENTS.MACADDRESS);
            Log.d("atul_new_mac_address", newMacAddress);
        }
    }

    private void waitForDiscovery() {
        try {
            MavidApplication.setDeviceListener(new DeviceListener() {
                @Override
                public void newDeviceFound(final DeviceInfo deviceInfo) {
                    LibreLogger.d(this, "suma in newdevice found connect to wifi activity" + deviceInfo.getFriendlyName());
                    MavidApplication.currentDeviceIP = deviceInfo.getIpAddress();
                    if (isThisDeviceLookingFor(deviceInfo) && !configuredeDeviceFound) {
                        mSACConfiguredIpAddress = deviceInfo.getIpAddress();
                        MavidApplication.mCurrentDeviceAddress = mSACConfiguredIpAddress;
                        MavidApplication.currentDeviceIP = mSACConfiguredIpAddress;
                        Log.v("StoringDeviceIp", "Storing MavidWifi " + mSACConfiguredIpAddress);
                        LibreLogger.d(this, "Hurray !! New Device Found and same as Configured Device"
                                + deviceInfo.getFriendlyName() + "mSacipAddress " + mSACConfiguredIpAddress + "deviceInfoIpAddress: " + deviceInfo.getIpAddress());
                        {
                            /*changes specific to alexa check*/
                            if (mSACConfiguredIpAddress != null && !mSACConfiguredIpAddress.isEmpty()) {
                                if (mHandler.hasMessages(Constants.ALEXA_CHECK_TIMEOUT)) {
                                    mHandler.removeMessages(Constants.ALEXA_CHECK_TIMEOUT);
                                }

//                                    Intent intent = new Intent(MavidWifiConfigureSpeakerActivity.this,WifiYesAlexaLoginNoHomeScreen.class);
//                                    Bundle bundle = new Bundle();
//                                    bundle.putString("mSACConfiguredIpAddress",mSACConfiguredIpAddress);
//                                    intent.putExtras(bundle);
//                                    startActivity(intent);
//                                    finish();

                                if (isAutoNetworkChangeShownOnce && alert != null) {
                                    alert.dismiss();
                                }

                                checkForAlexaToken(mSACConfiguredIpAddress);
                                configuredeDeviceFound = true;

                                mHandler.removeMessages(CONFIGURED_DEVICE_FOUND);
                            }
                        }
                    } else {
                        if (!callWaitForDiscoveryOnce) {
                            Log.d("atul_wait_for_dis", "configured device found");
                            mHandler.sendEmptyMessage(CONFIGURED_DEVICE_FOUND);
                            callWaitForDiscoveryOnce = true;
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

    private void checkForAlexaToken(String mSACConfiguredIpAddress) {
//        showLoader("Please Wait", "Fetching Alexa Token");
        LibreMavidHelper.askRefreshToken(mSACConfiguredIpAddress, new CommandStatusListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                closeLoader();
                String messages = messageInfo.getMessage();
                Log.d("DeviceManager", " got alexa token " + messages);
                handleAlexaRefreshTokenStatus(messageInfo.getIpAddressOfSender(), messageInfo.getMessage());
            }

            @Override
            public void failure(Exception e) {
                closeLoader();
                Log.d("alexa_token", e.toString());
            }

            @Override
            public void success() {
                closeLoader();
            }
        });
    }

    private void handleAlexaRefreshTokenStatus(String current_ipaddress, String refreshToken) {
//        countDownTimer.cancel();
        if (refreshToken != null && !refreshToken.isEmpty()) {
            /*not logged in*/
            Intent i = new Intent(MavidWifiConfigureSpeakerActivity.this, AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("speakerIpaddress", current_ipaddress);
            i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(i);
            finish();
        } else {
            Intent newIntent = new Intent(MavidWifiConfigureSpeakerActivity.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("speakerIpaddress", current_ipaddress);
            newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(newIntent);
            finish();
        }
    }

    private String mSACConfiguredIpAddress = "";
    @SuppressLint("HandlerLeak")
    private final
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LibreLogger.d(this, "Handler Message Got " + msg.toString());

            if (msg.what == Constants.HTTP_POST_SOMETHINGWRONG) {
                ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (!wifiCheck.isConnected()) {
                    mHandler.removeCallbacksAndMessages(null);
                    LibreLogger.d(this, "hang - 35sec timeout is over. showing error");
                    showAlertDialogForConnectingToWifi();
                }
                LibreLogger.d(this, "suma in connectTowifi http somethingwentwrong");

            } else if (msg.what == Constants.HTTP_POST_DONE_SUCCESSFULLY) {
                mWifiManager.disconnect();
//                updateConnectionStatus(getResources().getString(R.string.connectingToWifi));
                connectToSpecificNetwork(ssid, password, security);
                LibreLogger.d(this, "suma in connectTowifi http done successfully");
            } else if (msg.what == Constants.CONNECTED_TO_MAIN_SSID_SUCCESS) {
                /* Removing Failed Callback */
                mHandler.removeMessages(Constants.CONNECTED_TO_MAIN_SSID_FAIL);

                if (getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this).equalsIgnoreCase(ssid)) {

                    LibreLogger.d(this, "Connected To Main SSID " + ssid);
//                    alertForNetworkChange(MavidWifiConfigureSpeakerActivity.this);
                    if (!isConnectedStatusShown) {
                        isConnectedStatusShown = true;
                        updateConnectionStatus(getResources().getString(R.string.connectedTo) + " " + ssid);
                    }
                    sendMSearchInIntervalOfTime();
                    LibreLogger.d(this, "suma in connectTowifi msearch interval of time");
                    waitForDiscovery();

                } else {
                    if (!isAutoNetworkChangeShownOnce) {
                        showAlertDialogForClickingWrongNetwork();
                        isAutoNetworkChangeShownOnce = true;
                    }

                }
            } else if (msg.what == Constants.HTTP_POST_FAILED) {
                buildSnackBar(getString(R.string.httpPostFailed));
//                Toast.makeText(MavidWifiConfigureSpeakerActivity.this, getString(R.string.httpPostFailed), Toast.LENGTH_LONG).show();
                LibreLogger.d(this, "suma in connectTowifi http post failed");


            } else if (msg.what == Constants.SEARCHING_FOR_DEVICE) {
                if (mHandler.hasMessages(Constants.HTTP_POST_SOMETHINGWRONG)) {
                    mHandler.removeMessages(Constants.HTTP_POST_SOMETHINGWRONG);
                }
//                updateConnectionStatus(getResources().getString(R.string.searchingForDevice));
                mHandler.sendEmptyMessageDelayed(Constants.TIMEOUT_FOR_SEARCHING_DEVICE, 70000);
                LibreLogger.d(this, "suma in connectTowifi searchingfordevice");


            } else if (msg.what == Constants.TIMEOUT_FOR_SEARCHING_DEVICE) {
                showDialogifDeviceNotFound(getString(R.string.noDeviceFound));
                LibreLogger.d(this, "suma in connectTowifi nodevicefound");
                //MavidApplication.isSacConfiguredSuccessfully=false;
                /*Call the Activity To PlayNewScreen */
                //onBackPressed();

            } else if (msg.what == CONFIGURED_DEVICE_FOUND) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.removeMessages(CONFIGURED_DEVICE_FOUND);

                //stopMsearch
                mBackgroundMSearchStoppedDeviceFound = true;
                mTaskHandlerForSendingMSearch.removeCallbacks(mMyTaskRunnableForMSearch);
                Log.d("atul", "Device Configured");
//                updateConnectionStatus(getResources().getString(R.string.deviceConnected));
//                deviceConnectionStatus.setVisibility(View.VISIBLE);
//                enableAlexaLogin();
                /*
                enableNetworkCallbacks();*/
                closeLoader();
                showConfigurationSucessfulMessage();

                LibreLogger.d(this, "suma in connectTowifi devicefound");

            } else if (msg.what == Constants.CONNECTED_TO_DIFFERENT_SSID) {
//                if (!isAutoNetworkChangeShownOnce) {
//                    showAlertDialogForClickingWrongNetwork();
//                    isAutoNetworkChangeShownOnce = true;
//                }
                LibreLogger.d(this, "suma in connectTowifi wrongnw: id " + getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this));

                if (!ssid.equals(getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this))) {
                    ShowAlertDynamicallyGoingWrongSSID("", getResources().getString(R.string.title_error_connection) + "( " + getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this) + ")" +
                            " \n" + getString(R.string.title_error_connection2) + "(" + ssid + ")" + "\n" + getString(R.string.title_error_connection3) + "( " + ssid + ")", MavidWifiConfigureSpeakerActivity.this);
                    LibreLogger.d(this, "Wifi Configure\n" + ssid + "mobile connected SSID\n" + getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this));


                }

            } else {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {
                    if (getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this).contains(ssid)) {
                        mHandler.sendEmptyMessage(Constants.CONNECTED_TO_MAIN_SSID_SUCCESS);
                        LibreLogger.d(this, "suma in connectTowifi wifisucess");

                    } else if (!getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this).contains(Constants.WAC_SSID)
                            || !getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this).contains(Constants.WAC_SSID2)
                            && !getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this).contains("<unknown ssid>")) {

//                        if (!isAutoNetworkChangeShownOnce) {
//                            showAlertDialogForConnectedToWrongNetwork();
//                        }
                        LibreLogger.d(this, "suma in connectTowifi unknownssid");

                    }
                } else {
                    alertBoxForNetworkOff();
                    LibreLogger.d(this, "suma in connectTowifi wrongnetwork");
                }

            }

        }
    };


    private void showAlertDialogForConnectedToWrongNetwork() {
        if (!MavidWifiConfigureSpeakerActivity.this.isFinishing()) {

            alertWrongNetwork = null;


            alertWrongNetwork = new Dialog(MavidWifiConfigureSpeakerActivity.this);

            isAutoNetworkChangeShownOnce = true;

            alertWrongNetwork.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alertWrongNetwork.setContentView(R.layout.custom_single_button_layout);

            alertWrongNetwork.setCancelable(false);


            tv_alert_title = alertWrongNetwork.findViewById(R.id.tv_alert_title);

            tv_alert_message = alertWrongNetwork.findViewById(R.id.tv_alert_message);

            btn_ok = alertWrongNetwork.findViewById(R.id.btn_ok);

            String Message = String.format(getString(R.string.newrestartApp),
                    getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this), ssid);
            tv_alert_title.setText("");

            tv_alert_message.setText(Message);

            btn_ok.setText(getString(R.string.gotoSettings));


            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertWrongNetwork.dismiss();
                    alertWrongNetwork = null;
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 1234);
                }
            });

            alertWrongNetwork.show();

        }
    }

    public boolean isThisDeviceLookingFor(DeviceInfo deviceInfo) {
        if (deviceInfo != null) {
            String deviceName = deviceInfo.getFriendlyName();
            deviceName = deviceName.substring(deviceName.length() - 5);
            if (deviceName.equalsIgnoreCase(newMacAddress)) {
                Log.d("atul_looking_for", newMacAddress);
                return true;
            }
        }
        return false;
    }

    private class ConnectionReceiver extends BroadcastReceiver {

        private final String mNetworkSsidToConnect;

        ConnectionReceiver(String ssid) {
            mNetworkSsidToConnect = ssid;
        }

        @Override

        public void onReceive(Context context, Intent intent) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            LibreLogger.d(this, "OnRecieve Method: Wifi Info " + wifiInfo.getSSID());
            LibreLogger.d(this, "OnRecieve Method: mNetworkSSIDToConnect " + mNetworkSsidToConnect);
            LibreLogger.d(this, "OnRecieve Method: Network State " + networkInfo.getState());
            LibreLogger.d(this, "OnRecieve Method: Network Detailed State " + networkInfo.getDetailedState());
            LibreLogger.d(this, "Supplicant State " + wifiInfo.getSupplicantState());
            if (wifiInfo.getSupplicantState() == SupplicantState.INACTIVE) {
                if (mHandler.hasMessages(Constants.CONNECTED_TO_MAIN_SSID_FAIL))
                    mHandler.removeMessages(Constants.CONNECTED_TO_MAIN_SSID_FAIL);
                mHandler.sendEmptyMessageDelayed(Constants.CONNECTED_TO_MAIN_SSID_FAIL, 60000);
            }
            boolean mVariable;
            if ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)) {
                mVariable = true;
            } else {
                mVariable = networkInfo.isConnected();
            }

            /*if (networkInfo != null && (networkInfo.isConnected())) */
            if (mVariable) {
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    LibreLogger.d(this, " suma in  OnRecieve Wifi Info " + wifiInfo.getSSID());
                    LibreLogger.d(this, " suma in  OnRecieve mNetworkSSIDToConnect " + mNetworkSsidToConnect);
                    LibreLogger.d(this, " suma in OnRecieve Network State " + networkInfo.getState());
                    if (wifiInfo.getSSID().contains(mNetworkSsidToConnect)) {
                        dismissWrongSSidAlerDialog();
                        LibreLogger.d(this, " suma in OnRecieve dismissed wrong ssid");
                        mHandler.sendEmptyMessageDelayed(Constants.CONNECTED_TO_MAIN_SSID_SUCCESS, 500);
                    } else if (!wifiInfo.getSSID().contains(Constants.WAC_SSID5) || !wifiInfo.getSSID().contains(Constants.WAC_SSID2)
                            && !wifiInfo.getSSID().contains("<unknown ssid>")) {
                        if (mHandler.hasMessages(Constants.CONNECTED_TO_MAIN_SSID_FAIL))
                            mHandler.removeMessages(Constants.CONNECTED_TO_MAIN_SSID_FAIL);
                        LibreLogger.d(this, "suma in OnRecieve goto diff ssdi: n.w_state: " + networkInfo.getState() + "ssidName: " + wifiInfo.getSSID());
                        mHandler.sendEmptyMessage(Constants.CONNECTED_TO_DIFFERENT_SSID);
                    }
                }
            }
        }
    }


    public void dismissWrongSSidAlerDialog() {
        if (wrongSSidAlert != null) {
            if (wrongSSidAlert.isShowing()) {
                wrongSSidAlert.dismiss();
                wrongSSidAlert = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (mTaskHandlerForSendingMSearch.hasMessages(MSEARCH_TIMEOUT_SEARCH)) {
                mTaskHandlerForSendingMSearch.removeMessages(MSEARCH_TIMEOUT_SEARCH);
            }
            mTaskHandlerForSendingMSearch.removeCallbacks(mMyTaskRunnableForMSearch);
            unregisterReceiver(connectionReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        disableNetworkChangeCallBack();
        disableNetworkOffCallBack();
        String mConnectedSsidName = getconnectedSSIDname(this);
        if (mConnectedSsidName.equalsIgnoreCase(ssid)) {
            LibreLogger.d(this, "onStartComplete MainSSIDSuccess ");
            mHandler.sendEmptyMessage(Constants.CONNECTED_TO_MAIN_SSID_SUCCESS);
        } else {
            mHandler.sendEmptyMessage(Constants.HTTP_POST_DONE_SUCCESSFULLY);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MavidWifiConfigureSpeakerActivity.this, MavidHomeTabsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialogifDeviceNotFound(final String Message) {
        if (!MavidWifiConfigureSpeakerActivity.this.isFinishing()) {

            alreadyDialogueShown = true;

            alert = new Dialog(MavidWifiConfigureSpeakerActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);


            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);


            tv_alert_title.setText("");

            tv_alert_message.setText(Message);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    alert = null;
                    if (Message.contains(getString(R.string.noDeviceFound))) {
                        Intent ssid = new Intent(MavidWifiConfigureSpeakerActivity.this,
                                MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(ssid);
                        finish();
                        return;
                    }
                }
            });

            if (alert != null) {
                if (!alert.isShowing()) {
                    alert.show();
                }
            }
        }
    }


    private int shiftPriorityAndSave(final WifiManager wifiMgr) {
        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        final int size = configurations.size();
        for (int i = 0; i < size; i++) {
            final WifiConfiguration config = configurations.get(i);
            config.priority = i;
            wifiMgr.updateNetwork(config);
        }
        wifiMgr.saveConfiguration();
        return size;
    }


    private void showAlertDialogForClickingWrongNetwork() {
        if (!MavidWifiConfigureSpeakerActivity.this.isFinishing()) {


            alert = new Dialog(MavidWifiConfigureSpeakerActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);


            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);

            String Message = String.format(getString(R.string.newrestartApp),
                    getconnectedSSIDname(MavidWifiConfigureSpeakerActivity.this), ssid);
            tv_alert_title.setText("");

            tv_alert_message.setText(Message);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    alert = null;
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 1234);
                }
            });

            alert.show();

        }
    }

    private int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        for (final WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }


    /* Converting a String to Quoted String
     * Syntax for Setting SSID in wifi Conf : / SSIDNAME /*/
    private String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        final int lastPos = string.length() - 1;
        if (lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }
        return "\"" + string + "\"";
    }


    public void sendMSearchInIntervalOfTime() {
        mHandler.sendEmptyMessageDelayed(Constants.SEARCHING_FOR_DEVICE, 500);
        mTaskHandlerForSendingMSearch.postDelayed(mMyTaskRunnableForMSearch, MSEARCH_TIMEOUT_SEARCH);

    }

    private final int MSEARCH_TIMEOUT_SEARCH = 2000;
    private Handler mTaskHandlerForSendingMSearch = new Handler();
    public boolean mBackgroundMSearchStoppedDeviceFound = false;
    private Runnable mMyTaskRunnableForMSearch = new Runnable() {
        @Override
        public void run() {
//            LibreLogger.d(this, "My task is Sending 1 Minute Once M-Search");
            /* do what you need to do */
            if (mBackgroundMSearchStoppedDeviceFound)
                return;

            try {
                LibreMavidHelper.advertise();
                LibreLogger.d(this, "suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery mavid wifi");
                LibreLogger.d(this, "My task is Sending 1 Minute Once M-Search");
            } catch (WrongStepCallException e) {
                e.printStackTrace();
            }
            /* and here comes the "trick" */
            mTaskHandlerForSendingMSearch.postDelayed(this, MSEARCH_TIMEOUT_SEARCH);
        }
    };

    private void showAlertDialogForConnectingToWifi() {

        if (!MavidWifiConfigureSpeakerActivity.this.isFinishing()) {

            alertWrongNetwork = null;

            alertWrongNetwork = new Dialog(MavidWifiConfigureSpeakerActivity.this);


            alertWrongNetwork.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alertWrongNetwork.setContentView(R.layout.custom_single_button_layout);

            alertWrongNetwork.setCancelable(false);


            tv_alert_title = alertWrongNetwork.findViewById(R.id.tv_alert_title);

            tv_alert_message = alertWrongNetwork.findViewById(R.id.tv_alert_message);

            btn_ok = alertWrongNetwork.findViewById(R.id.btn_ok);


            tv_alert_title.setText("");
            tv_alert_message.setText(getString(R.string.noNetwork));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertWrongNetwork.dismiss();
                    alertWrongNetwork = null;
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 1234);

                }
            });

            alertWrongNetwork.show();

        }
    }

    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(MavidWifiConfigureSpeakerActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);


        cafeBar.show();
    }

    private void updateConnectionStatus(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //add a snackbar
//                deviceConnectionStatus.setText(s);
                Log.d("atul_wifi_alexa_login", s);
                buildSnackBar(s);

            }
        });
    }

    private String getScanResultSecurity(String mSecurity) {
        final String[] securityModes = {WEP
                , PSK};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (mSecurity.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return OPEN;
    }


    /**
     * Start to connect to a specific wifi network
     */
    private void connectToSpecificNetwork(final String mNetworkSsidToConnect, final String mNetworkPassKeyToConnect, final String mNetworkSecurityToConnect) {


        new Thread(new Runnable() {

            @Override

            public void run() {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (networkInfo.isConnected() && wifiInfo.getSSID().replace("\"", "").equals(mNetworkSsidToConnect)) {
                    mHandler.sendEmptyMessage(Constants.CONNECTED_TO_MAIN_SSID_SUCCESS);
                    return;
                }
                final WifiConfiguration conf = new WifiConfiguration();
                conf.allowedAuthAlgorithms.clear();
                conf.allowedGroupCiphers.clear();
                conf.allowedPairwiseCiphers.clear();
                conf.allowedProtocols.clear();

                conf.allowedKeyManagement.clear();

                LibreLogger.d(this, "Nework Security  Scan Resut To Connect From Function " + getScanResultSecurity(mNetworkSecurityToConnect));
                LibreLogger.d(this, "Network Security To Connect " + mNetworkSecurityToConnect);
                conf.SSID = convertToQuotedString(mNetworkSsidToConnect);  //convertToQuotedString(mScanResult.SSID);
                // Make it the highest priority.
                int newPri = getMaxPriority(mWifiManager) + 1;
                if (newPri > MAX_PRIORITY) {
                    newPri = shiftPriorityAndSave(mWifiManager);
                }
                LibreLogger.d(this, "Network Security To Connect " + mNetworkSecurityToConnect +
                        "priority Generated is " + newPri);
                conf.status = WifiConfiguration.Status.ENABLED;
                conf.priority = newPri;
                switch (getScanResultSecurity(mNetworkSecurityToConnect)) {
                    case WEP:
                        conf.wepKeys[0] = "\"" + mNetworkPassKeyToConnect + "\"";
                        conf.wepTxKeyIndex = 0;
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        break;
                    case PSK:
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                        conf.preSharedKey = "\"" + mNetworkPassKeyToConnect + "\"";
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
                        break;
                    case OPEN:
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        break;
                }

                int netId = mWifiManager.addNetwork(conf);
                LibreLogger.d(this, "Netid We got For the Ssid is " + mNetworkSsidToConnect +
                        "and Netid is " + netId);
                if (netId == -1) {
                    LibreLogger.d(this, "Failed to set the settings for  " + mNetworkSsidToConnect);

                    final List<WifiConfiguration> mWifiConfiguration = mWifiManager.getConfiguredNetworks();
                    for (int i = 0; i < mWifiConfiguration.size(); i++) {
                        String configSSID = mWifiConfiguration.get(i).SSID;
                        LibreLogger.d(this, "Config SSID" + configSSID + "Active SSID" + conf.SSID);
                        if (configSSID.equals(conf.SSID)) {
                            netId = mWifiConfiguration.get(i).networkId;
                            LibreLogger.d(this, "network id" + netId);
                            break;
                        }
                    }
                }
                connectionReceiver = new MavidWifiConfigureSpeakerActivity.ConnectionReceiver(mNetworkSsidToConnect);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                intentFilter.addAction("android.net.wifi.STATE_CHANGE");
                intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                registerReceiver(connectionReceiver, intentFilter);
                mHandler.sendEmptyMessageDelayed(Constants.CONNECTED_TO_MAIN_SSID_FAIL, 120000);
                //disabling other networks
                List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
                Iterator<WifiConfiguration> iterator = networks.iterator();

                while (iterator.hasNext()) {
                    WifiConfiguration wifiConfig = iterator.next();
                    if (wifiConfig.SSID.equals(mNetworkSsidToConnect))
                        netId = wifiConfig.networkId;
                    else
                        mWifiManager.disableNetwork(wifiConfig.networkId);

                }

                boolean mCatchDisconnect = mWifiManager.enableNetwork(netId, true);
                mHandler.sendEmptyMessageDelayed(Constants.HTTP_POST_SOMETHINGWRONG, 30000);
                LibreLogger.d(this, "Wifi MAnager calling enableNetwork" + mCatchDisconnect);
            }

        }).start();

    }

}
