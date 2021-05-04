
package com.libre.irremote.SAC;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;


import com.libre.irremote.adapters.ConfigureSpeakerWifiListImageAdapter;
import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.OtherSetupApproach.WifiHotSpotOrSacSetupActivity;
import com.libre.irremote.R;
import com.libre.irremote.utility.SACUtils;
import com.libre.libresdk.Util.LibreLogger;
import com.sembozdemir.viewpagerarrowindicator.library.ViewPagerArrowIndicator;

import java.util.ArrayList;
import java.util.List;


public class SACInstructionActivity extends BaseActivity {

    private AppCompatButton mOpenSettings;
    private AppCompatButton mNextButton;
    private int PERMISSION_ACCESS_COARSE_LOCATION = 0x100;
    //    private AlertDialog alert;
    private final int GET_SCAN_LIST_HANDLER = 0x200;
    private final int CONNECT_TO_SAC = 0x300;
    ImageView back;

    ViewPager viewPager;

    ViewPagerArrowIndicator viewPagerArrowIndicator;

    ConfigureSpeakerWifiListImageAdapter configureSpeakerWifiListImageAdapter;

    List<Integer> imageList = new ArrayList<>();

    TextView tvStep2SetupInstruction;

    String selectedSSID = "LsConfigure";

    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    private Dialog alert;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case GET_SCAN_LIST_HANDLER:
                    closeLoader();
                    somethingWentWrong(SACInstructionActivity.this);
                    LibreLogger.d(this, "suma dialog check getscanlisthandler");
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mavid_device_setup_instructions);
        mOpenSettings = (AppCompatButton) findViewById(R.id.btnCallingWifiSettings);

        viewPagerArrowIndicator = findViewById(R.id.viewPagerArrowIndicator);

        viewPager = findViewById(R.id.viewPager);

        tvStep2SetupInstruction = findViewById(R.id.tv_step_2_setup_instruction);

        mOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableNetworkChangeCallBack();
                disableNetworkOffCallBack();
                String mPreviousSSID = getconnectedSSIDname(SACInstructionActivity.this);
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, CONNECT_TO_SAC);
            }
        });
        mNextButton = (AppCompatButton) findViewById(R.id.btnNext);
