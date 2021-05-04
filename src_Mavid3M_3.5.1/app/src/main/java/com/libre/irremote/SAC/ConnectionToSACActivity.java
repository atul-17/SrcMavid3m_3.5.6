package com.libre.irremote.SAC;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.libre.irremote.BaseActivity;
import com.libre.irremote.MavidApplication;
import com.libre.irremote.OtherSetupApproach.WifiHotSpotOrSacSetupActivity;
import com.libre.irremote.R;
import com.libre.libresdk.Util.LibreLogger;

import java.lang.reflect.Method;

public class ConnectionToSACActivity extends BaseActivity {

    Button btnNext;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 0x100;
    private AlertDialog alert;
    private final int GET_SCAN_LIST_HANDLER = 0x200;
    private final int CONNECT_TO_SAC = 0x300;
    Method dataConnSwitchmethod;
    Object ITelephonyStub;
    boolean isEnabled=false;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case GET_SCAN_LIST_HANDLER:
                    closeLoader();
                    somethingWentWrong(ConnectionToSACActivity.this);
                    break;
            }
            return true;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_to_sac);
        if (isMobileDataEnabled()) {
                //Mobile data is enabled and do whatever you want here
                LibreLogger.d(this, "Mobile data On Do Something");
               MavidApplication.mobileDataEnabled = true;
                mobileDataOnOff(ConnectionToSACActivity.this);
            } else {
                LibreLogger.d(this, "suma mobile data disabled here");
                MavidApplication.mobileDataEnabled = false;
                //Mobile data is disabled here
            }

        initViews();
    }

    private void initViews() {
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doNext();
            }
        });
    }

    private boolean isMobileDataEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm1 = (ConnectivityManager) ConnectionToSACActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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


    private void doNext() {
        /*showLoader();
        handler.sendEmptyMessageDelayed(GET_SCAN_LIST_HANDLER,15000);
        // To get Wifi scan list, send ScanReq as the message to sendCustomSACMessage.
        LibreMavidHelper.sendCustomSACMessage("ScanReq", new SACListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                LibreLogger.d(this,"Recieved scan list "+messageInfo.getMessage());
                handler.removeMessages(GET_SCAN_LIST_HANDLER);
                closeLoader();
                Intent intent = new Intent(ConnectionToSACActivity.this, WifiConfigureActivity.class);
                intent.putExtra(Constants.INTENTS.SCAN_LIST,messageInfo.getMessage().toString());
                startActivity(intent);
                finish();
            }
            @Override
            public void failure(Exception e) {
                handler.removeMessages(GET_SCAN_LIST_HANDLER);
                closeLoader();
                somethingWentWrong(ConnectionToSACActivity.this);
            }
        });*/

        startActivity(new Intent(ConnectionToSACActivity.this, WifiHotSpotOrSacSetupActivity.class));
        LibreLogger.d(this,"suma in oshotspot screen in do next connectiontosac activity");
    }

    ProgressDialog mProgressDialog;
    public void showLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("ShowingLoader", "Showing loader method");
                if (mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.show(ConnectionToSACActivity.this, getResources().getString(R.string.notice), getResources().getString(R.string.retrieving) + "...", true, true, null);
                }
                mProgressDialog.setCancelable(false);
                if (!mProgressDialog.isShowing()) {
                    if (!(ConnectionToSACActivity.this.isFinishing())) {
                        mProgressDialog = ProgressDialog.show(ConnectionToSACActivity.this, getResources().getString(R.string.notice), getResources().getString(R.string.retrieving) + "...", true, true, null);
                    }
                }

            }
        });
    }

    public void closeLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    if (!(ConnectionToSACActivity.this.isFinishing())) {
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.dismiss();
                        mProgressDialog.cancel();
                    }
                }
            }
        });

    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
