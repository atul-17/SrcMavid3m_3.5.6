package com.libre.irremote;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.danimahardhika.cafebar.CafeBar;
import com.google.gson.Gson;
import com.libre.irremote.adapters.DeviceListAdapter;
import com.libre.irremote.BLEApproach.BLEBlinkingFragment;
import com.libre.irremote.irActivites.IRRestoreSelectionActivity;
import com.libre.irremote.models.ModelLdapi2AcModes;
import com.libre.irremote.models.ModelRemoteDetails;
import com.libre.irremote.models.ModelRemoteSubAndMacDetils;
import com.libre.irremote.utility.FirmwareClasses.FirmwareUpdateHashmap;
import com.libre.irremote.utility.FirmwareClasses.UpdatedMavidNodes;
import com.libre.irremote.utility.FirmwareClasses.XmlParser;
import com.libre.irremote.alexa_signin.AlexaSignInActivity;

import com.libre.irremote.alexa_signin.AlexaThingsToTryDoneActivity;

import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.Listeners.DeviceListener;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.utility.OnButtonClickCallback;
import com.libre.irremote.utility.OnButtonClickListViewInterface;
import com.libre.irremote.utility.OnDeviceApiSucessCallback;
import com.libre.irremote.utility.OnGetUserApplianceUserInfoInterface;
import com.libre.irremote.utility.UIRelatedClass;
import com.libre.irremote.irActivites.irMultipleRemotes.IRAddOrSelectRemotesActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DeviceListFragment extends Fragment implements OnButtonClickListViewInterface {

    ListView deviceListView;
    private ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();
    DeviceListAdapter deviceListAdapter;
    TextView config_item, config_item1;
    //    TextView tvNoSpeakerFound;
    SwipeRefreshLayout refreshLayout;
    //    ImageView imagerefresh;
    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;
    private Dialog alert;

    AppCompatButton btn_ok;
    AppCompatButton btn_cancel;
    int avsStateOneCount = 0, avsStatetwoCount = 0, avsStatezeroCount = 0;
    private static boolean isAVSState2InProgress = false, isAVSState0InProgress = false;

    private XmlParser myXml;
    private ArrayList<DeviceInfo> deviceInfoListSwipe = new ArrayList<>();

    UpdatedMavidNodes updateNode;

    FirmwareUpdateHashmap updateHash = FirmwareUpdateHashmap.getInstance();

    AppCompatTextView tv_connected_speakers_label;
    String myDeviceIp;
    FrameLayout no_device_frame_layout;

    AppCompatTextView tv_refresh;

    AppCompatTextView tv_setup_speaker;

    CafeBar cafeBar;

    String TAG = DeviceListFragment.class.getSimpleName();
    UIRelatedClass uiRelatedClass = new UIRelatedClass();

    private Dialog mDialog;

    ModelRemoteSubAndMacDetils modelRemoteSubAndMacDetils = new ModelRemoteSubAndMacDetils();

    Timer irButtonListTimerTask = new Timer();

    private final int LDAPI2_TIMOUT = 1;

    HashMap<String, String> workingRemoteButtonsHashMap = new HashMap();


    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case LDAPI2_TIMOUT:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeLoader();
                            irButtonListTimerTask.cancel();
                            Log.d(TAG, "Error");
                            uiRelatedClass.buidCustomSnackBarWithButton(getActivity(), "There sees to be an error!!.Please try after some time",
                                    "Go Back", (AppCompatActivity) getActivity());
                        }
                    });
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mavid_devices_list, container, false);
        deviceInfoList.clear();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);


        if (getActivity() != null) {
            ((MavidHomeTabsActivity) getActivity()).showProgressBar();
            Handler handler = new Handler();
            ((MavidHomeTabsActivity) getActivity()).ivRefresh.setVisibility(View.VISIBLE);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        if (deviceInfoList.size() > 0) {
                            no_device_frame_layout.setVisibility(View.GONE);
                            tv_connected_speakers_label.setVisibility(View.VISIBLE);
                        } else {
                            tv_connected_speakers_label.setVisibility(View.GONE);
                            no_device_frame_layout.setVisibility(View.VISIBLE);
                            ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();

                        }
                    }
                }
            };
            handler.postDelayed(runnable, 5000);
        }

        if (getContext() != null) {
            deviceListAdapter = new DeviceListAdapter(getContext(), deviceInfoList, DeviceListFragment.this, (MavidHomeTabsActivity) getActivity());
            deviceListView.setAdapter(deviceListAdapter);
            deviceListAdapter.setClickCallback(this);
        }
        setDeviceListenerInterface();

        Log.d(TAG, "sub: " + getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", ""));

    }


    private void initViews(View view) {


        deviceListView = (ListView) view.findViewById(R.id.deviceListView);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);

//        tvNoSpeakerFound = view.findViewById(R.id.tv_no_speaker_found);

        tv_connected_speakers_label = view.findViewById(R.id.tv_connected_speakers_label);

        no_device_frame_layout = view.findViewById(R.id.no_device_frame_layout);

        tv_refresh = view.findViewById(R.id.tv_refresh);

        tv_setup_speaker = view.findViewById(R.id.tv_setup_speaker);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                ((MavidHomeTabsActivity) getActivity()).showProgressBar();

                no_device_frame_layout.setVisibility(View.GONE);

                buildSnackBar("Refreshing..");

