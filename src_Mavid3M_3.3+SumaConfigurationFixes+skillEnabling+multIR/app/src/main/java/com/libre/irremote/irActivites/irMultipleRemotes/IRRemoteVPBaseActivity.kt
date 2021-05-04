package com.mavid.fragments.irMultipleRemotes


import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.libre.irremote.R
import com.libre.irremote.models.ModelLdapi2AcModes
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo


import kotlinx.android.synthetic.main.activity_ir_add_remote_vp.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


abstract class IRRemoteVPBaseActivity : AppCompatActivity() {

    var TAG = "IRRemoteVPBaseActivity"

    // tab titles
    private var titles: MutableList<String> = ArrayList()

    lateinit var progressDialog: Dialog


    var uIRelatedClass = UIRelatedClass()

    lateinit var frameContent: FrameLayout

    lateinit var sharedPreferences: SharedPreferences

    var selectedAppliance = "2"//by default tvp

    var tvSelectedBrand: String = ""
    var tvRemoteId: String = "0"

    var tvpSelectedBrand = ""
    var tvpRemoteId = ""


    var acSelectedBrand = ""



    var modelTvRemoteDetails = ModelRemoteDetails()

    var modelTvpRemoteDetails = ModelRemoteDetails()

    var modelAcRemoteDetails = ModelRemoteDetails()

    var gson: Gson? = Gson()


    // var selectedTabIndex: Int = 0//by default selecting tvp

    lateinit var apiViewModel: ApiViewModel

    companion object {
        var IRRemoteVPBaseActivity: IRRemoteVPBaseActivity? = null
    }

    var modelLdapi2AcModesList: MutableList<ModelLdapi2AcModes> = ArrayList()

    val LDAPI2_TIMOUT = 1

    var irButtonListTVTimerTask: Timer? = Timer()

    var irButtonListTVPTimerTask: Timer? = Timer()

    var irButtonListACTimerTask: Timer? = Timer()


    var modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

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
        //  buildrogressDialog()

        selectedAppliance = getSelectedAppliance().toString()

