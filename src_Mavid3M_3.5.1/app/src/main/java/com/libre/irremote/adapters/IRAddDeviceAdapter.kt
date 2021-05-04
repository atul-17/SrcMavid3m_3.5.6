package com.libre.irremote.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelApplianceList
import kotlinx.android.synthetic.main.show_ir_appliance_adapter_layout.view.*

class IRAddDeviceAdapter(val context: Context, val appliancesList: MutableList<ModelApplianceList>) : RecyclerView.Adapter<IRAddDeviceAdapter.IRAddDeviceHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IRAddDeviceHolder {
        return IRAddDeviceHolder(LayoutInflater.from(context).inflate(R.layout.show_ir_appliance_adapter_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return appliancesList.size
    }

    override fun onBindViewHolder(holder: IRAddDeviceHolder, position: Int) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.itemView.mdiAppliancesIconView.text = Html.fromHtml("&#x" + appliancesList[position].materialDesignCode + ";", Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.itemView.mdiAppliancesIconView.text = Html.fromHtml("&#x" + appliancesList[position].materialDesignCode + ";")
        }

        holder.itemView.mdiAppliancesIconView.setTextColor(context.resources.getColor(R.color.white))
        holder.itemView.tvApplianceName.text = appliancesList[position].applianceName
    }


    inner class IRAddDeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

}