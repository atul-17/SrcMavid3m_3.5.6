package com.libre.irremote.BLE_SAC;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.cafebar.CafeBar;
import com.google.android.material.textfield.TextInputLayout;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.SAC.WifiSecurityConfigurationItemClickInterface;
import com.libre.irremote.adapters.WifiListBottomSheetAdapter;
import com.libre.irremote.BLEApproach.BLEEvntBus;
import com.libre.irremote.BLEApproach.BleCommunication;
import com.libre.irremote.BLEApproach.BleReadInterface;
import com.libre.irremote.BLEApproach.BleWriteInterface;
import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidBLEConfigureSpeakerActivity;
import com.libre.irremote.R;
import com.libre.irremote.SAC.BLEConnectToWifiActivity;
import com.libre.irremote.SAC.WifiYesAlexaLoginNoHomeScreen;
import com.libre.irremote.SAC.WifiConfigurationItemClickInterface;
import com.libre.irremote.adapters.WifiListBottomSheetAdapterForSecurityType;
import com.libre.libresdk.Constants.BundleConstants;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListener;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.models.ModelWifiScanList;
import com.libre.irremote.models.ScanListData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.v;
import static com.libre.irremote.Constants.Constants.WIFI_CONNECTING_NOTIFICATION;

public class BLEConfigureActivity extends BaseActivity implements View.OnClickListener,
        BleReadInterface, BleWriteInterface, WifiConfigurationItemClickInterface, WifiSecurityConfigurationItemClickInterface, WifiScanListInterface {
    boolean isModified = false;
    //AdapterView.OnItemSelectedListener,
    TextInputLayout textInputPasswordLayout;
    TextView wrongPwd_text;
    private TextInputEditText textInputWifiPassword;
    String[] securityArray = {"NONE", "WPA2-PSK", "WPA/WPA2"};
    //    private Spinner ssidSpinner;
    private Button btn_next;
    private static final int REQUEST_ENABLE_BT = 1;
    public BluetoothAdapter mBluetoothAdapter;
    private String passPhrase, SSID = "";
    private String SSIDValue;
    private int security;
    boolean alreadyExecuted = false, alreadyresponse4Reached;
    Map<String, String> scanListMap = new TreeMap<>();
    private ArrayList<String> scanList = new ArrayList<>();
    //    LinearLayout passLyt;
    private final int GET_SCAN_LIST_HANDLER = 0x200;
    private final int CONNECT_TO_SAC = 0x300;
    ArrayAdapter securityAdapter;
    static boolean isActivityActive = false;
    private ImageView back;
    int count = 0, alexacount = 0;
    String configuratonfailedStringBle;
    private String mSACConfiguredIpAddress = "", getAlexaLoginStatus = "";
    int configuratonfailedIndexBle;
    String sacBleConnecting, finalConnectedArray, finalConnectedString, FilteredfinalIpAddress, filteredAlexaLogin;
    int sacBleConnectingIndex, finalConnectedIndex, finalIpIndez, alexaLoginIndex;

    BottomSheetDialog bottomSheetDialog;
    BottomSheetDialog bottomSheetDialogForSecurity;

    AppCompatImageView iv_down_arrow;
    AppCompatImageView iv_down_arrow_security;

    AppCompatTextView tvSelectedWifi;
    AppCompatTextView tvSelectedWifiSecurity;

    private boolean showScanListFromWifiManager = false;

    AppCompatButton btn_cancel;

    List<ModelWifiScanList> modelWifiScanList = new ArrayList<>();

    CafeBar cafeBar;

    private Handler setupHandler;
    private Runnable setupRunnable;
    private int setUpTimeOut;

    WifiListBottomSheetAdapter wifiListBottomSheetAdapter;
    WifiListBottomSheetAdapterForSecurityType wifiListBottomSheetAdapterForSecurityType;

    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

//    SwipeRefreshLayout swipe_refresh;

    TextView tv_no_data;

    RecyclerView rv_wifi_list;

    FloatingTextButton fab_refresh;

    boolean isSacTimedOutCalled = false;
    CheckBox rememCheckBox;
    private boolean isSecurityTypeSelected = false;

    TextInputEditText tvSelectedWifiOther;

    private LinearLayout linearLayoutOthers;

    WifiReceiver receiverWifi;
    WifiManager wifiManager;

    private ArrayList<ScanListData> scanListDataRecievedFromWifiManager;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case GET_SCAN_LIST_HANDLER:
                    closeLoader();
                    handleScandlistExceptionMethod2();
//                    handler.removeMessages(GET_SCAN_LIST_HANDLER);
//                    scanListFailed("Scan List", "Fetching scan list failed.Please try again",
//                            BLEConfigureActivity.this);

                    break;
            }
            if (message.what == WIFI_CONNECTING_NOTIFICATION) {
                closeLoader();

                LibreLogger.d(this, "wifi timer for every 30seconds in wifi connecting HANDLER\n");
                LibreLogger.d(this, "suma in read wifi status intervals connecting timeout for 30 seconds ");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                BleCommunication bleCommunication = new BleCommunication(BLEConfigureActivity.this);
                BleCommunication.writeInteractorReadWifiStatus();
                LibreLogger.d(this, "wifi timer for every 30seconds write interactor reas wifi status");
                if (MavidApplication.readWifiStatus == true) {
                    LibreLogger.d(this, "suma in wifi connecting write interactor reas wifi status success");
                } else {
                    LibreLogger.d(this, "suma in wifi connecting write interactor reas wifi status failure");


                }
//                    }
//                });
                LibreLogger.d(this, "wifi timer for every 30seconds getting count \n" + count);

                if (count == 5 || MavidApplication.readWifiStatusNotificationBLE) {
                    LibreLogger.d(this, "wifi timer for every 30seconds getting count \n");
//                    bleCommunication = new BleCommunication(BLEConfigureActivity.this);
//                    BleCommunication.writeInteractorStopSac();
                    ShowAlertDynamicallyGoingToHomeScreen("Configuration failed",
                            " Please make sure your speaker is blinking multiple colours and then try again.", BLEConfigureActivity.this);

                }
            } else if (message.what == Constants.WIFI_CONNECTED_NOTIFICATION) {
                LibreLogger.d(this, "suma in wifi connecting event send empty msg delayed for 30 seconds two ");

            } else if (message.what == Constants.READ_ALEXA_STATUS_IN_INTERVALS) {
                LibreLogger.d(this, "suma in state is getting readalexastatus intervals");
                if (alexacount == 5) {
                    LibreLogger.d(this, "suma in state is getting readalexastatus intervals inside alexa count \n");

                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        inItWidgets();
        MavidApplication.noReconnectionRuleToApply = true;
        //    setBluetooth(false);
        isActivityActive = true;
        setupHandler = new Handler(Looper.getMainLooper());
        rememCheckBox = (CheckBox) findViewById(R.id.rememCheckBox);


        setupRunnable = new Runnable() {
            public void run() {
                closeLoader();
                updateResponseUI("Unable to Establish Connection...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(BLEConfigureActivity.this, MavidHomeTabsActivity.class));
                finish();
            }
        };

        showScanListFromWifiManager = false;
        scanListDataRecievedFromWifiManager = new ArrayList<>();
        handler.sendEmptyMessageDelayed(GET_SCAN_LIST_HANDLER, 15000);
        LibreLogger.d(this, "SUMA IN SCANREQ onCreate sendinghandler");
        showLoader("Please wait we are getting the scan list", "");

    }

    public int getDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1 << 8 | b2 & 0xFF);

        LibreLogger.d(this, "Data length is returned as s" + s);
        return s;
    }

    private void inItWidgets() {

        linearLayoutOthers = findViewById(R.id.id_linear_wifi_others);
        linearLayoutOthers.setVisibility(View.GONE);
        textInputWifiPassword = findViewById(R.id.text_input_wifi_password);
        textInputPasswordLayout = findViewById(R.id.textInputPasswordLayout);
        wrongPwd_text = findViewById(R.id.wrong_pwd_txtview);
        wrongPwd_text.setVisibility(View.INVISIBLE);
        btn_cancel = findViewById(R.id.btn_cancel);

        tvSelectedWifiOther = findViewById(R.id.text_input_wifi_ssid_other);

        btn_next = findViewById(R.id.btn_next);

        iv_down_arrow = findViewById(R.id.iv_down_arrow);


        iv_down_arrow_security = findViewById(R.id.iv_down_arrow_security);
        tvSelectedWifi = findViewById(R.id.tv_selected_wifi);
        tvSelectedWifiSecurity = findViewById(R.id.tv_selected_wifi_security);

        fab_refresh = findViewById(R.id.fab_refresh);

        btn_next.setOnClickListener(this);

        scanList = getSSIDList();
        securityAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, scanList);
        securityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SSID = getconnectedSSIDname(BLEConfigureActivity.this);


        back = findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        iv_down_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupBottomSheetForWifiList();
            }
        });

        iv_down_arrow_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupBottomSheetForWifiSecurityList();
            }
        });


        fab_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baos.reset();

                isModified = false;
                finalPayloadLength = 0;
                scaCred = false;
                refreshSSIDList();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    public boolean isTextVisible(TextView textView) {
        return textView.getText().toString().equalsIgnoreCase(getResources().getString(R.string.show));
    }


    public void setupBottomSheetForWifiList() {
        View view = getLayoutInflater().inflate(R.layout.show_wifi_list_bottom_sheet, null);
        tv_no_data = view.findViewById(R.id.tv_no_data);
        rv_wifi_list = view.findViewById(R.id.rv_wifi_list);
        AppCompatImageView iv_close_icon = view.findViewById(R.id.iv_close_icon);


        for (ModelWifiScanList modelWifiScanList : modelWifiScanList) {
            Log.d("atul", "ssidName: " + modelWifiScanList.getSsid() + " rssi: " + modelWifiScanList.getRssi());
        }
        setWifiListBottomSheetAdapter();

        iv_close_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(BLEConfigureActivity.this);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(false);

        bottomSheetDialog.show();

    }

    public void setupBottomSheetForWifiSecurityList() {
        View view = getLayoutInflater().inflate(R.layout.show_wifi_security, null);
        tv_no_data = view.findViewById(R.id.tv_no_data);
        rv_wifi_list = view.findViewById(R.id.rv_wifi_list);
        AppCompatImageView iv_close_icon = view.findViewById(R.id.iv_close_icon);


        setWifiListBottomSheetAdapterForSecurity();

        iv_close_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogForSecurity.dismiss();
            }
        });

        bottomSheetDialogForSecurity = new BottomSheetDialog(BLEConfigureActivity.this);
        bottomSheetDialogForSecurity.setContentView(view);
        bottomSheetDialogForSecurity.setCancelable(false);

        bottomSheetDialogForSecurity.show();

    }

    private boolean validate() {
        if (security != 0) {
            if (textInputWifiPassword.getText().length() > 64) {
                buildSnackBar("Password should be  less than 64 characters!");
                return false;
            }
            if (!isSecurityTypeSelected) {
                buildSnackBar(("Please Select Security Type..."));

            }
            if (textInputWifiPassword.getText().toString().length() == 0) {
                buildSnackBar("Please enter Password!");
                return false;
            }

            if (textInputWifiPassword.getText().toString().length() < 8) {
                buildSnackBar("Password should be of minimum 8 characters!");
            }
        }
        return true;
    }


    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(BLEConfigureActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);


        cafeBar.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*startActivity(new Intent(BLEConfigureActivity.this, DeviceListFragment.class));
        finish();*/
    }

    int finalPayloadLength;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    boolean scaCred = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BLEEvntBus event) {

        byte[] value = event.message;
        d("Bluetooth", "Value received: scan list\n " + getDataLength(value));
        int response2 = getDataLength(value);
        LibreLogger.d(this, "ble event bus read bluetooth suma in finalpayload  bleconfigure activity on\n " + response2);

        if (response2 == 4) {
            if (!alreadyresponse4Reached) {
                alreadyresponse4Reached = true;
                d("BLEConfigureActivity", "suma in bleconfigure activity connecting in response 4\n");
                closeLoader();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        count++;
                        if (count < 6) {
                            handler.sendEmptyMessage(WIFI_CONNECTING_NOTIFICATION);
                            LibreLogger.d(this, "wifi timer for every 30seconds" + count);
                            handler.postDelayed(this, 30000);
                        }
                    }
                }, 30000);

                if (value[2] == 0 && value.length > 6) {
                    LibreLogger.d(this, "suma in data length\n" + value.length);
                    for (int i = 5; i < value.length - 5; i++) {
                        LibreLogger.d(this, "suma in gettin index value" + value[i]);
                        try {
                            sacBleConnecting = new String(value, "UTF-8");
                            LibreLogger.d(this, "suma wifi connecting response4 string\n" + sacBleConnecting);
                            sacBleConnectingIndex = sacBleConnecting.indexOf("Connecting");
                            LibreLogger.d(this, "suma wifi connecting only splitted array string\n" + sacBleConnecting.substring(sacBleConnectingIndex));

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoader("Please Wait...", "" + sacBleConnecting.substring(sacBleConnectingIndex));
                            d("BLEConfigureActivity", "suma in bleconfigure activity connecting string runonUI THREAD\n");

                        }
                    });


                }
            }
        }

        if (response2 == 5) {
            d("BLEConfigureActivity", "bluetooth suma in finalpayload else in if suma in bleconfigure activity connected\n" + response2);
            //closeLoader();
            handler.removeMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);
            // MavidApplication.deviceConnectedSSID
            LibreLogger.d(this, "suma in wifi connecting close loader 5 " + new String(value));
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    try {
                        finalConnectedArray = new String(value, "UTF-8");
                        finalConnectedIndex = finalConnectedArray.indexOf("Conn");
                        finalConnectedString = finalConnectedArray.substring(finalConnectedIndex);

                        LibreLogger.d(this, "wifi timer for every 30seconds connected status response 5\n" + finalConnectedArray.substring(finalConnectedIndex));

                        String connectedSsid = tvSelectedWifi.getText().toString();

                        if (connectedSsid.equalsIgnoreCase("Other Network"))
                            connectedSsid = tvSelectedWifiOther.getText().toString();

                        MavidApplication.deviceConnectedSSID = connectedSsid;


                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                // closeLoader();

                String phone_SSID_connected = getconnectedSSIDname(BLEConfigureActivity.this);
                // LibreLogger.d(this, "suma wifi connecting text response wifi timer getting connected ssid \n" + MavidApplication.deviceConnectedSSID + "phone connected wifi ssid\n" + phone_SSID_connected);
//                Intent intent = new Intent(BLEConfigureActivity.this, MavidBLEConfigureSpeakerActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString(BundleConstants.SACConstants.SSID, tvSelectedWifi.getText().toString());
//                intent.putExtras(bundle);
//                startActivity(intent);

//                Intent newIntent = new Intent(BLEConfigureActivity.this, MavidBLEConfigureSpeakerActivity.class);
//                startActivity(newIntent);
//                finish();

                LibreLogger.d(this, "atul_check_ MavidApplication.deviceConnectedSSID value: " + MavidApplication.deviceConnectedSSID);

                if (!alreadyExecuted) {
                    alreadyExecuted = true;

                    Intent intent1 = new Intent(this, MavidBLEConfigureSpeakerActivity.class);
                    startActivity(intent1);

                    finish();
                }

                // BleCommunication.writeInteractorDeviceStatus();
                LibreLogger.d(this, "suma wifi connecting text response String RESPONSE 5\n");

            }

            BleCommunication.writeInteractorDeviceStatus();
        }

        if (response2 == 14) {
            closeLoader();
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC timeout dialogue");
            //StopSAC
            if (!isSacTimedOutCalled) {
                BleCommunication bleCommunication = new BleCommunication(BLEConfigureActivity.this);
                BleCommunication.writeInteractorStopSac();
                LibreLogger.d(this, "suma in getting stop sac activity 14");
                ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..",
                        "Please put the device to setup mode", BLEConfigureActivity.this);
                isSacTimedOutCalled = true;
            }
        }
        if (response2 == 20) {
            LibreLogger.d(this, "Suma in getting wrong password field 1");
            closeLoader();
            wrongPwd_text.setVisibility(View.VISIBLE);

        }

        if (response2 == 17) {
            handler.removeMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);
            MavidApplication.got17DeviceStatusResponse = true;

            LibreLogger.d(this, "Getting the ble Status suma in gettin response value Read Device status");
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    LibreLogger.d(this, "suma in gettin index value in 17\n" + value[i]);
                    String deviceStatus = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        deviceStatus = new String(value, StandardCharsets.UTF_8);
                    }
                    LibreLogger.d(this, "Getting the ble Status getting device status\n" + deviceStatus);

                    finalIpIndez = deviceStatus.indexOf("IpAdd:");
                    LibreLogger.d(this, "Getting the ble Status getting ipAddress\n" + deviceStatus.substring(finalIpIndez));
                    FilteredfinalIpAddress = deviceStatus.substring(finalIpIndez);
                    String[] splittedIpValue = FilteredfinalIpAddress.split(":");
                    mSACConfiguredIpAddress = splittedIpValue[1];
                    MavidApplication.mBLESACDeviceAddress = mSACConfiguredIpAddress;
                    LibreLogger.d(this, "Getting the ble Status getting ipAddress index 1\n" + splittedIpValue[1] + "mSacConfiguredIp" + mSACConfiguredIpAddress);

                    alexaLoginIndex = deviceStatus.indexOf("AlexaLogin");
                    filteredAlexaLogin = deviceStatus.substring(alexaLoginIndex);
                    String[] splittedAlexaLogin = filteredAlexaLogin.split(":");
                    getAlexaLoginStatus = splittedAlexaLogin[1];
                    LibreLogger.d(this, "Hotspot Credential Getting the ble Status getting AlexaLogin\n" + splittedAlexaLogin[1] + "login status\n" + getAlexaLoginStatus);


                }

            }
        }
        if (response2 == 15) {
            closeLoader();
            handler.removeCallbacksAndMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);
            LibreLogger.d(this, "wifi timer for every 30seconds in read wifi status Notification TWO 15\n");
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    LibreLogger.d(this, "suma in gettin index value" + value[i]);
                    try {
                        configuratonfailedStringBle = new String(value, "UTF-8");
                        configuratonfailedIndexBle = configuratonfailedStringBle.indexOf("Configuration");

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
                MavidApplication.readWifiStatusNotificationBLE = true;
                closeLoader();

                ShowAlertDynamicallyConfigurationFailed(configuratonfailedStringBle.substring(configuratonfailedIndexBle),
                        "Please make sure Credential entered is correct and Try Again!", BLEConfigureActivity.this);

                //     BleCommunication.writeInteractorStopSac();
//                LibreLogger.d(this, "suma in getting stop sac activity 15");

            }
        }
        if (response2 == 13) {
            closeLoader();
//            ShowAlertDynamicallyGoingToHomeScreen("Sac Not Allowed...",
//                    "Please make sure your device is in setup mode", BLEConfigureActivity.this);
        }
        if (response2 == 16) {
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
                    // LibreLogger.d(this,"suma in configuration failed 16"+configuratonfailedStringBle);
                    handler.removeCallbacksAndMessages(WIFI_CONNECTING_NOTIFICATION);
                    handler.removeCallbacksAndMessages(null);
                    ShowAlertDynamicallyGoingToHomeScreen("Configuration Failed", failedString, BLEConfigureActivity.this);

                }
            }
        }
        if (!scaCred) {
            if (value[2] == 1) {
                scaCred = true;
                finalPayloadLength = getDataLength(value);
                d("Bluetooth", "finalPayloadLength size: " + finalPayloadLength);
                d("Bluetooth", "bluetooth suma in finalpayload" + finalPayloadLength);
                LibreLogger.d(this, "suma check the condition in sacred if ");
            }
        } else {
            byte[] cd = new byte[finalPayloadLength];
            LibreLogger.d(this, "suma check the condition in sacred else");
            if (value[2] == 0 && value.length > 6) {
                LibreLogger.d(this, "suma in data length\n" + value.length);
                //getDataLengthString(value);

            }

            if (baos.size() <= finalPayloadLength) {
                int response = getDataLength(value);
                d("Bluetooth", "finalPayloadLength size: get response " + finalPayloadLength + "getting baos size\n" + baos.size());
                d("Bluetooth", "bluetooth suma in finalpayload else in if suma in bleconfigure activity" + response);
                LibreLogger.d(this, "suma check the condition in sacred else one more if check baos length if");

                try {
                    if (response == 0) {
                        d("Bluetooth", "sac credential ok vlaue in less than size baos\n" + response);
//                        showLoader("", "Waiting for device response...");
                    }
                    if (response == 1) {
                        closeLoader();
                        if (isActivityActive) {
                            updateResponseUI("Invalid credentials");
                            d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next response 1" + response);
                        }
                        d("Bluetooth", "invalid credentials" + response);
                    }
                    if (response2 == 20) {
                        LibreLogger.d(this, "Suma in getting wrong password field 2");

                    }
                    if (response == 10) {
                        closeLoader();
                        handler.removeMessages(GET_SCAN_LIST_HANDLER);
                        d("Bluetooth", "invalid  response after 10 response\n" + baos.toString() + "baoas\n" + baos);

                        if (isActivityActive) {
                            //updateResponseUI("Scan list received");
                            d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next response  10" + response);

                        }
                        getIntentParams(baos.toString());
                        String finalSsidList = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            finalSsidList = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                        }
                        scanList = getSSIDList();
                        MavidApplication.final_Scanlist_End = finalSsidList;

                        d("Bluetooth", "invalid credentials inactive2" + response);
                        if (response == 0) {
                            d("Bluetooth", "sac credential ok vlaue in after 10\n" + response);

                            d("Bluetooth", "bluetooth suma in finalpayload else in if try response2 0" + response);
                            if (isActivityActive) {
                                updateResponseUI("Credentials succesfully posted.");
                                LibreLogger.d(this, "suma in credential posted 1");
                            }
                            d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next2" + response);

                        }
                    }
                    if (response == 14) {
                        if (!isSacTimedOutCalled) {
                            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC timeout dialogue");
                            ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..",
                                    "Please put the device to setup mode", BLEConfigureActivity.this);
                            isSacTimedOutCalled = true;
                        }
                    }

                    if (response == 15) {
                        if (value[2] == 0 && value.length > 6) {
                            for (int i = 5; i < value.length - 5; i++) {
                                LibreLogger.d(this, "suma in gettin index value" + value[i]);
                                try {
                                    configuratonfailedStringBle = new String(value, "UTF-8");
                                    configuratonfailedIndexBle = configuratonfailedStringBle.indexOf("Configuration");

                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                            }
                            MavidApplication.readWifiStatusNotificationBLE = true;
                            LibreLogger.d(this, "wifi timer for every 30seconds in read wifi status Notification 15\n");

                        }
                    } else if (getDataLength(value) != 10 /*&& value[2] != 1*/) {

                        baos.write(value);
                        String finalSsidList = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            finalSsidList = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                        }
                        getSSIDList();
                        //suma
                        d("Bluetooth", "BleCommunication in suma ble state turning getting do next response scanlist getdatalengthvalue! 10" + response);
                    }

                } catch (IOException e) {
                    d("BLEConfigureActivity", e.getMessage());

                }
                d("Bluetooth", "baos STEP1 " + baos);
                d("BLEConfigureActivity", "bluetooth suma in finalpayload after exception! 10" + response);

            } else {
                d("Bluetooth", "strAscii: " + new String(cd, 0, finalPayloadLength));
                d("Bluetooth", "baos STEP2 " + baos);
                d("Bluetooth", "baos.size(): " + baos.size());
                int response = getDataLength(value);
                LibreLogger.d(this, "suma check the condition in sacred else one more if check baos length else");

                d("Bluetooth", "bluetooth suma in finalpayload response 0  in else getdatalength" + response);
                if (response == 0) {
                    d("Bluetooth", "sac credential ok vlaue else part\n" + response);

                    d("BLEConfigureActivity", "credentials ok" + value);
//                    if (isActivityActive) {
//                        updateResponseUI("Credentials succesfully posted.");
//                        LibreLogger.d(this,"suma in credential posted 2");
//
//                    }
                    d("Bluetooth", "credentials ok next value do next3\n" + response);

                } else if (response == 1) {
                    closeLoader();
                    if (isActivityActive) {
                        updateResponseUI("Invalid credentials");
                    }
                    d("Bluetooth", "bluetooth suma in finalpayload response 1 in else" + response);

                } else if (response2 == 20) {
                    LibreLogger.d(this, "Suma in getting wrong password field 3");

                } else if (response == 10) {
                    closeLoader();
                    if (isActivityActive) {
                        // updateResponseUI("Scan list received");
                    }
//                    try {
//                        String finalSsidList=new String(baos.toByteArray(),"UTF-8");
//                        LibreLogger.d(this,"invalid  response 10 getting encoded scan list\n"+finalSsidList);
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    //suma commenting ssid fetch
//                    try {
//                        baos.write(value);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    d("Bluetooth", "BleCommunication in suma ble state turning getting invalid  response 10\n" + baos.toString() + "baos\n" + baos);
//                    scanList = getSSIDList();
//                    securityAdapter.clear();
                    //BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);

                    String finalSsidList = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        finalSsidList = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                    }

                    LibreLogger.d(this, "Swetha BT and SUMA IN SSID in READ DATA response 10\n" + finalSsidList);
//
                    getIntentParams(finalSsidList);
                    securityAdapter = new ArrayAdapter(BLEConfigureActivity.this, android.R.layout.simple_spinner_item, scanList);
                    securityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    ssidSpinner.setAdapter(securityAdapter);
                    d("Bluetooth", "bluetooth suma in finalpayload response 10 in else" + response);

                }

            }
        }


    }


    private ArrayList<ScanListData> getScanList(String scanList) {
        d("populateScanlistMap", "scanList Recieved String is:  " + scanList);
        ArrayList<ScanListData> storedArrayList = new ArrayList<>();
        try {

            String result = scanList.replaceAll("[^\\w\\s]", "");
            String result1 = result.replaceAll("Items", "");
            String[] arrSplit_3 = null;

            if (result1 != null && result1.contains("SSID")) {
                arrSplit_3 = result1.split("SSID");
            } else if (result1 != null && result1.contains("SSI")) {
                arrSplit_3 = result1.split("SSI");
            }

            if (arrSplit_3 != null)
                for (int i = 1; i < arrSplit_3.length; i++) {
                    try {
                        String[] arrSplit1 = null;
                        if (arrSplit_3[i].contains("Security")) {
                            arrSplit1 = arrSplit_3[i].split("Security");
                        } else if (arrSplit_3[i].contains("Sec")) {
                            arrSplit1 = arrSplit_3[i].split("Sec");
                        }

                        if (arrSplit1 != null) {
                            String[] arrSplit = arrSplit1[1].split("\\s+");

                            if (arrSplit.length > 2) {
                                ScanListData storedDto = new ScanListData();
                                storedDto.setSsid(arrSplit1[0]);
                                storedDto.setSecurity(arrSplit[1]);
                                storedDto.setRssi("-" + arrSplit[3]);
                                storedArrayList.add(storedDto);
                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.v("populateScanlistMap", "getScanList Data: " + e);

                    }
                }

            for (int k = 0; k < storedArrayList.size(); k++) {
                ScanListData s = storedArrayList.get(k);
                d("populateScanlistMap", "Stored ArrayList SSID :" + s.getSsid() + " Security:" + s.getSecurity() + " RSSI:" + s.getRssi());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return storedArrayList;

    }

    private void handleScandlistExceptionMethod2() {
        Log.v("populateScanlistMap", "Getting List From Wifi..");
        Log.v("scanListDataTest", "gettingscanList From Wifi Manager::: " + scanList);
        showScanListFromWifiManager = true;
        getScanListDataFromWifiManager();
    }

    private void showScanList(ArrayList<ScanListData> scanListData) {
        scanListMap.clear();
        modelWifiScanList = new ArrayList<>();
        LibreLogger.d(this, "suma in handle scanlist data arratlist before \n " + scanListData);

        Log.v("populateScanlistMap", "showScanList ScanList is ::: " + scanListData.size());

        for (int i = 0; i < scanListData.size(); i++) {
            ScanListData obj = scanListData.get(i);
            LibreLogger.d(this, "suma in handle scanlist data arratlist\n " + scanListData);
            if (obj.getSsid() == null
                    || (obj.getSsid().isEmpty()) | obj.getSecurity() == null || obj.getSecurity().isEmpty() || obj.getRssi() == null || obj.getRssi().isEmpty()) {
                continue;
            }
            try {
                Integer.parseInt(obj.getRssi());
                scanListMap.put(obj.getSsid(), obj.getSecurity());
                modelWifiScanList.add(new ModelWifiScanList(obj.getSsid()
                        , obj.getSecurity(),
                        obj.getRssi()));
                LibreLogger.d(this, "suma in handle scanlist data arratlist inside for \n " + scanListData);

            } catch (NumberFormatException exception) {
                Log.v("populateScanlistMap", "showScanList ScanList Number Format Exception ::: " + exception);
                exception.printStackTrace();
            }

        }
        scanListMap.put("Other Network", "WPA-PSK");
        modelWifiScanList.add(new ModelWifiScanList("Other Network"
                , "WPA-PSK",
                "100"));
        LibreLogger.d(this, "suma in empty scanlist getting other network 8 ");
        if (bottomSheetDialog != null) {
            if (bottomSheetDialog.isShowing()) {
                setWifiListBottomSheetAdapter();
                LibreLogger.d(this, "suma in empty scanlist getting other network 1");
            }
        }
        if (modelWifiScanList.size() > 0) {
            iv_down_arrow.setVisibility(View.VISIBLE);
            LibreLogger.d(this, "suma in empty scanlist getting other network 2");

        } else {
            iv_down_arrow.setVisibility(View.INVISIBLE);
            LibreLogger.d(this, "suma in empty scanlist getting other network 3");

        }

    }


    private void handleScanException(ArrayList<ScanListData> scanListData) {
        showScanList(scanListData);
        if (modelWifiScanList.size() <= 1) {
            Log.v("populateScanlistMap", "handleException  ::: getting from wifi Manager ");
            getScanListDataFromWifiManager();
            Log.v("populateScanlistMap", "Recieved ScanList is three ::: " + scanList);

        }

    }

    private HashMap<String, String> getDataMapFromMessage(Scanner sc) {
        HashMap<String, String> dataMap = new HashMap<>();
        while (sc.hasNext()) {
            String message = sc.nextLine().toString();
            LibreLogger.d(this, "Getting the HotSpot Status suma final status in getDataMap msg\n" + message);
            String[] dataSplitArr = message.split(":");
            LibreLogger.d(this, "suma in gettin index value splitArrayy0\n" + dataSplitArr[0] + "splitArray1\n" + dataSplitArr[1] + "splitArray2\n" + dataSplitArr[2]);
            dataMap.put(dataSplitArr[0], dataSplitArr[1]);
        }
        return dataMap;
    }

    private void getDataLengthString(byte[] value) {
        if (value[2] == 0 && value.length > 6) {
            LibreLogger.d(this, "suma in data length\n" + value.length);
            for (int i = 5; i < value.length - 5; i++) {
                LibreLogger.d(this, "suma in gettin index value" + value[i]);
                try {
                    String finalConnecting = new String(value, "UTF-8");
                    int finalConnectingIndex = finalConnecting.indexOf("Connecting");
                    //  LibreLogger.d(this, "suma wifi connecting only splitted array string\n" + finalConnecting.substring(finalConnectingIndex));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next: {
                LibreLogger.d(this, "pls select wifi from list\n" + tvSelectedWifi.getText().toString() + "OTHER SSID\n" + tvSelectedWifiOther.getText() + "Security Type\n" + security);
                if (tvSelectedWifi.getText().toString().equals("Select a Wi-fi")) {
                    buildSnackBar("Please select a wifi from the list");
                    LibreLogger.d(this, "pls select wifi from list");
                    validate();
                } else if (tvSelectedWifi.getText().toString().equals("Other Network")) {
                    LibreLogger.d(this, "pls select wifi from list other network " + tvSelectedWifiOther.getText());
                    validate();
                    if (Objects.requireNonNull(tvSelectedWifiOther.getText()).toString().length() == 0) {
                        buildSnackBar("Please enter SSID");
                        validate();
                    } else if (!isSecurityTypeSelected) {
                        buildSnackBar(("Please Select Security Type..."));
                    } else if (textInputWifiPassword.getText().toString().length() == 0) {
                        buildSnackBar(("Please enter password..."));
                    } else {
                        buildSnackBar(("Please Wait,Sending Credentials..."));
                        LibreLogger.d(this, "pls select wifi from list wifi validate " + tvSelectedWifi.getText().toString());

                        String connectedSsid = tvSelectedWifi.getText().toString();
                        if (connectedSsid.equalsIgnoreCase("Other Network")) {
                            connectedSsid = tvSelectedWifiOther.getText().toString();
                            if (!isSecurityTypeSelected) {
                                buildSnackBar(("Please Select Security Type..."));
                                return;
                            }

                        }

                        final Bundle sacParams = new Bundle();
                        sacParams.putString(BundleConstants.SACConstants.SSID
                                , connectedSsid);

                        sacParams.putString(BundleConstants.SACConstants.PASSPHRASE
                                , textInputWifiPassword.getText().toString());
                        sacParams.putInt(BundleConstants.SACConstants.NETWORK_TYPE
                                , security);
                        d("Bluetooth", "suma in connect btn bluetooth credential posted  Noww1!!!...");
                        setupHandler.removeCallbacks(setupRunnable);
                        setupHandler.postDelayed(setupRunnable, 30000);

                        final String finalConnectedSsid = connectedSsid;
                        LibreMavidHelper.configureBLE(sacParams, new SACListener() {
                            @Override
                            public void success() {

                                d("Bluetooth", "suma in connect btn bluetooth credential posted  Noww2!!!...");

                            }

                            @Override
                            public void failure(String message) {

                                setupHandler.removeCallbacks(setupRunnable);
                                updateResponseUI(message);
                            }

                            @Override
                            public void successBLE(byte[] b) {
                                setupHandler.removeCallbacks(setupRunnable);
                                d("Bluetooth", "suma in connect btn bluetooth credential posted  Noww3!!!...");
                                if (isActivityActive) {
                                    updateResponseUI("Credentials succesfully posted.");
                                    LibreLogger.d(this, "suma in credential posted 3");
                                    if (rememCheckBox.isChecked()) {
                                        storeSSIDInfoToSharedPreferences(finalConnectedSsid, textInputWifiPassword.getText().toString());
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showLoader("", "Waiting for device response...");
                                        }
                                    });
                                }
                                BleCommunication bleCommunication = new BleCommunication(BLEConfigureActivity.this);
                                BleCommunication.writeInteractor(b);
                                wrongPwd_text.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                } else {
                    LibreLogger.d(this, "pls select wifi from list select a wifi");

                    if (validate()) {
                        buildSnackBar(("Please Wait,Sending Credentials..."));
                        LibreLogger.d(this, "pls select wifi from list wifi validate " + tvSelectedWifi.getText().toString());

                        String connectedSsid = tvSelectedWifi.getText().toString();
                        if (connectedSsid.equalsIgnoreCase("Other Network")) {
                            connectedSsid = tvSelectedWifiOther.getText().toString();
                            if (!isSecurityTypeSelected) {
                                buildSnackBar(("Please Select Security Type..."));
                                return;
                            }

                        }

                        final Bundle sacParams = new Bundle();
                        sacParams.putString(BundleConstants.SACConstants.SSID
                                , connectedSsid);

                        sacParams.putString(BundleConstants.SACConstants.PASSPHRASE
                                , textInputWifiPassword.getText().toString());
                        sacParams.putInt(BundleConstants.SACConstants.NETWORK_TYPE
                                , security);
                        d("Bluetooth", "suma in connect btn bluetooth credential posted  Noww1!!!...");
                        setupHandler.removeCallbacks(setupRunnable);
                        setupHandler.postDelayed(setupRunnable, 30000);

                        final String finalConnectedSsid = connectedSsid;
                        LibreMavidHelper.configureBLE(sacParams, new SACListener() {
                            @Override
                            public void success() {

                                d("Bluetooth", "suma in connect btn bluetooth credential posted  Noww2!!!...");

                            }

                            @Override
                            public void failure(String message) {

                                setupHandler.removeCallbacks(setupRunnable);
                                updateResponseUI(message);
                            }

                            @Override
                            public void successBLE(byte[] b) {
                                setupHandler.removeCallbacks(setupRunnable);
                                d("Bluetooth", "suma in connect btn bluetooth credential posted  Noww3!!!...");
                                if (isActivityActive) {
                                    updateResponseUI("Credentials succesfully posted.");
                                    LibreLogger.d(this, "suma in credential posted 3");
                                    if (rememCheckBox.isChecked()) {
                                        storeSSIDInfoToSharedPreferences(finalConnectedSsid, textInputWifiPassword.getText().toString());
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showLoader("", "Waiting for device response...");
                                        }
                                    });
                                }
                                BleCommunication.writeInteractor(b);
                                wrongPwd_text.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                }

                break;
            }

        }

    }


//    private HashMap<String,String> getDataMapFromMessage(String sc) {
//        HashMap<String,String> dataMap = new HashMap<>();
//       // while(sc.hasNext()){
//          //  String message = sc.nextLine().toString();
//
//            String[] dataSplitArr = sc.split(":");
//            if (dataSplitArr.length < 2){
//                continue;
//            }
//            dataMap.put(dataSplitArr[0],dataSplitArr[1]);
//       // }
//        return dataMap;
//    }

    private void doNext(Bundle sacParams) {
        Intent intent = new Intent(BLEConfigureActivity.this, WifiYesAlexaLoginNoHomeScreen.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sacParams.putString(Constants.INTENTS.MACADDRESS, getMacAddress());
        intent.putExtra(Constants.INTENTS.SAC_PARAMS, sacParams);
        startActivity(intent);
        finish();
    }

//    private void sendMSearchInIntervalOfTime() {
//        mHandler.sendEmptyMessageDelayed(Constants.SEARCHING_FOR_DEVICE, 500);
//        mTaskHandlerForSendingMSearch.postDelayed(mMyTaskRunnableForMSearch, MSEARCH_TIMEOUT_SEARCH);
//        Log.d("MavidCommunication", "My task is Sending 1 Minute Once M-Search msearch interval of time");
//    }

    private void doNext() {
        Intent intent = new Intent(BLEConfigureActivity.this, BLEConnectToWifiActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.INTENTS.MACADDRESS, getMacAddress());
//        intent.putExtra(BundleConstants.SACConstants.SSID, ssidSpinner.getSelectedItem().toString().trim());
        intent.putExtra(BundleConstants.SACConstants.PASSPHRASE, textInputWifiPassword.getText().toString());
        intent.putExtra(BundleConstants.SACConstants.NETWORK_TYPE, 8);
        startActivity(intent);
        finish();
    }

    private void refreshSSIDList() {
        showLoader("Please wait we are getting the scan list", "");
        handler.sendEmptyMessageDelayed(GET_SCAN_LIST_HANDLER, 30000);


        LibreLogger.d(this, "SUMA IN SCANREQ onCreate sendinghandler on refresh");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BleCommunication bleCommunication = new BleCommunication(BLEConfigureActivity.this);
                BleCommunication.writeInteractor();
            }
        });
    }

    private void updateResponseUI(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //closeLoader();
                buildSnackBar(message);
//                Toast.makeText(BLEConfigureActivity.this, "" + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getIntentParams(String scanList) {

        //handleScandlistExceptionMethod2();
        //getScanListDataFromWifiManager();
        populateScanlistMap(scanList);
        Log.v("scanListData", "getIntentParams scanList Data Size :" + scanListDataRecievedFromWifiManager.size());
        //showScanList(scanListDataRecievedFromWifiManager);
    }

    public void setWifiListBottomSheetAdapter() {

        Collections.sort(modelWifiScanList, new Comparator<ModelWifiScanList>() {
            @Override
            public int compare(ModelWifiScanList modelWifiScanList, ModelWifiScanList modelWifiScanList1) {
                //sorting via rssi values
                return Integer.parseInt(modelWifiScanList1.getRssi()) - Integer.parseInt(modelWifiScanList.getRssi());
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BLEConfigureActivity.this);
        wifiListBottomSheetAdapter = new WifiListBottomSheetAdapter(BLEConfigureActivity.this, modelWifiScanList);

        rv_wifi_list.setAdapter(wifiListBottomSheetAdapter);
        wifiListBottomSheetAdapter.setWifiConfigurationItemClickInterface(this);
        rv_wifi_list.setLayoutManager(linearLayoutManager);
//        swipe_refresh.setRefreshing(false);
        if (modelWifiScanList.size() > 0) {
            tv_no_data.setVisibility(View.GONE);

        } else {
            tv_no_data.setVisibility(View.VISIBLE);
        }
    }

    public void setWifiListBottomSheetAdapterForSecurity() {

        List<String> securityList = new ArrayList<>();
        securityList.add("OPEN");
        securityList.add("WPA-PSK");
        securityList.add("WPA/WPA2");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BLEConfigureActivity.this);
        wifiListBottomSheetAdapterForSecurityType = new WifiListBottomSheetAdapterForSecurityType(BLEConfigureActivity.this, securityList);

        rv_wifi_list.setAdapter(wifiListBottomSheetAdapterForSecurityType);
        wifiListBottomSheetAdapterForSecurityType.setWifiConfigurationForSeurity(this);
        rv_wifi_list.setLayoutManager(linearLayoutManager);

    }

    public void populateScanlistMap(final String scanList) {
        scanListMap.clear();
        modelWifiScanList = new ArrayList<>();
        Log.v("populateScanlistMap", "PopulateScanList Map Recieved... ::: " + scanList);
        try {
            JSONObject mainObj = new JSONObject(scanList);
            JSONArray scanListArray = mainObj.getJSONArray("Items");
            for (int i = 0; i < scanListArray.length(); i++) {
                JSONObject obj = (JSONObject) scanListArray.get(i);
                if (obj.getString("SSID") == null
                        || (obj.getString("SSID").isEmpty()) | obj.getString("Security") == null || obj.getString("Security").isEmpty() || obj.getString("RSSI") == null || obj.getString("RSSI").isEmpty()) {
                    continue;
                }
                scanListMap.put(obj.getString("SSID"), obj.getString("Security"));
                modelWifiScanList.add(new ModelWifiScanList(obj.getString("SSID")
                        , obj.getString("Security"),
                        obj.getString("RSSI")));
            }

            scanListMap.put("Other Network", "OPEN");
            modelWifiScanList.add(new ModelWifiScanList("Other Network"
                    , "WPA-PSK",
                    "100"));
            LibreLogger.d(this, "suma in empty scanlist getting other network 7");

            if (bottomSheetDialog != null) {
                if (bottomSheetDialog.isShowing()) {
                    setWifiListBottomSheetAdapter();
                }
            }
            if (modelWifiScanList.size() > 0) {
                iv_down_arrow.setVisibility(View.VISIBLE);
            } else {
                iv_down_arrow.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            Log.v("populateScanlistMap", "Json Parse Exception.......  " + e);

            Log.v("scanListData", "Json Parse Exception " + e);
            scanListMap.clear();
            modelWifiScanList = new ArrayList<>();
            scanListMap.put("Other Network", "OPEN");
            modelWifiScanList.add(new ModelWifiScanList("Other Network"
                    , "WPA-PSK",
                    "100"));
            LibreLogger.d(this, "suma in empty scanlist getting other network 8");

            if (bottomSheetDialog != null) {
                if (bottomSheetDialog.isShowing()) {
                    setWifiListBottomSheetAdapter();
                }
            }
            if (modelWifiScanList.size() > 0) {
                iv_down_arrow.setVisibility(View.VISIBLE);
            } else {
                iv_down_arrow.setVisibility(View.INVISIBLE);
            }
            e.printStackTrace();
            Log.v("scanListData", "Json Parse Exception scanListDataRecievedFromWifiManager size is " + scanListDataRecievedFromWifiManager.size());

            Log.v("populateScanlistMap", "Json Parse Exception ScanListData Reciever List size.......  " + scanListDataRecievedFromWifiManager.size());
            if (scanListDataRecievedFromWifiManager != null && scanListDataRecievedFromWifiManager.size() > 0) {
                showScanList(scanListDataRecievedFromWifiManager);
            } else
                handleScandlistExceptionMethod2();
            LibreLogger.d(this, "suma in exception logs " + e + "get loc msgs\n" + e.getLocalizedMessage() + "cause\n" + e.getCause());

        }
    }

    private void storeSSIDInfoToSharedPreferences(String deviceSSID, String password) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Your_Shared_Prefs", MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        editor.putString(deviceSSID, password);

        editor.apply();

    }

    private void getSSIDPasswordFromSharedPreference(String deviceSSID) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Your_Shared_Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.apply();
        LibreLogger.d(this, "suma in getting saved password" + pref.getString(deviceSSID, ""));
        textInputWifiPassword.setText(pref.getString(deviceSSID, ""));
        pref.getString(deviceSSID, "");

    }

    public ArrayList<String> getSSIDList() {
        ArrayList<String> scanList = new ArrayList<>();
        Set<String> keySet = scanListMap.keySet();
        for (String ssid : keySet) {
//             modelWifiScanList.add(new ModelWifiScanList())
            scanList.add(ssid);
        }
        handler.removeMessages(GET_SCAN_LIST_HANDLER);
        return scanList;
    }

    public String getMacAddress() {
        String ssid = getconnectedSSIDname(BLEConfigureActivity.this);
        ssid = ssid.substring(ssid.length() - 5);
        return ssid;
    }

    @Override
    public void onReadSuccess(byte[] data) {
        d("onReadSccBLEConfigure: ", "data : " + data);
    }


    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onWriteFailure() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClicked(int pos) {
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
        }
        tvSelectedWifi.setText(modelWifiScanList.get(pos).getSsid());
        getSSIDPasswordFromSharedPreference(modelWifiScanList.get(pos).getSsid());

        if (modelWifiScanList.get(pos).getSsid() != null && modelWifiScanList.get(pos).getSsid().equalsIgnoreCase("Other Network")) {
            linearLayoutOthers.setVisibility(View.VISIBLE);
        } else {
            linearLayoutOthers.setVisibility(View.GONE);
            switch (modelWifiScanList.get(pos).getSecurity()) {
                default:
                case "NONE":
                case "OPEN":
                    security = 0;
                    textInputPasswordLayout.setVisibility(View.GONE);
                    break;
                case "WPA-PSK":
                    security = 8;
                    textInputPasswordLayout.setVisibility(View.VISIBLE);
                    break;
                case "WPA/WPA2":
                    security = 4;
                    textInputPasswordLayout.setVisibility(View.VISIBLE);
                    break;
                case "WPAPSK":
                    security = 8;
                    textInputPasswordLayout.setVisibility(View.VISIBLE);
                    break;
                case "WPAWPA2":
                    security = 4;
                    textInputPasswordLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeWifi();
        registerWifiManager();
        EventBus.getDefault().register(this);
        LibreLogger.d(this, "suma wifi connecting text response register onStart BLECONF");

    }

    @Override
    protected void onResume() {
        super.onResume();
        MavidApplication.noReconnectionRuleToApply = true;
        initializeWifi();
        registerWifiManager();
        getScanListDataFromWifiManager();
        LibreLogger.d(this, "Checking which activity is getting called when BLEConfigureActivity");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterWifiManager();
        EventBus.getDefault().unregister(this);
        LibreLogger.d(this, "suma wifi connecting text response unregister onStop BLECONF");
    }

    @Override
    public void onSecurityTypeSelected(int position) {
        List<String> securityList = new ArrayList<>();
        securityList.add("OPEN");
        securityList.add("WPA-PSK");
        securityList.add("WPA/WPA2");

        if (bottomSheetDialogForSecurity != null) {
            bottomSheetDialogForSecurity.dismiss();
        }

        tvSelectedWifiSecurity.setText(securityList.get(position));
        switch (position) {

            case 0:
                isSecurityTypeSelected = true;
                security = 0;
                textInputPasswordLayout.setVisibility(View.GONE);
                break;
            case 1:
                isSecurityTypeSelected = true;
                security = 8;
                textInputPasswordLayout.setVisibility(View.VISIBLE);
                break;
            case 2:
                isSecurityTypeSelected = true;
                security = 4;
                textInputPasswordLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void registerWifiManager() {
        receiverWifi = new WifiReceiver(wifiManager, this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
    }

    private void unRegisterWifiManager() {
        unregisterReceiver(receiverWifi);
    }


    private String getSecurityType(String securityType) {
        String securityTypeData = "";
        if (securityType.contains("WPA-PSK")) {
            securityTypeData = "WPA-PSK";
        } else if (securityType.contains("WPA")) {
            securityTypeData = "WPA/WPA2";
        } else if (securityType.contains("WPA2")) {
            securityTypeData = "WPA/WPA2";
        }
        return securityTypeData;
    }

    private void initializeWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            // Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

    }

    private void getScanListDataFromWifiManager() {
        if (wifiManager == null)
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(BLEConfigureActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    BLEConfigureActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            closeLoader();
            showLoader("WIFI Scan", "Please Wait Getting WIFI List ");
            wifiManager.startScan();
        }
    }

    @Override
    public void onScanListRecievedFromWifiManager(ArrayList<ScanListData> scanListData) {

        Log.v("populateScanlistMap", "onScanListRecievedFromWifiManager Recieved data from WIFi Manager" + scanListData.size());
        Log.v("populateScanlistMap", "size of stored List" + scanListDataRecievedFromWifiManager.size());
        closeLoader();

        if ((scanListDataRecievedFromWifiManager.size() == 0 && scanListData != null && scanListData.size() > 0)) {
            scanListDataRecievedFromWifiManager.clear();
            scanListDataRecievedFromWifiManager.addAll(removeDuplicates(scanListData));
            showScanListFromWifiManager = true;
            if (showScanListFromWifiManager) {
                showScanListFromWifiManager = false;
                showScanList(scanListDataRecievedFromWifiManager);
            }
        }

    }

    public ArrayList<ScanListData> removeDuplicates(ArrayList<ScanListData> list) {
        Log.v("scanListDataTestRemove", "before removeDuplicates available:--->" + list.size());
        ArrayList<ScanListData> newList = new ArrayList();

        for (ScanListData element : list) {
            if (!isSsidAlreadyPresent(newList, element.getSsid())) {
                newList.add(element);
            }
        }
        Log.v("scanListDataTestRemove", "After removeDuplicates available:--->" + newList.size());
        return newList;
    }

    private boolean isSsidAlreadyPresent(ArrayList<ScanListData> scanListData, String ssid) {
        boolean isSsidAlreadyPresent = false;

        for (ScanListData scanListData1 : scanListData) {
            if (scanListData1.getSsid().equalsIgnoreCase(ssid))
                return true;
        }

        return isSsidAlreadyPresent;
    }


    class WifiReceiver extends BroadcastReceiver {
        WifiManager wifiManager;
        StringBuilder sb;
        WifiScanListInterface wifiScanListInterface;

        public WifiReceiver(WifiManager wifiManager, WifiScanListInterface wifiScanListInterface) {
            this.wifiManager = wifiManager;
            this.wifiScanListInterface = wifiScanListInterface;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                sb = new StringBuilder();
                List<ScanResult> wifiList = wifiManager.getScanResults();
                ArrayList<String> deviceList = new ArrayList<>();
                ArrayList<ScanListData> scanListDataArrayList = new ArrayList<>();


                for (ScanResult scanResult : wifiList) {
                    ScanListData scanListData = new ScanListData();
                    scanListData.setSsid(scanResult.SSID);
                    scanListData.setSecurity(getSecurityType(scanResult.capabilities));
                    scanListData.setRssi(String.valueOf(scanResult.level));
                    String s = sb.toString();
                    sb.setLength(0);
                    deviceList.add(s);
                    scanListDataArrayList.add(scanListData);
                }

                wifiScanListInterface.onScanListRecievedFromWifiManager(scanListDataArrayList);
                LibreLogger.d(this, "suma in empty scanlist from wifi manager");
            }
        }

    }
}
