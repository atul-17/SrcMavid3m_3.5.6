package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.AcBrandsSucessRepoModel
import kotlinx.android.synthetic.main.adapter_ir_select_tv_brands.view.*

class IRSelectAcBrandsAdapter(val context: Context, val acBrandsList: MutableList<AcBrandsSucessRepoModel>) : RecyclerView.Adapter<IRSelectAcBrandsAdapter.IRSelectAcrandsHolder>() {

    inner class IRSelectAcrandsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IRSelectAcrandsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_ir_select_tv_brands, parent, false);
        return IRSelectAcrandsHolder(view);
    }

    override fun getItemCount(): Int {
        return acBrandsList.size
    }

    override fun onBindViewHolder(holder: IRSelectAcrandsHolder, position: Int) {
        holder.itemView.tvBrandName.text = acBrandsList[position].name
    }


}