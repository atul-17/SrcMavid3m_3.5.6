package com.libre.irremote.BluetoothActivities;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter;
import com.libre.irremote.R;
import com.libre.libresdk.Util.LibreLogger;
import com.libre.irremote.models.ModelSectionHeadersBt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BtSourceDeviceInfoAdapter extends
        SectionRecyclerViewAdapter<ModelSectionHeadersBt, MavidSourceDeviceInfo,
                BtSourceDeviceInfoAdapter.BtSectionHeaderViewHolder, BtSourceDeviceInfoAdapter.BluetoothDevicesViewHolder> {

    //    RecyclerView.Adapter<BtSourceDeviceInfoAdapter.BluetoothDevicesViewHolder>
    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private int listItemLayout;
    //    private ArrayList<String> itemList;
    private Context context;
    public String deviceUUid;
    BtSourceDeviceInfo btDeviceInfo;
    private final int GET_BTSCAN_LIST_HANDLER = 0x200;
    Map<String, String> btListMap = new TreeMap<>();
    private ArrayList<String> btDeviceList = new ArrayList<>();
    private ArrayList<String> deviceInfoList = new ArrayList<>();
    //private BtSourceDeviceInfo btdeviceInfo;
//    private ArrayList<MavidSourceDeviceInfo> voodbtlist;
    private Handler handler1;
//    ProgressDialog dialog;
//    BluetoothDevicesViewHolder holder;

    AlertDialog dialog;
    private AvaliablePairedDeviceInterface avaliablePairedDeviceInterface;
    private List<ModelSectionHeadersBt> modelSectionHeadersBTList;

    private AppCompatActivity appCompatActivity;

    private ShowCustomAlertDialogInterface showCustomAlertDialogInterface;


    public void setShowCustomAlertDialogInterface(ShowCustomAlertDialogInterface showCustomAlertDialogInterface) {
        this.showCustomAlertDialogInterface = showCustomAlertDialogInterface;
    }
    // Constructor of the class
//    public BtSourceDeviceInfoAdapter(Context context, int layoutId, BtSourceDeviceInfo btdeviceInfo, ArrayList<ModelSectionHeadersBt> modelSectionHeadersBtArrayList) {
//        listItemLayout = layoutId;
//        this.context = context;
//        this.btDeviceInfo = btdeviceInfo;
////        this.voodbtlist = voodbtlist;
//        this.modelSectionHeadersBtArrayList = modelSectionHeadersBtArrayList;
//    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case GET_BTSCAN_LIST_HANDLER:


                    break;
            }
            return true;
        }
    });

    public BtSourceDeviceInfoAdapter(Context context, List<ModelSectionHeadersBt> sectionItemList,
                                     AvaliablePairedDeviceInterface avaliablePairedDeviceInterface,
                                     BtSourceDeviceInfo deviceInfo, AppCompatActivity appCompatActivity) {
        super(context, sectionItemList);
        this.context = context;
        this.avaliablePairedDeviceInterface = avaliablePairedDeviceInterface;
        this.modelSectionHeadersBTList = sectionItemList;
        this.btDeviceInfo = deviceInfo;
        this.appCompatActivity = appCompatActivity;
    }

    // get the size of the list
