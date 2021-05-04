package com.libre.irremote.OtherSetupApproach;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;


import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.alexa_signin.AlexaSignInActivity;
import com.libre.irremote.alexa_signin.AlexaThingsToTryDoneActivity;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.Util.LibreLogger;

import java.lang.reflect.Method;

public class OSHotSpotSuccessAlexaLogin extends AppCompatActivity {

    AppCompatButton btnAlexaLogin, noAlexaLogin;
    String mSACConfiguredIpAddress = "";
    AppCompatImageView ivHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yes_alexa_login_no_home_screen);
        MavidApplication.hotSpotAlexaCalled = true;

        mSACConfiguredIpAddress = getIntent().getStringExtra("IPADDRESS");
        LibreLogger.d(this, "suma in hotspot connection suma in alexa login" + mSACConfiguredIpAddress);

        btnAlexaLogin = findViewById(R.id.btnNext);

        noAlexaLogin = findViewById(R.id.noalexalogin);

        btnAlexaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LibreLogger.d(this, "suma get mobile data state\n" + getMobileDataState());
//                if(isMobileDataEnabled()) {
                TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (tm != null && tm.isDataEnabled())
                        LibreLogger.d(this, "suma in hotspot connection suma is data enabled\n" + tm.isDataEnabled());

                    checkForAlexaToken(mSACConfiguredIpAddress);
                }
                LibreLogger.d(this, "suma in hotspot connection suma in alexa login if mobiledata enabled" + mSACConfiguredIpAddress);

//                }else{
//                    ShowHotSpotFailed();
//                }

            }

        });

        noAlexaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OSHotSpotSuccessAlexaLogin.this, MavidHomeTabsActivity.class));
                finish();
            }
        });

        ivHome = findViewById(R.id.iv_home);
        ivHome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                LibreLogger.d(this, "user pressed home button ");
                startActivity(new Intent(OSHotSpotSuccessAlexaLogin.this, MavidHomeTabsActivity.class));
                finish();

            }
        });
    }

    private void ShowHotSpotFailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getResources().getString(R.string.dataOff));
        builder.setMessage(getResources().getString(R.string.dataOffWait));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
                startActivity(intent);
                //dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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

            }

            @Override
            public void success() {

            }
        });
    }

    public boolean getMobileDataState() {
        try {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (null != getMobileDataEnabledMethod) {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                return mobileDataEnabled;
            }
        } catch (Exception ex) {
            Log.e("OSAlexa", "Error getting mobile data state", ex);
        }

        return false;
    }

    private void handleAlexaRefreshTokenStatus(String current_ipaddress, String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            /*not logged in*/
            Intent i = new Intent(OSHotSpotSuccessAlexaLogin.this, AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("speakerIpaddress", current_ipaddress);
            i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(i);
            finish();
        } else {
            Intent newIntent = new Intent(OSHotSpotSuccessAlexaLogin.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("speakerIpaddress", current_ipaddress);
            newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(newIntent);
            finish();
        }
    }

    private boolean isMobileDataEnabled() {
        boolean mobileDataEnabled = false;
        ConnectivityManager cm1 = (ConnectivityManager) OSHotSpotSuccessAlexaLogin.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(OSHotSpotSuccessAlexaLogin.this, MavidHomeTabsActivity.class);
        startActivity(intent);
        finish();
    }

}
