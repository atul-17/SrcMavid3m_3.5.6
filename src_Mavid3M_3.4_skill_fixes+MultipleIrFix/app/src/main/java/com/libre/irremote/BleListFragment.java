package com.libre.irremote;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.libre.irremote.BLEApproach.BLEScanActivity;
import com.libre.irremote.BLEApproach.LeDeviceListAdapter;
import com.libre.irremote.utility.BusLeScanBluetoothDevicesEventProgress;

import com.libre.irremote.R;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.Util.LibreLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class BleListFragment extends Fragment {

    public ListView listView;
    public LeDeviceListAdapter leDeviceListAdapter;
    public FloatingTextButton refresh;
    SwipeRefreshLayout refreshLayout;

    String ProductIdText;
    public String mDeviceAddress;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ble_list_fragment, container, false);
        return view;
    }


    //calls whenever  bus has posted any bluetooth devices
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getBluetoothScannedDevices(BusLeScanBluetoothDevicesEventProgress.BusScanBluetoothDevices busScanBluetoothDevices) {
        if (getActivity() != null) {
            if (((BLEScanActivity) getActivity()).bluetoothDeviceList == null) {
                ((BLEScanActivity) getActivity()).getBluetoothDeviceList();
            }
            //keep adding any devices which are posted by the bus
            leDeviceListAdapter.addDevice(busScanBluetoothDevices.getBluetoothDevice());
            leDeviceListAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventBus.getDefault().register(this);

        listView = (ListView) view.findViewById(R.id.listView);

        refresh = view.findViewById(R.id.refresh);


        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (((BLEScanActivity) getActivity()).checkLocationPermission()) {
                        if (((BLEScanActivity) getActivity()).checkLocationIsEnabled(getActivity())) {

                            ((BLEScanActivity) getActivity()).bluetoothDeviceList.clear();
                            leDeviceListAdapter.notifyDataSetChanged();

                            ((BLEScanActivity) getActivity()).showLoader("Please wait, we are trying to scan the device.", "");
                            ((BLEScanActivity) getActivity()).scanLeDevice(true);
                        }
                    }
                }
            }
        });


        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getActivity() != null) {
                    ((BLEScanActivity) getActivity()).buildSnackBar("Refreshing..");
                    Log.d("DeviceListFragment", "Refreshing");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                endRefresh();
                                if (((BLEScanActivity) getActivity()).checkLocationPermission()) {
                                    if (((BLEScanActivity) getActivity()).checkLocationIsEnabled(getActivity())) {

                                        ((BLEScanActivity) getActivity()).bluetoothDeviceList.clear();
                                        leDeviceListAdapter.notifyDataSetChanged();

                                        ((BLEScanActivity) getActivity()).showLoader("Please wait, we are trying to scan the device.", "");
                                        ((BLEScanActivity) getActivity()).scanLeDevice(true);
                                    }
                                }
                            }
                        }
                    }, 2000);
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() != null) {
                    ((BLEScanActivity) getActivity()).showLoader("Connecting", "");
                    ((BLEScanActivity) getActivity()).handlerSac.sendEmptyMessageDelayed(((BLEScanActivity) getActivity()).BLE_CONNECTING_STATUS, 20000);
                }
                final BluetoothDevice device = leDeviceListAdapter.getDevice(position);

                if (getActivity() != null) {
                    LibreLogger.d(this, "msg digest symmentric  standard product ID onItem Click\n" + device);
                    if (device == null) return;

                    ((BLEScanActivity) getActivity()).mBluetoothAdapter.stopLeScan(((BLEScanActivity) getActivity()).leScanCallback);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.v("BLEListFragment", "Ble List Fragment....on Connect");
                        ((BLEScanActivity) getActivity()).mBluetoothLeService.connect(device.getAddress());
                    }

                    mDeviceAddress = device.getAddress();
                    MavidApplication.mDeviceAddress = mDeviceAddress;

                    Log.v("StoringDeviceIp", "storing IP Addresss:" + MavidApplication.mCurrentDeviceAddress);
                    MavidApplication.lookFor = device.getName();
                    LibreLogger.d(this, "msg digest symmentric  standard productID Item get device name\n" + device.getName());

                    LibreMavidHelper.symmentricKey = "5375f0a6c57f076093602318907e44f2";

                }
            }
        });


        if (getActivity() != null) {
            initAdapter(((BLEScanActivity) getActivity()).bluetoothDeviceList);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void endRefresh() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    public void initAdapter(ArrayList<BluetoothDevice> bluetoothDeviceArrayList) {
        leDeviceListAdapter = new LeDeviceListAdapter(getContext(), bluetoothDeviceArrayList);
        listView.setAdapter(leDeviceListAdapter);
        listView.setDivider(getResources().getDrawable(R.drawable.horizontal_divider));
    }

}
