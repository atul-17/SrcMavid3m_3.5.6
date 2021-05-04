package com.libre.irremote.BLEApproach;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.SAC.SACInstructionActivity;

import static com.libre.irremote.BLEApproach.BLEScanActivity.MY_PERMISSIONS_REQUEST_LOCATION;
import static com.libre.irremote.BLEApproach.BluetoothLeService.mBluetoothGatt;

public class BLEBlinkingFragment extends Fragment implements View.OnClickListener {

    private Button btnContinue, btnBt;
    private BLEManager bleManager;
    private static final int REQUEST_ENABLE_BT = 1;
    private ImageView back;
    CafeBar cafeBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bleblinking, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inItWidgets(view);
        bleManager = BLEManager.getInstance(getActivity());
        if (getActivity() != null) {
            ((MavidHomeTabsActivity) getActivity()).ivRefresh.setVisibility(View.GONE);
        }
    }

    private void inItWidgets(View view) {
        btnContinue = (Button) view.findViewById(R.id.btnContinue);
        btnBt = (Button) view.findViewById(R.id.btnBt);
        btnBt.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        // Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();

    }

    public void buildSnackBar(String message) {
        if (getContext() != null) {
            CafeBar.Builder builder = CafeBar.builder(getContext());
            builder.autoDismiss(true);
            builder.customView(R.layout.custom_snackbar_layout);

            cafeBar = builder.build();
            AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
            tv_message.setText(message);

            cafeBar.show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnContinue:
                startActivity(new Intent(getActivity(), SACInstructionActivity.class));
                break;
            case R.id.btnBt:
                turnOnBtCheck();
                Log.d("Suma", "is ble enabled check yes or no\n" + bleManager.isBluetoothEnabled());
                break;
        }
    }


    private void turnOnBtCheck() {
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            buildSnackBar(getActivity().getResources().getString(R.string.ble_not_supported));
        }

        if (!bleManager.isBTAdapterExist()) {
            buildSnackBar(getActivity().getResources().getString(R.string.error_bluetooth_not_supported));
            getActivity().finish();
            return;
        }

        if (!bleManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (bleManager.isBluetoothEnabled()) {
            buildSnackBar(getActivity().getResources().getString(R.string.BtOn));
            startActivity(new Intent(getActivity(), BLEScanActivity.class));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                }
                return;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (getActivity() != null) {
            if (!(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)) {
                startActivity(new Intent(getActivity(), BLEScanActivity.class));
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
