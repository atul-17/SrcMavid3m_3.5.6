package com.libre.irremote.BluetoothActivities;

import android.app.Dialog;

import androidx.appcompat.widget.SwitchCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.R;
import com.libre.irremote.utility.DB.MavidNodes;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.Util.BusEventProgressUpdate;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.models.ModelSectionHeadersBt;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MavidBtDiscoverActivity extends BaseActivity implements AvaliablePairedDeviceInterface,
        ShowCustomAlertDialogInterface {
    private RecyclerView recyclerViewShowBluetoothDevices;
    Map<String, String> btListMap = new TreeMap<>();
    private ArrayList<String> btDeviceList = new ArrayList<>();
    private ArrayList<String> deviceInfoList = new ArrayList<>();
    ImageView backBtn;
    private ArrayList<MavidSourceDeviceInfo> voodbtlist = new ArrayList<>();
    private ArrayList<MavidSourceDeviceInfo> voodPairedList = new ArrayList<>();

    private BtSourceDeviceInfo btdeviceInfo;
    SwipeRefreshLayout refreshLayout;
    //voodbtdeviceInfo
    private static DeviceInfo deviceInfo;
    JSONArray btScanListArray;
//    TextView refreshTextview;

    List<ModelSectionHeadersBt> modelSectionHeadersBtList = new ArrayList<>();

    BtSourceDeviceInfoAdapter itemArrayAdapter;

    Dialog dialog;

    boolean isAddedToPairedList = false;

//    AlertDialog alertDialog;

//    FloatingTextButton fab_refresh;

    CafeBar cafeBar;


    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    private Dialog alert;

    AppCompatImageView iv_refresh;

    boolean isTherePairedBtDevices = false;

    boolean isThereAvaliableDevices = false;

    String ipAddress;

    int callMSearchfiveTimes = 0;

    private SwitchCompat switch_bt_audio_src;

    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mavid_bt_discover);


        ipAddress = getIntent().getStringExtra(Constants.INTENTS.IP_ADDRESS);
        deviceInfo = MavidNodes.getInstance().getDeviceInfoFromDB(ipAddress);

        backBtn = findViewById(R.id.iv_back);


        recyclerViewShowBluetoothDevices = (RecyclerView) findViewById(R.id.recycler_view_show_bluetooth_devices);

        iv_refresh = findViewById(R.id.iv_refresh);

        EventBus.getDefault().register(this);

        getIntentParams();

        iv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isThereAvaliableDevices = false;
                buildSnackBar("Refreshing..");
                Log.d("DeviceListFragment", "Refreshing");
              //  refreshBtDiscovery();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (btDeviceList.size() == 0) {
            LibreLogger.d(this, "suma check bt device list size is 0");
        }
        if (btdeviceInfo != null) {

            btDeviceList.add(btdeviceInfo.getFriendlyName());
            btDeviceList = getBtDeviceList();
            LibreLogger.d(this, "suma in voodbtsetting for device ID" + btdeviceInfo.getDeviceID());

            LibreLogger.d(this, "suma iapddress" + ipAddress);
            btdeviceInfo.setIpAddress(ipAddress);
            LibreLogger.d(this, "suma in voodbtsetting for device ID" + btdeviceInfo.getDeviceID());

        }

        LibreLogger.d(this, "suma in voodbtsetting for devicename size" + btDeviceList.size());
        LibreLogger.d(this, "suma in voodbtsetting for devicename sizelistsize" + deviceInfoList.size());


        switch_bt_audio_src = findViewById(R.id.switch_bt_audio_src);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshView);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                buildSnackBar("Refreshing..");
                isThereAvaliableDevices = false;
                Log.d("DeviceListFragment", "Refreshing");
              //  refreshBtDiscovery();
            }
        });

        getA2dpBtType();


        switch_bt_audio_src.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showProgressLoadingAlertDialog();
                if (deviceInfo.getA2dpTypeValue().contains("SOURCE")) {
                   // LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(),
//                            LibreMavidHelper.COMMANDS.START_BT_SOURCE, "A2DP-SINK", new CommandStatusListenerWithResponse() {
//                                @Override
//                                public void response(final MessageInfo messageInfo) {
//
//                                    dismissProgressLoadingAlertDialog();
//                                    if (!messageInfo.getMessage().isEmpty()) {
//                                        JSONObject jObject = null;
//                                        try {
//                                            jObject = new JSONObject(messageInfo.getMessage());
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            String btTypeSink = jObject.getString("BT_TYPE");
//                                            LibreLogger.d(this, "suma in A2dp sink jsonobject next" + btTypeSink);
//                                            deviceInfo.setA2dpTypeValue(btTypeSink);
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    closeLoader();
//                                                    Log.d("suma in A2dp sink", String.valueOf(switch_bt_audio_src.isChecked()));
//                                                    checkAndUnCheckBluetoothSettingsA2dPButton(switch_bt_audio_src.isChecked());
//                                                }
//                                            });
//
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void failure(Exception e) {
//                                    Log.d("atul_2", e.getLocalizedMessage());
//                                    showLoader("Try Again Later...", "Action Failed");
//                                    final Timer timer3 = new Timer();
//                                    timer3.schedule(new TimerTask() {
//                                        public void run() {
//                                            closeLoader();
//                                            Intent intent = new Intent(MavidBtDiscoverActivity.this, MavidHomeTabsActivity.class);
//                                            startActivity(intent);
//                                            finish();
//                                            timer3.cancel();
//                                        }
//                                    }, 3000);
//                                }
//
//                                @Override
//                                public void success() {
//
//                                }
//                            });
                } else if (deviceInfo.getA2dpTypeValue().contains("SINK")) {
                  //  LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(),
//                            LibreMavidHelper.COMMANDS.START_BT_SOURCE, "A2DP-SOURCE", new CommandStatusListenerWithResponse() {
//                        @Override
//                        public void response(final MessageInfo messageInfo) {
//
//                            if (!messageInfo.getMessage().isEmpty()) {
//
//                                JSONObject jObject = null;
//                                try {
//                                    jObject = new JSONObject(messageInfo.getMessage());
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//
//                                try {
//                                    String btTypeSource = jObject.getString("BT_TYPE");
//                                    LibreLogger.d(this, "suma in A2dp sink jsonobject next" + btTypeSource);
//                                    deviceInfo.setA2dpTypeValue(btTypeSource);
//
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            closeLoader();
//                                            Log.d("suma in A2dp sink", String.valueOf(switch_bt_audio_src.isChecked()));
//                                            checkAndUnCheckBluetoothSettingsA2dPButton(switch_bt_audio_src.isChecked());
//
//                                        }
//                                    });
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
//
//                        }
//
//                        @Override
//                        public void failure(Exception e) {
//                            Log.d("atul_3", e.toString());
//                            showLoader("Try Again Later...", "Action Failed");
//                            final Timer timer3 = new Timer();
//                            timer3.schedule(new TimerTask() {
//                                public void run() {
//                                    closeLoader();
//                                    Intent intent = new Intent(MavidBtDiscoverActivity.this, MavidHomeTabsActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                    timer3.cancel();
//                                }
//                            }, 3000);
//
//                        }
//
//                        @Override
//                        public void success() {
//
//                        }
//                    });
                }


                if (deviceInfo.getA2dpTypeValue().contains("SOURCE")) {
                    if (switch_bt_audio_src.isChecked()) {
                        LibreLogger.d(this, " a2dp status is checked is yes or no  source if ");

                    } else {
                        if (!switch_bt_audio_src.isChecked()) {
                            LibreLogger.d(this, " a2dp status is checked is yes or no  source else ");

                        }
                    }
                }

                if (deviceInfo.getA2dpTypeValue().contains("SINK")) {
                    if (switch_bt_audio_src.isChecked()) {
                        LibreLogger.d(this, " a2dp status is checked is yes or no sink if ");

                    } else {
                        if (!switch_bt_audio_src.isChecked()) {
                            LibreLogger.d(this, " a2dp status is checked is yes or no  sink else ");

                        }
                    }
                }
            }
        });


    }


    public void checkAndUnCheckBluetoothSettingsA2dPButton(Boolean isChecked) {
        if (deviceInfo.getA2dpTypeValue().contains("SOURCE")) {
            if (isChecked) {
                callMSearchfiveTimes = 0;
                callMSearchEvery500ms(new Timer());
                getBtScanList(ipAddress);
            }

        } else if (deviceInfo.getA2dpTypeValue().contains("SINK")) {
            if (itemArrayAdapter != null) {
                modelSectionHeadersBtList.clear();
                itemArrayAdapter.notifyDataChanged(modelSectionHeadersBtList);
            }
            buildSnackBar("your Device is in Sink mode");
        }
    }


    private void getA2dpBtType() {
        showProgressLoadingAlertDialog();
//        LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.START_BT_STATUS, "", new CommandStatusListenerWithResponse() {
//            @Override
//            public void response(MessageInfo messageInfo) {
//
//                Log.d("atul", "in mavid application get the status" + messageInfo.getMessage() + "friendly name:  " + deviceInfo.getFriendlyName());
//                if (!messageInfo.getMessage().isEmpty()) {
//
//                    JSONObject jObject = null;
//                    try {
//                        jObject = new JSONObject(messageInfo.getMessage());
//
//                        String btTypeSource = jObject.getString("BT_TYPE");
//                        LibreLogger.d(this, "suma in A2dp sink jsonobject next" + btTypeSource);
//
//                        deviceInfo.setA2dpTypeValue(btTypeSource);
////                        closeLoader();
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Log.d("atul json ex", e.getMessage());
//
//                    }
//
//                }
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        setBTSourceVisibility();
//                    }
//                });
//
//            }
//
//            @Override
//            public void failure(final Exception e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d("atul  ex", e.getMessage());
////                        closeLoader();
//                        dismissProgressLoadingAlertDialog();
//                    }
//                });
//
//            }
//
//            @Override
//            public void success() {
//                closeLoader();
//            }
//        });
    }

    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(MavidBtDiscoverActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);

        cafeBar.show();

    }


    public void setBTSourceVisibility() {
        if (!deviceInfo.getA2dpTypeValue().equals("")) {

            if (deviceInfo.getA2dpTypeValue().contains("SINK")) {
                switch_bt_audio_src.setChecked(false);
                if (itemArrayAdapter != null) {
                    modelSectionHeadersBtList.clear();
                    itemArrayAdapter.notifyDataChanged(modelSectionHeadersBtList);
                }
                dismissProgressLoadingAlertDialog();

                buildSnackBar("Your Device is in Sink mode");
            } else if (deviceInfo.getA2dpTypeValue().contains("SOURCE")) {
                callMSearchEvery500ms(new Timer());
                callMSearchfiveTimes = 0;
                switch_bt_audio_src.setChecked(true);
                getBtScanList(ipAddress);


            }
        } else {
            if (itemArrayAdapter != null) {
                modelSectionHeadersBtList.clear();
                itemArrayAdapter.notifyDataChanged(modelSectionHeadersBtList);
            }
            dismissProgressLoadingAlertDialog();
        }
    }

