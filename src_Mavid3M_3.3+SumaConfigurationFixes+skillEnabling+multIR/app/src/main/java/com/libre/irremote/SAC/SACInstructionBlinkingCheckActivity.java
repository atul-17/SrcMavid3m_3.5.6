package com.libre.irremote.SAC;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.libre.irremote.BLEApproach.BLEBlinkingFragment;
import com.libre.irremote.BLEApproach.BLEManager;
import com.libre.irremote.BLEApproach.BLEScanActivity;
import com.libre.irremote.Constants.Constants;

import com.libre.irremote.R;
import com.libre.libresdk.Util.LibreLogger;

/**
 * activity not used
 */

public class SACInstructionBlinkingCheckActivity extends AppCompatActivity {

    AppCompatButton btnNext;
    //    TextView notBlinking;
    Dialog notBlinkingDialog;
    ImageView back;
    private Dialog alert;
    private BLEManager bleManager;
    private NetworkInfo wifiCheck;

    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacinstruction_blinking_check);
        initViews();
        bleManager = BLEManager.getInstance(SACInstructionBlinkingCheckActivity.this);
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout., container, false);
//        return view;
//    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLocationPermissionEnabled()) {
            afterPermit();
        } else {
            askPermit();
        }
    }

    public boolean isLocationPermissionEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private void initViews() {
//        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bleManager.isBluetoothEnabled()) {
                    startActivity(new Intent(SACInstructionBlinkingCheckActivity.this, BLEScanActivity.class));
                } else {
                    startActivity(new Intent(SACInstructionBlinkingCheckActivity.this, BLEBlinkingFragment.class));
                }

            }
        });


//        notBlinking = (TextView) findViewById(R.id.notBlinking);
//        notBlinking.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showNotBlinkingDialog();
//            }
//        });
//        back = (ImageView) findViewById(R.id.back);
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
    }

    private void showNotBlinkingDialog() {
        notBlinkingDialog = new Dialog(SACInstructionBlinkingCheckActivity.this);
        notBlinkingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        notBlinkingDialog.setCancelable(false);
        notBlinkingDialog.setContentView(R.layout.pico_not_blinking_dialog);
        Button ok = (Button) notBlinkingDialog.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notBlinkingDialog.dismiss();
            }
        });
        notBlinkingDialog.show();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(SACInstructionBlinkingCheckActivity.this, MavidHomeTabsActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish();
//
//    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askPermit() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Constants.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            afterPermit();
        } else if (requestCode == Constants.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // user checked Never Ask again
                Log.d("asking permit", "permit ACCESS_COARSE_LOCATION Denied for ever");
                // show dialog



                alert = new Dialog(SACInstructionBlinkingCheckActivity.this);

                alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

                alert.setContentView(R.layout.custom_single_button_layout);

                alert.setCancelable(false);

                tv_alert_title = alert.findViewById(R.id.tv_alert_title);

                tv_alert_message = alert.findViewById(R.id.tv_alert_message);

                btn_ok = alert.findViewById(R.id.btn_ok);

                tv_alert_title.setText(getString(R.string.permitNotAvailable));

                tv_alert_message.setText(R.string.permissionMsg);

                btn_ok.setText(getString(R.string.gotoSettings));



                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
                if (alert != null && !alert.isShowing())
                    alert.show();

//                AlertDialog.Builder requestPermission = new AlertDialog.Builder(SACInstructionBlinkingCheckActivity.this);
//                requestPermission.setTitle(getString(R.string.permitNotAvailable))
//                        .setMessage(getString(R.string.permissionMsg))
//                        .setPositiveButton(getString(R.string.gotoSettings), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //navigate to settings
//
//                            }
//                        })
//                        .setCancelable(false);
//                if (alert == null) {
//                    alert = requestPermission.create();
//                }

            }
        }
    }

    private void afterPermit() {
        if (!isLocationEnabled()) {
            LibreLogger.d(this, "Location is disabled");
            askToEnableLocationService();
        }
    }

    private void askToEnableLocationService() {

        alert = new Dialog(SACInstructionBlinkingCheckActivity.this);

        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

        alert.setContentView(R.layout.custom_single_button_layout);

        alert.setCancelable(false);

        tv_alert_title = alert.findViewById(R.id.tv_alert_title);

        tv_alert_message = alert.findViewById(R.id.tv_alert_message);

        btn_ok = alert.findViewById(R.id.btn_ok);


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

//        AlertDialog.Builder builder = new AlertDialog.Builder(SACInstructionBlinkingCheckActivity.this);
//        builder.setTitle(getResources().getString(R.string.locationServicesIsOff))
//                .setMessage(getResources().getString(R.string.enableLocation))
//                .setPositiveButton(getResources().getString(R.string.gotoSettings), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.cancel();
//                        turnGPSOn();
//                    }
//                });
//        builder.create();
//        builder.show();
    }

    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            return false;
        } else {
            return true;
        }
    }

    private void turnGPSOn() {
        Intent intent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

}
