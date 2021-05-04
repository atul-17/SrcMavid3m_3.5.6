package com.libre.irremote.irActivites.irMultipleRemotes.remotelistadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.libre.irremote.R
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.utility.OnButtonClickListViewInterface
import com.libre.irremote.utility.OnClickOfDeleteButtonInterface


class RemoteListAdapters(private val context: Context,
                         remoteList: ArrayList<String>,
                         onClickinterface: OnButtonClickListViewInterface,
                         private var onClickOfDeleteButtonInterface: OnClickOfDeleteButtonInterface,
                         private var remoteListStored: MutableList<ModelRemoteDetails>) : RecyclerView.Adapter<RemoteListAdapters.RemoteListBottomSheetHolder>() {

    private val remotListString = remoteList

    private var mContext: Context = context

    private var clickInterface: OnButtonClickListViewInterface = onClickinterface

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemoteListBottomSheetHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_ir_remote_list, parent, false)
        mContext = context
        return RemoteListBottomSheetHolder(view)
    }

    override fun onBindViewHolder(holder: RemoteListBottomSheetHolder, position: Int) {
        val remoteName = remotListString[position]
        holder.tv_ssid_name.text = remoteName
        holder.rootViewLayout.setOnClickListener {
            clickInterface.onClickListview(position)
        }

        holder.ivDelete.setOnClickListener {
            onClickOfDeleteButtonInterface.onClickOfDelete(remoteListStored[position],position)
        }

    }

    override fun getItemCount(): Int {
        return remotListString.size
    }

    class RemoteListBottomSheetHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_ssid_name: TextView
        var rootViewLayout: RelativeLayout
        var image_arrow: ImageView

        lateinit var ivDelete: AppCompatImageView

        init {
            tv_ssid_name = itemView.findViewById(R.id.id_txt_heading)
            rootViewLayout = itemView.findViewById(R.id.id_root_view)
            image_arrow = itemView.findViewById(R.id.id_image_goto)
            ivDelete = itemView.findViewById(R.id.ivDelete)
        }
    }


}