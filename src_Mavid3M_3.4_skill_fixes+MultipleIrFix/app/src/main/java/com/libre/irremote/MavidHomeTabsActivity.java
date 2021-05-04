package com.libre.irremote;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.BLEApproach.BLEBlinkingFragment;
import com.libre.irremote.BLEApproach.BLEManager;
import com.libre.irremote.BLEApproach.BLEScanActivity;
import com.libre.irremote.Constants.Constants;

import com.libre.irremote.R;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.Util.LibreLogger;

import java.util.Timer;
import java.util.TimerTask;

public class MavidHomeTabsActivity extends BaseActivity {

    BottomNavigationView bottomNavigation;
    Toolbar toolbar;

    public AppCompatImageView ivRefresh;

    private boolean isDoubleTap;

    DeviceListFragment deviceListFragment;

    BLEBlinkingFragment bleBlinkingFragment;

    MavidSettingsFragment mavidSettingsFragment;

    boolean isbleBlinkingFragment = false;

    Bundle bundle = new Bundle();

    ProgressBar progress_bar;

    public View view_line;

    Dialog fwUpdateDialog;

    TextView dialogTitle;

    int status = 0;

    Handler handler = new Handler();

    CafeBar cafeBar;

    private BLEManager bleManager;

    private Dialog alert;


    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    private Handler mTaskHandlerForSendingMSearch = new Handler();

    private final int MSEARCH_TIMEOUT_SEARCH = 2000;


    String mSACConfiguredIpAddress;

    int callMSearchfiveTimes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mavid_activity_layout);

        bleManager = BLEManager.getInstance(MavidHomeTabsActivity.this);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            isbleBlinkingFragment = bundle.getBoolean("isbleBlinkingFragment", false);
            mSACConfiguredIpAddress = bundle.getString("mSACConfiguredIpAddress");
        }


        if (mSACConfiguredIpAddress != null) {
            callMSearchEvery500ms(new Timer());
        }


        initView();


//        ShowAlertDynamicallyGoingToHomeScreen("Please wait","adad",MavidHomeTabsActivity.this);

        if (isbleBlinkingFragment) {
            if (!bleManager.isBluetoothEnabled()) {
                bottomNavigation.setSelectedItemId(R.id.action_add);
                dismissProgressBar();
                inflateFragments(BLEBlinkingFragment.class.getSimpleName());
            } else {
                bottomNavigation.setSelectedItemId(R.id.action_discover);
                inflateFragments(DeviceListFragment.class.getSimpleName());
            }
        } else {
            bottomNavigation.setSelectedItemId(R.id.action_discover);
            inflateFragments(DeviceListFragment.class.getSimpleName());
        }

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_discover:
                        inflateFragments(DeviceListFragment.class.getSimpleName());
                        break;

                    case R.id.action_add:
                        dismissProgressBar();
                        if (bleManager.isBluetoothEnabled()) {
                            //go directly to ble scan list activity
                            startActivity(new Intent(MavidHomeTabsActivity.this, BLEScanActivity.class));
                        } else {
                            //go to the bleBlinking fragment
                            inflateFragments(BLEBlinkingFragment.class.getSimpleName());
                        }

                        break;

                    case R.id.action_settings:
                        dismissProgressBar();
                        inflateFragments(MavidSettingsFragment.class.getSimpleName());
                        break;
                }
                return true;
            }
        });

