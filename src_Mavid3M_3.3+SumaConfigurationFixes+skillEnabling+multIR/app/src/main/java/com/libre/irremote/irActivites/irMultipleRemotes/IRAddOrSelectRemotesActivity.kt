package com.libre.irremote.irActivites.irMultipleRemotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.libre.irremote.R
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import com.libre.irremote.utility.OnButtonClickListViewInterface
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo

import com.libre.irremote.irActivites.irMultipleRemotes.remotelistadapters.RemoteListAdapters
import com.mavid.fragments.irMultipleRemotes.IRAcApplianceActivity
import com.mavid.fragments.irMultipleRemotes.IRRemoteSelectionActivity
import com.mavid.fragments.irMultipleRemotes.IRTVPApplianceActivity
import com.mavid.fragments.irMultipleRemotes.IRTelevisionApplianceActivity

import kotlinx.android.synthetic.main.activity_remote_selection.*
import kotlinx.android.synthetic.main.toolbar_custom_layout.*

class IRAddOrs : AppCompatActivity(), OnButtonClickListViewInterface {

    var bundle1 = Bundle()

    var deviceInfo: DeviceInfo? = null;

    var remoteListAdapters: RemoteListAdapters? = null
    var remoteList: ArrayList<String>? = null
    var remoteListStored: MutableList<ModelRemoteDetails> = ArrayList();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_remote_selection)

        bundle1 = intent.extras!!

        if (bundle1 != null) {
            deviceInfo = bundle1.getSerializable("deviceInfo") as DeviceInfo
        }

        remoteList = ArrayList()
        remoteList!!.add("")

        setWifiListAdapter()

        id_icon_back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })


        id_add_icon.setOnClickListener {
            val intent = Intent(this@IRAddOrs, IRRemoteSelectionActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            intent.putExtras(bundle)
            startActivity(intent)
        }

    }

    fun setWifiListAdapter() {
        val linearLayoutManager = LinearLayoutManager(this@IRAddOrs)

        val remoteList = ArrayList<String>()

        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()


        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {


            var modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            remoteListStored = modelRemoteSubAndMacDetils.modelRemoteDetailsList

            for (i in remoteListStored) {
                remoteList.add(i.selectedBrandName)
            }
        }

        remoteListAdapters = RemoteListAdapters(this@IRAddOrs, remoteList, this)
        remote_list_Recycler_view.setAdapter(remoteListAdapters)
        remote_list_Recycler_view.setLayoutManager(linearLayoutManager)

    }

    override fun onClickListview(position: Int) {
        if (remoteListStored != null && remoteListStored.size > position) {
            var modelRemoteDetails = remoteListStored[position]
            if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {
                gotToTvActivity(position)
            } else if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {
                gotToTVPActivity(position)
            } else if (modelRemoteDetails.selectedAppliance == "3" || modelRemoteDetails.selectedAppliance == "AC") {
                gotToAcActivity(position)
            }
        }

    }

    fun gotToTvActivity(pos: Int) {
        val gson = Gson()
        val intent = Intent(this, IRTelevisionApplianceActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putInt("remoteIndex", remoteListStored[pos].index)
        bundle.putString("remoteId", remoteListStored[pos].remoteId)
        bundle.putString("workingRemoteData", gson.toJson(remoteListStored[pos].remotesHashMap))
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun gotToAcActivity(pos: Int) {
        val gson = Gson()
        val intent = Intent(this, IRAcApplianceActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putInt("remoteIndex", remoteListStored[pos].index)
        bundle.putString("remoteId", remoteListStored[pos].remoteId)
        bundle.putString("workingRemoteData", gson.toJson(remoteListStored[pos].ac_remotelist))
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun gotToTVPActivity(pos: Int) {
        val gson = Gson()
        val intent = Intent(this, IRTVPApplianceActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putInt("remoteIndex", remoteListStored[pos].index)
        bundle.putString("remoteId", remoteListStored[pos].remoteId)
        bundle.putString("workingRemoteData", gson.toJson(remoteListStored[pos].remotesHashMap))
        intent.putExtras(bundle)
        startActivity(intent)
    }


}