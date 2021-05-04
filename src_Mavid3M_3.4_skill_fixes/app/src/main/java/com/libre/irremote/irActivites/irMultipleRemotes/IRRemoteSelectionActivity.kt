package com.mavid.fragments.irMultipleRemotes

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.libre.irremote.R
import com.libre.irremote.irActivites.IRSelectTvOrTVPOrAcRegionalBrandsActivity
import com.libre.irremote.irActivites.IRTvpBrandActivity
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import kotlinx.android.synthetic.main.activity_ir_selection_devices.*

class IRRemoteSelectionActivity : AppCompatActivity() {

    var bundle = Bundle()

    var deviceInfo: DeviceInfo? = null

    var image_btn: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_selection_devices)

        bundle = intent.extras!!

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
        }

        image_btn = findViewById(R.id.id_remote_image_tvp)


        id_remote_image_tvp.setOnClickListener {
            val intent = Intent(this@IRRemoteSelectionActivity, IRTvpBrandActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        id_remote_image_ac.setOnClickListener {
            val intent = Intent(this@IRRemoteSelectionActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            bundle.putBoolean("isAc", true)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        id_remote_image_tv.setOnClickListener {
            val intent = Intent(this@IRRemoteSelectionActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            bundle.putBoolean("isTv", true)
            intent.putExtras(bundle)
            startActivity(intent)
        }

    }
}
