package com.libre.irremote.alexa_signin;

import android.app.Dialog;
import android.os.Bundle;

import com.libre.irremote.BLEApproach.BleCommunication;
import com.libre.irremote.BLEApproach.BleWriteInterface;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.libresdk.Constants.BundleConstants;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.Util.LibreLogger;

public class BLEYesAlexaLoginNoHomeScreen extends BaseActivity implements BleWriteInterface {


    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";
    private static final int MAX_PRIORITY = 99999;
    private WifiManager mWifiManager;
    //    private AlertDialog alertConnectingtoNetwork;
    private NetworkInfo wifiCheck;
    private boolean alreadyDialogueShown = false;
    private WifiManager.MulticastLock multicastLock;

    TextView deviceConnectionStatus;
    private String ssid;
    private String password;
    private String security;
    private String lookFor;
    private String mSACConfiguredIpAddress = "", alexaLoginStatus;

    /**
     * Broadcast receiver for connection related events
     */
//    private AlertDialog alert;
    AppCompatButton btnNext, noAlexaLogin;

    private Dialog customDialog;

    private AppCompatImageView iv_home;


    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (ssid == null)
            return;
        if (mWifi.isConnected()) {
            if (getconnectedSSIDname(BLEYesAlexaLoginNoHomeScreen.this).contains(ssid)) {
                LibreLogger.d(this, "suma in connectTowifi wifisucess");
            } else if (!getconnectedSSIDname(BLEYesAlexaLoginNoHomeScreen.this).contains(Constants.WAC_SSID) || !getconnectedSSIDname(BLEYesAlexaLoginNoHomeScreen.this).contains(Constants.WAC_SSID2)
                    && !getconnectedSSIDname(BLEYesAlexaLoginNoHomeScreen.this).contains("<unknown ssid>")) {

                showAlertDialogForClickingWrongNetwork();
                LibreLogger.d(this, "suma in connectTowifi unknownssid");
            }
        } else {
            alertBoxForNetworkOff();
            LibreLogger.d(this, "suma in connectTowifi wrongnetwork");
        }

    }

    private void enableNetworkCallbacks() {
        enableNetworkChangeCallBack();
        enableNetworkOffCallBack();
    }

    private AlertDialog alertDialogNetworkOff;

    public void alertBoxForNetworkOff() {
        if (!BLEYesAlexaLoginNoHomeScreen.this.isFinishing()) {

            if (customDialog == null) {

                customDialog = new Dialog(BLEYesAlexaLoginNoHomeScreen.this);

                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                customDialog.setContentView(R.layout.custom_single_button_layout);

                customDialog.setCancelable(false);

                tv_alert_title = customDialog.findViewById(R.id.tv_alert_title);

                tv_alert_message = customDialog.findViewById(R.id.tv_alert_message);

                btn_ok = customDialog.findViewById(R.id.btn_ok);


            }

            tv_alert_title.setText(R.string.wifiConnectivityStatus);

            tv_alert_message.setText(getString(R.string.connectToWifi));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customDialog.dismiss();

                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 1235);
                    try {
                        LibreMavidHelper.advertiseWithIp(mSACConfiguredIpAddress,BLEYesAlexaLoginNoHomeScreen.this);
                    } catch (WrongStepCallException e) {
                        e.printStackTrace();
                    }
                }
            });

            customDialog.show();

        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {
            LibreLogger.d(this, "came back from wifi list");
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!mWifi.isConnected()) {
                alertBoxForNetworkOff();
            } else {
                restartApp(getApplicationContext());
            }
        } else if (requestCode == 1235) {
            restartApp(getApplicationContext());
        }
    }


    private void showAlertDialogForClickingWrongNetwork() {
        if (!BLEYesAlexaLoginNoHomeScreen.this.isFinishing()) {

            if (customDialog == null) {

                customDialog = new Dialog(BLEYesAlexaLoginNoHomeScreen.this);

                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                customDialog.setContentView(R.layout.custom_single_button_layout);

                customDialog.setCancelable(false);

                tv_alert_title = customDialog.findViewById(R.id.tv_alert_title);

                tv_alert_message = customDialog.findViewById(R.id.tv_alert_message);

                btn_ok = customDialog.findViewById(R.id.btn_ok);


            }

            String Message = String.format(getString(R.string.newrestartApp),
                    getconnectedSSIDname(BLEYesAlexaLoginNoHomeScreen.this), ssid);

            tv_alert_title.setText("");

            tv_alert_message.setText(Message);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customDialog.dismiss();
                    customDialog = null;
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 1234);
                }
            });
            if (!BLEYesAlexaLoginNoHomeScreen.this.isFinishing()) {
                customDialog.show();
            }


        }
    }

    @Override
    public void onBackPressed() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yes_alexa_login_no_home_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // getSACPARAMS();
        mSACConfiguredIpAddress = getIntent().getStringExtra("IPADDRESS");
        alexaLoginStatus = getIntent().getStringExtra(Constants.INTENTS.ALEXA_LOGIN_STATUS);
        LibreLogger.d(this, "suma in hotspot connection suma in alexa login\n" + alexaLoginStatus);
        ssid = getIntent().getStringExtra(BundleConstants.SACConstants.SSID);
        LibreLogger.d(this, "suma in hotspot connection suma in getting ssid connected\n" + ssid);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initViews();
        enableAlexaLogin();
        try {
            WifiManager m_wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (m_wifiManager != null) {
                multicastLock = m_wifiManager.createMulticastLock("multicastLock");
            }
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
        } catch (Exception e) {
            multicastLock = null;
        }

    }

    private void initViews() {
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleCommunication bleCommunication = new BleCommunication(BLEYesAlexaLoginNoHomeScreen.this);
                BleCommunication.writeInteractorStopSac();
                LibreLogger.d(this,"suma in getting stop sac activity mavid ble  yes alexa no home screen next btn");

                checkForAlexaToken(mSACConfiguredIpAddress);
            }
        });
        iv_home = findViewById(R.id.iv_home);

        iv_home.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                LibreLogger.d(this, "user pressed home button ");
                startActivity(new Intent(BLEYesAlexaLoginNoHomeScreen.this, MavidHomeTabsActivity.class));
                finish();
            }
        });

        noAlexaLogin = findViewById(R.id.noalexalogin);
        noAlexaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(BLEYesAlexaLoginNoHomeScreen.this, MavidHomeTabsActivity.class));
                LibreLogger.d(this, " No Alexa Login Sending StopSac ");
            }
        });

    }

    private void disbleAlexaLogin() {
        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);
    }

    private void enableAlexaLogin() {
        btnNext.setEnabled(true);
        btnNext.setAlpha(1f);
    }


    private void checkForAlexaToken(String mSACConfiguredIpAddress) {
        LibreMavidHelper.askRefreshToken(mSACConfiguredIpAddress, new CommandStatusListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                String messages = messageInfo.getMessage();
                Log.d("DeviceManager", " got alexa token " + messages);
                handleAlexaRefreshTokenStatus(messageInfo.getIpAddressOfSender(), messageInfo.getMessage());
            }

            @Override
            public void failure(Exception e) {
                Log.d("alexa",e.getMessage());
            }

            @Override
            public void success() {

            }
        });
    }

    private void handleAlexaRefreshTokenStatus(String current_ipaddress, String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty() && alexaLoginStatus != null && alexaLoginStatus.contains("Yes")) {
            /*not logged in*/
            Intent i = new Intent(BLEYesAlexaLoginNoHomeScreen.this, AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("speakerIpaddress", current_ipaddress);
            i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(i);
            finish();
        } else {
            Intent newIntent = new Intent(BLEYesAlexaLoginNoHomeScreen.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("speakerIpaddress", current_ipaddress);
            newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(newIntent);
            finish();
        }
    }

    public boolean isThisDeviceLookingFor(DeviceInfo deviceInfo) {
        if (deviceInfo != null) {
            String deviceName = deviceInfo.getFriendlyName();
            deviceName = deviceName.substring(deviceName.length() - 5);
            if (deviceName.equalsIgnoreCase(lookFor)) {
                return true;
            }
        }
        return false;
    }

    public void getSACPARAMS() {
        if (getIntent().hasExtra(Constants.INTENTS.SAC_PARAMS)) {
            Bundle bundle = getIntent().getBundleExtra(Constants.INTENTS.SAC_PARAMS);
            ssid = bundle.getString(BundleConstants.SACConstants.SSID);
            LibreLogger.d(this, "suma in Alexa Login Screen getting SSID\n" + ssid);
            password = bundle.getString(BundleConstants.SACConstants.PASSPHRASE);
            int sec = bundle.getInt(BundleConstants.SACConstants.NETWORK_TYPE);
            if (sec == 0) {
                security = OPEN;
            } else {
                security = PSK;
            }
            lookFor = bundle.getString(Constants.INTENTS.MACADDRESS);
        }
    }

    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onWriteFailure() {

    }
}