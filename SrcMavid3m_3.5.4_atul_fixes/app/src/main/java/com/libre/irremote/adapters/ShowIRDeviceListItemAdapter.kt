package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelEndPointObject
import kotlinx.android.synthetic.main.show_ir_device_list_item_adapter.view.*

class ShowIRDeviceListItemAdapter(val context: Context, val modelIRDeviceEndPointObjectList: MutableList<ModelEndPointObject>) : RecyclerView.Adapter<ShowIRDeviceListItemAdapter.ShowIRDeviceListItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowIRDeviceListItemHolder {
       return ShowIRDeviceListItemHolder(LayoutInflater.from(context).inflate(R.layout.show_ir_device_list_item_adapter,parent,false))
    }

    override fun getItemCount(): Int {
       return modelIRDeviceEndPointObjectList.size
    }

    override fun onBindViewHolder(holder: ShowIRDeviceListItemHolder, position: Int) {

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            holder.itemView.tvDeviceTypeIcon.text = Html.fromHtml("&#x${modelIRDeviceList[position]};", Html.FROM_HTML_MODE_LEGACY)
//        } else {
//            holder.itemView.tvDeviceTypeIcon.text = Html.fromHtml("&#x$modelIRDeviceList[position];")
//        }

        holder.itemView.tvIrDeviceName.text = modelIRDeviceEndPointObjectList[position].friendlyName
    }

    inner class ShowIRDeviceListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

}