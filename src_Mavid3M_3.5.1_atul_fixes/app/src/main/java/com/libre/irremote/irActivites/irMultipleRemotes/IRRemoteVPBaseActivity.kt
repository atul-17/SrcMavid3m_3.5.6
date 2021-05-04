package com.libre.irremote.irActivites.irMultipleRemotes


import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.libre.irremote.R
import com.libre.irremote.models.ModelLdapi2AcModes
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo


import kotlinx.android.synthetic.main.activity_ir_add_remote_vp.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


abstract class IRRemoteVPBaseActivity : AppCompatActivity() {

    var TAG = "IRRemoteVPBaseActivity"


    lateinit var progressDialog: Dialog


    var uIRelatedClass = UIRelatedClass()


    lateinit var sharedPreferences: SharedPreferences

    var selectedAppliance = "2"//by default tvp

    var tvpSelectedBrand = ""


    var gson: Gson? = Gson()


    lateinit var apiViewModel: ApiViewModel

    companion object {
        var IRRemoteVPBaseActivity: IRRemoteVPBaseActivity? = null
    }

    var modelLdapi2AcModesList: MutableList<ModelLdapi2AcModes> = ArrayList()

    val LDAPI2_TIMOUT = 1

    var irButtonListTVTimerTask: Timer? = Timer()

    var irButtonListTVPTimerTask: Timer? = Timer()

    var irButtonListACTimerTask: Timer? = Timer()


    var workingRemoteButtonsHashMap = HashMap<String, String>()

    val uiRelatedClass = UIRelatedClass()

    var myHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what = msg.what
            when (msg.what) {
                LDAPI2_TIMOUT -> runOnUiThread(Runnable {
                    dismissLoader()
                    if (irButtonListTVTimerTask != null) {
                        irButtonListTVTimerTask!!.cancel()
                    }
                    if (irButtonListTVPTimerTask != null) {
                        irButtonListTVPTimerTask!!.cancel()
                    }

                    if (irButtonListACTimerTask != null) {
                        irButtonListACTimerTask!!.cancel()
                    }

                    Log.d(TAG, "Error")
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRRemoteVPBaseActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRRemoteVPBaseActivity)
                })
            }
        }
    }

    protected abstract fun getLayoutResourceId(): Int

    protected abstract fun getSelectedAppliance(): Int

    protected abstract fun getDeviceInfoData(): DeviceInfo?

    protected abstract fun getSelectedRemoteIndex(): Int

    protected abstract fun getSelectedRemoteId(): String?


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        selectedAppliance = getSelectedAppliance().toString()

    }


    override fun onDestroy() {
        super.onDestroy()
        IRRemoteVPBaseActivity = null
    }


    fun buildrogressDialog() {
        progressDialog = Dialog(this@IRRemoteVPBaseActivity)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_bar)
        progressDialog.setCancelable(false)

        val progress_title: AppCompatTextView = progressDialog.findViewById(R.id.progress_title)
        val progress_bar: ProgressBar = progressDialog.findViewById(R.id.progress_bar)
        val progress_message: AppCompatTextView = progressDialog.findViewById(R.id.progress_message)

        progress_message.visibility = View.GONE
        progress_title.text = "Please Wait..."
    }

    fun showProgressBar() {
        runOnUiThread {
            if (!progressDialog.isShowing) {
                progressDialog.show()
            }
        }
    }

    fun dismissLoader() {
        runOnUiThread {
            progressDialog.dismiss()
        }
    }


    fun showSucessfullMessage() {
        uIRelatedClass?.buildSnackBarWithoutButton(this@IRRemoteVPBaseActivity,
                window?.decorView!!.findViewById(android.R.id.content), "Successfully sent data to the device")
    }

    fun showErrorMessage() {
        uIRelatedClass?.buildSnackBarWithoutButton(this@IRRemoteVPBaseActivity,
                window?.decorView!!.findViewById(android.R.id.content), "There was an error while sending data to the device")
    }

    /** LDApi#4*/
    private fun buiidJsonForSendingTheKeyPressed(index: Int, apiId: Int,
                                                 applianceType: String, remoteId: String?, keys: String): JSONObject {
        val paylodJsonObject = JSONObject()
        paylodJsonObject.put("ID", apiId)

        val dataJSONObject = JSONObject()
        dataJSONObject.put("appliance", applianceType.toInt())//type of the appliance
        dataJSONObject.put("rId", remoteId?.toInt())//remote if the selected user
        dataJSONObject.put("index", index)
        var keysJsonArray = JSONArray()
        keysJsonArray.put(keys)

        dataJSONObject.put("keys", keysJsonArray)//button name ie pressed

        paylodJsonObject.put("data", dataJSONObject)

        return paylodJsonObject
    }


    /** LDApi#4*/
    fun sendTheKeysPressedIntoTheMavid3MDevice(index: Int, keysPressed: String, onRemoteKeyPressedInterface: OnRemoteKeyPressedInterface) {

        selectedAppliance = getSelectedAppliance().toString()

        var remoteId: String? = "0"
        //device acknowledged or device sucess response


        remoteId = getSelectedRemoteId()

        Log.d(TAG, "sendingRemoteButton: ".plus(buiidJsonForSendingTheKeyPressed(index, 4,
                selectedAppliance, remoteId, keysPressed.plus(",1")).toString()))

        LibreMavidHelper.sendCustomCommands(getDeviceInfoData()?.ipAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buiidJsonForSendingTheKeyPressed(index, 4, selectedAppliance, remoteId, keysPressed.plus(",1")).toString(),
                object : CommandStatusListenerWithResponse {
                    override fun response(messageInfo: MessageInfo?) {
                        if (messageInfo != null) {
                            val dataJsonObject = JSONObject(messageInfo?.message)
                            Log.d(TAG, "ldapi_#4_Response".plus(dataJsonObject).toString())
                            val status = dataJsonObject.getInt("Status")

                            //device acknowledged or device sucess response
                            if (status == 2 || status == 1) {
                                onRemoteKeyPressedInterface.onKeyPressed(true)
                            } else {
                                onRemoteKeyPressedInterface.onKeyPressed(false)
                            }
                        }
                    }

                    override fun failure(e: Exception?) {
                        dismissLoader()
                        Log.d(TAG, "ExceptionWhileSendingKeyPressed$e")
                        onRemoteKeyPressedInterface.onKeyPressed(false)
                    }

                    override fun success() {

                    }
                })
    }




}
