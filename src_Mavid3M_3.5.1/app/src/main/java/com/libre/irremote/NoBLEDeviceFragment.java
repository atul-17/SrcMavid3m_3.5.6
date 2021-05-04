package com.libre.irremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.libre.irremote.R;

import com.libre.irremote.BLEApproach.BLEScanActivity;
import com.libre.irremote.SAC.SACInstructionActivity;
import com.libre.irremote.utility.BusLeScanBluetoothDevicesEventProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NoBLEDeviceFragment extends Fragment {

    AppCompatButton btnContinue;

    AppCompatButton btn_refresh;

    boolean isFragmentLoaded = false;

    AppCompatTextView tvTurnOnBle;


    private final BroadcastReceiver bStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                boolean bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON;

                if (bluetoothState){
                    //on
                    tvTurnOnBle.setVisibility(View.GONE);
                }else{
                    //off
                    tvTurnOnBle.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_no_ble_device, container, false);
        if (getActivity()!=null) {
            getActivity().registerReceiver(bStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
           getActivity().unregisterReceiver(bStateReceiver);
        }
    }

    //calls whenever  bus has posted any bluetooth devices
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getBluetoothScannedDevices(final BusLeScanBluetoothDevicesEventProgress.BusScanBluetoothDevices busScanBluetoothDevices) {
        if (getActivity() != null) {
            if (((BLEScanActivity) getActivity()).bluetoothDeviceList == null) {
                ((BLEScanActivity) getActivity()).getBluetoothDeviceList();
            } else {
                //check and keep adding the devices
                if (!checkIfBluetoothDeviceIsPresent(busScanBluetoothDevices.getBluetoothDevice())) {
                    ((BLEScanActivity) getActivity()).bluetoothDeviceList.add(busScanBluetoothDevices.getBluetoothDevice());
                }
            }
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    if (((BLEScanActivity) getActivity()).bluetoothDeviceList.size() > 0 && !isFragmentLoaded && getActivity() != null) {
                        isFragmentLoaded = true;
                        ((BLEScanActivity) getActivity()).loadFragments(new BleListFragment());
                    }
                }
            }
        }, 1000);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventBus.getDefault().register(this);

        btnContinue = view.findViewById(R.id.btnContinue);

        btn_refresh = view.findViewById(R.id.btn_refresh);

        tvTurnOnBle = view.findViewById(R.id.tvTurnOnBle);

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {

                    if (((BLEScanActivity) getActivity()).checkLocationPermission()) {
                        if (((BLEScanActivity) getActivity()).checkLocationIsEnabled(getActivity())) {
                            ((BLEScanActivity) getActivity()).showLoader("Please wait, we are trying to scan the device.", "");
                            ((BLEScanActivity) getActivity()).scanLeDevice(true);
                        }
                    }
                }
            }
        });


        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SACInstructionActivity.class);
                startActivity(intent);
            }
        });
    }



    public boolean checkIfBluetoothDeviceIsPresent(BluetoothDevice bluetoothDevice) {
        if (getActivity() != null) {
            for (BluetoothDevice bluetoothDevice1 : ((BLEScanActivity) getActivity()).bluetoothDeviceList) {
                if (bluetoothDevice1.getName().equals(bluetoothDevice.getName())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
