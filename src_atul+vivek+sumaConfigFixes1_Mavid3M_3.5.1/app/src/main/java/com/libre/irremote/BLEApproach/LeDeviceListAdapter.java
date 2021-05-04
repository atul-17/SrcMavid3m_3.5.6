package com.libre.irremote.BLEApproach;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.libre.irremote.R;

import java.util.ArrayList;


public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;
    Context context;


    public LeDeviceListAdapter(Context context, ArrayList<BluetoothDevice> bluetoothDeviceList) {
        this.context = context;
        this.mLeDevices = bluetoothDeviceList;
    }

    private ArrayList<BluetoothDevice> getDeviceInfoArrayList() {
        return this.mLeDevices;
    }

    private Context getContext() {
        return this.context;
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            //create view
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.listitem_device, null);
        }

        TextView deviceName = (TextView) view.findViewById(R.id.device_name);

        BluetoothDevice device = mLeDevices.get(i);
        final String BTName = device.getName();
        if (BTName != null && BTName.length() > 0)
            deviceName.setText(BTName);
        else
            deviceName.setText("unknown_device");

        return view;
    }
}
