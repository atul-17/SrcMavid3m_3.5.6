package com.libre.irremote.irActivites.irMultipleRemotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.libre.irremote.R
import com.libre.irremote.irActivites.IRSelectTvOrTVPOrAcRegionalBrandsActivity
import com.libre.irremote.irActivites.IRTvpBrandActivity
import com.libre.irremote.utility.MaterialDesignIconView
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo


class IRSelectionDevicesActivity : AppCompatActivity() {

    var deviceInfo: DeviceInfo? = null

    var bundle = Bundle()

    var image1: ImageView? = null
    var image2: ImageView? = null
    var image3: AppCompatImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_selection_devices)

        bundle = intent.extras!!

        image1 = findViewById(R.id.id_remote_image_tv)
        image2 = findViewById(R.id.id_remote_image_tvp)
        image3 = findViewById(R.id.id_remote_image_ac)

        image1?.setOnClickListener(View.OnClickListener {
            selectTvRemoteType1()
        })

        image2?.setOnClickListener(View.OnClickListener {
            selectTvRemoteType2()
        })

        image3?.setOnClickListener(View.OnClickListener {
            selectTvRemoteType3()
        })

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
        }


    }

    fun selectTvRemoteType1() {
        val intent = Intent(this@IRSelectionDevicesActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putBoolean("isTv", true)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun selectTvRemoteType2() {
        val intent = Intent(this@IRSelectionDevicesActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo);
        bundle.putBoolean("isAc", true)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun selectTvRemoteType3() {
        val intent = Intent(this@IRSelectionDevicesActivity, IRTvpBrandActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        intent.putExtras(bundle)
        startActivity(intent)
    }
}