//    @Override
//    public int getItemCount() {
//        Log.d("atul", String.valueOf(modelSectionHeadersBTList.size()));
//        return modelSectionHeadersBTList == null ? 0 : modelSectionHeadersBTList.size();
//    }


    //section view holder
    @Override
    public BtSectionHeaderViewHolder onCreateSectionViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.section_header_adapter_layout, viewGroup, false);

        return new BtSectionHeaderViewHolder(view);
    }

    @Override
    public BluetoothDevicesViewHolder onCreateChildViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_bt_source_device, viewGroup, false);
        return new BluetoothDevicesViewHolder(view);
    }

    @Override
    public void onBindSectionViewHolder(BtSectionHeaderViewHolder btSectionHeaderViewHolder, int i, ModelSectionHeadersBt modelSectionHeadersBt) {
        btSectionHeaderViewHolder.tvSectionHeaderName.setText(modelSectionHeadersBt.getSectionText());
        if (modelSectionHeadersBt.getSectionText().equals("Available Devices")) {
            btSectionHeaderViewHolder.llHeader.setBackgroundColor(appCompatActivity.getResources().getColor(R.color.btn_app_mid_pink));
        } else {
            btSectionHeaderViewHolder.llHeader.setBackgroundColor(appCompatActivity.getResources().getColor(R.color.alexaBlue));
        }

    }


    @Override
    public void onBindChildViewHolder(final BluetoothDevicesViewHolder bluetoothDevicesViewHolder,
                                      final int sectionPosition, final int childPosition, final MavidSourceDeviceInfo mavidSourceDeviceInfo) {


        bluetoothDevicesViewHolder.item.setText(mavidSourceDeviceInfo.getFriendlyName());

        if (mavidSourceDeviceInfo.getConnectionStatus().equals("CONNECTED")){
            bluetoothDevicesViewHolder.iv_bluetooth_icon.setVisibility(View.VISIBLE);
        }else{
            bluetoothDevicesViewHolder.iv_bluetooth_icon.setVisibility(View.GONE);
        }

        if (mavidSourceDeviceInfo.getDeviceID() != null) {

            bluetoothDevicesViewHolder.mainlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    showProgressLoadingAlertDialog();

                    Log.d("atul_bt_device", "Ip address: " + btDeviceInfo.getIpAddress() + "mavid device id: " + mavidSourceDeviceInfo.getDeviceID());

                  //  LibreMavidHelper.sendCustomCommands(btDeviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.START_BT_CONNECT,
//                            mavidSourceDeviceInfo.getDeviceID(), new CommandStatusListenerWithResponse() {
//                                @Override
//                                public void response(final MessageInfo messageInfo) {
//
//                                    appCompatActivity.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//
//                                            LibreLogger.d(this, "atul in item array adapter getting to know connected status"
//                                                    + mavidSourceDeviceInfo + "And device connected\n" + messageInfo.getMessage());
//
//                                            btDeviceInfo.setConnectedStatus(messageInfo.getMessage());
//
//                                            if (!messageInfo.getMessage().isEmpty()) {
//
//
//                                                mavidSourceDeviceInfo.setConnectionStatus(messageInfo.getMessage());
//
//                                                modelSectionHeadersBTList.get(sectionPosition).getChildItems().get(childPosition).setConnectionStatus(messageInfo.getMessage());
//
//                                                avaliablePairedDeviceInterface.setBluetoothDevice(
//                                                        mavidSourceDeviceInfo.getConnectionStatus(),
//                                                        mavidSourceDeviceInfo.getDeviceID(),
//                                                        modelSectionHeadersBTList.get(sectionPosition).getSectionText(),
//                                                        sectionPosition, childPosition, new DismissProgressBarInterface() {
//                                                            @Override
//                                                            public void dismissProgress() {
//                                                                dismissProgressLoadingAlertDialog();
//
//                                                            }
//                                                        });
//                                            } else {
//                                                dismissProgressLoadingAlertDialog();
//                                                showCustomAlertDialogInterface.showCustomAlertDialog();
//                                            }
//                                        }
//                                    });
//
//                                }
//
//                                @Override
//                                public void failure(Exception e) {
//                                    e.printStackTrace();
//                                    Log.d("atul bt error", e.toString());
//
//                                    //show action failed ..Please try again
//                                    appCompatActivity.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            dismissProgressLoadingAlertDialog();
//                                            showCustomAlertDialogInterface.showCustomAlertDialog();
//                                        }
//                                    });
//                                }
//
//                                @Override
//                                public void success() {
//
//                                }
//                            });
//                BtnUpair.setText("");

//                if (modelSectionHeadersBTList.size() == 0) {
//                    LibreLogger.d(this, "suma clicked btn later" + sectionPosition + "itemlist size3\n" + modelSectionHeadersBTList.size());
//                    Intent intent = new Intent(context, DeviceSettingsActivity.class);
//                    intent.putExtra(Constants.INTENTS.IP_ADDRESS, btDeviceInfo.getIpAddress());
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(intent);
////                     Intent intent = new Intent(context, DeviceSettingsActivity.class);
////                     context.startActivity(intent);
//                }

                }
            });
        }
    }

    private void showProgressLoadingAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog);
        dialog = builder.create();
        dialog.show();

    }

    private void dismissProgressLoadingAlertDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


