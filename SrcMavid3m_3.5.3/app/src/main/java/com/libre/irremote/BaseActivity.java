package com.libre.irremote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.danimahardhika.cafebar.CafeBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.libre.irremote.BLEApproach.BLEScanActivity;
import com.libre.irremote.BLEApproach.BleCommunication;
import com.libre.irremote.BLEApproach.BleWriteInterface;
import com.libre.irremote.alexa_signin.AlexaSignInActivity;

import com.libre.irremote.R;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.models.ModelSaveHotSpotName;
import com.libre.irremote.receivers.GpsLocationReceiver;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by bhargav on 6/2/18.
 */

public class BaseActivity extends AppCompatActivity implements BleWriteInterface {
    private boolean listenToNetworkChanges;
    private boolean listenToWifiConnectingStatus;
    private NetworkReciever networkReciever;
    private AlertDialog alertDialog;
    AlertDialog.Builder builder;
    private Dialog alert;
    private Dialog alert1;
    private Dialog alert2;

    String checkWhichActivity;
    private static String SSID = "";
    CafeBar cafeBar;

    ProgressDialog mProgressDialog;

    private Dialog mDialog;

    AppCompatTextView progress_title;
    ProgressBar progress_bar;

    AppCompatTextView progress_message;


    private static final long SCAN_PERIOD = 10000;

    private boolean mScanning;


    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;
    AppCompatButton btn_cancel;

    private static String previousSSid = "0";


    public GpsLocationReceiver gpsLocationReceiver = new GpsLocationReceiver();

//    public void showLoader(final String title, final String message) {
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("ShowingLoader", "Showing loader method");
//                if (mProgressDialog == null) {
//                    try {
//                        mProgressDialog = ProgressDialog.show(BaseActivity.this, title, message, true, true, null);
//                        mProgressDialog.setCancelable(false);
//                    } catch (WindowManager.BadTokenException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (!mProgressDialog.isShowing()) {
//                    if (!(BaseActivity.this.isFinishing())) {
//                        mProgressDialog = ProgressDialog.show(BaseActivity.this, title, message, true, true, null);
//                        mProgressDialog.setCancelable(false);
//
//                    }
//                }
//
//            }
//        });
//    }

    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(BaseActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    public void showLoader(final String title, final String message) {

        if (!(BaseActivity.this.isFinishing())) {

            if (mDialog == null) {
                mDialog = new Dialog(BaseActivity.this);
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mDialog.setContentView(R.layout.custom_progress_bar);

                mDialog.setCancelable(false);
                progress_title = mDialog.findViewById(R.id.progress_title);
                progress_bar = mDialog.findViewById(R.id.progress_bar);
                progress_message = mDialog.findViewById(R.id.progress_message);
            }
            Log.d("ShowingLoader", "Showing loader method");
            progress_title.setText(title);
            progress_message.setText(message);
            progress_bar.setIndeterminate(true);
            progress_bar.setVisibility(View.VISIBLE);
            if (!(getApplicationContext() instanceof BaseActivity && ((BaseActivity) getApplicationContext()).isFinishing())) {
                mDialog.show();
            }

        }
    }


    public boolean checkLocationIsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !MavidApplication.doneLocationChange) {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGpsEnabled && !isNetworkEnabled) {
                askToEnableLocationService(context);
                LibreLogger.d(this, "suma in n/w change 25");
                MavidApplication.doneLocationChange = false;
                return false;
            } else {
                MavidApplication.doneLocationChange = true;
                LibreLogger.d(this, "suma in n/w change 26");

                //MavidApplication.bothLOCPERMISSIONgIVEN=true;

            }
        }
//        MavidApplication.doneLocationChange = true;
        return true;
    }

    public void askToEnableLocationService(final Context context) {

        if (alert == null) {

            alert = new Dialog(context);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText(context.getResources().getString(R.string.locationServicesIsOff));

        tv_alert_message.setText(context.getResources().getString(R.string.enableLocation));

        btn_ok.setText(context.getResources().getString(R.string.gotoSettings));

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                turnGPSOn(context);
            }
        });

        alert.show();