//    public void refreshBtDiscovery() {
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                refreshLayout.setRefreshing(true);
//            }
//        }, 200);
//
//        callMSearchfiveTimes = 0;
//        showProgressLoadingAlertDialog();
//        modelSectionHeadersBtList.clear();
//        voodPairedList.clear();
//        itemArrayAdapter.notifyDataChanged(modelSectionHeadersBtList);
//        final Timer timer = new Timer();
//        try {
//            Log.d("atul_refresh", "device ip address " + deviceInfo.getIpAddress());
//            LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.START_BT_SEARCH, "", new CommandStatusListenerWithResponse() {
//                @Override
//                public void response(final MessageInfo messageInfo) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (!messageInfo.getMessage().isEmpty()) {
//                                LibreLogger.d(this, "suma in bt device info adapter\n" + messageInfo.getMessage());
//                                JSONObject mainObj = null;
//                                try {
//                                    mainObj = new JSONObject(messageInfo.getMessage());
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                try {
//                                    btScanListArray = mainObj.getJSONArray("BLUETOOTH DEVICES");
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                populateBtDeviceList(messageInfo.getMessage().toString());
//
//                                isAddedToPairedList = false;
//                                isTherePairedBtDevices = false;
//
//                                callMSearchEvery500ms(timer);
//
//                            } else {
//                                populateBtDeviceList(messageInfo.getMessage().toString());
//                            }
//                        }
//                    });
//                }
//
//                @Override
//                public void failure(final Exception e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissProgressLoadingAlertDialog();
//                            showActionFailedDailog();
//                            Log.d("atul_bt_error", e.toString());
//                        }
//                    });
//                }
//
//                @Override
//                public void success() {
//
//                }
//            });
//        } catch (final Exception e) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    dismissProgressLoadingAlertDialog();
//                    showActionFailedDailog();
//                    Log.d("Bluetooth", e.toString());
//                }
//            });
//        }
//
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                endRefresh();
//            }
//        }, 3500);
//    }


    public void callMSearchEvery500ms(final Timer timer){
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                LibreMavidHelper.setAdvertiserMessage(getMsearchPayload());
                try {
                    if (callMSearchfiveTimes < 5) {
                        LibreMavidHelper.advertise();
                        callMSearchfiveTimes = callMSearchfiveTimes + 1;
                        Log.d("atul_m-search", "count:" + String.valueOf(callMSearchfiveTimes));
                    } else {
                        timer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (itemArrayAdapter!=null) {
                                    itemArrayAdapter.notifyDataChanged(modelSectionHeadersBtList);
                                }
                            }
                        });
                    }
                } catch (WrongStepCallException e) {
                    e.printStackTrace();
                    Log.d("atul_m-search_except", e.toString());
                }
            }
        };
        timer.schedule(doAsynchronousTask, 0, 500); //execute in every 500 ms
    }

    private void endRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        });
    }


    public void getIntentParams() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            ipAddress = bundle.getString(Constants.INTENTS.IP_ADDRESS);
        }

