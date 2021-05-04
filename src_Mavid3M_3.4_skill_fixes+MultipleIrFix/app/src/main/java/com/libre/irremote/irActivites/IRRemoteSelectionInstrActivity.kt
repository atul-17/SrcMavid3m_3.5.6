package com.libre.irremote.irActivites

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.libre.irremote.R
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import kotlinx.android.synthetic.main.ir_selection_instrs_activity.*

class IRRemoteSelectionInstrActivity : AppCompatActivity() {

    var bundle = Bundle()

    var applianceId: Int = 0

    var applianceBrandName: String = ""

    var ipAddress: String = ""

    var deviceInfo: DeviceInfo? = null

    var selectedApplianceType: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ir_selection_instrs_activity)

        bundle = intent.extras!!

        if (bundle != null) {
            applianceId = bundle.getInt("applianceId", 0)
            applianceBrandName = bundle.getString("applianceBrandName", "")
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
            ipAddress = bundle.getString("ipAddress", "")

            selectedApplianceType = bundle.getString("selectedApplianceType", "1")
        }

        when (selectedApplianceType) {
            "1",
            "TV"
            -> {
                tvHeading.text = "TV Remote Selection"
                tvInstructionsMsg.text = applianceBrandName.plus(" TV have several remotes available , please follow the instructions to select the remote that suits your TV")
            }
            "2",
            "TVP"
            -> {
                tvHeading.text = "Set Top Box Remote Selection"
                tvInstructionsMsg.text = applianceBrandName.plus(" Set Top Box have several remotes available , please follow the instructions to select the remote that suits your Set Top Box")
            }
            "3" -> {
                //ac
                tvHeading.text = "AC Remote Selection"
                tvInstructionsMsg.text = applianceBrandName.plus(" AC have several remotes available , please follow the instructions to select the remote that suits your AC")

            }
        }


        btnNext.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("applianceId", applianceId)
            bundle.putString("applianceBrandName", applianceBrandName)
            bundle.putString("ipAddress", ipAddress)
            bundle.putSerializable("deviceInfo", deviceInfo)
            bundle.putString("selectedApplianceType", selectedApplianceType)
            if (selectedApplianceType == "3") {
                val intent = Intent(this@IRRemoteSelectionInstrActivity, IRAcRemoteSelectionActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                val intent = Intent(this@IRRemoteSelectionInstrActivity, IRTvOrTvpRemoteSelectionActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            finish()
        }
    }
}