package com.libre.irremote.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.libre.irremote.R;
import com.libre.irremote.SAC.WifiConfigurationItemClickInterface;
import com.libre.irremote.models.ModelWifiScanList;

import java.util.List;

public class WifiListBottomSheetAdapter extends RecyclerView.Adapter<WifiListBottomSheetAdapter.WifiListBottomSheetViewHolder> {

    private Context context;
    private List<ModelWifiScanList> modelWifiScanList;
    private WifiConfigurationItemClickInterface wifiConfigurationItemClickInterface;

    public void setWifiConfigurationItemClickInterface(WifiConfigurationItemClickInterface wifiConfigurationItemClickInterface) {
        this.wifiConfigurationItemClickInterface = wifiConfigurationItemClickInterface;
    }

    public WifiListBottomSheetAdapter(Context context, List<ModelWifiScanList> modelWifiScanList) {
        this.context = context;
        this.modelWifiScanList = modelWifiScanList;
    }

    @NonNull
    @Override
    public WifiListBottomSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_scan_list_item_adapter, parent, false);

        return new WifiListBottomSheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WifiListBottomSheetViewHolder holder, int position) {
        holder.tv_ssid_name.setText(modelWifiScanList.get(position).getSsid());
        holder.tv_ssid_security.setText(modelWifiScanList.get(position).getSecurity());

        if ((Integer.parseInt(modelWifiScanList.get(position).getRssi()) == 100)) {
            holder.iv_wifi_rssi.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_other));
        }
        else if (Integer.parseInt(modelWifiScanList.get(position).getRssi()) <=0
                && Integer.parseInt(modelWifiScanList.get(position).getRssi()) > -45) {
            //very good signal
            holder.iv_wifi_rssi.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_excellent));

        } else if (Integer.parseInt(modelWifiScanList.get(position).getRssi()) <=-45
                && Integer.parseInt(modelWifiScanList.get(position).getRssi()) > -70) {
            //ok
            holder.iv_wifi_rssi.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_good));

        } else if (Integer.parseInt(modelWifiScanList.get(position).getRssi()) <=-70 &&
                Integer.parseInt(modelWifiScanList.get(position).getRssi()) > -82) {
            //low
            holder.iv_wifi_rssi.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_fair));

        } else if (Integer.parseInt(modelWifiScanList.get(position).getRssi()) <=-82) {
            //very low
            holder.iv_wifi_rssi.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_weak));
        }


        holder.ll_ssid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiConfigurationItemClickInterface.onItemClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelWifiScanList.size();
    }

    public static class WifiListBottomSheetViewHolder extends RecyclerView.ViewHolder {

        TextView tv_ssid_name;
        TextView tv_ssid_security;
        LinearLayout ll_ssid;
        AppCompatImageView iv_wifi_rssi;

        public WifiListBottomSheetViewHolder(View itemView) {
            super(itemView);
            tv_ssid_name = itemView.findViewById(R.id.tv_ssid_name);
            tv_ssid_security = itemView.findViewById(R.id.tv_ssid_security);
            ll_ssid = itemView.findViewById(R.id.ll_ssid);
            iv_wifi_rssi = itemView.findViewById(R.id.iv_wifi_rssi);
        }
    }

}
