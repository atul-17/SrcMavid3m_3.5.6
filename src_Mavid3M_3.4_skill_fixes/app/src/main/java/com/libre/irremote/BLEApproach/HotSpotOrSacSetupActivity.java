package com.libre.irremote.BLEApproach;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.libre.irremote.BLE_SAC.BLEConfigureActivity;
import com.libre.irremote.BaseActivity;

import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.OtherSetupApproach.BLEHotSpotCredentialsActivity;
import com.libre.irremote.R;
import com.libre.libresdk.Util.LibreLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * BLE diffrent options screen
 */

public class HotSpotOrSacSetupActivity extends BaseActivity implements OnClickListener, BleWriteInterface, BleReadInterface {

    private Button btnYes, btnNo;
    private ImageView back;
    private BLEManager bleManager;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_spot_or_sac_setup);
        MavidApplication.noReconnectionRuleToApply=true;

        inItWidgets();

//        EventBus.getDefault().register(this);

        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    private void inItWidgets() {
        btnYes = findViewById(R.id.btnYes);
        btnYes.setOnClickListener(this);
        btnNo = findViewById(R.id.btnNo);
        btnNo.setOnClickListener(this);
        back = findViewById(R.id.iv_back);
        bleManager = BLEManager.getInstance(HotSpotOrSacSetupActivity.this);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (bleManager.isBluetoothEnabled()) {
          //  super.onBackPressed();
            startActivity(new Intent(HotSpotOrSacSetupActivity.this, BLEScanActivity.class));

        } else {
            //go to ble blinking fragment
            Bundle bundle = new Bundle();
            bundle.putBoolean("isbleBlinkingFragment", true);
            startActivity(new Intent(HotSpotOrSacSetupActivity.this, MavidHomeTabsActivity.class).putExtras(bundle));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnYes:
                startActivity(new Intent(HotSpotOrSacSetupActivity.this, BLEHotSpotCredentialsActivity.class));
                break;
            case R.id.btnNo:
                BleCommunication bleCommunication = new BleCommunication(HotSpotOrSacSetupActivity.this);
                BleCommunication.writeInteractor();
                if(MavidApplication.BLEServiceisNullScanREQ){
//                    try {
//                        Thread.sleep(5000);
//                        LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has NULL VALUE INSIDE IF Thread .Sleep ");
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                   LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has NULL VALUE INSIDE IF");
                   startActivity(new Intent(HotSpotOrSacSetupActivity.this, BLEConfigureActivity.class));

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            buildSnackBar("Device BLE Service is Null..Please Try Again Later!!.");
//                            Toast.makeText(HotSpotOrSacSetupActivity.this,"Device Service is Null",Toast.LENGTH_SHORT);
//                            startActivity(new Intent(HotSpotOrSacSetupActivity.this, BLEScanActivity.class));
//                            LibreLogger.d("SCANREQ", "Suma get the BLE SERVICE VALUE has NULL VALUE INSIDE IF");
//                            buildSnackBar("Retrying BLE Connection");
//                        }
//                    });
                }
                else {
                    startActivity(new Intent(HotSpotOrSacSetupActivity.this, BLEConfigureActivity.class));
                    LibreLogger.d(this, "suma in bleconfigure activity connecting in response else ");
                    LibreLogger.d("SCANREQ","Suma get the BLE SERVICE VALUE has NULL VALUE INSIDE ELSE");

                }
                break;
        }
    }


    @Override
    public void onWriteSuccess() {
    }

    @Override
    public void onWriteFailure() {

    }

    @Override
    public void onReadSuccess(byte[] data) {
        Log.d("HotSpotOrSacSetup", "data : " + data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BLEEvntBus event) {
//        Toast.makeText(BLEHotSpotCredentialsActivity.this, "hotspot : " + event.message, Toast.LENGTH_SHORT).show();

        byte[] value = event.message;
        Log.d("Hotspot", "Value received: " + value);
        int response = getDataLength(value);
        LibreLogger.d(this, "suma in ble configure activity get the value in sac setup activity\n" + response);
        if (response == 14) {
            showSacTimeoutAlert(HotSpotOrSacSetupActivity.this, this);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    public int getDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1 << 8 | b2 & 0xFF);

        LibreLogger.d(this, "Data length is returned as s" + s);
        return s;
    }

}