//                Toast.makeText(getContext(), "Refreshing..", Toast.LENGTH_SHORT).show();
                Log.d("DeviceListFragment", "Refreshing");

                if (deviceListAdapter != null) {
                    deviceInfoList.clear();
                    deviceListAdapter.notifyDataSetChanged();
                }

                if (getActivity() != null) {
                    ((MavidHomeTabsActivity) getActivity()).refreshDiscovery();
                    LibreLogger.d(this, "suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery devicelist1");

                }

                try {
                    MavidApplication.setDeviceListener(new DeviceListener() {
                        @Override
                        public void newDeviceFound(final DeviceInfo deviceInfo) {
                            LibreLogger.d(this, "suma in newdevice found refresh1" + deviceInfo.getFriendlyName());

                            if (!isDiscoveredAlreadySwipeLog(deviceInfo)) {

                                LibreLogger.d(this, "suma in newdevice found refresh2" + deviceInfo.getFriendlyName());

                                deviceInfoList.add(deviceInfo);

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (deviceInfoList.size() > 0) {
                                                no_device_frame_layout.setVisibility(View.GONE);
                                                tv_connected_speakers_label.setVisibility(View.VISIBLE);
                                            } else {
                                                no_device_frame_layout.setVisibility(View.VISIBLE);
                                                tv_connected_speakers_label.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }

                                LibreLogger.d(this, " A new device is found whose ip discovered already 2" + deviceInfo.getIpAddress() + "friendlyname" + deviceInfo.getFriendlyName());
//                                LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.FW_URL, "OTA_XML_URL", new CommandStatusListenerWithResponse() {
//                                    @Override
//                                    public void failure(Exception e) {
//
//                                    }
//
//                                    @Override
//                                    public void success() {
//
//                                    }
//
//                                    @Override
//                                    public void response(MessageInfo messageInfo) {
//                                        LibreLogger.d(this, "suma in response device list 2\n" + messageInfo.getMessage() + "device ip" + deviceInfo.getFriendlyName());
//
//                                        deviceInfo.setFw_url(messageInfo.getMessage());
//                                        LibreLogger.d(this, "suma in mavid application check device url" + messageInfo.getMessage() + "device ip" + deviceInfo.getFriendlyName());
//                                        myXml = new XmlParser(deviceInfo.getFw_url());
//                                        myXml.fetchXml(new DownloadMyXmlListener() {
//
//                                            @Override
//                                            public void success(String fw_version, String bsl_version) {
//                                                LibreLogger.d(this, "getFw_version: " + fw_version);
//                                                if (!fw_version.isEmpty()) {
//                                                    MavidApplication.urlFailure = false;
//                                                    deviceInfo.setUpdatedFwVersion(fw_version);
//                                                    deviceInfo.setBslInfoAfterSplit(bsl_version);
//                                                    CheckFirmwareInfoClass compareFirmwareClass = new CheckFirmwareInfoClass(deviceInfo);
//                                                    updateNode = getMavidNodesUpdate(deviceInfo.getIpAddress());
//                                                    updateNode.setFriendlyname(deviceInfo.getFriendlyName());
//                                                    updateNode.setFwVersion(deviceInfo.getFwVersion());
//                                                    FirmwareUpdateHashmap.getInstance().checkIfNodeAlreadyPresentinList(deviceInfo.getIpAddress());
//
//                                                    LibreLogger.d(this, "suma in mavid application already notpresent" + deviceInfo.getFriendlyName());
//                                                    updateNode.setFwUpdateNeededBtnEnable(compareFirmwareClass.checkFirmwareUpdateButtonEnableorDisable(deviceInfo.getIpAddress()));
//                                                    if (updateNode.isFwUpdateNeededBtnEnableCheck()) {
//                                                        MavidApplication.checkIsFwBtnLatest = true;
//                                                        LibreLogger.d(this, "suma need fwupdatebtn has latest no need update secondcheck" + updateNode.getFriendlyname());
//                                                    } else {
//                                                        MavidApplication.checkIsFwBtnLatest = false;
//                                                        LibreLogger.d(this, "suma need fwupdatebtn latest yes need update secondcheck" + updateNode.getFriendlyname());
//                                                    }
//
//                                                    updateNode.setFirmwareUpdateNeeded(compareFirmwareClass.checkIfFirmwareUpdateNeeded(deviceInfo.getIpAddress()));
//                                                    if (updateNode.isFirmwareUpdateNeeded()) {
//                                                        LibreLogger.d(this, "suma need fw" + updateNode.getFriendlyname());
//                                                        MavidApplication.isFwNeeded = true;
//                                                    } else {
//                                                        LibreLogger.d(this, "suma need fw no" + updateNode.getFriendlyname());
//                                                        MavidApplication.isFwNeeded = false;
//                                                    }
//
//
//                                                    // }
//                                                }
//
//                                            }
//
//                                            @Override
//                                            public void failure(Exception e) {
//                                                if (!MavidApplication.fwupdatecheckPrivateBuild) {
//                                                    MavidApplication.urlFailure = true;
//                                                }
//                                                LibreLogger.d(this, "exception while parsing the XML 1 check suma" + e.toString());
//                                                // myXml = new XmlParser(deviceInfo.getFw_url());
//                                            }
//                                        });
//                                    }
//                                });
                            }
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
                                    }
                                });
                            }
                        }

                        @Override
                        public void deviceGotRemoved(DeviceInfo deviceInfo) {

                        }

                        @Override
                        public void deviceDataReceived(MessageInfo messageInfo) {

                        }

                        @Override
                        public void failures(Exception e) {

                        }

                        @Override
                        public void checkFirmwareInfo(DeviceInfo deviceInfo) {
                            LibreLogger.d(this, "suma in mavid application check firmwareinfo" + deviceInfo.getFriendlyName());
                            LibreLogger.d(this, "suma in devicesetting activity" + deviceInfo.getBslOldValue());
                            LibreLogger.d(this, "suma in devicesetting activity" + deviceInfo.getBslNewValue());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (deviceInfoList.size() > 0) {
                            no_device_frame_layout.setVisibility(View.GONE);
                            tv_connected_speakers_label.setVisibility(View.VISIBLE);
                        } else {
                            tv_connected_speakers_label.setVisibility(View.GONE);
                            no_device_frame_layout.setVisibility(View.VISIBLE);
                        }
                        if (getActivity() != null) {
                            ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
                        }
                        endRefresh();
                    }
                }, 5000);

            }
        });

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
//                LibreMavidHelper.askRefreshToken(deviceInfoList.get(i).getIpAddress(), new CommandStatusListenerWithResponse() {
//                    @Override
//                    public void response(MessageInfo messageInfo) {
//                        String messages = messageInfo.getMessage();
//                        Log.d("DeviceManager", " got alexa token " + messages);
//                        handleAlexaRefreshTokenStatus(messageInfo.getIpAddressOfSender(), messageInfo.getMessage());
//                    }
//
//                    @Override
//                    public void failure(Exception e) {
//
//                    }
//
//                    @Override
//                    public void success() {
//
//                    }
//                });
//                LibreMavidHelper.askRefreshToken(deviceInfoList.get(i).getIpAddress(), new CommandStatusListenerWithResponse() {
//                    @Override
//                    public void response(MessageInfo messageInfo) {
//                        String messages = messageInfo.getMessage();
//                        Log.v("RecievedState", "state is getting thread timer state 2 if suma check inside callback"+avsStatetwoCount);
//                        Log.d("DeviceManager", " got alexa token After Sac" + messages);
//                        messages = messages.substring(messages.length() - 1);
//                        handleAlexaLoginTokenStatus(deviceInfoList.get(i).getIpAddress(),,messages);
//                    }
//
//                    @Override
//                    public void failure(Exception e) {
//
//                    }
//
//                    @Override
//                    public void success() {
//
//                    }
//                });
              //  showLoader("Please Wait!!", "Fetching Alexa Login Status...");

                //askRefreshToken((deviceInfoList.get(i).getIpAddress()));
                myDeviceIp = deviceInfoList.get(i).getIpAddress();
            }
        });
        if (getActivity() != null) {
            tv_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MavidHomeTabsActivity) getActivity()).showProgressBar();

                    if (deviceListAdapter != null) {
                        deviceInfoList.clear();
                        deviceListAdapter.notifyDataSetChanged();
                    }

                    no_device_frame_layout.setVisibility(View.GONE);

                    ((MavidHomeTabsActivity) getActivity()).refreshDiscovery();
                    LibreLogger.d(this, "suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery devicelist2");

                    setDeviceListenerInterface();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                if (deviceInfoList.size() > 0) {
                                    no_device_frame_layout.setVisibility(View.GONE);
                                    tv_connected_speakers_label.setVisibility(View.VISIBLE);
                                } else {
                                    tv_connected_speakers_label.setVisibility(View.GONE);
                                    no_device_frame_layout.setVisibility(View.VISIBLE);
                                }
                                ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
                            }
                            endRefresh();
                        }
                    }, 5000);
                }
            });
        }


        tv_setup_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    ((MavidHomeTabsActivity) getActivity()).bottomNavigation.setSelectedItemId(R.id.action_add);//add a new device
                    ((MavidHomeTabsActivity) getActivity()).inflateFragments(BLEBlinkingFragment.class.getSimpleName());
                }
            }
        });

        if (getActivity() != null) {

            ((MavidHomeTabsActivity) getActivity()).ivRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (deviceListAdapter != null) {
                        deviceInfoList.clear();
                        deviceListAdapter.notifyDataSetChanged();
                    }
                    buildSnackBar("Refreshing..");

//                    Toast.makeText(getContext(), "Refreshing..", Toast.LENGTH_SHORT).show();
                    Log.d("DeviceListFragment", "Refreshing");
                    if (getActivity() != null) {
                        ((MavidHomeTabsActivity) getActivity()).showProgressBar();
                    }
                    no_device_frame_layout.setVisibility(View.GONE);


                    ((MavidHomeTabsActivity) getActivity()).refreshDiscovery();
                    LibreLogger.d(this, "suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery devicelst3");

                    setDeviceListenerInterface();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {

                                if (deviceInfoList.size() > 0) {
                                    no_device_frame_layout.setVisibility(View.GONE);
                                    tv_connected_speakers_label.setVisibility(View.VISIBLE);
                                } else {
                                    tv_connected_speakers_label.setVisibility(View.GONE);
                                    no_device_frame_layout.setVisibility(View.VISIBLE);
                                    ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();

                                }
                            }
                            endRefresh();
                            if (((MavidHomeTabsActivity) getActivity()) != null) {
                                ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
                            }
                        }
                    }, 5000);
                }
            });
        }
    }


    public void setDeviceListenerInterface() {
        try {
            MavidApplication.setDeviceListener(new DeviceListener() {
                @Override
                public void newDeviceFound(final DeviceInfo deviceInfo) {
                    Log.d("DeviceListFragment", " A new device is found whose ip address is " + deviceInfo.getIpAddress());
                    if (getActivity() != null) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isDiscoveredAlready(deviceInfo)) {
                                    LibreLogger.d(this, " A new device is found whose ip discovered already 3" + deviceInfo.getIpAddress() + "friendlyname" + deviceInfo.getFriendlyName());
                                    deviceInfoList.add(deviceInfo);
                                    LibreLogger.d(this, "suma in device list activity" + deviceInfoList.size());
                                    if (deviceInfoList.size() > 0) {
                                        no_device_frame_layout.setVisibility(View.GONE);
                                        tv_connected_speakers_label.setVisibility(View.VISIBLE);
                                    } else {
                                        tv_connected_speakers_label.setVisibility(View.GONE);
                                        no_device_frame_layout.setVisibility(View.VISIBLE);
                                    }
//                                    LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.FW_URL, "OTA_XML_URL", new CommandStatusListenerWithResponse() {
//                                        @Override
//                                        public void failure(Exception e) {
//                                            if (((MavidHomeTabsActivity) getActivity()) != null) {
//                                                ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
//                                            }
//                                        }
//
//                                        @Override
//                                        public void success() {
//
//                                        }
//
//                                        @Override
//                                        public void response(MessageInfo messageInfo) {
//                                            LibreLogger.d(this, "suma in response device list 2\n" + messageInfo.getMessage() + "device ip" + deviceInfo.getFriendlyName());
//                                            deviceInfo.setFw_url(messageInfo.getMessage());
//                                            LibreLogger.d(this, "suma in mavid application check device url" + messageInfo.getMessage() + "device ip" + deviceInfo.getFriendlyName());
//                                            myXml = new XmlParser(deviceInfo.getFw_url());
//                                            myXml.fetchXml(new DownloadMyXmlListener() {
//
//                                                @Override
//                                                public void success(String fw_version, String bsl_version) {
//                                                    LibreLogger.d(this, "getFw_version: " + fw_version);
//                                                    if (!fw_version.isEmpty()) {
//                                                        MavidApplication.urlFailure = false;
//                                                        deviceInfo.setUpdatedFwVersion(fw_version);
//                                                        deviceInfo.setBslInfoAfterSplit(bsl_version);
//                                                        CheckFirmwareInfoClass compareFirmwareClass = new CheckFirmwareInfoClass(deviceInfo);
//                                                        updateNode = getMavidNodesUpdate(deviceInfo.getIpAddress());
//                                                        updateNode.setFriendlyname(deviceInfo.getFriendlyName());
//                                                        updateNode.setFwVersion(deviceInfo.getFwVersion());
//
//                                                        FirmwareUpdateHashmap.getInstance().checkIfNodeAlreadyPresentinList(deviceInfo.getIpAddress());
//
//                                                        LibreLogger.d(this, "suma in mavid application already notpresent" + deviceInfo.getFriendlyName());
//                                                        updateNode.setFwUpdateNeededBtnEnable(compareFirmwareClass.checkFirmwareUpdateButtonEnableorDisable(deviceInfo.getIpAddress()));
//                                                        if (updateNode.isFwUpdateNeededBtnEnableCheck()) {
//                                                            MavidApplication.checkIsFwBtnLatest = true;
//                                                            LibreLogger.d(this, "suma need fwupdatebtn has latest no need update secondcheck" + updateNode.getFriendlyname());
//                                                        } else {
//                                                            MavidApplication.checkIsFwBtnLatest = false;
//                                                            LibreLogger.d(this, "suma need fwupdatebtn latest yes need update secondcheck" + updateNode.getFriendlyname());
//                                                        }
//
//                                                        updateNode.setFirmwareUpdateNeeded(compareFirmwareClass.checkIfFirmwareUpdateNeeded(deviceInfo.getIpAddress()));
//                                                        if (updateNode.isFirmwareUpdateNeeded()) {
//                                                            LibreLogger.d(this, "suma need fw" + updateNode.getFriendlyname());
//                                                            MavidApplication.isFwNeeded = true;
//                                                        } else {
//                                                            LibreLogger.d(this, "suma need fw no" + updateNode.getFriendlyname());
//                                                            MavidApplication.isFwNeeded = false;
//                                                        }
//
//
//                                                        // }
//                                                    }
//
//                                                }
//
//                                                @Override
//                                                public void failure(Exception e) {
//                                                    if (!MavidApplication.fwupdatecheckPrivateBuild) {
//                                                        MavidApplication.urlFailure = true;
//                                                    }
//                                                    LibreLogger.d(this, "exception while parsing the XML 2mcheck suma" + e.toString());
//                                                    // myXml = new XmlParser(deviceInfo.getFw_url());
//                                                }
//                                            });
//                                        }
//                                    });

                                }


                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            deviceListAdapter.notifyDataSetChanged();
                                            ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
                                        }
                                    });

                                }
                            }
                        });
                    }
                }

                @Override
                public void deviceGotRemoved(DeviceInfo deviceInfo) {

                }

                @Override
                public void deviceDataReceived(MessageInfo messageInfo) {

                }

                @Override
                public void failures(Exception e) {
                    if (getActivity() != null) {
                        ((MavidHomeTabsActivity) getActivity()).dismissProgressBar();
                    }
                }

                @Override
                public void checkFirmwareInfo(DeviceInfo deviceInfo) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private boolean isDiscoveredAlready(DeviceInfo deviceInfo) {
        deviceInfoList.contains(deviceInfo);
        for (DeviceInfo info : deviceInfoList) {
            if (info.getIpAddress().equals(deviceInfo.getIpAddress())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        /*Suma : Adding here just to be not dependant on irrespective of device discovered or no*/
        /* Suma : Below method : Dynamically fetching the product id from asset folder*/
        /*Start of code*/
        InputStream input = null;
        try {
            input = getContext().getAssets().open("symmentrickey_of_productid.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //symmentrickey_of_productid.txt can't be more than 2 gigs.
        int size = 0;
        try {
            if (input != null) {
                size = input.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[size];
        try {
            if (input != null) {
                input.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // byte buffer into a string
        String text = new String(buffer);
        text = text.replaceAll("\n", "");
        LibreLogger.d(this, "msg digest symmentric  standard product ID mavidapplication" + text);
        /*End of code*/


//        MessageDigest md = null;
//        try {
//            md = MessageDigest.getInstance("MD5");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        byte[] hashInBytes = new byte[0];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            if (md != null) {
//                hashInBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
//            }
//        }
//        // bytes to hex
//        StringBuilder sb = new StringBuilder();
//        for (byte b : hashInBytes) {
//            sb.append(String.format("%02x", b));
//        }
//        LibreMavidHelper.symmentricKey=sb.toString();
//        LibreLogger.d(this,"suma in msg digest symmentric mavidapplication\n"+sb.toString());
//        LibreLogger.d(this,"suma in libre mavid helper\n"+LibreMavidHelper.symmentricKey);
///*End of the code*/

        try {
            LibreMavidHelper.advertise();
            LibreLogger.d(this, "suma in getting discovery msearch MyActivity BaseActivity onrefresh discovery devicelist4");

        } catch (WrongStepCallException e) {
            e.printStackTrace();
        }
    }

    public void removeDevicesFromDeviceList(DeviceInfo deviceInfo) {
        if (deviceInfoList != null && deviceInfoList.contains(deviceInfo)) {
            deviceInfoList.remove(deviceInfo);
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }


    private void handleAlexaRefreshTokenStatus(String current_ipaddress, String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            /*not logged in*/
            Intent i = new Intent(getContext(), AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("speakerIpaddress", current_ipaddress);
            i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(i);

        } else {
            Intent newIntent = new Intent(getContext(), AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("speakerIpaddress", current_ipaddress);
            newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
            startActivity(newIntent);
        }
    }

    private boolean isDiscoveredAlreadySwipeLog(DeviceInfo deviceInfo) {
        deviceInfoList.contains(deviceInfo);
        for (DeviceInfo info : deviceInfoList) {
            if (info.getIpAddress().equals(deviceInfo.getIpAddress())) {
                return true;
            }
        }
        return false;
    }

    private Handler askRefreshTokenForEvery5SecHandler = new Handler(Looper.getMainLooper());
    private Runnable askRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
            Log.v("RecievedState", "Handler Running...  Runnable...Ask Refresh Token>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //askRefreshToken(myDeviceIp);
            isAVSState2InProgress = false;
            if (avsStatetwoCount < 18)
                askRefreshTokenForEvery5SecHandler.postDelayed(this, 5000);
        }
    };

    private Handler askRefreshTokenForEvery5SecHandlerState0 = new Handler(Looper.getMainLooper());
    private Runnable askRefreshRunnableState0 = new Runnable() {
        @Override
        public void run() {
            askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
            Log.v("RecievedState", "Handler Running...  Runnable...Ask Refresh Token STATE 0>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
           // askRefreshToken(myDeviceIp);
            isAVSState0InProgress = false;
            if (avsStatezeroCount < 10)
                askRefreshTokenForEvery5SecHandlerState0.postDelayed(this, 5000);
        }
    };

    private void askRefreshToken(final String i) {

        LibreMavidHelper.askRefreshToken(i, new CommandStatusListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                String messages = messageInfo.getMessage();
                Log.d("DeviceManager", " AVS STATE got alexa token After Sac Device list fragment" + messages);
                messages = messages.substring(messages.length() - 1);
                handleAlexaLoginTokenStatus(i, messages);

            }

            @Override
            public void failure(Exception e) {
                Log.d("DeviceManager", " AVS STATE got alexa token After Sac Device list fragment failure");
                closeLoader();
               getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alert == null) {

                            alert = new Dialog(getContext());

                            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

                            alert.setContentView(R.layout.custom_single_button_layout);

                            alert.setCancelable(false);

                            tv_alert_title = alert.findViewById(R.id.tv_alert_title);

                            tv_alert_message = alert.findViewById(R.id.tv_alert_message);

                            btn_ok = alert.findViewById(R.id.btn_ok);
                        }

                        tv_alert_title.setText("Failure");

                        tv_alert_message.setText("Failed to get AVS STATE response from Device.Try Again Later!!.");

                        btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alert.dismiss();
                                //StopSAC
                                alert = null;

                            }
                        });

                        alert.show();
                    }
                });

            }


            @Override
            public void success() {

            }
        });

    }

    public UpdatedMavidNodes getMavidNodesUpdate(String ipAddress) {
        if (updateHash.getUpdateNode(ipAddress) != null) {
            LibreLogger.d(this, "updating old node" + updateNode.getFriendlyname());
            return updateHash.getUpdateNode(ipAddress);
        }
        LibreLogger.d(this, "creating new node");
        return new UpdatedMavidNodes();
    }

    private synchronized void handleAlexaLoginTokenStatus(String current_ipadress, String AlexaLoginStatus) {
        try {
            int messageVal = Integer.parseInt(AlexaLoginStatus);
            if (messageVal > 3)
                AlexaLoginStatus = "3";
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.v("RecievedState", "onHandle Alexa Login Status devicelist...>>>>>>>>>>>>>>>>333333" + AlexaLoginStatus);


        switch (AlexaLoginStatus) {
            case "0":
                Log.v("RecievedState", "state is 0");
                // showLoader("Please Wait!!","Fetching Alexa Login Status");

                if (!isAVSState0InProgress) {
                    isAVSState0InProgress = true;
                    avsStatezeroCount = avsStatezeroCount + 1;
                    Log.v("RecievedState", "AVS STATE 0  devicelist Count IS AVS_STATE0...." + avsStatezeroCount);

                    if (avsStatezeroCount < 16) {
                        Log.v("RecievedState", "AVS STATE 0 devicelist COUNT LESS THAN 6..................." + avsStatezeroCount);
                        askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                        askRefreshTokenForEvery5SecHandlerState0.postDelayed(askRefreshRunnableState0, 5000);

                    } else {
                        showLoader("Alert", "Unable To Connect to Speaker");
                        Log.v("RecievedState", "AVS STATE 0 devicelist COUNT LESS THAN 6 ELSE ..................." + avsStatezeroCount);
                        Log.v("RecievedState", "state is 0>>>>devicelist count greater than 6...stopping ask refresh token");
                        askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Fetching Alexa Login State from Device failed.Please try again later!!....", Toast.LENGTH_SHORT).show();
                                Intent ssid = new Intent(getActivity(), MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(ssid);
                                Log.v("RecievedState", "AVS STATE 0 COUNT isFinishing onBTN click ..................." + avsStatetwoCount);
                                // finish();
                            }
                        });


                    }

                } else {
                    Log.v("RecievedState", "AVS STATE 0 devicelist IN PROGRESSSSSSS ELSE");

                }
                break;

            case "1":
                closeLoader();
                askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                Log.v("RecievedState", "state is devicelist 1");
                Intent newIntent = new Intent(getContext(), AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.putExtra("speakerIpaddress", current_ipadress);
                newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
                startActivity(newIntent);
                break;

            case "2":
                // Log.v("RecievedState", "on Response...state is>>>>>>>>>>>>>>>>>>>>>>>>444444" + isAVSState2InProgress);

                if (!isAVSState2InProgress) {
                    isAVSState2InProgress = true;
                    avsStatetwoCount = avsStatetwoCount + 1;
                    Log.v("RecievedState", "AVS STATE 2 devicelist Count IS AVS_STATE2...." + avsStatetwoCount);

                    if (avsStatetwoCount < 18) {
                        Log.v("RecievedState", "AVS STATE 2 devicelist COUNT LESS THAN 6..................." + avsStatetwoCount);
                        askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
                        askRefreshTokenForEvery5SecHandler.postDelayed(askRefreshRunnable, 5000);

                    } else {
                        showLoader("Alert", "Unable To Connect to Speaker");
                        Log.v("RecievedState", "AVS STATE 2 devicelist COUNT LESS THAN 6 ELSE ..................." + avsStatetwoCount);
                        Log.v("RecievedState", "state is 2>>>> count greater than 6...stopping ask refresh token");
                        askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Fetching Alexa Login State from Device failed.Please try again later!!....", Toast.LENGTH_SHORT).show();
                                //ShowAlertDynamicallyGoingToHomeScreen("ALert","Unable to Connect to Speaker..",MavidBLEConfigureSpeakerActivity.this);
                                //showTimeOutDailog();
                                Intent ssid = new Intent(getContext(),
                                        MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(ssid);
                                Log.v("RecievedState", "AVS STATE 2 devicelist COUNT isFinishing onBTN click ..................." + avsStatetwoCount);

                            }
                        });


                    }

                } else {
                    Log.v("RecievedState", "AVS STATE 2 IN PROGRESSSSSSS ELSE");

                }

                break;

            case "3":
                closeLoader();
                Intent i = new Intent(getContext(), AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("speakerIpaddress", current_ipadress);
                i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
                startActivity(i);
                askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
                Log.v("RecievedState", "AVS STATE 3 in DEVICELIST");

                break;

            case "-1":
                buildSnackBar("Device Unable to Login");
                // intentToHome();
                Log.v("RecievedState", "state is -1");
                break;


        }


    }


    /**
     * This device api to check the uuid
     */
    private void CallLDAPI5ToCheckIfTheStatusOfUUid(String ipAddress, String payload, final int position) {

        LibreMavidHelper.sendCustomCommands(ipAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST, payload, new CommandStatusListenerWithResponse() {
                    @Override
                    public void response(MessageInfo messageInfo) {
                        JSONObject dataJsonObject = null;
                        try {
                            dataJsonObject = new JSONObject(messageInfo.getMessage());
                            Log.d(TAG, "ldapi#5_response" + (dataJsonObject).toString());

                            int statusCode = dataJsonObject.getInt("Status");

                            JSONObject payloadObject = dataJsonObject.getJSONObject("payload");


                            switch (statusCode) {
                                case 3:
                                    //sucess // no_error //ack

                                    int UuidStatus = payloadObject.getInt("UuidStatus");

                                    JSONArray applianceJsonArray = null;


                                    if (payloadObject.has("ApplianceInfo")) {
                                        applianceJsonArray = payloadObject.getJSONArray("ApplianceInfo");
                                    }

                                    UUIDStatusChecking(UuidStatus, applianceJsonArray, deviceInfoList.get(position));
                                    break;


                                case -3:
                                case -2:
                                case -1:
                                case 0:
                                    //error
                                    uiRelatedClass.buidCustomSnackBarWithButton(getActivity(),
                                            "There seems to be an error,Please try after sometime", "OK", (AppCompatActivity) getActivity());
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "json_error_lsdapi5" + e.getMessage());
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                        closeLoader();
                        Log.d(TAG, "ldapi_response_exception" + e.toString());
                    }

                    @Override
                    public void success() {

                    }
                });

    }


    private void UUIDStatusChecking(int UuidStatus, JSONArray applianceJsonArray, DeviceInfo deviceInfo) {
        switch (UuidStatus) {
            case 1:
                /**UUID empty (status code was not present,
                 * device updated the UUID what App provided)
                 * Factory reset case,
                 * If "Yes",
                 * Call LDAPI#1,
                 * update the device the info one by on
                 * or delete from cloud if the user says
                 * "No"
                 * */
                getUserApplianceInfoDetails(getActivity()
                        .getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", ""), deviceInfo, new OnGetUserApplianceUserInfoInterface() {
                    @Override
                    public void onApiResponseCallback(@NotNull DeviceInfo deviceInfo, @NotNull String bodyObject) {
                        //UUID is empty
                        closeLoader();
                        //data is present
                        gotoRestoreSelectionActivity(deviceInfo, bodyObject);
                    }
                });
                break;

            case 2:
                /** Different status code
                 *  present—show the error message
                 *  show a dialog
                 *  */
                closeLoader();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uiRelatedClass.showCustomDialogForUUIDMismatch((AppCompatActivity) getActivity(), new OnButtonClickCallback() {
                            @Override
                            public void onClick(boolean isSucess) {
                                Log.d(TAG, "show the error and restrict user.");
                            }
                        });
                    }
                });
                break;

            case 3:
                /**
                 * UUID Matches.
                 * Go ahead and read the payload.
                 * Read the appliance info and check
                 * if the appliances match with the one
                 * present in the app
                 * */

                checkIfThereIsApplianceMismatchWithDevice(applianceJsonArray, deviceInfo);


                break;
        }
    }


    private HashMap<ModelRemoteDetails, String> addAppAppliancesDataToList(String macId) {

//        List<ModelRemoteDetails> appliancesList = new ArrayList<>();
        HashMap<ModelRemoteDetails, String> appliancesListHashmap = new HashMap<>();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String modelRemoteDetailsString = sharedPreferences.getString("applianceInfoList", "");

        if (!modelRemoteDetailsString.isEmpty()) {

            modelRemoteSubAndMacDetils = new ModelRemoteSubAndMacDetils();

            modelRemoteSubAndMacDetils = gson.fromJson(modelRemoteDetailsString, ModelRemoteSubAndMacDetils.class);

            if (modelRemoteSubAndMacDetils.getMac().equals(macId)) {
                if (modelRemoteSubAndMacDetils.getModelRemoteDetailsList() != null) {
                    if (modelRemoteSubAndMacDetils.getModelRemoteDetailsList().size() > 0) {
                        for (ModelRemoteDetails modelRemoteDetails : modelRemoteSubAndMacDetils.getModelRemoteDetailsList()) {

                            appliancesListHashmap.put(modelRemoteDetails, "1");

//                            appliancesList = modelRemoteSubAndMacDetils.getModelRemoteDetailsList();
                        }
                    }
                }
            }
        }

        return appliancesListHashmap;
    }

    private HashMap<ModelRemoteDetails, String> addDeviceApplianceInfoToAList(JSONArray deviceApplianceJsonArray) {
//        List<ModelRemoteDetails> mavidDeviceAppliancesList = new ArrayList();

        HashMap<ModelRemoteDetails, String> mavidDeviceAppliancesHashmap = new HashMap<>();

        for (int i = 0; i < deviceApplianceJsonArray.length(); i++) {
            ModelRemoteDetails modelRemoteDetails = new ModelRemoteDetails();

            try {
                JSONObject applianceJsonObject = (JSONObject) deviceApplianceJsonArray.get(i);

                modelRemoteDetails.setSelectedAppliance(String.valueOf(applianceJsonObject.getInt("appliance")));

                modelRemoteDetails.setSelectedBrandName(applianceJsonObject.getString("bName"));

                String brandName = modelRemoteDetails.getSelectedBrandName();

                modelRemoteDetails.setSelectedBrandName(brandName.replaceAll(",$", ""));


                modelRemoteDetails.setRemoteId(String.valueOf(applianceJsonObject.getInt("rId")));

                //modelRemoteDetails.setGroupId(applianceJsonObject.getInt("group"));

                modelRemoteDetails.setIndex(Integer.valueOf(applianceJsonObject.getString("index")));


                modelRemoteDetails.setGroupdName("Scene1");

                if (modelRemoteDetails.getSelectedAppliance().equals("1") || modelRemoteDetails.getSelectedAppliance().equals("TV")) {
                    modelRemoteDetails.setCustomName("TV");
                } else if (modelRemoteDetails.getSelectedAppliance().equals("2") || modelRemoteDetails.getSelectedAppliance().equals("TVP")) {
                    modelRemoteDetails.setCustomName("My Box");
                } else if (modelRemoteDetails.getSelectedAppliance().equals("3") || modelRemoteDetails.getSelectedAppliance().equals("AC")) {
                    modelRemoteDetails.setCustomName("AC");
                }

                modelRemoteDetails.setBrandId(String.valueOf(applianceJsonObject.getInt("bId")));

//                mavidDeviceAppliancesList.add(modelRemoteDetails);
                mavidDeviceAppliancesHashmap.put(modelRemoteDetails, "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mavidDeviceAppliancesHashmap;
    }


    private void deleteTvORTvpORACDetailsInSharedPref(String macId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String modelRemoteDetailsString = sharedPreferences.getString("applianceInfoList", "");

        if (!modelRemoteDetailsString.isEmpty()) {
            //if data is present
            modelRemoteSubAndMacDetils = new ModelRemoteSubAndMacDetils();

            modelRemoteSubAndMacDetils = gson.fromJson(modelRemoteDetailsString, ModelRemoteSubAndMacDetils.class);

            if (modelRemoteSubAndMacDetils.getMac().equals(macId)) {
                //removing all the appliances present for that mavid device
                Log.d(TAG, "deletedAllDataFromSharedPref");
                modelRemoteSubAndMacDetils.getModelRemoteDetailsList().removeAll(modelRemoteSubAndMacDetils.getModelRemoteDetailsList());
            }
        }
        //saving the details
        modelRemoteDetailsString = gson.toJson(modelRemoteSubAndMacDetils);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("applianceInfoList", modelRemoteDetailsString);
        editor.apply();
    }

    int nextAppliance = 0;

    private void checkIfThereIsApplianceMismatchWithDevice(final JSONArray deviceApplianceJsonArray, DeviceInfo deviceInfo) {

        //if the device data and  app data is not present
        ///TODO:call get api and  check if that is also empty
        //then goto the vp tabs
        if (deviceApplianceJsonArray.length() == 0 && checkIfAppDataIsEmpty(deviceInfo.getUSN())) {

            getUserApplianceInfoDetails(getActivity()
                    .getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", ""), deviceInfo, new OnGetUserApplianceUserInfoInterface() {
                @Override
                public void onApiResponseCallback(@NotNull DeviceInfo deviceInfo, @NotNull String bodyObject) {
                    //if there is data in the cloud
                    //goto to restore selection activity
                    gotoRestoreSelectionActivity(deviceInfo, bodyObject);
                }
            });
            Log.d(TAG, "bothDeviceAndAppDataIsEmpty");
        } else {

            HashMap<ModelRemoteDetails, String> deviceApplianceInfoHashMap = addDeviceApplianceInfoToAList(deviceApplianceJsonArray);

            HashMap<ModelRemoteDetails, String> appDeviceApplianceInfoHashMap = addAppAppliancesDataToList(deviceInfo.getUSN());

            //if the mavid device and app data list size are smae
            if (deviceApplianceInfoHashMap.size() == appDeviceApplianceInfoHashMap.size()) {
                //compare the data objects of mavidData and appData
                boolean isMatching = commpareMavidDeviceDataAndAppData(deviceApplianceInfoHashMap, appDeviceApplianceInfoHashMap);
                if (isMatching) {
                    //goto vp activity
                    gotoIRAddRemoteVPActivity(deviceInfo);
                } else {
                    //if it is not matching
                    //call the get api and update the data of the app
                    getUserApplianceInfoDetails(getActivity()
                                    .getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", ""),
                            deviceInfo, new OnGetUserApplianceUserInfoInterface() {
                                @Override
                                public void onApiResponseCallback(@NotNull final DeviceInfo deviceInfo, @NotNull String bodyObject) {
                                    try {
                                        JSONObject bodyJsonObject = new JSONObject(bodyObject);

                                        JSONObject applianceJsonObject = bodyJsonObject.optJSONObject("Appliance");
                                        //deleteAll the data present in the app
                                        deleteTvORTvpORACDetailsInSharedPref(deviceInfo.getUSN());

                                        if (applianceJsonObject != null) {
                                            ModelRemoteDetails modelRemoteDetails = parseApplianceJsonObject((JSONObject) applianceJsonObject);
                                            //call ldapi#2
                                            timerTaskToGreyOutRemoteButtons(deviceInfo.getIpAddress(),
                                                    modelRemoteDetails, deviceInfo.getUSN(), new OnDeviceApiSucessCallback() {
                                                        @Override
                                                        public void onSucessCallback() {
                                                            gotoIRAddRemoteVPActivity(deviceInfo);
                                                        }
                                                    });
                                        } else {
                                            final JSONArray applianceJsonArray = bodyJsonObject.optJSONArray("Appliance");

                                            /**
                                             * 1)remove the old data present in the app
                                             * 2)updating the tv or tvp details
                                             * */

                                            nextAppliance = 0;

                                            List<ModelRemoteDetails> modelRemoteDetailsList = new ArrayList<>();

                                            for (int i = 0; i < applianceJsonArray.length(); i++) {
                                                final ModelRemoteDetails modelRemoteDetails = parseApplianceJsonObject((JSONObject) applianceJsonArray.get(i));
                                                modelRemoteDetailsList.add(modelRemoteDetails);
                                            }

                                            callLdapi2ForTVOrTVPorAC(modelRemoteDetailsList, deviceInfo);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                }
            }
            /**
             * 1) device factory reset has happened
             * or
             * 2) user has unintsalled the app
             * or
             * 3) another user has installed the same app
             *    and moddified the contents of the device
             *
             * */
            else if (deviceApplianceInfoHashMap.size() == 0 && appDeviceApplianceInfoHashMap.size() > 0) {
                //device reset or user has modfied the data in another phone
                getUserApplianceInfoDetails(getActivity()
                        .getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", ""), deviceInfo, new OnGetUserApplianceUserInfoInterface() {
                    @Override
                    public void onApiResponseCallback(@NotNull DeviceInfo deviceInfo, @NotNull String bodyObject) {
                        //there is data in the cloud
                        //but no data in mavid
                        //and there is data in app
                        //goto restore selectionActivity
                        gotoRestoreSelectionActivity(deviceInfo, bodyObject);
                    }
                });
            } else {
                //app has no data and mavid device has data
                //it is the case  of appUninstall or
                //modified app data in another app

                getUserApplianceInfoDetails(getActivity()
                        .getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", ""), deviceInfo, new OnGetUserApplianceUserInfoInterface() {
                    @Override
                    public void onApiResponseCallback(@NotNull final DeviceInfo deviceInfo, @NotNull String bodyObject) {
                        try {
                            JSONObject bodyJsonObject = new JSONObject(bodyObject);

                            JSONObject applianceJsonObject = bodyJsonObject.optJSONObject("Appliance");

                            deleteTvORTvpORACDetailsInSharedPref(deviceInfo.getUSN());

                            if (applianceJsonObject != null) {
                                ModelRemoteDetails modelRemoteDetails = parseApplianceJsonObject((JSONObject) applianceJsonObject);

                                //call ldapi#2
                                timerTaskToGreyOutRemoteButtons(deviceInfo.getIpAddress(),
                                        modelRemoteDetails, deviceInfo.getUSN(), new OnDeviceApiSucessCallback() {
                                            @Override
                                            public void onSucessCallback() {
                                                gotoIRAddRemoteVPActivity(deviceInfo);
                                            }
                                        });

                            } else {
                                final JSONArray applianceJsonArray = bodyJsonObject.optJSONArray("Appliance");

                                /**
                                 * 1)remove the old data present in the app
                                 * 2)updating the tv or tvp details
                                 * */

                                nextAppliance = 0;

                                List<ModelRemoteDetails> modelRemoteDetailsList = new ArrayList<>();

                                for (int i = 0; i < applianceJsonArray.length(); i++) {
                                    final ModelRemoteDetails modelRemoteDetails = parseApplianceJsonObject((JSONObject) applianceJsonArray.get(i));
                                    modelRemoteDetailsList.add(modelRemoteDetails);
                                }

                                callLdapi2ForTVOrTVPorAC(modelRemoteDetailsList, deviceInfo);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    public void callLdapi2ForTVOrTVPorAC(final List<ModelRemoteDetails> modelRemoteDetailsList,
                                         final DeviceInfo deviceInfo) {

        irButtonListTimerTask = new Timer();

        timerTaskToGreyOutRemoteButtons(deviceInfo.getIpAddress(),
                modelRemoteDetailsList.get(nextAppliance), deviceInfo.getUSN(), new OnDeviceApiSucessCallback() {
                    @Override
                    public void onSucessCallback() {
                        nextAppliance++;
                        if (modelRemoteDetailsList.size() == nextAppliance) {
                            //we have reached the end of the list
                            //goto the next actvvity
                            gotoIRAddRemoteVPActivity(deviceInfo);
                        } else {
                            callLdapi2ForTVOrTVPorAC(modelRemoteDetailsList, deviceInfo);
                        }
                    }
                });

    }

    private boolean commpareMavidDeviceDataAndAppData(HashMap<ModelRemoteDetails,
            String> mavidApplianceInfoHashMapList, HashMap<ModelRemoteDetails, String> appApplianceInfoHashmapList) {

        Log.d(TAG, "compareAppliances" + mavidApplianceInfoHashMapList.equals(appApplianceInfoHashmapList));

        if (mavidApplianceInfoHashMapList.equals(appApplianceInfoHashmapList)) {
            return true;
        }

        return false;
    }

    private boolean checkIfAppDataIsEmpty(String macId) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String modelRemoteDetailsString = sharedPreferences.getString("applianceInfoList", "");

        if (!modelRemoteDetailsString.isEmpty()) {

            modelRemoteSubAndMacDetils = new ModelRemoteSubAndMacDetils();


            modelRemoteSubAndMacDetils = gson.fromJson(modelRemoteDetailsString, ModelRemoteSubAndMacDetils.class);

            if (modelRemoteSubAndMacDetils.getMac().equals(macId)) {
                if (modelRemoteSubAndMacDetils.getModelRemoteDetailsList() != null) {
                    if (modelRemoteSubAndMacDetils.getModelRemoteDetailsList().size() > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private ModelRemoteDetails parseApplianceJsonObject(JSONObject applianceObject) {

        ModelRemoteDetails modelRemoteDetails = new ModelRemoteDetails();

        try {
            modelRemoteDetails.setSelectedBrandName(applianceObject.getString("BrandName"));
            modelRemoteDetails.setRemoteId(applianceObject.getString("RemoteID"));
            modelRemoteDetails.setBrandId(applianceObject.getString("BrandID"));
            if (applianceObject.get("Appliance").equals("TV")) {
                modelRemoteDetails.setSelectedAppliance("1");
            } else if (applianceObject.get("Appliance").equals("TVP")) {
                modelRemoteDetails.setSelectedAppliance("2");
            } else if (applianceObject.get("Appliance").equals("AC")) {
                modelRemoteDetails.setSelectedAppliance("3");
            }
            modelRemoteDetails.setIndex(Integer.parseInt(applianceObject.getString("index")));
            modelRemoteDetails.setCustomName(applianceObject.getString("CustomName"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return modelRemoteDetails;
    }


    private void gotoRestoreSelectionActivity(DeviceInfo deviceInfo, String applianceInfo) {
        closeLoader();
        Intent intent = new Intent(getActivity(), IRRestoreSelectionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("deviceInfo", deviceInfo);
        bundle.putString("applianceInfo", applianceInfo);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void getUserApplianceInfoDetails(String sub, final DeviceInfo deviceInfo, final OnGetUserApplianceUserInfoInterface onGetUserApplianceUserInfoInterface) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        String baseUrl = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/usermangement?" + ("sub=") + (sub) + "&Mac=" + deviceInfo.getUSN();


        Log.d(TAG, "requestedURl: " + baseUrl);

        StringRequest getUserMgtDetailsRequest = new StringRequest(Request.Method.GET, baseUrl, response -> {
            Log.d(TAG, "getUserManagementDetails: response" + (response));

            try {
                JSONObject responseObject = new JSONObject(response);

                JSONObject bodyJsonObject = responseObject.optJSONObject("body");

                if (bodyJsonObject != null) {
                    //body key value is a json object
                    JSONArray applianceJsonArray = bodyJsonObject.optJSONArray("Appliance");
                    if (applianceJsonArray != null) {
                        if (applianceJsonArray.length() > 0) {
                            onGetUserApplianceUserInfoInterface.onApiResponseCallback(deviceInfo, String.valueOf(bodyJsonObject));
                        } else {
                            closeLoader();
                            //if cloud data is also empty—first time user—Go ahead with normal flowr

                            //deleteAll the data present in the app if cloud has no data
                            deleteTvORTvpORACDetailsInSharedPref(deviceInfo.getUSN());

                            gotoIRAddRemoteVPActivity(deviceInfo);
                        }
                    } else {
                        //appliance key might be json obejct
                        JSONObject applianceJsonObject = bodyJsonObject.optJSONObject("Appliance");
                        if (applianceJsonObject != null) {
                            onGetUserApplianceUserInfoInterface.onApiResponseCallback(deviceInfo, String.valueOf(bodyJsonObject));
                        }
                    }
                } else {
                    //body key value is a json array
                    JSONArray bodyJsonArray = responseObject.optJSONArray("body");

                    if (bodyJsonArray != null) {

                        if (bodyJsonArray.length() > 0) {

                            bodyJsonObject = bodyJsonArray.getJSONObject(0);

                            JSONArray applianceJsonArray = bodyJsonObject.optJSONArray("Appliance");
                            if (applianceJsonArray != null) {
                                if (applianceJsonArray.length() > 0) {

                                    onGetUserApplianceUserInfoInterface.onApiResponseCallback(deviceInfo, String.valueOf(bodyJsonObject));

                                } else {
                                    closeLoader();
                                    //if cloud data is also empty—first time user—Go ahead with normal flowr

                                    //deleteAll the data present in the app if cloud has no data
                                    deleteTvORTvpORACDetailsInSharedPref(deviceInfo.getUSN());

                                    gotoIRAddRemoteVPActivity(deviceInfo);
                                }
                            }
                        } else {
                            closeLoader();
                            //if cloud data is also empty—first time user—Go ahead with normal flowr

                            //deleteAll the data present in the app if cloud has no data
                            deleteTvORTvpORACDetailsInSharedPref(deviceInfo.getUSN());

                            gotoIRAddRemoteVPActivity(deviceInfo);
                        }
                    } else {
                        //body key is a string
                        closeLoader();
                        //new user or sub has been deleted form cloud
                        gotoIRAddRemoteVPActivity(deviceInfo);
                    }


                }

            } catch (
                    JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(getActivity(),
                            getActivity().getWindow().getDecorView().findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime");

                } else if (volleyError instanceof AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(getActivity(),
                            getActivity().getWindow().getDecorView().findViewById(android.R.id.content), "AuthFailure error occurred, please try again later");


                } else if (volleyError instanceof ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(getActivity(),
                                getActivity().getWindow().getDecorView().findViewById(android.R.id.content), "Server error occurred, please try again later");
                    }

                } else if (volleyError instanceof NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(getActivity(),
                            getActivity().getWindow().getDecorView().findViewById(android.R.id.content), "Network error occurred, please try again later");

                } else if (volleyError instanceof ParseError) {
                    uiRelatedClass.buildSnackBarWithoutButton(getActivity(),
                            getActivity().getWindow().getDecorView().findViewById(android.R.id.content), "Parser error occurred, please try again later");
                }

            }
        });


        getUserMgtDetailsRequest.setRetryPolicy(new

                DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(getUserMgtDetailsRequest);


    }


    public JSONObject buildPayloadForLdapi2AcORTVoRTVP(int index, int remoteId, String selectedAppliance) {
        JSONObject payloadJsonObject = new JSONObject();

        /** <ID>: 1 : TV
         2 : STB
         3 : AC*/

        try {
            payloadJsonObject.put("ID", 2);
            JSONObject dataJsonObject = new JSONObject();

            if (selectedAppliance.equals("1") || selectedAppliance.equals("TV")) {
                dataJsonObject.put("appliance", 1);
            } else if (selectedAppliance.equals("2") || selectedAppliance.equals("TVP")) {
                dataJsonObject.put("appliance", 2);
            } else if (selectedAppliance.equals("3") || selectedAppliance.equals("Ac")) {
                dataJsonObject.put("appliance", 3);//tv//tvp//ac
            }
            dataJsonObject.put("index", index);
            dataJsonObject.put("rId", remoteId);

            payloadJsonObject.put("data", dataJsonObject);

            Log.d(TAG, "ldapi#2_payload" + (payloadJsonObject.toString()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return payloadJsonObject;
    }


    public void updateWorkingRemoteButtons(String applianceType, List<ModelLdapi2AcModes> modelLdapi2AcModesList) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String workingRemoteButtonsString = gson.toJson(workingRemoteButtonsHashMap);

        if (applianceType.equals("1") || applianceType.equals("TV")) {
            editor.putString("workingTVRemoteButtons", workingRemoteButtonsString);
        } else if (applianceType.equals("2") || applianceType.equals("TVP")) {
            editor.putString("workingTVPRemoteButtons", workingRemoteButtonsString);
        } else {
            String workingRemoteButtonsACString = gson.toJson(modelLdapi2AcModesList);
            editor.putString("workingACRemoteButtons", workingRemoteButtonsACString);
        }

        editor.apply();


    }

    public void timerTaskToGreyOutRemoteButtons(final String ipdAddress,
                                                final ModelRemoteDetails modelRemoteDetails,
                                                final String macId, final OnDeviceApiSucessCallback deviceApiSucessCallback) {

        myHandler.sendEmptyMessageDelayed(LDAPI2_TIMOUT, 25000);

        irButtonListTimerTask.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "calling_Ldapi#2_every_5_secs");

                if (modelRemoteDetails.getSelectedAppliance().equals("1") || modelRemoteDetails.getSelectedAppliance().equals("TV")
                        || modelRemoteDetails.getSelectedAppliance().equals("2") || modelRemoteDetails.getSelectedAppliance().equals("TVP")) {

                    getButtonPayloadForTVAndTVP(ipdAddress, macId, modelRemoteDetails, deviceApiSucessCallback);

                } else {
                    getDeviceLdapi2WorkingButtonListForAc(ipdAddress, macId,
                            modelRemoteDetails, deviceApiSucessCallback);
                }
            }
        }, 0, 5000);
    }

    List<ModelLdapi2AcModes> modelLdapi2AcModesList = new ArrayList();

    public void getButtonPayloadForTVAndTVP(String ipdAddress,
                                            final String macId,
                                            final ModelRemoteDetails modelRemoteDetails,
                                            final OnDeviceApiSucessCallback deviceApiSucessCallback) {
        LibreMavidHelper.sendCustomCommands(ipdAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForLdapi2AcORTVoRTVP(modelRemoteDetails.getIndex(), Integer.parseInt(modelRemoteDetails.getRemoteId()), modelRemoteDetails.getSelectedAppliance()).toString(),
                new CommandStatusListenerWithResponse() {
                    @Override
                    public void response(MessageInfo messageInfo) {
                        Log.d(TAG, "ldapi#2_response: " + (messageInfo.getMessage()));

                        JSONObject responseJSONObject = null;
                        try {
                            responseJSONObject = new JSONObject(messageInfo.getMessage());
                            int statusCode = responseJSONObject.getInt("Status");

                            switch (statusCode) {

                                case 3:

                                    myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT);
                                    myHandler.removeCallbacksAndMessages(null);

                                    irButtonListTimerTask.cancel();

                                    /** get the button list from the data json object  */
                                    JSONObject payloadJsonObject = responseJSONObject.getJSONObject("payload");
                                    JSONArray buttonJsonArray = payloadJsonObject.getJSONArray("keys");

                                    Log.d(TAG, "buttonList: $buttonJsonArray");

                                    workingRemoteButtonsHashMap = new HashMap();

                                    int i = 0;

                                    while (i < buttonJsonArray.length()) {
                                        String buttonNameString = buttonJsonArray.getString(i);

                                        workingRemoteButtonsHashMap.put(buttonNameString, "1");

                                        i++;
                                    }

                                    updateApplianceInfoInSharedPref(modelRemoteDetails, macId);


                                    removeWorkingTvAndTvpORACButtons(modelRemoteDetails.getSelectedAppliance());

                                    updateWorkingRemoteButtons(modelRemoteDetails.getSelectedAppliance(),
                                            null);

                                    deviceApiSucessCallback.onSucessCallback();

                                    break;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            irButtonListTimerTask.cancel();

                            myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT);

                            closeLoader();

                            Log.d(TAG, "exeception:" + (e.toString()));
                            uiRelatedClass.buidCustomSnackBarWithButton(getActivity(),
                                    "There sees to be an error!!.Please try after some time",
                                    "Go Back", (AppCompatActivity) getActivity());
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                        irButtonListTimerTask.cancel();

                        myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT);

                        closeLoader();

                        Log.d(TAG, "exeception:" + (e.toString()));
                        uiRelatedClass.buidCustomSnackBarWithButton(getActivity(),
                                "There sees to be an error!!.Please try after some time",
                                "Go Back", (AppCompatActivity) getActivity());
                    }

                    @Override
                    public void success() {

                    }
                });
    }


    public void getDeviceLdapi2WorkingButtonListForAc(String ipdAddress,
                                                      final String macId,
                                                      final ModelRemoteDetails modelRemoteDetails,
                                                      final OnDeviceApiSucessCallback deviceApiSucessCallback) {

        LibreMavidHelper.sendCustomCommands(ipdAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForLdapi2AcORTVoRTVP(modelRemoteDetails.getIndex(), Integer.parseInt(modelRemoteDetails.getRemoteId()), modelRemoteDetails.getSelectedAppliance()).toString(), new CommandStatusListenerWithResponse() {
                    @Override
                    public void response(MessageInfo messageInfo) {
                        Log.d(TAG, "ldapi#2_response: " + (messageInfo.getMessage()));

                        JSONObject responseJSONObject = null;
                        try {
                            responseJSONObject = new JSONObject(messageInfo.getMessage());
                            int statusCode = responseJSONObject.getInt("Status");

                            switch (statusCode) {

                                case 2:

                                    myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT);
                                    myHandler.removeCallbacksAndMessages(null);

                                    irButtonListTimerTask.cancel();

                                    JSONObject payloadJsonObject = null;
                                    try {
                                        payloadJsonObject = responseJSONObject.getJSONObject("payload");

                                        JSONArray modesJsonArray = payloadJsonObject.getJSONArray("modes");

                                        Log.d(TAG, "modes" + (modesJsonArray.toString()));

                                        modelLdapi2AcModesList = new ArrayList();


                                        for (int i = 0; i < modesJsonArray.length(); i++) {
                                            modelLdapi2AcModesList.add(parseLdapi2Response((JSONObject) modesJsonArray.get(i)));
                                        }

                                        updateApplianceInfoInSharedPref(modelRemoteDetails, macId);

                                    } catch (JSONException jsonException) {
                                        jsonException.printStackTrace();
                                    }

                                    removeWorkingTvAndTvpORACButtons(modelRemoteDetails.getSelectedAppliance());


                                    updateWorkingRemoteButtons("3", modelLdapi2AcModesList);

                                    deviceApiSucessCallback.onSucessCallback();


                                    break;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            irButtonListTimerTask.cancel();

                            myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT);

                            closeLoader();

                            Log.d(TAG, "exeception:" + (e.toString()));
                            uiRelatedClass.buidCustomSnackBarWithButton(getActivity(),
                                    "There sees to be an error!!.Please try after some time",
                                    "Go Back", (AppCompatActivity) getActivity());
                        }


                    }

                    @Override
                    public void failure(Exception e) {
                        irButtonListTimerTask.cancel();

                        myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT);

                        closeLoader();

                        Log.d(TAG, "exeception:" + (e.toString()));
                        uiRelatedClass.buidCustomSnackBarWithButton(getActivity(),
                                "There sees to be an error!!.Please try after some time",
                                "Go Back", (AppCompatActivity) getActivity());
                    }

                    @Override
                    public void success() {

                    }
                });
    }


    public ModelLdapi2AcModes parseLdapi2Response(JSONObject jsonObject) {

        ModelLdapi2AcModes modelLdapi2AcModes = new ModelLdapi2AcModes();

        try {
            modelLdapi2AcModes.setMode(jsonObject.getString("mode"));

            modelLdapi2AcModes.setDefault(jsonObject.getBoolean("is_default"));
            modelLdapi2AcModes.setTempAllowed(jsonObject.getBoolean("temp_allowed"));


            modelLdapi2AcModes.setMinTemp(jsonObject.getInt("min_temp"));

            modelLdapi2AcModes.setMaxTemp(jsonObject.getInt("max_temp"));


            modelLdapi2AcModes.setSpeedAllowed(jsonObject.getBoolean("speed_allowed"));

            modelLdapi2AcModes.setDirectionAllowed(jsonObject.getBoolean("direction_allowed"));


            modelLdapi2AcModes.setSwingAllowed(jsonObject.getBoolean("swing_allowed"));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return modelLdapi2AcModes;
    }

    private void updateApplianceInfoInSharedPref(ModelRemoteDetails modelRemoteDetails, String macId) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String modelRemoteDetailsString = sharedPreferences.getString("applianceInfoList", "");


        if (!modelRemoteDetailsString.isEmpty()) {

            modelRemoteSubAndMacDetils = new ModelRemoteSubAndMacDetils();


            modelRemoteSubAndMacDetils = gson.fromJson(modelRemoteDetailsString, ModelRemoteSubAndMacDetils.class);


            if (modelRemoteSubAndMacDetils.getMac().equals(macId)) {

                //update the appliance list  details in the list to the exsting device
                modelRemoteSubAndMacDetils.getModelRemoteDetailsList().add(buidlRemoteDetails(modelRemoteDetails));

                Log.d(TAG, "updatedApplianceList" + modelRemoteDetails.getSelectedBrandName());
            } else {
                //new device
                modelRemoteSubAndMacDetils.setSub(sharedPreferences.getString("sub", ""));

                modelRemoteSubAndMacDetils.setMac(macId);

                List<ModelRemoteDetails> appllianceInfoList = new ArrayList<ModelRemoteDetails>();

                appllianceInfoList.add(buidlRemoteDetails(modelRemoteDetails));

                modelRemoteSubAndMacDetils.setModelRemoteDetailsList(appllianceInfoList);
            }
        } else {
            //new user and first device
            modelRemoteSubAndMacDetils = new ModelRemoteSubAndMacDetils();

            modelRemoteSubAndMacDetils.setSub(sharedPreferences.getString("sub", ""));

            modelRemoteSubAndMacDetils.setMac(macId);

            List<ModelRemoteDetails> appllianceInfoList = new ArrayList<ModelRemoteDetails>();

            appllianceInfoList.add(buidlRemoteDetails(modelRemoteDetails));

            modelRemoteSubAndMacDetils.setModelRemoteDetailsList(appllianceInfoList);
        }


        modelRemoteDetailsString = gson.toJson(modelRemoteSubAndMacDetils);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("applianceInfoList", modelRemoteDetailsString);

        editor.apply();

    }


    public void removeWorkingTvAndTvpORACButtons(String selectedAppliance) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.contains("workingTVRemoteButtons") && (selectedAppliance.equals("1") || selectedAppliance.equals("TV"))) {

            editor.remove("workingTVRemoteButtons");
        }

        if (sharedPreferences.contains("workingTVPRemoteButtons") && (selectedAppliance.equals("2") || selectedAppliance.equals("TVP"))) {
            editor.remove("workingTVPRemoteButtons");
        }

        if (sharedPreferences.contains("workingACRemoteButtons") && (selectedAppliance.equals("3") || selectedAppliance.equals("AC"))) {
            editor.remove("workingACRemoteButtons");
        }

        editor.apply();

    }

    private ModelRemoteDetails buidlRemoteDetails(ModelRemoteDetails modelRemoteDetails) {
        modelRemoteDetails.setSelectedAppliance(modelRemoteDetails.getSelectedAppliance());

        if (modelRemoteDetails.getSelectedAppliance() == "1" || modelRemoteDetails.getSelectedAppliance() == "TV") {
            // modelRemoteDetails.customName = "TV"//for now hardcoding the customa name
            modelRemoteDetails.setRemotesHashMap(workingRemoteButtonsHashMap);
        } else if (modelRemoteDetails.getSelectedAppliance() == "2" || modelRemoteDetails.getSelectedAppliance() == "TVP") {
            // modelRemoteDetails.customName = "My Box"//for now hardcoding the customa name
            modelRemoteDetails.setRemotesHashMap(workingRemoteButtonsHashMap);
        } else if (modelRemoteDetails.getSelectedAppliance() == "3" || modelRemoteDetails.getSelectedAppliance() == "AC") {
            // modelRemoteDetails.customName = "AC"
            modelRemoteDetails.setAc_remotelist(modelLdapi2AcModesList);
        }

        modelRemoteDetails.setCustomName(modelRemoteDetails.getCustomName());

        // modelRemoteDetails.setGroupId(1);

        modelRemoteDetails.setGroupdName("Scene1");

        modelRemoteDetails.setRemoteId(modelRemoteDetails.getRemoteId());

        modelRemoteDetails.setSelectedBrandName(modelRemoteDetails.getSelectedBrandName());

        modelRemoteDetails.setBrandId(modelRemoteDetails.getBrandId());

        return modelRemoteDetails;
    }


    private void gotoIRAddRemoteVPActivity(DeviceInfo deviceInfo) {
        closeLoader();
        Log.d(TAG, "gotoIRAddRemoteVPActivity");
        //Intent intent = new Intent(getActivity(), IRAddRemoteVPActivity.class);
        Intent intent = new Intent(getActivity(), IRAddOrSelectRemotesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("deviceInfo", deviceInfo);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void showLoader(final String title, final String message) {

        if (!(getActivity().isFinishing())) {

            if (mDialog == null) {
                mDialog = new Dialog(getActivity());
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mDialog.setContentView(R.layout.custom_progress_bar);

                mDialog.setCancelable(false);
                AppCompatTextView progress_title = mDialog.findViewById(R.id.progress_title);
                ProgressBar progress_bar = mDialog.findViewById(R.id.progress_bar);
                AppCompatTextView progress_message = mDialog.findViewById(R.id.progress_message);
                progress_title.setText(title);
                progress_message.setText(message);
                progress_bar.setIndeterminate(true);
                progress_bar.setVisibility(View.VISIBLE);
            }

            mDialog.show();
        }
    }

    public void closeLoader() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    if (!(getActivity().isFinishing())) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }
        });
    }

    private String buildPayloadForLdapi() {
        JSONObject payloadObject = new JSONObject();
        try {
            payloadObject.put("ID", 5);

            JSONObject dataObject = new JSONObject();

            dataObject.put("uuid", getActivity().getSharedPreferences("Mavid", Context.MODE_PRIVATE)
                    .getString("sub", ""));

            payloadObject.put("data", dataObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "ldapi5_payload" + payloadObject.toString());
        return payloadObject.toString();
    }

    @Override
    public void onClickListview(int position) {
        Log.d(TAG, "clicked_on_remoteIcon");
        showLoader("Please wait", "");

        CallLDAPI5ToCheckIfTheStatusOfUUid(deviceInfoList.get(position).getIpAddress(), buildPayloadForLdapi(), position);
    }
}


//    public void disconnect() {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w("Disconnect", "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.disconnect();
