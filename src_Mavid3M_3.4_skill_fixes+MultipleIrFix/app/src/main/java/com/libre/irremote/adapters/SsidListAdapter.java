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
import com.libre.irremote.models.ModelDeviceState;

import java.util.List;

public class SsidListAdapter extends RecyclerView.Adapter<SsidListAdapter.SsidListBottomSheetHolder> {

    private Context context;
    private List<ModelDeviceState> modelDeviceStateList;
    private WifiConfigurationItemClickInterface wifiConfigurationItemClickInterface;
    private String connectedSsid;

    public void setWifiConfigurationItemClickInterface(WifiConfigurationItemClickInterface wifiConfigurationItemClickInterface) {
        this.wifiConfigurationItemClickInterface = wifiConfigurationItemClickInterface;
    }

    public SsidListAdapter(Context context, List<ModelDeviceState> modelDeviceStateList,String connectedSsid) {
        this.context = context;
        this.modelDeviceStateList = modelDeviceStateList;
        this.connectedSsid = connectedSsid;
    }

    @NonNull
    @Override
    public SsidListBottomSheetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_bottom_sheet_ssid_list_adapter, parent, false);
        return new SsidListBottomSheetHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull SsidListBottomSheetHolder holder, final int position) {
        holder.tv_ssid_name.setText(modelDeviceStateList.get(position).getSsid());
        holder.tv_ssid_profile.setText(modelDeviceStateList.get(position).getProfile());

        holder.ll_ssid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            wifiConfigurationItemClickInterface.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelDeviceStateList.size();
    }

    public static class SsidListBottomSheetHolder extends RecyclerView.ViewHolder {
        TextView tv_ssid_name;
        LinearLayout ll_ssid;
        TextView tv_ssid_profile;
        AppCompatImageView iv_connected_ssid_tick;

        public SsidListBottomSheetHolder(View itemView) {
            super(itemView);
            tv_ssid_name = itemView.findViewById(R.id.tv_ssid_name);
            ll_ssid = itemView.findViewById(R.id.ll_ssid);
            tv_ssid_profile = itemView.findViewById(R.id.tv_ssid_profile);


        }
    }
}
