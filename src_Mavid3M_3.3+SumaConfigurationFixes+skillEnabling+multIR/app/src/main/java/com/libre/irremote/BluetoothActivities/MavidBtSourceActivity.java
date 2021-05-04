package com.libre.irremote.BluetoothActivities;

import android.os.Bundle;
import android.widget.Button;

import com.libre.irremote.BaseActivity;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.R;
import com.libre.irremote.utility.DB.MavidNodes;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;

import org.json.JSONArray;

public class MavidBtSourceActivity extends BaseActivity {
    Button btSearchButton;
    DeviceInfo deviceInfo;
    String ipApaddress;
    JSONArray btScanListArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mavid_bt_source);
        ipApaddress=getIntent().getStringExtra(Constants.INTENTS.IP_ADDRESS);
        deviceInfo= MavidNodes.getInstance().getDeviceInfoFromDB(ipApaddress);
        if(deviceInfo==null)
            return;
        btSearchButton=(Button)findViewById(R.id.BtSearchBtn);
//        btSearchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LibreMavidHelper.sendCustomCommands(deviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.START_BT_SEARCH, "", new CommandStatusListenerWithResponse() {
//                    @Override
//                    public void response(final MessageInfo messageInfo) {
//                        LibreLogger.d(this,"suma in device setitng screen bluetooth info"+messageInfo.getMessage());
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                closeLoader();
//                                //populateBtDeviceList(messageInfo.getMessage());
////                                btDeviceList=getBtDeviceList();
//                                if (!messageInfo.getMessage().isEmpty()) {
//                                    JSONObject mainObj = null;
//                                    try {
//                                        mainObj = new JSONObject(messageInfo.getMessage());
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                    try {
//                                        btScanListArray = mainObj.getJSONArray("BLUETOOTH DEVICES");
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                                if(btScanListArray != null && btScanListArray.length() > 0 ) {
//                                Intent intent = new Intent(MavidBtSourceActivity.this, MavidBtDiscoverActivity.class);
//                                intent.putExtra(Constants.INTENTS.BT_SOURCE_SCANLIST, messageInfo.getMessage().toString());
//                                intent.putExtra(Constants.INTENTS.IP_ADDRESS, messageInfo.getIpAddressOfSender());
//                                startActivity(intent);
//                                }
//                                else{
//                                    LibreLogger.d(this,"no bt device"+messageInfo.getMessage());
//                                    //Toast.makeText(getApplicationContext(), "no bt device", Toast.LENGTH_SHORT).show();
//                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MavidBtSourceActivity.this);
//                                    builder.setMessage("There are no Bluetooth Source devices to manage")
//                                            .setCancelable(false)
//                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    //do things
//                                                    dialog.dismiss();
//                                                }
//                                            });
//                                    AlertDialog alert = builder.create();
//                                    alert.show();
//                                }
//
//                            }
//                                 });
//
//                    }
//
//                    @Override
//                    public void failure(Exception e) {
//                        showLoader("Try Again Later...","Action Failed");
//                        final Timer timer3 = new Timer();
//                        timer3.schedule(new TimerTask() {
//                            public void run() {
//                                closeLoader();
//                                Intent intent = new Intent(MavidBtSourceActivity.this, DeviceSettingsActivity.class);
//                                startActivity(intent);
//                                finish();
//                                timer3.cancel();
//                            }
//                        }, 3000);
//
//                    }
//
//                    @Override
//                    public void success() {
//
//                    }
//                });
////
//            }
//    });
        }
}
