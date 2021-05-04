package com.libre.irremote;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.BLEApproach.BLEEvntBus;
import com.libre.irremote.BLEApproach.BLEScanActivity;
import com.libre.irremote.BLEApproach.BleCommunication;
import com.libre.irremote.BLEApproach.BleReadInterface;
import com.libre.irremote.BLEApproach.BleWriteInterface;
import com.libre.irremote.BLEApproach.HotSpotOrSacSetupActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.alexa_signin.AlexaSignInActivity;

import com.libre.irremote.R;
import com.libre.libresdk.Util.LibreLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static android.util.Log.d;
import static com.libre.irremote.Constants.Constants.READ_ALEXA_STATUS_IN_INTERVALS;
import static com.libre.irremote.Constants.Constants.TIMEOUT_FOR_SEARCHING_DEVICE;
import static com.libre.irremote.Constants.Constants.WIFI_CONNECTING_NOTIFICATION;


public class MavidBLEConfigureSpeakerActivity extends BaseActivity implements BleReadInterface, BleWriteInterface {

    String sacBleConnecting;
    int count = 1, sacBleConnectingIndex, alexacount = 0;
    private final int GET_SCAN_LIST_HANDLER = 0x200;
    boolean alreadyExecuted = false, alreadySignINShown = false;
    String finalConnectedArray, finalConnectedString;
    int finalConnectedIndex;
    boolean isSacTimedOutCalled = false;

    String configuratonfailedStringBle;

    int configuratonfailedIndexBle;

    boolean scaCred = false;

    static boolean isActivityActive = false;

    CafeBar cafeBar;

    int finalPayloadLength;
    private boolean alreadyDialogueShown = false;
    boolean isRefreshedAlexaToken = false;
    String sacSsid;
    Bundle bundle;
    byte[] b;
    private Dialog alert, wrongSSidAlert;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case GET_SCAN_LIST_HANDLER:
                    handler.removeMessages(GET_SCAN_LIST_HANDLER);
//                    scanListFailed("Scan List", "Fetching scan list failed.Please try again", BLEConfigureActivity.this);
//                    somethingWentWrong();
                    break;
            }
            if (message.what == WIFI_CONNECTING_NOTIFICATION) {

                LibreLogger.d(this, "wifi timer for every 30seconds in wifi connecting HANDLER\n");
                LibreLogger.d(this, "suma in read wifi status intervals connecting timeout for 30 seconds ");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
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
                            " Please make sure your speaker is blinking multiple colours and then try again.", MavidBLEConfigureSpeakerActivity.this);

                }
            } else if (message.what == Constants.WIFI_CONNECTED_NOTIFICATION) {
                LibreLogger.d(this, "suma in wifi connecting event send empty msg delayed for 30 seconds two ");

            } else if ((message.what == TIMEOUT_FOR_SEARCHING_DEVICE)) {
                handler.removeMessages(TIMEOUT_FOR_SEARCHING_DEVICE);
                showDialogifDeviceNotFound(getString(R.string.noDeviceFound));
                LibreLogger.d(this, "suma in connectTowifi nodevicefound");

            } else if (message.what == READ_ALEXA_STATUS_IN_INTERVALS) {
                LibreLogger.d(this, "suma in state is getting readalexastatus intervals");
                if (alexacount == 5) {
                    LibreLogger.d(this, "suma in state is getting readalexastatus intervals inside alexa count \n");

                }
            }

        }
    };


    private void showDialogifDeviceNotFound(final String Message) {
        if (!MavidBLEConfigureSpeakerActivity.this.isFinishing()) {

            alreadyDialogueShown = true;

            alert = new Dialog(MavidBLEConfigureSpeakerActivity.this);

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
                        Intent ssid = new Intent(MavidBLEConfigureSpeakerActivity.this, MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(ssid);
                        finish();
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

    public int getDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1 << 8 | b2 & 0xFF);

        LibreLogger.d(this, "Data length is returned as s" + s);
        return s;
    }


//    private void getDataLengthString(byte[] value) {
//        if (value[2] == 0 && value.length > 6) {
//            LibreLogger.d(this, "suma in data length\n" + value.length);
//            for (int i = 5; i < value.length - 5; i++) {
//                LibreLogger.d(this, "suma in gettin index value" + value[i]);
//                try {
//                    String finalConnecting = new String(value, "UTF-8");
//                    int finalConnectingIndex = finalConnecting.indexOf("Connecting");
//                   // LibreLogger.d(this, "suma wifi connecting only splitted array string\n" + finalConnecting.substring(finalConnectingIndex));
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mavid_configure_speaker_activity);
        MavidApplication.noReconnectionRuleToApply = true;
        LibreLogger.d(this, "suma checking alexa sign in activity oncreate");

        disableNetworkChangeCallBack();
        disableNetworkOffCallBack();
        handler.sendEmptyMessageDelayed(GET_SCAN_LIST_HANDLER, 30000);
        isActivityActive = true;