//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle()
//                .setMessage()
//                .setPositiveButton(, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//
//
//                    }
//                });
//        builder.setCancelable(false);
//        builder.create();
//        builder.show();
    }

    public void turnGPSOn(Context context) {
        Intent intent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public void closeLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!BaseActivity.this.isFinishing() && mDialog != null) {
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }
        });
    }

//    public void closeLoader() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                    if (!(BaseActivity.this.isFinishing())) {
//                        // mProgressDialog.setCancelable(false);
//                        mProgressDialog.dismiss();
//                        mProgressDialog.cancel();
//                    }
//                }
//            }
//        });

//}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SSID = getconnectedSSIDname(BaseActivity.this);
        super.onCreate(savedInstanceState);
        enableNetworkChangeCallBack();
        enableNetworkOffCallBack();
        networkReciever = new NetworkReciever();
//        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
//        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
//        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        this.registerReceiver(BTReceiver, filter1);
//        this.registerReceiver(BTReceiver, filter2);
//        this.registerReceiver(BTReceiver, filter3);
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        try {
//            registerReceiver(networkReciever, intentFilter);
//        }catch (Exception e){
//            Log.d("BaseActivity","receiver "+e.getMessage());
//        }

        registerReceiver(gpsLocationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWhichActivity = BaseActivity.this.getClass().getSimpleName().toString();
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        if (locationManager != null) {
//            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//            LibreLogger.d(this, "suma in n/w change 19\n" + isGpsEnabled + "n/w enabled\n" + isNetworkEnabled);
//
//        }
        Log.d("suma in mac", "receiver in onresume base activity");
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(BTReceiver, filter1);
        this.registerReceiver(BTReceiver, filter2);
        this.registerReceiver(BTReceiver, filter3);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        try {
            registerReceiver(networkReciever, intentFilter);
        } catch (Exception e) {
            Log.d("BaseActivity", "receiver " + e.getMessage());
        }

//        LibreMavidHelper.setAdvertiserMessage(getMsearchPayload());
//        try {
//            LibreMavidHelper.advertise();
//            LibreLogger.d(this,"suma in getting discovery msearch MyActivity BaseActivity onresume");
//
//        } catch (WrongStepCallException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableNetworkChangeCallBack();
        disableNetworkOffCallBack();
        try {
            this.unregisterReceiver(networkReciever);
            this.unregisterReceiver(gpsLocationReceiver);
        } catch (Exception e) {

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("suma in mac", "receiver in onstop base activity");

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public String getMsearchPayload() {
        String mSearchPayload = "M-SEARCH * HTTP/1.1\r\n" +
                "MX: 10\r\n" +
                "ST: urn:schemas-upnp-org:device:DDMSServer:1\r\n" +
                "HOST: 239.255.255.250:1800\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "\r\n";
        return mSearchPayload;
    }

    public void disableNetworkOffCallBack() {
        listenToWifiConnectingStatus = false;
    }

    public void enableNetworkOffCallBack() {
        listenToWifiConnectingStatus = true;
    }

    public void disableNetworkChangeCallBack() {
        listenToNetworkChanges = false;
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            String checkWhichActivity = BaseActivity.this.getClass().getSimpleName().toString();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                checkWhichActivity = BaseActivity.this.getClass().getSimpleName().toString();
                //  startActivity(new Intent(BaseActivity.this, HotSpotOrSacSetupActivity.class));
//                  Toast.makeText(getApplicationContext(), "BT Connected", Toast.LENGTH_SHORT).show();
                LibreLogger.d(this, "SUMA IN BASEACTIVITY BleCommunication in suma ble state turning connected");

                MavidApplication.isACLDisconnected = false;
                switch (checkWhichActivity) {
                    case "BLEConfigureActivity":
                        // startActivity(new Intent(BaseActivity.this, HotSpotOrSacSetupActivity.class));
                        // Toast.makeText(getApplicationContext(), "BLE Device Connected..Try Again ur Device Setup", Toast.LENGTH_SHORT).show();

                        break;
                    case "MavidBLEConfigureSpeakerActivity":
                        LibreLogger.d(this, "Checking which activity is getting called when CONNECTED in receiver MavidBle" + checkWhichActivity);
                        break;
                    case "HotSpotOrSacSetupActivity":
                        LibreLogger.d(this, "Checking which activity is getting called when CONNECTED in receiver sacsetup option" + checkWhichActivity);
                        // startActivity(new Intent(BaseActivity.this, BLEScanActivity.class));
                        LibreLogger.d(this, "fire On BLE Conenction Success next onHotspotSacSetup");

                        break;
                    case "SignUpConfirm":
                    case "UserActivity":
                    case "StelleLoginActivity":
                    case "ConnectingToMainNetwork":
                    case "AlexaLangUpdateActivity":
                    case "AlexaSignInActivity":
                    case "AlexaThingsToTryDoneActivity":
                    case "Terms_Conditions_Stelle":
                    case "Firmware_Update_Stelle":
                        return;
                }
                LibreLogger.d(this, "Checking which activity is getting called when CONNECTED in receiver" + checkWhichActivity);

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                checkWhichActivity = BaseActivity.this.getClass().getSimpleName().toString();

                //suma commenting toast conn lost
//                if(this.getClass().getName().equals("BLEConfigureActivity")){
//                    LibreLogger.d(this, "BleCommunication in suma ble state turning DISCONNECTED in receiver if"+checkWhichActivity);
//
//                }
//                else{
//                    LibreLogger.d(this, "BleCommunication in suma ble state turning DISCONNECTED in receiver else"+checkWhichActivity);
//
//                }

                LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver" + checkWhichActivity);
                if (checkWhichActivity.equals("BLEConfigureActivity")) {
                    LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver BLEConfigure" + checkWhichActivity);

                } else if (checkWhichActivity.equals("MavidBLEConfigureSpeakerActivity")) {
                    if (MavidApplication.checkifAlexaSignStartedDuringConfiguration) {
                        MavidApplication.checkIsFwBtnLatest = true;
                        MavidApplication.checkifAlexaSignStartedDontGOTOHomeTABS = true;

                        Intent newIntent = new Intent(BaseActivity.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        newIntent.putExtra("speakerIpaddress", MavidApplication.currentDeviceIP);
                        LibreLogger.d(this, "suma wifi connecting text response String checking alexa sign in activity8");
                        LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver MAVIDBLEConfigure" + checkWhichActivity);


                    } else {
                        startActivity(new Intent(BaseActivity.this, MavidHomeTabsActivity.class));
                        LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver HOMETABS" + checkWhichActivity);

                    }
                    //   LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver MavidBle"+checkWhichActivity);

                }
                //Toast.makeText(getApplicationContext(), "Connection lost to the device.Try again later", Toast.LENGTH_SHORT).show();

//                 else if(checkWhichActivity.equals("HotSpotOrSacSetupActivity")){
//                     LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver sacsetup option"+checkWhichActivity);
//                      startActivity(new Intent(BaseActivity.this, BLEScanActivity.class));
//                 }
//                switch (checkWhichActivity) {
//                    case "BLEConfigureActivity":
//                      //  startActivity(new Intent(BaseActivity.this, HotSpotOrSacSetupActivity.class));
//                      //  startActivity(new Intent(BaseActivity.this, BLEScanActivity.class));
//                        LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver BLEConfigure"+checkWhichActivity);
//                        Toast.makeText(getApplicationContext(), "Connection lost to the device.Try again later", Toast.LENGTH_SHORT).show();
//                      break;
//                    case "MavidBLEConfigureSpeakerActivity":
//                        LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver MavidBle"+checkWhichActivity);
//                        //startActivity(new Intent(BaseActivity.this, MavidHomeTabsActivity.class));
//
//                        break;
//                    case "HotSpotOrSacSetupActivity":
//                        LibreLogger.d(this, "Checking which activity is getting called when DISCONNECTED in receiver sacsetup option"+checkWhichActivity);
//                      //  startActivity(new Intent(BaseActivity.this, BLEScanActivity.class));
//                       // Toast.makeText(getApplicationContext(), "Connection lost to the device.Try again later", Toast.LENGTH_SHORT).show();
//                      break;
//                    case "SignUpConfirm":
//                    case "UserActivity":
//                    case "StelleLoginActivity":
//                    case "ConnectingToMainNetwork":
//                    case "AlexaLangUpdateActivity":
//                    case "AlexaSignInActivity":
//                    case "AlexaThingsToTryDoneActivity":
//                    case "Terms_Conditions_Stelle":
//                    case "Firmware_Update_Stelle":
//                        return;
//                }
                //  Toast.makeText(getApplicationContext(), "Connection Lost to the Device"+getApplicationContext(), Toast.LENGTH_SHORT).show();
                //   LibreLogger.d(this, "SUMA IN SCANREQ BleCommunication in suma ble state turning disconnected baseactivity");
                MavidApplication.isACLDisconnected = true;
//                Intent ssid = new Intent(BaseActivity.this, MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(ssid);
//              //  Log.v("RecievedState", "AVS STATE 0 COUNT isFinishing onBTN click ..................." + avsStatetwoCount);
//                finish();
                //Do something if disconnected suma commenting for now addimg log instead
                LibreLogger.d(this, "SUMA IN BASEACTIVITY BleCommunication in suma ble state turning disconnected");
                //  Toast.makeText(getApplicationContext(), "Connection lost to the device.Try again later", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void enableNetworkChangeCallBack() {
        listenToNetworkChanges = true;
    }


//    private void scanLeDevice(final Handler handler, final boolean enable, final Context context,
//                              final BluetoothAdapter mBluetoothAdapter) {
//
//
//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    closeLoader();
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(leScanCallback);
//                    if (leDeviceListAdapter.getCount()==0){
//                        Intent intent  = new Intent(context, NoBLEDeviceFragment.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                }
//            }, SCAN_PERIOD);
//
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(leScanCallback);
//        } else {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(leScanCallback);
//        }
//    }
//
//    private BluetoothAdapter.LeScanCallback leScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi,
//                                     byte[] scanRecord) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (device.getName() != null) {
//                                String modelType1 = "Libre";
//                                String modelType2 = "Microdot";
//                                String modelType3 = "Wave";
//
//                                if (device.getName().toLowerCase().contains(modelType1.toLowerCase())
//                                        || device.getName().toLowerCase().contains(modelType2.toLowerCase())
//                                        || device.getName().toLowerCase().contains(modelType3.toLowerCase())) {
////                                    closeLoader();
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            //add a bus to
////                                            leDeviceListAdapter.addDevice(device);
////                                            leDeviceListAdapter.notifyDataSetChanged();
//                                        }
//                                    });
//
//                                }
////        device.connectGatt()
////                                }
//                            }
//                        }
//                    });
//                }
//            };

    public String getconnectedSSIDname(Context mContext) {
        WifiManager wifiManager;
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();

        Log.d("BaseActivity", "getconnectedSSIDname wifiInfo = " + wifiInfo.toString() + "locationChangeValue:");
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public String getExpectedMSearchResponse() {
        String message = "MSEARCH RESPONSE";
        return message;
    }


    public void refreshDiscovery() {
        try {
            LibreMavidHelper.advertise();
            LibreLogger.d(this, "suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery");

        } catch (WrongStepCallException e) {
            e.printStackTrace();
        }
    }

    public void showSacTimeoutAlert(final AppCompatActivity appCompatActivity, final BleWriteInterface bleWriteInterface) {

        if (alert == null) {

            alert = new Dialog(appCompatActivity);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText("SAC Timeout");

        tv_alert_message.setText("Please put the device into the setup mode");

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                //StopSAC
                alert = null;
                BleCommunication bleCommunication = new BleCommunication(bleWriteInterface);
                BleCommunication.writeInteractorStopSac();
                LibreLogger.d(this, "suma in getting stop sac activity 31");

                startActivity(new Intent(appCompatActivity, MavidHomeTabsActivity.class));
                finish();
            }
        });

        alert.show();


    }


    public void mobileDataOnOff(Context context) {
        try {

            if (alert == null) {

                alert = new Dialog(context);

                alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

                alert.setContentView(R.layout.custom_single_button_layout);

                alert.setCancelable(false);

                tv_alert_title = alert.findViewById(R.id.tv_alert_title);

                tv_alert_message = alert.findViewById(R.id.tv_alert_message);

                btn_ok = alert.findViewById(R.id.btn_ok);

            }

            tv_alert_title.setText("");

            tv_alert_message.setText(getResources().getString(R.string.switchOffMobiledata));


            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ////startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    alert.dismiss();
                    alert = null;
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
                    startActivity(intent);
                }
            });

            alert.show();


//            alertDialog = null;
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//            builder.setMessage(getResources().getString(R.string.switchOffMobiledata))
//                    .setCancelable(false)
//                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            //startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
//                            Intent intent = new Intent(Intent.ACTION_MAIN);
//                            intent.setClassName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
//                            startActivity(intent);
//                        }
//                    });
//
//            if (alertDialog == null) {
//                alertDialog = builder.show();
//            }
//
//            alertDialog.show();

        } catch (Exception e) {

        }
    }

    public void somethingWentWrong(final Context context) {
        try {

            if (alert == null) {

                alert = new Dialog(context);

                alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

                alert.setContentView(R.layout.custom_single_button_layout);

                alert.setCancelable(false);

                tv_alert_title = alert.findViewById(R.id.tv_alert_title);

                tv_alert_message = alert.findViewById(R.id.tv_alert_message);

                btn_ok = alert.findViewById(R.id.btn_ok);
            }

            tv_alert_title.setText("");
            tv_alert_message.setText(getResources().getString(R.string.somethingWentWrong));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    ((AppCompatActivity) context).finish();
                    alert = null;
                }
            });
            alert.show();

