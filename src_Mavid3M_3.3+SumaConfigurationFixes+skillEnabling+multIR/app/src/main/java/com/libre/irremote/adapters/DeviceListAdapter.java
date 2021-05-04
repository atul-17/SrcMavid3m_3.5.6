package com.libre.irremote.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.libre.irremote.Constants.Constants;
import com.libre.irremote.DeviceListFragment;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.DeviceSettingsActivity;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.utility.OnButtonClickListViewInterface;

import java.util.ArrayList;

/**
 * Created by bhargav on 20/2/18.
 */

public class DeviceListAdapter extends BaseAdapter {
    ArrayList<DeviceInfo> deviceInfoArrayList;
    Context context;
    DeviceListFragment fragment;
    MavidHomeTabsActivity homeTabsActivity;


    private OnButtonClickListViewInterface clickCallback;

    public void setClickCallback(OnButtonClickListViewInterface clickCallback) {
        this.clickCallback = clickCallback;
    }

    String TAG = DeviceListAdapter.class.getSimpleName();

    public DeviceListAdapter(Context context, ArrayList<DeviceInfo> deviceInfoArrayList, DeviceListFragment deviceListFragment, MavidHomeTabsActivity mavidHomeTabsActivity) {
        this.context = context;
        this.deviceInfoArrayList = deviceInfoArrayList;
        this.fragment = deviceListFragment;
        this.homeTabsActivity = mavidHomeTabsActivity;
    }

    private ArrayList<DeviceInfo> getDeviceInfoArrayList() {
        return this.deviceInfoArrayList;
    }

    private Context getContext() {
        return this.context;
    }

    @Override
    public int getCount() {
        return getDeviceInfoArrayList().size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            //create view
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.device_list_item, null);
        }
        TextView deviceFriendlyName = (TextView) view.findViewById(R.id.deviceName);
        deviceFriendlyName.setSelected(true);
        deviceFriendlyName.setText(deviceInfoArrayList.get(i).getFriendlyName());
        LibreLogger.d(this,"suma in device list adapter get device name\n"+deviceInfoArrayList.get(i).getFriendlyName());
        Button otaUpgradeButton = (Button) view.findViewById(R.id.otaUpgrade);

        ImageView advancedSettings = view.findViewById(R.id.advancedSettings);

        advancedSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DeviceSettingsActivity.class);
                saveDeviceIpFromPreference(context,deviceInfoArrayList.get(i).getIpAddress());

                Log.v("DeviceListIpTest","Ip Test :"+deviceInfoArrayList.get(i).getIpAddress());


                intent.putExtra(Constants.INTENTS.IP_ADDRESS, deviceInfoArrayList.get(i).getIpAddress());
                context.startActivity(intent);
            }
        });

        ImageView ivRemote = view.findViewById(R.id.ivRemote);

        final SharedPreferences sharedPreferences = context.getSharedPreferences("Mavid", Context.MODE_PRIVATE);

        ivRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCallback.onClickListview(i);
            }
        });


        return view;
    }





    private String getOTAMessage(String message) {
        String tempArr[] = message.split(":");
        if (tempArr.length > 1) {
            return tempArr[1];
        }
        return null;
    }

    public void showMessageCommunicationStatus(final String title, final String message) {

        homeTabsActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(getContext().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                alert.show();
            }
        });

    }

    private static final String PREFS_NAME = "DeviceIpAddress";
    private static final String PREFS_KEY_NAME = "DeviceIpKEY";

    public boolean saveDeviceIpFromPreference(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_KEY_NAME, value);
        return editor.commit();
    }

}
