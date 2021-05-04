package com.libre.irremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.libre.irremote.R;
import com.libre.irremote.irActivites.IRSignUpLoginWebViewActivity;


public class SplashScreenActivity extends BaseActivity {

    private TextView mAppVersion;
    public static final String APP_RESTARTING = "RestartingApplication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAppVersion = (TextView) findViewById(R.id.mAppVersion);

        mAppVersion.setText(getVersion(getApplicationContext()));

        final SharedPreferences sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE);

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (sharedPreferences.getBoolean("userLoggedIn", false)) {
                    Intent n1 = new Intent(SplashScreenActivity.this, MavidHomeTabsActivity.class);
                    startActivity(n1);
                    finish();
                } else {
                    //IRSignUpLoginWebViewActivity
                    //goto to login webview activity
                    Intent intent = new Intent(SplashScreenActivity.this, IRSignUpLoginWebViewActivity.class);
                    startActivity(intent);
//                    Intent n1 = new Intent(SplashScreenActivity.this, MavidHomeTabsActivity.class);
//                    startActivity(n1);
//                    finish();
                }

            }
        }, 1200);

    }

    public String getVersion(Context context) {
        String Version = "";
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (pInfo != null)
            Version = pInfo.versionName;

        return Version;
    }
}