//    @Override
//    public void onBindViewHolder(BluetoothDevicesViewHolder holder, final int listPosition) {
//        final TextView item = holder.item;
//        final TextView BtnUpair = holder.btnUpair;
//        final TextView shorttext = holder.showMore;
//        final LinearLayout mainlayout = holder.mainlayout;
//
//        item.setText(voodbtlist.get(listPosition).getFriendlyName());
//
//        LibreLogger.d(this, "suma in deleted item bt1" + listPosition);
//
//        mainlayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                LibreLogger.d(this, "suma in item array adapter" + voodbtlist.get(listPosition) + "And device id" + voodbtlist.get(listPosition).getDeviceID());
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setCancelable(false); // if you want user to wait for some process to finish,
//                builder.setView(R.layout.layout_loading_dialog);
//                final AlertDialog dialog = builder.create();
//
////                btSetting(btDeviceInfo.getIpAddress(), voodbtlist.get(listPosition).getDeviceID());
//
//                try {
//                    for (int i = 0; i < voodbtlist.size(); i++) {
//                        if (listPosition <= voodbtlist.size()) {
//                            if (voodbtlist != null) {
//                                dialog.show();
//                                LibreLogger.d(this, "suma in item array adapter getting to know connected status" + voodbtlist.get(listPosition).getFriendlyName() + "And device connected/n" + btDeviceInfo.getConnectedStatus());
//                                LibreMavidHelper.sendCustomCommands(btDeviceInfo.getIpAddress(), LibreMavidHelper.COMMANDS.START_BT_CONNECT, voodbtlist.get(listPosition).getDeviceID(), new CommandStatusListenerWithResponse() {
//                                    @Override
//                                    public void response(MessageInfo messageInfo) {
//                                        LibreLogger.d(
//                                                this, "suma in btsourcedeviceinfo adapter\n" + messageInfo.getMessage());
//                                        dialog.dismiss();
//                                        btDeviceInfo.setConnectedStatus(messageInfo.getMessage());
//                                        voodbtlist.get(listPosition).setConnectionStatus(messageInfo.getMessage());
//                                        LibreLogger.d(this, "suma in item array adapter getting to know connected status2" + voodbtlist.get(listPosition) + "And device connected/n" + btDeviceInfo.getConnectedStatus());
//                                        if (btDeviceInfo.getConnectedStatus() != "") {
//                                            BtnUpair.setText(btDeviceInfo.getConnectedStatus());
//                                        } else {
//                                            Log.d("atul", btDeviceInfo.getConnectedStatus());
//                                        }
//
//                                    }
//
//                                    @Override
//                                    public void failure(Exception e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    @Override
//                                    public void success() {
//
//                                    }
//                                });
//                                BtnUpair.setText("");
//                                //dialog.cancel();
//                                //dialog.dismiss();
//
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LibreLogger.d(this, "Exception occurred while finding duplicates");
//                }
//
//                LibreLogger.d(this, "suma clicked btn later" + listPosition + "itemlist size2\n" + itemList.size());
//                if (voodbtlist.size() == 0) {
//                    LibreLogger.d(this, "suma clicked btn later" + listPosition + "itemlist size3\n" + itemList.size());
//                    Intent intent = new Intent(context, DeviceSettingsActivity.class);
//                    intent.putExtra(Constants.INTENTS.IP_ADDRESS, btDeviceInfo.getIpAddress());
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(intent);
////                     Intent intent = new Intent(context, DeviceSettingsActivity.class);
////                     context.startActivity(intent);
//                }
//
//
//                return false;
//            }
//        });
//
//    }

    // load data in each row element
