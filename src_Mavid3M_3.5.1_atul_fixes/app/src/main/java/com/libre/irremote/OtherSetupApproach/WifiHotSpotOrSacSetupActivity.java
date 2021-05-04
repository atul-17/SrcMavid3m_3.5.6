package com.libre.irremote.OtherSetupApproach;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.BLEApproach.BLEManager;
import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.SAC.WifiConfigureActivity;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListenerWithResponse;
import com.libre.libresdk.Util.LibreLogger;

import java.lang.reflect.Method;


/**
 * Wifi two options screen
 */

public class WifiHotSpotOrSacSetupActivity extends BaseActivity implements View.OnClickListener {

    private Button btnYes, btnNo;
    private final int GET_SCAN_LIST_HANDLER = 0x200;
    private ImageView ivBack;

    private BLEManager bleManager;
    CafeBar cafeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_spot_or_sac_setup);
//        if (isMobileDataEnabled()) {
//            //Mobile data is enabled and do whatever you want here
//            LibreLogger.d(this, "Mobile data On Do Something");
//            MavidApplication.mobileDataEnabled = true;
//            mobileDataOnOff(WifiHotSpotOrSacSetupActivity.this);
//        } else {
//            LibreLogger.d(this, "suma mobile data disabled here");
//            MavidApplication.mobileDataEnabled = false;
//            //Mobile data is disabled here
//        }
        LibreLogger.d(this,"suma in getting wifi hotspot wifi option");
        inItWidgets();
    }

    private void inItWidgets() {
        bleManager = BLEManager.getInstance(WifiHotSpotOrSacSetupActivity.this);
        btnYes = (Button) findViewById(R.id.btnYes);
        btnYes.setOnClickListener(this);
        btnNo = (Button) findViewById(R.id.btnNo);
        btnNo.setOnClickListener(this);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(WifiHotSpotOrSacSetupActivity.this, MavidHomeTabsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isbleBlinkingFragment", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(WifiHotSpotOrSacSetupActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    private boolean isMobileDataEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm1 = (ConnectivityManager) WifiHotSpotOrSacSetupActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnYes:
//                startActivity(new Intent(WifiHotSpotOrSacSetupActivity.this, BLEHotSpotCredentialsActivity.class));
                startActivity(new Intent(WifiHotSpotOrSacSetupActivity.this, WifiHotSpotCredentialsActivity.class));

                break;
            case R.id.btnNo:

                showLoader(getResources().getString(R.string.notice),getResources().getString(R.string.retrieving));

                handler.sendEmptyMessageDelayed(GET_SCAN_LIST_HANDLER,15000);
                // To get Wifi scan list, send ScanReq as the message to sendCustomSACMessage.
                LibreMavidHelper.sendCustomSACMessage("ScanReq", new SACListenerWithResponse() {
                    @Override
                    public void response(MessageInfo messageInfo) {
                        LibreLogger.d(this,"Recieved scan list "+messageInfo.getMessage());
                        handler.removeMessages(GET_SCAN_LIST_HANDLER);
                        closeLoader();
                        Intent intent = new Intent(WifiHotSpotOrSacSetupActivity.this, WifiConfigureActivity.class);
                        intent.putExtra(Constants.INTENTS.SCAN_LIST,messageInfo.getMessage().toString());
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void failure(Exception e) {
                        handler.removeMessages(GET_SCAN_LIST_HANDLER);
                        closeLoader();
                        somethingWentWrong(WifiHotSpotOrSacSetupActivity.this);
                        LibreLogger.d(this,"suma in oshotspot screen in do next OSHotspotorSacsetup activity");
                        Log.d("suma_exception",e.toString());

                    }
                });
                break;
        }
    }

//    ProgressDialog mProgressDialog;
//    public void showLoader() {
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("ShowingLoader", "Showing loader method");
//                if (mProgressDialog == null) {
//                    mProgressDialog = ProgressDialog.show(WifiHotSpotOrSacSetupActivity.this, getResources().getString(R.string.notice), getResources().getString(R.string.retrieving) + "...", true, true, null);
//                }
//                mProgressDialog.setCancelable(false);
//                if (!mProgressDialog.isShowing()) {
//                    if (!(WifiHotSpotOrSacSetupActivity.this.isFinishing())) {
//                        mProgressDialog = ProgressDialog.show(WifiHotSpotOrSacSetupActivity.this, getResources().getString(R.string.notice), getResources().getString(R.string.retrieving) + "...", true, true, null);
//                    }
//                }
//
//            }
//        });
//    }

//    public void closeLoader() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                    if (!(WifiHotSpotOrSacSetupActivity.this.isFinishing())) {
//                        mProgressDialog.setCancelable(false);
//                        mProgressDialog.dismiss();
//                        mProgressDialog.cancel();
//                    }
//                }
//            }
//        });
//
//    }
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
//                buildSnackBar("BT Connected");
//                Toast.makeText(getApplicationContext(), "BT Connected", Toast.LENGTH_SHORT).show();
                LibreLogger.d(this,"BleCommunication in suma ble state turning CONNECTED in receiver OSHotspot");

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
                LibreLogger.d(this,"suma in ");
//                buildSnackBar("Connection Lost to the Device");
//                Toast.makeText(getApplicationContext(), "Connection Lost to the Device", Toast.LENGTH_SHORT).show();
                LibreLogger.d(this,"BleCommunication in suma ble state turning disconnected OsHotspotSac");

            }
            //else if...
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(BTReceiver, filter1);
        this.registerReceiver(BTReceiver, filter2);
        this.registerReceiver(BTReceiver, filter3);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case GET_SCAN_LIST_HANDLER:
                    closeLoader();
                    somethingWentWrong(WifiHotSpotOrSacSetupActivity.this);
                    LibreLogger.d(this,"suma in oshotspot screen in do next connectiontosac activity in getting scan list handler");

                    break;
            }
            return true;
        }
    });


}