//       if(MavidApplication.got17DeviceStatusResponse){
//           if(!MavidApplication.deviceConnectedSSID.equals(getconnectedSSIDname(MavidBLEConfigureSpeakerActivity.this))){
////                BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
////                BleCommunication.writeInteractorStopSac();
//               LibreLogger.d(this,"AVS STATE state is getting sending wrong network stopsac");
//
//           }
//           else {
////               if (!alreadySignINShown) {
////                   alreadySignINShown = true;
//                   Intent Intent = new Intent(MavidBLEConfigureSpeakerActivity.this, AlexaSignInActivity.class);
//                   Intent.putExtra("speakerIpaddress", MavidApplication.mBLESACDeviceAddress);
//                   startActivity(Intent);
//                   LibreLogger.d(this, "suma checking alexa sign in activity1");
//                   MavidApplication.currentDeviceIP = MavidApplication.mBLESACDeviceAddress;
//                   MavidApplication.checkifAlexaSignStartedDuringConfiguration = true;
//                   LibreLogger.d(this, "suma wifi connecting text response String in MAVIDBLE ALEXA SIGNIN\n" + MavidApplication.mBLESACDeviceAddress + "count\n" + count);
//            //   }
//           }
//       }
//        BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
//        BleCommunication.writeInteractor(b);


    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        LibreLogger.d(this, "suma wifi connecting text response register onStart MAVIDBLECONF");

    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        LibreLogger.d(this, "suma wifi connecting text response unregister onStop MAVIDBLECONF");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BLEEvntBus event) {
        byte[] value = event.message;

        int response2 = getDataLength(value);
        d("RecievedState", "state is getting value" + response2);

        if (!alreadyExecuted) {
            alreadyExecuted = true;
            if (response2 == 17) {
                handler.removeMessages(WIFI_CONNECTING_NOTIFICATION);
                handler.removeCallbacksAndMessages(null);
                d("RecievedState", "Getting the ble Status state is getting value inside execute only once\n" + response2 + "count boolean\n" + alreadyExecuted);

                if (!MavidApplication.deviceConnectedSSID.equals(getconnectedSSIDname(MavidBLEConfigureSpeakerActivity.this))) {
//                BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
//                BleCommunication.writeInteractorStopSac();
                    LibreLogger.d(this, "AVS STATE state is getting sending wrong network stopsac");

                } else {
                    if (!alreadySignINShown) {
                        alreadySignINShown = true;

                        dismissWrongSSidAlerDialog();

                        Intent intent = new Intent(MavidBLEConfigureSpeakerActivity.this, AlexaSignInActivity.class);
                        intent.putExtra("speakerIpaddress", MavidApplication.mBLESACDeviceAddress);
                        BleCommunication.writeInteractorStopSac();

                        intent.putExtra("BeforeSignScreen", MavidBLEConfigureSpeakerActivity.class.getSimpleName());

                        startActivity(intent);

                        finish();

                        if (HotSpotOrSacSetupActivity.hotSpotOrSacSetupActivity != null) {
                            HotSpotOrSacSetupActivity.hotSpotOrSacSetupActivity.finish();
                        }

                        if (BLEScanActivity.bleScanActivity != null) {
                            BLEScanActivity.bleScanActivity.finish();
                        }

                        finish();

                        LibreLogger.d(this, "suma checking alexa sign in activity1");
                        MavidApplication.currentDeviceIP = MavidApplication.mBLESACDeviceAddress;
                        MavidApplication.checkifAlexaSignStartedDuringConfiguration = true;
                        LibreLogger.d(this, "suma wifi connecting text response String in MAVIDBLE ALEXA SIGNIN\n" + MavidApplication.mBLESACDeviceAddress + "count\n" + count);
//
//                                          BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
//                                          BleCommunication.writeInteractorStopSac();
                    }


//            catch(UnsupportedEncodingException e){
//                        e.printStackTrace();
//                    }
                    //  }
//                LibreLogger.d(this, "suma wifi connecting text response String in MAVIDBLE ALEXA SIGNIN\n"+mSACConfiguredIpAddress);
//                Intent Intent = new Intent(MavidBLEConfigureSpeakerActivity.this, AlexaSignInActivity.class);
//                Intent.putExtra("speakerIpaddress", mSACConfiguredIpAddress);
//                startActivity(Intent);
//                LibreLogger.d(this,"suma checking alexa sign in activity1");
//                MavidApplication.currentDeviceIP=mSACConfiguredIpAddress;
//                MavidApplication.checkifAlexaSignStartedDuringConfiguration=true;
//                BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
//                BleCommunication.writeInteractorStopSac();


//                Intent Intent = new Intent(MavidBLEConfigureSpeakerActivity.this, AlexaSignInActivity.class);
//                Intent.putExtra("speakerIpaddress", mSACConfiguredIpAddress);
//                startActivity(Intent);
//                LibreLogger.d(this,"suma checking alexa sign in activity1");
//                MavidApplication.currentDeviceIP=mSACConfiguredIpAddress;
//                MavidApplication.checkifAlexaSignStartedDuringConfiguration=true;
//                LibreLogger.d(this, "suma wifi connecting text response String in MAVIDBLE ALEXA SIGNIN\n"+mSACConfiguredIpAddress);


                }
//                        alreadyExecuted = true;
            }

        }
        if (response2 == 4) {
            d("BLEConfigureActivity", "bluetooth suma in finalpayload else in if suma in bleconfigure activity connecting\n");

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
                        sacBleConnectingIndex = sacBleConnecting.indexOf("Connecting");
                        LibreLogger.d(this, "suma wifi connecting only splitted array string\n" + sacBleConnecting.substring(sacBleConnectingIndex));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }


            }

        }


        if (response2 == 5) {
            d("BLEConfigureActivity", "bluetooth suma in finalpayload else in if suma in bleconfigure activity connected\n" + response2);
            //closeLoader();
            handler.removeMessages(WIFI_CONNECTING_NOTIFICATION);
            handler.removeCallbacksAndMessages(null);

            LibreLogger.d(this, "suma in wifi connecting close loader 5");
            if (value[2] == 0 && value.length > 6) {
                for (int i = 5; i < value.length - 5; i++) {
                    try {
                        finalConnectedArray = new String(value, "UTF-8");
                        finalConnectedIndex = finalConnectedArray.indexOf("Conn");
                        finalConnectedString = finalConnectedArray.substring(finalConnectedIndex);
                        LibreLogger.d(this, "wifi timer for every 30seconds connected status\n" + finalConnectedArray.substring(finalConnectedIndex) + "final connected string\n" + finalConnectedString);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateResponseUI(finalConnectedString);

                }
            });
//            BleCommunication.writeInteractorDeviceStatus();
//            try {
//                LibreMavidHelper.advertise();
//                LibreLogger.d(this,"suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery mavid ble");
//
//            } catch (WrongStepCallException e) {
//                e.printStackTrace();
//            }
            //doNext();
//            //StopSAC
//            BleCommunication bleCommunication = new BleCommunication(BLEConfigureActivity.this);
//            BleCommunication.writeInteractorStopSac();


        }

        if (response2 == 14) {
            closeLoader();
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC timeout dialogue");
            //StopSAC
            if (!isSacTimedOutCalled) {
                BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
                BleCommunication.writeInteractorStopSac();
                LibreLogger.d(this, "suma in getting stop sac activity 14 mavidble configure");

                ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..",
                        "Please put the device to setup mode", MavidBLEConfigureSpeakerActivity.this);
                isSacTimedOutCalled = true;
            }
        }


        if (response2 == 17) {
//                handler.removeMessages(WIFI_CONNECTING_NOTIFICATION);
//                handler.removeCallbacksAndMessages(null);
            //  closeLoader();
            LibreLogger.d(this, "Getting the ble Status suma in gettin response value Read Device status MavidBLEConfigureSpeakerActivity");

        }

        if (response2 == 15) {
            closeLoader();
            MavidApplication.checkifAlexaSignStartedDontGOTOHomeTABS = false;
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
//                BleCommunication bleCommunication = new BleCommunication(BLEConfigureActivity.this);
//                bleCommunication = new BleCommunication(BLEConfigureActivity.this);
                ShowAlertDynamicallyGoingToHomeScreen(configuratonfailedStringBle.substring(configuratonfailedIndexBle),
                        "Please make sure Credential entered is correct and Try Again!", MavidBLEConfigureSpeakerActivity.this);
                //BleCommunication.writeInteractorStopSac();
                //suma commenting to check ble disconnect issue
                LibreLogger.d(this, "suma in getting stop sac activity mavid ble configure 15");

            }
        }

        if (response2 == 13) {
            closeLoader();
//            ShowAlertDynamicallyGoingToHomeScreen("Sac Not Allowed...",
//                    "Please make sure your device is in setup mode", MavidBLEConfigureSpeakerActivity.this);
        }
        if (response2 == 16) {
            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC connection wifi failed");
            // ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..","Please put the device to setup mode");
        }
        if (!scaCred) {
            if (value[2] == 1) {
                scaCred = true;
                finalPayloadLength = getDataLength(value);
                d("Bluetooth", "finalPayloadLength size: " + finalPayloadLength);
                d("Bluetooth", "bluetooth suma in finalpayload" + finalPayloadLength);
                LibreLogger.d(this, "suma check the condition in sacred if ");
            }
            LibreLogger.d(this, "suma wifi connecting text response else case2");

        } else {
            LibreLogger.d(this, "suma wifi connecting text response else case1");
            byte[] cd = new byte[finalPayloadLength];
            LibreLogger.d(this, "suma check the condition in sacred else");
//            if (value[2] == 0 && value.length > 6) {
//                LibreLogger.d(this, "suma in data length\n" + value.length);
//              //  getDataLengthString(value);
//
//            }
            if (baos.size() <= finalPayloadLength) {
                LibreLogger.d(this, "suma wifi connecting text response else case3");

                int response = getDataLength(value);
                d("Bluetooth", "finalPayloadLength size: get response " + finalPayloadLength + "getting baos size\n" + baos.size());
                d("Bluetooth", "bluetooth suma in finalpayload else in if suma in bleconfigure activity" + response);
                LibreLogger.d(this, "suma check the condition in sacred else one more if check baos length if");

                try {
                    if (response == 0) {
                        d("Bluetooth", "sac credential ok vlaue in less than size baos\n" + response);
                        showLoader("", "Waiting for device response...");
                    }
                    if (response == 1) {
                        closeLoader();
                        if (isActivityActive) {
                            updateResponseUI("Invalid credentials");
                            d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next response 1" + response);
                        }
                        d("Bluetooth", "invalid credentials" + response);
                    }


                    if (response == 14) {
                        if (!isSacTimedOutCalled) {
                            LibreLogger.d(this, "Getting the HotSpot Status suma in gettin response value SAC timeout dialogue");
                            ShowAlertDynamicallyGoingToHomeScreen("Setup Timeout..",
                                    "Please put the device to setup mode", MavidBLEConfigureSpeakerActivity.this);
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

                        d("Bluetooth", "bluetooth suma in finalpayload else in if try response  do next response scanlist getdatalengthvalue! 10" + response);
                    }

                } catch (IOException e) {
                    Log.d("BLEConfigureActivity", e.getMessage());
                }
                d("Bluetooth", "baos " + baos);
                d("BLEConfigureActivity", "bluetooth suma in finalpayload after exception! 10" + response);

            } else {
                d("Bluetooth", "strAscii: " + new String(cd, 0, finalPayloadLength));
                d("Bluetooth", "baos " + baos);
                d("Bluetooth", "baos.size(): " + baos.size());
                int response = getDataLength(value);
                LibreLogger.d(this, "suma check the condition in sacred else one more if check baos length else");

                d("Bluetooth", "bluetooth suma in finalpayload response 0  in else getdatalength" + response);
                if (response == 0) {
                    d("Bluetooth", "sac credential ok vlaue else part\n" + response);

                    d("BLEConfigureActivity", "credentials ok" + value);
                    if (isActivityActive) {
                        updateResponseUI("Credentials succesfully posted.");
                    }
                    d("Bluetooth", "credentials ok next value do next3\n" + response);

                } else if (response == 1) {
                    closeLoader();
                    if (isActivityActive) {
                        updateResponseUI("Invalid credentials");
                    }
                    d("Bluetooth", "bluetooth suma in finalpayload response 1 in else" + response);

                }


            }
        }
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


    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(MavidBLEConfigureSpeakerActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MavidApplication.noReconnectionRuleToApply = true;

        LibreLogger.d(this, "Checking which activity is getting called in onresume MAVIDBLE\n" + MavidApplication.isACLDisconnected);
        LibreLogger.d(this, "suma in getting device ssid\n" + MavidApplication.deviceConnectedSSID + "mobile connected SSID\n" + getconnectedSSIDname(MavidBLEConfigureSpeakerActivity.this));
        if (!MavidApplication.deviceConnectedSSID.equals(getconnectedSSIDname(MavidBLEConfigureSpeakerActivity.this))) {
            ShowAlertDynamicallyGoingWrongSSICONNECTED_TO_DIFFERENT_SSIDD("", getResources().getString(R.string.title_error_connection) + "( " + getconnectedSSIDname(this) + ")" +
                    " \n" + getString(R.string.title_error_connection2) + "(" + MavidApplication.deviceConnectedSSID + ")" + "\n" + getString(R.string.title_error_connection3) + "( " + MavidApplication.deviceConnectedSSID + ")", this);
            LibreLogger.d(this, "suma in getting device ssid ONE\n" + MavidApplication.deviceConnectedSSID + "mobile connected SSID\n" + getconnectedSSIDname(MavidBLEConfigureSpeakerActivity.this));

        } else {
            if (MavidApplication.clickedWrongNetwork) {
                LibreLogger.d(this, "suma in getting device ssid TWO\n" + MavidApplication.deviceConnectedSSID + "mobile connected SSID\n" + getconnectedSSIDname(MavidBLEConfigureSpeakerActivity.this));
                alertForNetworkChange(MavidBLEConfigureSpeakerActivity.this);
                LibreLogger.d(this, "suma in n/w change 3");

            }
        }
//        if(MavidApplication.clickedWrongNetwork&&){
//            alertForNetworkChange(MavidBLEConfigureSpeakerActivity.this);
////            BleCommunication bleCommunication = new BleCommunication(MavidBLEConfigureSpeakerActivity.this);
////            BleCommunication.writeInteractorStopSac();
//        }
        //device is connected to <dev ssid>
    }


    public void dismissWrongSSidAlerDialog() {
        if (wrongSSidAlert != null) {
            if (wrongSSidAlert.isShowing()) {
                wrongSSidAlert.dismiss();
                wrongSSidAlert = null;
            }
        }
    }

    public void ShowAlertDynamicallyGoingWrongSSICONNECTED_TO_DIFFERENT_SSIDD(String title, String message, final AppCompatActivity appCompatActivity) {


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

    @Override
    public void onReadSuccess(byte[] data) throws IOException {
        d("onReadSccBLEConfigure: ", "data : " + data);
    }

    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onWriteFailure() {

    }


}