//        mNextButton.setVisibility(View.GONE);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doNext();
            }
        });
        back = (ImageView) findViewById(R.id.iv_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        viewPagerArrowIndicator.setArrowIndicatorRes(R.drawable.left_arrow,
                R.drawable.right_viewpager_arrow);

        configureSpeakerWifiListImageAdapter = new ConfigureSpeakerWifiListImageAdapter(SACInstructionActivity.this, getImageList());
        viewPager.setAdapter(configureSpeakerWifiListImageAdapter);
        viewPagerArrowIndicator.bind(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //f first index item -0 will have LSConfigure image
                //second index item - 1 will have MicrodotConfigure
                //third index item - 2 will have WaveConfigure

                if (position == 0) {
                    selectedSSID = "LSConfigure";
                } else if (position == 1) {
                    selectedSSID = "MicrodotConfigure";
                } else if (position == 2) {
                    selectedSSID = "WaveConfigure";
                }

                tvStep2SetupInstruction.setText("Go to Wi-Fi settings and select " + selectedSSID + "_xxxxxxx from the Wi-Fi list");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public List<Integer> getImageList() {
        imageList.add(R.drawable.ls_configure_image);
        imageList.add(R.drawable.microdot_image);
        imageList.add(R.drawable.wave_image);

        return imageList;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //check if ACCESS_COARSE_LOCATION permission is allowed
        if (isLocationPermissionEnabled(SACInstructionActivity.this)) {
            afterPermit();
        } else {
            askLocationPermission();
        }

    }

    private void afterPermit() {
        if (SACUtils.isSACNetwork(getconnectedSSIDname(SACInstructionActivity.this))) {
            mNextButton.performClick();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(SACInstructionActivity.this, MavidHomeTabsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isbleBlinkingFragment", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
        /*startActivity(new Intent(SACInstructionActivity.this, DeviceListActivity.class));
        finish();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONNECT_TO_SAC) {
            if (!SACUtils.isSACNetwork(getconnectedSSIDname(SACInstructionActivity.this))) {
                //showAlertDialogForClickingWrongNetwork();
                LibreLogger.d(this, "suma dialog check in onactivity");

                return;
            } else {
                mNextButton.setVisibility(View.VISIBLE);
            }
            // mNextButton.performClick();
        }
    }

    private void doNext() {
        startActivity(new Intent(SACInstructionActivity.this, WifiHotSpotOrSacSetupActivity.class));
        finish();
    }



    public String getSelectedSSID() {
        if (selectedSSID.equals("LSConfigure")) {
            return Constants.WAC_SSID;
        } else if (selectedSSID.equals("MicrodotConfigure")) {
            return Constants.WAC_SSID2;
        } else if (selectedSSID.equals("WaveConfigure")) {
            return Constants.WAC_SSID3;
        }
        return Constants.WAC_SSID;
    }

    public void showAlertDialogForClickingWrongNetwork() {
        if (!SACInstructionActivity.this.isFinishing()) {

            alert = null;

            alert = new Dialog(SACInstructionActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);


            String Message = getResources().getString(R.string.title_error_sac_message) + "\n(" + "LSConfigure_XXXX/MicrodotConfigure_XXXX/WAVEConfigure_"+ "XXXX)";

            tv_alert_title.setText("");

            tv_alert_message.setText(Message);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                }
            });

            alert.show();


//            AlertDialog.Builder builder = new AlertDialog.Builder(SACInstructionActivity.this);
//
//            builder.setMessage(Message)
//                    .setCancelable(false)
//                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            alert.dismiss();
//
//                        }
//                    });
//
//            if (alert == null) {
//                alert = builder.show();
//                TextView messageView = (TextView) alert.findViewById(android.R.id.message);
//                messageView.setGravity(Gravity.CENTER);
//            }


        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askLocationPermission() {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // user checked Never Ask again
//            LibreLogger.d(this, "askLocationPermission permit READ_EXTERNAL_STORAGE Denied for ever");
            // show dialog

            alert = null;

            alert = new Dialog(SACInstructionActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);

            tv_alert_title.setText(getString(R.string.permitNotAvailable));

            tv_alert_message.setText(R.string.enableLocation);

            btn_ok.setText(getString(R.string.gotoSettings));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            if (alert != null && !alert.isShowing())
                alert.show();

//            AlertDialog.Builder requestPermission = new AlertDialog.Builder(SACInstructionActivity.this);
//            requestPermission.setTitle(getString(R.string.permitNotAvailable))
//                    .setMessage(getString(R.string.enableStoragePermit))
//                    .setPositiveButton(getString(R.string.gotoSettings), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            //navigate to settings
//                            alert.dismiss();
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", getPackageName(), null);
//                            intent.setData(uri);
//                            startActivity(intent);
//                        }
//                    })
//                    .setCancelable(false);
//            if (alert == null) {
//                alert = requestPermission.create();
//            }


            return;
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);

    }


//    public boolean isLocationPermissionEnabled() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                LibreLogger.d(this, "checking permission ACCESS_COARSE_LOCATION permission is enabled");
//                return true;
//            }
//        } else {
//            //Android OS version is less than M. So permission is always enabled
//            LibreLogger.d(this, "checking permission OS is less than M, ACCESS_COARSE_LOCATION permission is enabled");
//            return true;
//        }
//        LibreLogger.d(this, "checking permission ACCESS_COARSE_LOCATION permission is not enabled");
//
//        return false;
//    }

    public static Boolean isLocationPermissionEnabled(Context context) {

        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//// This is new method provided in API 28
//            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            return lm.isLocationEnabled();
//        } else {
//        // This is Deprecated in API 28
//            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            return lm.isLocationEnabled();
//
//        }
    }
}
