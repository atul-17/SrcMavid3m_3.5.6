package com.libre.irremote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelGetTvpBrandsSucessResponse
import kotlinx.android.synthetic.main.adapter_ir_select_tv_brands.view.*

class IrSelectTvpBrandsAdapter(val context: Context, val modelGetTvpBrandsSucessResponseList
: MutableList<ModelGetTvpBrandsSucessResponse>) : RecyclerView.Adapter<IrSelectTvpBrandsAdapter.IrSelectTvpBrandsHolder>() {


    inner class IrSelectTvpBrandsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IrSelectTvpBrandsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_ir_select_tv_brands,parent,false)
        return IrSelectTvpBrandsHolder(view)
    }

    override fun getItemCount(): Int {
      return modelGetTvpBrandsSucessResponseList.size
    }

    override fun onBindViewHolder(holder: IrSelectTvpBrandsHolder, position: Int) {
        holder.itemView.tvBrandName.text= modelGetTvpBrandsSucessResponseList[position].title
    }

}