//            alertDialog = null;
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//            builder.setMessage(getResources().getString(R.string.somethingWentWrong))
//                    .setCancelable(false)
//                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            alertDialog.dismiss();
//                        }
//                    });
//
//            if (alertDialog == null) {
//                alertDialog = builder.show();
//                TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
//                messageView.setGravity(Gravity.CENTER);
//            }
//
//            alertDialog.show();

        } catch (Exception e) {

        }
    }

    public void alertForNetworkChange(final Context context) {
        if (!BaseActivity.this.isFinishing()) {
            /* If Restarting of Network Is happening We can discard the Network Change */
            alert = new Dialog(context);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);

            tv_alert_title.setText("");

            tv_alert_message.setText(context.getResources().getString(R.string.restartTitle));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    alert = null;
                    if (MavidApplication.clickedWrongNetwork) {
                        BleCommunication bleCommunication = new BleCommunication(BaseActivity.this);
                        BleCommunication.writeInteractorStopSac();
                        LibreLogger.d(this, "suma sent stop sac base activity 32");
                    }
                    restartApp(getApplicationContext());

                }
            });
            alert.show();
        }
    }


    public void ShowAlertDynamicallyGoingToHomeScreen(String title, String message, final AppCompatActivity appCompatActivity) {

        if (alert == null) {
            alert = new Dialog(appCompatActivity);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);


        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                startActivity(new Intent(appCompatActivity.getApplicationContext(), MavidHomeTabsActivity.class));
                finish();
            }
        });


        alert.show();
        Log.d("atul", "alert dialog");


    }

    public void ShowAlertDynamicallyConfigurationFailed(String title, String message, final AppCompatActivity appCompatActivity) {

        if (alert1 == null) {
            alert1 = new Dialog(appCompatActivity);

            alert1.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert1.setContentView(R.layout.custom_single_button_layout);

            alert1.setCancelable(false);

            tv_alert_title = alert1.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert1.findViewById(R.id.tv_alert_message);

            btn_ok = alert1.findViewById(R.id.btn_ok);


        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert1.dismiss();
                BleCommunication.writeInteractorStopSac();
                LibreLogger.d(this, "suma in getting stop sac activity 15");
                startActivity(new Intent(appCompatActivity.getApplicationContext(), MavidHomeTabsActivity.class));
                finish();

            }
        });


        alert1.show();


    }




    public void ShowAlertDynamicallyGoingWrongNetwork(String title, String message, final AppCompatActivity appCompatActivity) {


        if (alert == null) {


            alert = new Dialog(appCompatActivity);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);


        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                MavidApplication.clickedWrongNetwork = true;
                refreshDiscovery();
            }
        });

        if (!appCompatActivity.isFinishing()) {
            alert.show();
        }
    }






    public void ShowAlert_AlexaStatusStuckat2(String title, String message, final AppCompatActivity appCompatActivity) {


        if (alert1 == null) {


            alert1 = new Dialog(appCompatActivity);

            alert1.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert1.setContentView(R.layout.custom_single_button_layout);

            alert1.setCancelable(false);

            tv_alert_title = alert1.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert1.findViewById(R.id.tv_alert_message);

            btn_ok = alert1.findViewById(R.id.btn_ok);


        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                refreshDiscovery();
                Intent newIntent = new Intent(BaseActivity.this, MavidHomeTabsActivity.class);
                startActivity(newIntent);
                finish();


//                startActivity(new Intent(appCompatActivity.getApplicationContext(), MavidHomeTabsActivity.class));
//                finish();

            }
        });


        alert1.show();
        //  Log.d("atul", "alert dialog");


    }


    public void scanListFailed(String title, String message, final AppCompatActivity appCompatActivity) {
        if (alert == null) {


            alert = new Dialog(appCompatActivity);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);


        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                // appCompatActivity.onBackPressed();
                Toast.makeText(BaseActivity.this, "Connection lost to the device", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(BaseActivity.this, MavidHomeTabsActivity.class));
                finish();
            }
        });


        alert.show();
    }

    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onWriteFailure() {

    }

    //// receiver ////
    class NetworkReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Context newContext = getApplicationContext();

            Log.d("Receiver", "action = " + intent.getAction() + ", listenToNetworkChanges = " + listenToNetworkChanges);

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (!listenToNetworkChanges)
                return;

            if ((activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) == false &&
                    listenToWifiConnectingStatus) {
//                Toast.makeText(getApplicationContext(), "Network is disconnected", Toast.LENGTH_SHORT).show();

                try {

                    if (activeNetworkInfo != null) {
                        Log.d("Receiver", "Active network info:" + activeNetworkInfo.isConnectedOrConnecting());
                        Log.d("Receiver", "Active network info:" + activeNetworkInfo.isConnectedOrConnecting());

                        if (activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                            Log.d("Receiver", "Active network type:MOBILE");
                    } else {
                        Log.d("Receiver", "Active network interface is null...and hence we will show the alert box");
                        MavidApplication.doneLocationChange = false;
                    }
                } catch (Exception e) {

                }

                SSID = "";
                //alertBoxForNetworkOff();
                return;
            }

//            if (!intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")
//                    && !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
//                return;
//
//            if (SSID.equals(getconnectedSSIDname(newContext))) {
//                return;
//            }
//
//            // network change has happened
//            WifiManager wifiManager =
//                    (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//            final String ssid = getconnectedSSIDname(newContext);
//            //|| ssid.equals("<unknownssid>")
//            if (ssid == null || ssid.equals("") || ssid.equals("<unknown ssid>"))
//                return;

            if (!intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")
                    && !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
                return;


//            if (SSID.equals(getconnectedSSIDname(newContext))) {
//                return;
//            }

            // network change has happened
            WifiManager wifiManager =
                    (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            final String ssid = getconnectedSSIDname(newContext);

            Log.d("Receiver", "current SSID " + ssid + " previous SSid  " + previousSSid);


            //|| ssid.equals("<unknownssid>")
            boolean isSSidValid = true;

            if ((previousSSid.equalsIgnoreCase(ssid) ||
                    ssid.equalsIgnoreCase("<unknown ssid>") ||
                    previousSSid.equalsIgnoreCase("<unknown ssid>") ||
                    previousSSid.equalsIgnoreCase("0"))) {
                isSSidValid = true;
            } else {
                isSSidValid = false;
            }


            if (isSSidValid)
                previousSSid = ssid;
            //return;
            Log.d("Receiver", " M " + MavidApplication.doneLocationChange + " ssidValid " + isSSidValid);

            if (MavidApplication.doneLocationChange) {
                if (!ssid.equals(previousSSid))
                    alertForNetworkChange(BaseActivity.this);
                LibreLogger.d(this, "suma in n/w change 1");

            } else {
                LibreLogger.d(this, "suma in n/w change 2\n" + checkWhichActivity + "Connected ssid\n" + getconnectedSSIDname(BaseActivity.this));
                if (MavidApplication.bothLOCPERMISSIONgIVEN) {
                    LibreLogger.d(this, "suma in n/w change 11\n" + checkWhichActivity + "Connected ssid\n" + MavidApplication.bothLOCPERMISSIONgIVEN);
                    // alertForNetworkChange(context);

                } else {
//                   LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//                   boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                   boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    LibreLogger.d(this, "suma in n/w change 16\n");

                    if ((!(context instanceof BLEScanActivity))) {
                        //other than ble activity call network change alert box
                        // if(!MavidApplication.onlyAppLocationGiven) {
                        //   alertForNetworkChange(context);
                        LibreLogger.d(this, "suma in n/w change 10\n" + checkWhichActivity + "Connected ssid\n" + MavidApplication.bothLOCPERMISSIONgIVEN);
                        // }
//                           else{
//                               LibreLogger.d(this, "suma in n/w change 40\n" + checkWhichActivity + "Connected ssid\n" + MavidApplication.bothLOCPERMISSIONgIVEN);
//
//                           }
                    }
                }

            }
        }

    }


    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            context.startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        }
    }

    public String getHotSpotName(String deviceName) {
        SharedPreferences mPrefs = getSharedPreferences("Mavid", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String hotspotNamesListString = mPrefs.getString("hotspotNamesList", "");

        Type type = new TypeToken<List<ModelSaveHotSpotName>>() {
        }.getType();

        if (hotspotNamesListString != null) {
            if (!hotspotNamesListString.isEmpty()) {
                List<ModelSaveHotSpotName> modelSaveHotSpotNameList = gson.fromJson(hotspotNamesListString, type);
                if (modelSaveHotSpotNameList.size() > 0) {
                    for (ModelSaveHotSpotName modelSaveHotSpotName : modelSaveHotSpotNameList) {
                        if (modelSaveHotSpotName.friendlyName != null) {
                            if (modelSaveHotSpotName.friendlyName.equals(deviceName)) {
                                return modelSaveHotSpotName.hotSpotName;
                            }
                        }
                    }
                }
            }
        }
        return "Phone's Personal HotSpot";
    }


    public void removeOlderHotSpotName(String deviceName) {

    }

    public void alertBoxForNetworkOff() {
        if (!BaseActivity.this.isFinishing()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    BaseActivity.this);

            // set title
            alertDialogBuilder.setTitle(getString(R.string.wifiConnectivityStatus));

            // set dialog message
            alertDialogBuilder
                    .setMessage(getString(R.string.connectToWifi))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivityForResult(intent, 1234);

                        }
                    });

            // create alert dialog
            if (alertDialog == null)
                alertDialog = alertDialogBuilder.create();

            // show it

            try {
                alertDialog.show();
            } catch (Exception e) {

            }
            Log.d("Receiver", "connect to wifi dialog shown");
        }
    }

}