//        getBtScanList(ipAddress);


    }


    @Override
    protected void onResume() {
        super.onResume();
        callMSearchEvery500ms(new Timer());
    }

    public void getBtScanList(String ipAddress) {

        //if bt scan list is empty only
        // then check whether avaliable devices and paired devices are both not present
        if (!dialog.isShowing()) {
            showProgressLoadingAlertDialog();
        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        dismissProgressLoadingAlertDialog();
//                        if (!isThereAvaliableDevices && !isTherePairedBtDevices) {
//                            customAlertForNoBtDevicesPresent();
//                        }
//                    }
//                });
//            }
//        }, 5000);
//        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.START_BT_SEARCH, "", new CommandStatusListenerWithResponse() {
//            @Override
//            public void response(final MessageInfo messageInfo) {
//                LibreLogger.d(this, "suma in device setitng screen bluetooth info" + messageInfo.getMessage());
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!messageInfo.getMessage().isEmpty()) {
//                            populateBtDeviceList(messageInfo.getMessage());
//                        }
//                        buildSnackBar("Your Device is in Source mode");
//                        if (!isTherePairedBtDevices) {
//                            dismissProgressLoadingAlertDialog();
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void failure(Exception e) {
//                dismissProgressLoadingAlertDialog();
//                showLoader("Try Again Later...", "Action Failed");
//                Log.d("atul_bt_settings_excep", e.toString());
//                final Timer timer3 = new Timer();
//                timer3.schedule(new TimerTask() {
//                    public void run() {
//                        closeLoader();
//                        Intent intent = new Intent(MavidBtDiscoverActivity.this, MavidHomeTabsActivity.class);
//                        startActivity(intent);
//                        finish();
//                        timer3.cancel();
//                    }
//                }, 3000);
//
//            }
//
//            @Override
//            public void success() {
//
//            }
//        });
    }


    public boolean isbtScanListEmpty(String scanList) {
        try {
            JSONObject mainObj = new JSONObject(scanList);
            JSONArray scanListArray = mainObj.getJSONArray("BLUETOOTH DEVICES");
            if (scanListArray.length() > 0) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }


    public void customAlertForNoBtDevicesPresent() {

        if (alert == null) {

            alert = new Dialog(MavidBtDiscoverActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }
        if (!MavidBtDiscoverActivity.this.isFinishing()) {
            alert.show();
        }

        tv_alert_title.setText("Bluetooth Devices");

        tv_alert_message.setText("There are no Bluetooth Source devices to manage");

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                onBackPressed();
            }
        });
    }

    public void populateBtDeviceList(final String scanList) {
        btListMap.clear();
        try {
            JSONObject mainObj = new JSONObject(scanList);
            JSONArray scanListArray = mainObj.getJSONArray("BLUETOOTH DEVICES");
            LibreLogger.d(this, "suma in voodbtsetting btscanlist" + scanListArray);
            voodbtlist.clear();
            for (int i = 0; i < scanListArray.length(); i++) {
                JSONObject obj = (JSONObject) scanListArray.get(i);
                if (obj.getString("friendlyName") == null
                    /* || (obj.getString("friendlyName").isEmpty())*/) {
                    continue;
                }
                btListMap.put(obj.getString("friendlyName"), obj.getString("uniqueDeviceId"));
                deviceInfoList.add(obj.getString("friendlyName"));

                btdeviceInfo = new BtSourceDeviceInfo(obj.getString("friendlyName"), obj.getString("uniqueDeviceId"));
                btdeviceInfo.setIpAddress(ipAddress);
                LibreLogger.d(this, "Bt FriendlyName and UniqueDeviceID" + obj.getString("friendlyName") + "ID" + obj.getString("uniqueDeviceId"));
                btdeviceInfo.setFriendlyName(obj.getString("friendlyName"));
                btdeviceInfo.setDeviceID(obj.getString("uniqueDeviceId"));

                MavidSourceDeviceInfo voodBtDeviceInfo = new MavidSourceDeviceInfo(obj.getString("uniqueDeviceId"), obj.getString("friendlyName"));
                voodbtlist.add(voodBtDeviceInfo);

                isThereAvaliableDevices = true;
            }

            modelSectionHeadersBtList = new ArrayList<>();
            if (!isThereAvaliableDevices) {
                voodbtlist.add(new MavidSourceDeviceInfo(null, "No device found"));
            }
            modelSectionHeadersBtList.add(new ModelSectionHeadersBt("Available Devices", voodbtlist));
            dismissProgressLoadingAlertDialog();
            setItemArrayAdapter();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setItemArrayAdapter() {

        recyclerViewShowBluetoothDevices.setLayoutManager(new LinearLayoutManager(this));
        itemArrayAdapter = new BtSourceDeviceInfoAdapter(this, modelSectionHeadersBtList,
                this,
                btdeviceInfo, MavidBtDiscoverActivity.this);

        recyclerViewShowBluetoothDevices.setAdapter(itemArrayAdapter);
        itemArrayAdapter.setShowCustomAlertDialogInterface(MavidBtDiscoverActivity.this);

    }

    public ArrayList<String> getBtDeviceList() {
        ArrayList<String> btList = new ArrayList<>();
        Set<String> keySet = btListMap.keySet();
        for (String btDevice : keySet) {
            LibreLogger.d(this, "BtDeviceName : " + btDevice);
            btList.add(btDevice);

        }
        return btList;
    }

    public boolean checkIfDeviceIsAlreadyAddedToPairedList(String deviceId) {
        for (ModelSectionHeadersBt modelSectionHeadersBt : modelSectionHeadersBtList) {
            if (modelSectionHeadersBt.getSectionText().equals("Paired Devices")) {
                for (MavidSourceDeviceInfo mavidSourceDeviceInfo : modelSectionHeadersBt.getChildItems()) {
                    if (mavidSourceDeviceInfo.getDeviceID().equals(deviceId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getA2DPStatusUpdates(BusEventProgressUpdate.SendFirmwareProgressEvents busEventsGetA2DPStatus) {

        Log.d("atul_in_event_bus", busEventsGetA2DPStatus.getA2dpDeviceName() + " : "
                + busEventsGetA2DPStatus.getA2dpDeviceUUID() + " : " + busEventsGetA2DPStatus.getA2dpStatus());


        if (busEventsGetA2DPStatus.getA2dpStatus() != null
                && busEventsGetA2DPStatus.getA2dpDeviceUUID() != null && busEventsGetA2DPStatus.getA2dpDeviceName() != null) {

            if (!busEventsGetA2DPStatus.getA2dpStatus().isEmpty()) {


                if (busEventsGetA2DPStatus.getA2dpStatus().equals("CONNECTED")
                        && busEventsGetA2DPStatus.getConnctedDeviceFriendlyName().equals(deviceInfo.getFriendlyName())
                        && !checkIfDeviceIsAlreadyAddedToPairedList(busEventsGetA2DPStatus.getA2dpDeviceUUID()) && !isAddedToPairedList) {

                    isTherePairedBtDevices = true;
                    getVoodPairedList();

                    Log.d("atul_in_added_pair_dev", busEventsGetA2DPStatus.getA2dpDeviceName() + " : "
                            + busEventsGetA2DPStatus.getA2dpDeviceUUID() + " : " + busEventsGetA2DPStatus.getA2dpStatus() + "boolean: " + isAddedToPairedList);

                    //add a new section called pairedDevices
                    MavidSourceDeviceInfo pairedDevice = new MavidSourceDeviceInfo(busEventsGetA2DPStatus.getA2dpDeviceUUID(), busEventsGetA2DPStatus.getA2dpDeviceName());

                    pairedDevice.setConnectionStatus(busEventsGetA2DPStatus.getA2dpStatus());

                    ModelSectionHeadersBt pairedDevicesSection = new ModelSectionHeadersBt("Paired Devices", voodPairedList);

                    if (itemArrayAdapter!=null) {
                        itemArrayAdapter.insertNewSection(pairedDevicesSection);
                    }
                    modelSectionHeadersBtList.add(pairedDevicesSection);

                    itemArrayAdapter.insertNewChild(pairedDevice, getSectionPositionOfPairedDevices());

                    itemArrayAdapter.notifyDataChanged(modelSectionHeadersBtList);

                    isAddedToPairedList = true;

                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void setBluetoothDevice(String connectionStatus, String deviceId, String sectionText, int sectionPos, int childPos, DismissProgressBarInterface dismissProgressBarInterface) {

        Log.d("atul_btDevice_Interface", "connectionStatus:  " + connectionStatus + "DeviceId  " + deviceId + "sectionText:  " + sectionText);

        isAddedToPairedList = false;

        Iterator<ModelSectionHeadersBt> modelSectionHeadersBtIterator = modelSectionHeadersBtList.iterator();

        while (modelSectionHeadersBtIterator.hasNext()) {

            ModelSectionHeadersBt modelSectionHeadersBt = modelSectionHeadersBtIterator.next();

            //Available Devices
            if (sectionText.equalsIgnoreCase("Available Devices")) {

                Iterator<MavidSourceDeviceInfo> iteratorBluetoothDisConnctedDevices = modelSectionHeadersBt.getChildItems().iterator();

                if(iteratorBluetoothDisConnctedDevices.hasNext()) {

                    MavidSourceDeviceInfo
                            avaliableDevice = iteratorBluetoothDisConnctedDevices.next();

                    if (avaliableDevice.getDeviceID().equals(deviceId)) {

                        if (avaliableDevice.getConnectionStatus().equalsIgnoreCase("CONNECTED")) {
                            //remove from avaliableDevice section
                            iteratorBluetoothDisConnctedDevices.remove();

                            if (getSizeOfAvaliableDevices() == 0) {
                                if (getSectionPositionOfAvaliableDevices() != -1) {
                                    itemArrayAdapter.removeSection(getSectionPositionOfAvaliableDevices());
                                }
                            }

                            if (checkPairedSectionIsPresent()) {
                                //add to paired Device section
                                itemArrayAdapter.insertNewChild(avaliableDevice, getSectionPositionOfPairedDevices());
                            } else {
                                getVoodPairedList();
                                if (!checkIfDeviceIsAlreadyAddedToPairedList(avaliableDevice.getDeviceID()) && !isAddedToPairedList) {
                                    //add a new section called pairedDevices
                                    MavidSourceDeviceInfo pairedDevice = new MavidSourceDeviceInfo(avaliableDevice.getDeviceID(), avaliableDevice.getFriendlyName());

                                    ModelSectionHeadersBt pairedDevicesSection = new ModelSectionHeadersBt("Paired Devices", voodPairedList);


                                    itemArrayAdapter.insertNewSection(pairedDevicesSection);

                                    modelSectionHeadersBtList.add(pairedDevicesSection);

                                    itemArrayAdapter.insertNewChild(pairedDevice, getSectionPositionOfPairedDevices());

                                    isAddedToPairedList = true;
                                }
                            }
                        }else{
                            buildSnackBar("The device still in "+connectionStatus+" State");
                        }
                    }
                }

            } else {

                Iterator<MavidSourceDeviceInfo> iteratorBluetoothConnectedDevics = modelSectionHeadersBt.getChildItems().iterator();

                while (iteratorBluetoothConnectedDevics.hasNext()) {

                    MavidSourceDeviceInfo pairedDevice = iteratorBluetoothConnectedDevics.next();

                    if (pairedDevice.getDeviceID().equals(deviceId)) {

                        if (pairedDevice.getConnectionStatus().equalsIgnoreCase("NOT_CONNECTED")) {

                            //remove from paired Device
                            iteratorBluetoothConnectedDevics.remove();

                            if (getSizeOfPairedDevices() == 0) {
                                if (getSectionPositionOfPairedDevices() != -1) {
                                    itemArrayAdapter.removeSection(getSectionPositionOfPairedDevices());//paired
                                }
                            }

                            if (checkAvailableSectionIsPresent()) {

                                //add to avaliable device
                                itemArrayAdapter.insertNewChild(pairedDevice, getSectionPositionOfAvaliableDevices());
                            } else {
                                getVoodbtlist();

                                //add a avaliable section

                                MavidSourceDeviceInfo avaliableDevice = new MavidSourceDeviceInfo(pairedDevice.getDeviceID(), pairedDevice.getFriendlyName());

                                ModelSectionHeadersBt avaliableDevicesSection = new ModelSectionHeadersBt("Available Devices", voodbtlist);


                                itemArrayAdapter.insertNewSection(avaliableDevicesSection);

                                modelSectionHeadersBtList.add(avaliableDevicesSection);

                                itemArrayAdapter.insertNewChild(avaliableDevice, getSectionPositionOfAvaliableDevices());

                            }
                        }
                    }
                }
            }
        }

        dismissProgressLoadingAlertDialog();
        itemArrayAdapter.notifyDataSetChanged();
        dismissProgressBarInterface.dismissProgress();
        //refreshBtDiscovery();

    }

    private void showProgressLoadingAlertDialog() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MavidBtDiscoverActivity.this);
            builder.setCancelable(false); // if you want user to wait for some process to finish,
            builder.setView(R.layout.layout_loading_dialog);
            dialog = builder.create();
        }
        if (!MavidBtDiscoverActivity.this.isFinishing()) {
            dialog.show();
        }

    }


    private void dismissProgressLoadingAlertDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public int getSizeOfPairedDevices() {
        for (ModelSectionHeadersBt modelSectionHeadersBt : modelSectionHeadersBtList) {
            if (modelSectionHeadersBt.getSectionText().equals("Paired Devices")) {
                return modelSectionHeadersBt.getChildItems().size();
            }
        }
        return 0;
    }


    public int getSizeOfAvaliableDevices() {
        for (ModelSectionHeadersBt modelSectionHeadersBt : modelSectionHeadersBtList) {
            if (modelSectionHeadersBt.getSectionText().equalsIgnoreCase("Available Devices")) {
                return modelSectionHeadersBt.getChildItems().size();
            }
        }
        return 0;
    }

    public int getSectionPositionOfPairedDevices() {
        for (int i = 0; i < modelSectionHeadersBtList.size(); i++) {
            if (modelSectionHeadersBtList.get(i).getSectionText().equalsIgnoreCase("Paired Devices")) {
                return i;
            }
        }
        return -1;
    }

    public int getSectionPositionOfAvaliableDevices() {
        for (int i = 0; i < modelSectionHeadersBtList.size(); i++) {
            if (modelSectionHeadersBtList.get(i).getSectionText().equalsIgnoreCase("Available Devices")) {
                return i;
            }
        }
        return -1;
    }

    public boolean checkPairedSectionIsPresent() {
        for (ModelSectionHeadersBt modelSectionHeadersBt : modelSectionHeadersBtList) {
            if (modelSectionHeadersBt.getSectionText().equals("Paired Devices")) {
                return true;
            }
        }
        return false;
    }

    public boolean checkAvailableSectionIsPresent() {
        for (ModelSectionHeadersBt modelSectionHeadersBt : modelSectionHeadersBtList) {
            if (modelSectionHeadersBt.getSectionText().equals("Available Devices")) {
                return true;
            }
        }
        return false;
    }


    public ArrayList<MavidSourceDeviceInfo> getVoodPairedList() {
        if (voodPairedList == null) {
            voodPairedList = new ArrayList<>();
        }
        return voodPairedList;
    }

    public ArrayList<MavidSourceDeviceInfo> getVoodbtlist() {
        if (voodbtlist == null) {
            voodbtlist = new ArrayList<>();
        }
        return voodbtlist;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void showActionFailedDailog() {

        if (alert == null) {

            alert = new Dialog(MavidBtDiscoverActivity.this);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            btn_ok = alert.findViewById(R.id.btn_ok);
        }

        tv_alert_title.setText("");
        tv_alert_message.setText("Action Failed...Please Try again");

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                onBackPressed();
            }
        });

        alert.show();
    }

    @Override
    public void showCustomAlertDialog() {
        showActionFailedDailog();
    }

}

