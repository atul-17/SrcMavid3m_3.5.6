package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelIRDeviceModelsTypes
import kotlinx.android.synthetic.main.show_ir_device_models_adapter_layout.view.*

class ShowIRDeviceModelTypeAdapter(val context: Context, val modelTypeNameList: MutableList<ModelIRDeviceModelsTypes>)
    : RecyclerView.Adapter<ShowIRDeviceModelTypeAdapter.ShowIRDeviceModelTypeHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowIRDeviceModelTypeHolder {
        return ShowIRDeviceModelTypeHolder(LayoutInflater.from(context).inflate(R.layout.show_ir_device_models_adapter_layout, parent, false))
    }

    override fun getItemCount(): Int {

        return modelTypeNameList.size
    }

    override fun onBindViewHolder(holder: ShowIRDeviceModelTypeHolder, position: Int) {
        holder.itemView.iRModelsTypeName.text = modelTypeNameList[position].modelTyoeName
    }


    inner class ShowIRDeviceModelTypeHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}