//        //remove comments before releasing
//        if (getSharedPreferences("Mavid", Context.MODE_PRIVATE).getBoolean("userLoggedIn", false)) {
//            //call refresh token  only if user is logged in
//            showProgressBar();
//            emailAuthAndTokenRefreshApis.getAccessTokenApi(
//                    getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("refreshToken", ""),
//                    MavidHomeTabsActivity.this, new ApiSucessCallback() {
//                        @Override
//                        public void onSucess(@org.jetbrains.annotations.Nullable String code, @org.jetbrains.annotations.Nullable String message) {
//                            dismissProgressBar();
//                        }
//                    });
//        }

    }


    public void callMSearchEvery500ms(final Timer timer) {
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                LibreMavidHelper.setAdvertiserMessage(getMsearchPayload());
                try {
                    if (callMSearchfiveTimes < 5) {
                        LibreMavidHelper.advertiseWithIp(mSACConfiguredIpAddress, MavidHomeTabsActivity.this);
                        callMSearchfiveTimes = callMSearchfiveTimes + 1;
                        Log.d("atul_m-search", "count:" + String.valueOf(callMSearchfiveTimes) + "ipAddress: " + mSACConfiguredIpAddress);
                    } else {
                        closeLoader();
                        timer.cancel();
                    }
                } catch (WrongStepCallException e) {
                    e.printStackTrace();
                    Log.d("atul_m-search_except", e.toString());
                }
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 500 ms
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLocationPermissionEnabled()) {
            afterPermit();
            LibreLogger.d(this,"suma in n/w change 31");
           MavidApplication.bothLOCPERMISSIONgIVEN=true;
         ///   MavidApplication.onlyAppLocationGiven=false;


        } else {
            askPermit();
            LibreLogger.d(this,"suma in n/w change 33");
//            MavidApplication.bothLOCPERMISSIONgIVEN=true;
//            MavidApplication.onlyAppLocationGiven=true;

        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askPermit() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Constants.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }


    public boolean isLocationPermissionEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            MavidApplication.doneLocationChange = false;
            return false;
        } else {
            return true;
        }
    }

    private void afterPermit() {
        if (!isLocationEnabled()) {
            LibreLogger.d(this, "Location is disabled");
            askToEnableLocationService();
            LibreLogger.d(this,"suma in n/w change 31");

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            MavidApplication.doneLocationChange = true;
            afterPermit();

        } else if (requestCode == Constants.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            MavidApplication.doneLocationChange = false;
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // user checked Never Ask again
                Log.d("asking permit", "permit ACCESS_COARSE_LOCATION Denied for ever");
                // show dialog

                if (alert == null) {

                    alert = new Dialog(MavidHomeTabsActivity.this);

                    alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    alert.setContentView(R.layout.custom_single_button_layout);

                    alert.setCancelable(false);

                    tv_alert_title = alert.findViewById(R.id.tv_alert_title);

                    tv_alert_message = alert.findViewById(R.id.tv_alert_message);

                    btn_ok = alert.findViewById(R.id.btn_ok);
                }

                tv_alert_title.setText(getString(R.string.permitNotAvailable));

                tv_alert_message.setText(getString(R.string.permissionMsg));

                btn_ok.setText(getString(R.string.gotoSettings));

                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                        alert = null;
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

                alert.show();
            }
        }
    }

    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            MavidApplication.doneLocationChange = false;
            return false;
        } else {
            MavidApplication.doneLocationChange = true;
            return true;
        }
    }


    private void askToEnableLocationService() {
        if (!MavidApplication.doneLocationChange) {

            if (alert == null) {

                alert = new Dialog(MavidHomeTabsActivity.this);

                alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

                alert.setContentView(R.layout.custom_single_button_layout);

                alert.setCancelable(false);

                tv_alert_title = alert.findViewById(R.id.tv_alert_title);

                tv_alert_message = alert.findViewById(R.id.tv_alert_message);

                btn_ok = alert.findViewById(R.id.btn_ok);
            }

            tv_alert_title.setText(getResources().getString(R.string.locationServicesIsOff));

            tv_alert_message.setText(getResources().getString(R.string.enableLocation));

            btn_ok.setText(getResources().getString(R.string.gotoSettings));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    turnGPSOn();
                }
            });

            alert.show();

        }
    }

    private void turnGPSOn() {
        Intent intent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void initView() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        ivRefresh = findViewById(R.id.iv_refresh);

        progress_bar = findViewById(R.id.progress_bar);

        view_line = findViewById(R.id.view_line);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

    }

    public void inflateFragments(String fragmentClassName) {
        if (fragmentClassName != null) {
            if (fragmentClassName.equals(DeviceListFragment.class.getSimpleName())) {
                bottomNavigation.getMenu().getItem(0).setChecked(true);
                loadFragments(deviceListFragment = new DeviceListFragment());
            } else if (fragmentClassName.equals(BLEBlinkingFragment.class.getSimpleName())) {
                bottomNavigation.getMenu().getItem(1).setChecked(true);
                loadFragments(bleBlinkingFragment = new BLEBlinkingFragment());
            } else if (fragmentClassName.equals(MavidSettingsFragment.class.getSimpleName())) {
                bottomNavigation.getMenu().getItem(2).setChecked(true);
                loadFragments(mavidSettingsFragment = new MavidSettingsFragment());
            }
        }
    }

    public boolean loadFragments(Fragment fragment) {
        if (fragment != null) {
            try {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fl_container, fragment, fragment.getClass().getSimpleName())
                        .commit();
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                LibreLogger.d(this, "loadFragment exception" + e.getMessage());
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public void showProgressBar() {
        if (progress_bar != null) {
            view_line.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
        }
    }

    public void dismissProgressBar() {
        if (progress_bar != null) {
            view_line.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (isDoubleTap) {
            super.onBackPressed();
            ensureAppKill();
            return;
        }

        buildSnackBar(getString(R.string.doubleTapToExit));
//        Toast.makeText(this, getString(R.string.doubleTapToExit), Toast.LENGTH_SHORT).show();
        isDoubleTap = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isDoubleTap = false;
            }
        }, 2000);
    }


    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(MavidHomeTabsActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    public void ensureAppKill() {

        /* * Finish this activity, and tries to finish all activities immediately below it
         * in the current task that have the same affinity.*/
        ActivityCompat.finishAffinity(this);
        /* Killing our Android App with The PID For the Safe Case */
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);

    }
}