        // initializeUI()
    }

    fun initializeUI() {

        apiViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ApiViewModel::class.java)


        IRRemoteVPBaseActivity = this




        sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)


        //setApplianceInfoDetails()
    }


    fun hideTabLayout() {
        tabLayout.visibility = View.GONE
    }


    fun checkIfWeHaveWorkingTVRemoteButtonnsEmpty(): Boolean {
        if (getSharedPreferences("Mavid", Context.MODE_PRIVATE) != null) {
            if (getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("workingTVRemoteButtons", "")!!.isEmpty()) {
                return true
            }
        }
        return false
    }


    fun checkIfWeHaveWorkingTVPRemoteButtonsEmpty(): Boolean {
        if (getSharedPreferences("Mavid", Context.MODE_PRIVATE) != null) {
            if (getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("workingTVPRemoteButtons", "")!!.isEmpty()) {
                return true
            }
        }
        return false
    }

    fun checkIfWeHaveWorkingACRemoteButtonsEmpty(): Boolean {
        if (getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("workingACRemoteButtons", "")!!.isEmpty()) {
            return true
        }
        return false;
    }





    fun buildPayloadForLdapi2Ac(remoteId: Int): JSONObject {
        val payloadJsonObject: JSONObject = JSONObject()

        payloadJsonObject.put("ID", 2)

        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        dataJsonObject.put("appliance", 3)//tv//tvp//ac

        dataJsonObject.put("rId", remoteId)
        dataJsonObject.put("index", getSelectedRemoteIndex())
        payloadJsonObject.put("data", dataJsonObject)

        Log.d(TAG, "ldapi#2_payload".plus(payloadJsonObject.toString()))

        return payloadJsonObject
    }


    fun getDeviceLdapi2WorkingButtonListForAc(ipdAddress: String, remoteId: Int) {
        LibreMavidHelper.sendCustomCommands(ipdAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForLdapi2Ac(remoteId).toString(), object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {

                Log.d(TAG, "ldapi#2_response".plus(messageInfo?.message))

                try {
                    val responseJSONObject = JSONObject(messageInfo?.message)
                    val statusCode = responseJSONObject.getInt("Status")

                    when (statusCode) {
                        2 -> {

                            myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                            myHandler?.removeCallbacksAndMessages(null)
                            irButtonListACTimerTask?.cancel()

                            val payloadJsonObject = responseJSONObject.getJSONObject("payload")

                            var modesJsonArray = payloadJsonObject.getJSONArray("modes")

                            Log.d(TAG, "modes".plus(modesJsonArray.toString()))

                            modelLdapi2AcModesList = ArrayList()

                            for (i in 0 until modesJsonArray.length()) {
                                modelLdapi2AcModesList.add(parseLdapi2Response(modesJsonArray[i] as JSONObject))
                            }


                            val gson = Gson()

                            val workingRemoteButtonsString = gson.toJson(modelLdapi2AcModesList)

                            var editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                            editor.putString("workingACRemoteButtons", workingRemoteButtonsString)
                            editor.apply()

                        }
                    }

                } catch (e: JSONException) {
                    irButtonListACTimerTask?.cancel()
                    myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                    dismissLoader()
                    Log.d(TAG, "exeception:".plus(e.toString()))
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRRemoteVPBaseActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRRemoteVPBaseActivity)
                }
            }

            override fun failure(e: java.lang.Exception?) {
                irButtonListACTimerTask?.cancel()
                myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                dismissLoader()
                Log.d(TAG, "exeception:".plus(e.toString()))
                uiRelatedClass.buidCustomSnackBarWithButton(this@IRRemoteVPBaseActivity, "There sees to be an error!!.Please try after some time",
                        "Go Back", this@IRRemoteVPBaseActivity)
            }

            override fun success() {

            }

        })
    }


    fun parseLdapi2Response(jsonObject: JSONObject): ModelLdapi2AcModes {
        var modelLdapi2AcModes = ModelLdapi2AcModes()

        modelLdapi2AcModes.mode = jsonObject.getString("mode")
        modelLdapi2AcModes.isDefault = jsonObject.getBoolean("is_default")

        modelLdapi2AcModes.tempAllowed = jsonObject.getBoolean("temp_allowed")
        modelLdapi2AcModes.minTemp = jsonObject.getInt("min_temp")
        modelLdapi2AcModes.maxTemp = jsonObject.getInt("max_temp")

        modelLdapi2AcModes.speedAllowed = jsonObject.getBoolean("speed_allowed")
        modelLdapi2AcModes.directionAllowed = jsonObject.getBoolean("direction_allowed")

        modelLdapi2AcModes.swingAllowed = jsonObject.getBoolean("swing_allowed")

        return modelLdapi2AcModes
    }

    override fun onDestroy() {
        super.onDestroy()
        IRRemoteVPBaseActivity = null
    }

    fun hideApplianceVp() {
        appliancesSelectionVp.visibility = View.GONE
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

    /**
     * Call the LDAPI#2 every 5 seconds and check the status
     */
    fun timerTaskToReadDeviceStatusForTvAppliance(ipdAddress: String?, remoteId: Int, selectedApplianceType: String) {

        myHandler.sendEmptyMessageDelayed(LDAPI2_TIMOUT, 25000)
        irButtonListTVTimerTask!!.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "calling_Ldapi#2_every_5_secs")

                getButtonPayload(ipdAddress, remoteId, selectedApplianceType)

            }
        }, 0, 5000)
    }


    fun timerTaskToReadDeviceStatusForTvpAppliance(ipdAddress: String?, remoteId: Int, selectedApplianceType: String) {

        myHandler.sendEmptyMessageDelayed(LDAPI2_TIMOUT, 25000)
        irButtonListTVPTimerTask!!.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "calling_Ldapi#2_every_5_secs")

                getButtonPayload(ipdAddress, remoteId, selectedApplianceType)

            }
        }, 0, 5000)
    }


    fun getButtonPayload(ipdAddress: String?, remoteId: Int, selectedApplianceType: String) {
        LibreMavidHelper.sendCustomCommands(ipdAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForRemoteJsonListForLdapi2(remoteId, selectedApplianceType).toString(), object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo) {
                Log.d(TAG, "ldapi#2_" + messageInfo.message)
                try {
                    val responseJSONObject = JSONObject(messageInfo.message)
                    val statusCode = responseJSONObject.getInt("Status")
                    when (statusCode) {
                        3 -> {
                            myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                            myHandler.removeCallbacksAndMessages(null)

                            if (selectedApplianceType == "1" || selectedApplianceType == "TV") {
                                irButtonListTVTimerTask!!.cancel()
                            } else if (selectedApplianceType == "2" || selectedApplianceType == "TVP") {
                                irButtonListTVPTimerTask!!.cancel()
                            }

                            /** get the button list from the data json object  */
                            val payloadJsonObject = responseJSONObject.getJSONObject("payload")
                            val buttonJsonArray = payloadJsonObject.getJSONArray("keys")
                            Log.d(TAG, "buttonList: $buttonJsonArray")

                            workingRemoteButtonsHashMap = HashMap()

                            var i = 0
                            while (i < buttonJsonArray.length()) {
                                val buttonNameString = buttonJsonArray.getString(i)
                                workingRemoteButtonsHashMap[buttonNameString] = "1"
                                i++
                            }

                            //updating the appliance info in the app
                            updateWorkingRemoteButtons(selectedApplianceType)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (selectedApplianceType == "1" || selectedApplianceType == "TV") {
                        irButtonListTVTimerTask!!.cancel()
                    } else if (selectedApplianceType == "2" || selectedApplianceType == "TVP") {
                        irButtonListTVPTimerTask!!.cancel()
                    }
                    myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                    dismissLoader()
                    Log.d(TAG, "exeception:$e")
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRRemoteVPBaseActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", (this@IRRemoteVPBaseActivity))
                }
            }

            override fun failure(e: java.lang.Exception) {
                if (selectedApplianceType == "1" || selectedApplianceType == "TV") {
                    irButtonListTVTimerTask!!.cancel()
                } else if (selectedApplianceType == "2" || selectedApplianceType == "TVP") {
                    irButtonListTVPTimerTask!!.cancel()
                }
                myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                dismissLoader()
                Log.d(TAG, "exeception:$e")
                uiRelatedClass.buidCustomSnackBarWithButton(this@IRRemoteVPBaseActivity, "There sees to be an error!!.Please try after some time",
                        "Go Back", (this@IRRemoteVPBaseActivity))
            }

            override fun success() {}
        })
    }


    fun updateWorkingRemoteButtons(applianceType: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val workingRemoteButtonsString = gson!!.toJson(workingRemoteButtonsHashMap)
        if (applianceType == "1" || applianceType == "TV") {
            editor.putString("workingTVRemoteButtons", workingRemoteButtonsString)
        } else if (applianceType == "2" || applianceType == "TVP") {
            editor.putString("workingTVPRemoteButtons", workingRemoteButtonsString)
        } else {
            //Todo Ac needs to be implemented
        }
        dismissLoader()

        editor.apply()
    }

    /**
     * value: 2 for LDAPI#2 ie sending the remote api
     */
    fun buildPayloadForRemoteJsonListForLdapi2(remoteId: Int, selectedApplianceType: String): JSONObject {
        val payloadJsonObject = JSONObject()
        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
         * 2 : STB
         * 3 : AC</ID> */
        try {
            payloadJsonObject.put("ID", 2)
            if (selectedApplianceType == "1" || selectedApplianceType == "TV") {
                dataJsonObject.put("appliance", 1) //tv//tvp//ac
            } else if (selectedApplianceType == "2" || selectedApplianceType == "TVP") {
                dataJsonObject.put("appliance", 2) //tv//tvp//ac
            } else if (selectedApplianceType == "3" || selectedApplianceType == "AC") {
                dataJsonObject.put("appliance", 3) //tv//tvp//ac
            }
            dataJsonObject.put("rId", remoteId)
            dataJsonObject.put("index", getSelectedRemoteIndex())

            payloadJsonObject.put("data", dataJsonObject)
            Log.d(TAG, "ldapi#2_payload$payloadJsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return payloadJsonObject
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
        showProgressBar()

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


    override fun onBackPressed() {
        val fm: FragmentManager = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            appliancesSelectionVp.visibility = View.VISIBLE
            tabLayout.visibility = View.VISIBLE
            fm.popBackStackImmediate()

        } else {
            finish()
        }
    }
}
