package com.libre.irremote.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.libre.irremote.R;
import com.libre.irremote.SAC.WifiSecurityConfigurationItemClickInterface;

import java.util.List;

public class WifiListBottomSheetAdapterForSecurityType extends RecyclerView.Adapter<WifiListBottomSheetAdapterForSecurityType.WifiListBottomSheetViewHolder> {

    private Context context;
    private List<String> securityTpe;
    private WifiSecurityConfigurationItemClickInterface wifiConfigurationItemClickInterface;

    public void setWifiConfigurationForSeurity(WifiSecurityConfigurationItemClickInterface wifiConfigurationItemClickInterface) {
        this.wifiConfigurationItemClickInterface = wifiConfigurationItemClickInterface;
    }

    public WifiListBottomSheetAdapterForSecurityType(Context context, List<String> securityType) {
        this.context = context;
        this.securityTpe = securityType;
    }

    @NonNull
    @Override
    public WifiListBottomSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_scan_list_item_adapter, parent, false);

        return new WifiListBottomSheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WifiListBottomSheetViewHolder holder, final int position) {
        holder.tv_security_type.setText(securityTpe.get(position));
        holder.tv_security_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiConfigurationItemClickInterface.onSecurityTypeSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return securityTpe.size();
    }

    public static class WifiListBottomSheetViewHolder extends RecyclerView.ViewHolder {

        TextView tv_security_type;

        public WifiListBottomSheetViewHolder(View itemView) {
            super(itemView);
            tv_security_type = itemView.findViewById(R.id.tv_ssid_name);
        }
    }

}
