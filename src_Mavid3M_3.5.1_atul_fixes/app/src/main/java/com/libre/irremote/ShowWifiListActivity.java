package com.libre.irremote;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.adapters.SsidListAdapter;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.SAC.WifiConfigurationItemClickInterface;

import com.libre.irremote.R;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.irremote.models.ModelDeviceState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShowWifiListActivity extends BaseActivity implements WifiConfigurationItemClickInterface {


    RecyclerView ssidListRecyclerView;

    SsidListAdapter ssidListAdapter;

    String ipAddress;

    TextView tvNoData;

    private Dialog alert;
    CafeBar cafeBar;


    Bundle bundle = new Bundle();
    List<ModelDeviceState> modelDeviceStateList = new ArrayList<>();

    AppCompatImageView ivBack;

    AppCompatTextView tvConnectedSsidProfile;

    AppCompatTextView tvConnectedSsid;

    AppCompatTextView tvChooseNetworkLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_wifi_list_activity);
        initViews();
        appDeviceState();
    }

    public void initViews() {

        ssidListRecyclerView = findViewById(R.id.ssid_list_Recycler_view);
        tvNoData = findViewById(R.id.tv_no_data);
        ivBack = findViewById(R.id.iv_back);

        tvConnectedSsidProfile = findViewById(R.id.tv_connected_ssid_profile);

        tvConnectedSsid = findViewById(R.id.tv_connected_ssid);

        tvChooseNetworkLabel = findViewById(R.id.tv_choose_network_label);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            ipAddress = bundle.getString(Constants.INTENTS.IP_ADDRESS);
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    public void appDeviceState() {
        showLoader("Please Wait...", "");
        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.DEVICE_STATE, "", new CommandStatusListenerWithResponse() {

            @Override
            public void failure(Exception e) {
                closeLoader();
            }

            @Override
            public void success() {

            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void response(MessageInfo messageInfo) {
                Log.d("atul device state ", messageInfo.getMessage());
                try {
                    JSONArray deviceStateArray = new JSONArray(messageInfo.getMessage());
                    for (int i = 0; i < deviceStateArray.length(); i++) {

                        JSONObject deviceStateObject = deviceStateArray.getJSONObject(i);
                        if (deviceStateObject.getString("Name").equals("WiFi")) {

                            JSONArray ssidArray = new JSONArray(deviceStateObject.getString("SSID_list"));
                            for (int j = 0; j < ssidArray.length(); j++) {
                                final JSONObject ssidObject = ssidArray.getJSONObject(j);
                                Log.d("atul in ssidobject ", ssidObject.toString());
                                //add all ssid except the connected ssid
                                Log.d("atul_wifi_list",getconnectedSSIDname(ShowWifiListActivity.this));
                                if (!ssidObject.getString("ssid").equals(getconnectedSSIDname(ShowWifiListActivity.this))) {
                                    modelDeviceStateList.add(
                                            new ModelDeviceState(ssidObject.getString("ssid"), ssidObject.getString("profile")));
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                tvConnectedSsid.setText(ssidObject.getString("ssid"));
                                                tvConnectedSsidProfile.setText(" " + ssidObject.getString("profile") + " ");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setWifiListAdapter();
                            closeLoader();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    public void setWifiListAdapter() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ShowWifiListActivity.this);
        ssidListAdapter = new SsidListAdapter(ShowWifiListActivity.this, modelDeviceStateList, getconnectedSSIDname(ShowWifiListActivity.this));
        ssidListRecyclerView.setAdapter(ssidListAdapter);

        ssidListAdapter.setWifiConfigurationItemClickInterface(this);
        ssidListRecyclerView.setLayoutManager(linearLayoutManager);

        if (modelDeviceStateList.size() > 0) {
            tvNoData.setVisibility(View.GONE);
            tvChooseNetworkLabel.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            tvChooseNetworkLabel.setVisibility(View.GONE);
        }
    }

    public void showCustomAlertToChangeSSIDProfile(String title, String message) {
        if (alert == null) {

            alert = new Dialog(ShowWifiListActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setText(getResources().getString(R.string.gotoSettings));

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        alert.show();
    }

    @Override
    public void onItemClicked(final int pos) {
        showLoader("", "Please Wait...");
        JSONObject ssidObject = new JSONObject();
        try {
            ssidObject.put("ssid", modelDeviceStateList.get(pos).getSsid());
            ssidObject.put("profile", Integer.parseInt(modelDeviceStateList.get(pos).getProfile()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("atul on ssid clicked", ssidObject.toString());

        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.APP_WIFI_CONNECT, ssidObject.toString(), new CommandStatusListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                closeLoader();
                if (messageInfo.getMessage().equals("success")) {

                    if (!getconnectedSSIDname(ShowWifiListActivity.this)
                            .equals(modelDeviceStateList.get(pos).getSsid())) {
                        //connectd ssidName and the selected ssid does not match
                        //then take the user to change the wifiSettings
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //diffrent network
                                showCustomAlertToChangeSSIDProfile("Notice", "Device has been successfully connected to "
                                        + modelDeviceStateList.get(pos).getSsid() + ", please reconnect to " + modelDeviceStateList.get(pos).getSsid());
                            }
                        });
                    } else {
                        //same network
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showUserPresentInTheSameNetwork("Notice", "Device is already present in the same network");
                            }
                        });
                    }
                } else {
                    buildSnackBar("Action failed, Please try again later");
                }
                Log.d("atul_app_wifi_cnt", messageInfo.getMessage());
            }

            @Override
            public void failure(Exception e) {
                closeLoader();
                Log.d("atul_exception", e.toString());
            }

            @Override
            public void success() {

            }
        });
    }


    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(ShowWifiListActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();
    }

    public void showUserPresentInTheSameNetwork(String title, String message) {
        if (alert == null) {

            alert = new Dialog(ShowWifiListActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText(title);

        tv_alert_message.setText(message);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        alert.show();
    }
}
