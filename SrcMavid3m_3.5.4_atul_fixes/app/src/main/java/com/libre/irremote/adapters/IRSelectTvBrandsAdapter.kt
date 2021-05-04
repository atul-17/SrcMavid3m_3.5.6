package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.TvBrandsSucessRepoModel
import kotlinx.android.synthetic.main.adapter_ir_select_tv_brands.view.*

class IRSelectTvBrandsAdapter(val context: Context, val tvBrandsList: MutableList<TvBrandsSucessRepoModel>) : RecyclerView.Adapter<IRSelectTvBrandsAdapter.IRSelectTvBrandsHolder>() {

    inner class IRSelectTvBrandsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IRSelectTvBrandsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_ir_select_tv_brands, parent, false);
        return IRSelectTvBrandsHolder(view);
    }


    override fun getItemCount(): Int {
        return tvBrandsList.size
    }

    override fun onBindViewHolder(holder: IRSelectTvBrandsHolder, position: Int) {
        holder.itemView.tvBrandName.text= tvBrandsList[position].name
    }
}