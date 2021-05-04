package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelGetRegionalTvpPayloadSucess
import kotlinx.android.synthetic.main.adapter_ir_select_tv_brands.view.*

class IRSelectRegionalTvpAdapter(val context: Context
                                 , val modelGetTvpBrandsSucessResponseList: MutableList<ModelGetRegionalTvpPayloadSucess>) : RecyclerView.Adapter<IRSelectRegionalTvpAdapter.IRSelectRegionalTvpHolder>() {

    inner class IRSelectRegionalTvpHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IRSelectRegionalTvpHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_ir_select_tv_brands, parent, false)
        return IRSelectRegionalTvpHolder(view)
    }

    override fun getItemCount(): Int {
        return modelGetTvpBrandsSucessResponseList.size
    }

    override fun onBindViewHolder(holder: IRSelectRegionalTvpHolder, position: Int) {
        holder.itemView.tvBrandName.text= modelGetTvpBrandsSucessResponseList[position].title
    }
}