//    private void btSetting(final String ipAddress, final String deviceUUid) {
//        //send command to device
//        Log.d("atul", "btSetting");
//        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.START_BT_CONNECT, deviceUUid, new CommandStatusListenerWithResponse() {
//            @Override
//            public void response(MessageInfo messageInfo) {
//                LibreLogger.d(this, "suma in btsourcedeviceinfo adapter\n" + messageInfo.getMessage());
//                btDeviceInfo.setConnectedStatus(messageInfo.getMessage());
//                Log.d("atul", "connection:Status " + btDeviceInfo.getConnectedStatus() + "device name " + btDeviceInfo.getFriendlyName());
//                // MavidApplication.btChangeConnectedStatus=true;
//                // LibreLogger.d(this, "suma in voodbtsetting btscanlist empty" + scanListArray);
//
//            }
//
//            @Override
//            public void failure(Exception e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void success() {
//
//            }
//        });
//    }

    public void populateBtDeviceList(final String scanList) {
        btListMap.clear();
        try {
            JSONObject mainObj = new JSONObject(scanList);
            JSONArray scanListArray = mainObj.getJSONArray("BLUETOOTH DEVICES");
            LibreLogger.d(this, "suma in voodbtsetting btscanlist" + scanListArray);
            if (scanListArray.equals("")) {
                LibreLogger.d(this, "suma in voodbtsetting btscanlist empty" + scanListArray);

            } else {
                for (int i = 0; i < scanListArray.length(); i++) {
                    JSONObject obj = (JSONObject) scanListArray.get(i);
                    if (obj.getString("friendlyName") == null
                            || (obj.getString("friendlyName").isEmpty())) {
                        continue;
                    }
                    btListMap.put(obj.getString("friendlyName"), obj.getString("uniqueDeviceId"));
                    deviceInfoList.add(obj.getString("friendlyName"));
//                    btdeviceInfo = new BtSourceDeviceInfo(obj.getString("friendlyName"), obj.getString("uniqueDeviceId"));
//                    LibreLogger.d(this, "Bt FriendlyName and UniqueDeviceID" + obj.getString("friendlyName") + "ID" + obj.getString("uniqueDeviceId"));
//                    btdeviceInfo.setFriendlyName(obj.getString("friendlyName"));
//                    btdeviceInfo.setDeviceID(obj.getString("uniqueDeviceId"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    static class BtSectionHeaderViewHolder extends RecyclerView.ViewHolder {

        TextView tvSectionHeaderName;
        LinearLayout llHeader;

        public BtSectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionHeaderName = itemView.findViewById(R.id.tv_section_header_name);
            llHeader = itemView.findViewById(R.id.ll_header);
        }


    }


    // Static inner class to initialize the views of rows
    static class BluetoothDevicesViewHolder extends RecyclerView.ViewHolder {
        public TextView item, showMore;
        public TextView btnUpair;
        LinearLayout mainlayout;
        ImageView iv_bluetooth_icon;


        public BluetoothDevicesViewHolder(View itemView) {
            super(itemView);
//            itemView.setOnClickListener(this);
            item = (TextView) itemView.findViewById(R.id.short_content);
            showMore = (TextView) itemView.findViewById(R.id.short_content1);
            mainlayout = (LinearLayout) itemView.findViewById(R.id.mainlayout);

            // linearLayout = (LinearLayout) itemView.findViewById(R.id.linearlayout);
            btnUpair = (TextView) itemView.findViewById(R.id.advancedSettings);
//            btnUpair.setOnClickListener(this);
            iv_bluetooth_icon = itemView.findViewById(R.id.iv_bluetooth_icon);

        }

//        public class DownloadAsyncTask extends AsyncTask<Void, Integer, Void> {
//            private ProgressDialog mDialog;
//
//            // execution of result of Long time consuming operation
//            @Override
//            protected void onPostExecute(Void result) {
//
//                // progressDialog.show();
//                if (mDialog.isShowing()) {
//                    mDialog.dismiss();
//                }
//
//            }
//
//            // Things to be done before execution of long running operation.
//            @Override
//            protected void onPreExecute() {
//                mDialog = ProgressDialog.show(, "Hello", "Test");
//            }
//
//            // perform long running operation operation
//            @Override
//            protected Void doInBackground(Void... params) {
//                //System.out.println("doInBackground loading.." + id);
//                /*            String tempPath = FileUtils.createTempFile(id);
//                            for (int i = 0; i < imagePaths.size(); i++) {
//                                imagePaths.get(i).trim();
//                                try {
//                                    Bitmap imgTemp;
//                                    imgTemp = FileUtils.downloadBitmapFromURL(id, imagePaths.get(i), tempPath);
//                                    System.out.println("imgTemp: " + imgTemp);
//                                    if (imgTemp != null) {
//                                        // save image on sdcard.
//                                        // compress it for performance
//                                        Bitmap img = Bitmap.createScaledBitmap(imgTemp, 90, 80, true);
//                                        imgTemp.recycle();
//                                        FileUtils.saveDataToFile(img, tempPath, imagePaths.get(i));
//                                    }
//                                    else {
//                                        continue;
//                                    }
//                                }
//                                catch (IOException e) {
//                                    e.printStackTrace();
//                                    mDialog.dismiss();
//                                }
//                            }
//
//                            Looper.prepare();
//                            mDialog.dismiss();*/
//
//                try {
//                    Thread.sleep(5000);
//                }
//                catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            /*
//             * Things to be done while execution of long running operation is in
//             * progress.
//             */
//            @Override
//            protected void onProgressUpdate(Integer... values) {
//                if (mDialog.isShowing()) {
//                    mDialog.dismiss();
//                }
//            }
//        }

    }
}


