package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelIRDeviceManufacturerNames
import kotlinx.android.synthetic.main.show_ir_device_manufacture_adapter.view.*

class ShowIRDeviceManufactureMakersAdapter(val context: Context,val irMakersList:MutableList<ModelIRDeviceManufacturerNames>) : RecyclerView.Adapter<ShowIRDeviceManufactureMakersAdapter.ShowIRDeviceManufacturmakersHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowIRDeviceManufacturmakersHolder {
        return ShowIRDeviceManufacturmakersHolder(LayoutInflater.from(context).inflate(R.layout.show_ir_device_manufacture_adapter,parent,false))
    }

    override fun getItemCount(): Int {
       return irMakersList.size
    }

    override fun onBindViewHolder(holder: ShowIRDeviceManufacturmakersHolder, position: Int) {
       holder.itemView.iRMakersName.text = irMakersList[position].makersName
    }


    inner class ShowIRDeviceManufacturmakersHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}
}