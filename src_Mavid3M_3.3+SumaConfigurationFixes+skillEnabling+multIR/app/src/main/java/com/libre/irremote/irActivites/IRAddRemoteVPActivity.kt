package com.libre.irremote.irActivites

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.libre.irremote.R
import com.libre.irremote.adapters.ApplianceFragmentAdapter
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo
import com.libre.irremote.models.ModelLdapi2AcModes
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import kotlinx.android.synthetic.main.activity_ir_add_remote_vp.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class IRAddRemoteVPActivity : AppCompatActivity() {

    var TAG = IRAddRemoteVPActivity::class.java.simpleName

    // tab titles
    private var titles: MutableList<String> = ArrayList()

    lateinit var progressDialog: Dialog

    var bundle = Bundle()

    var deviceInfo: DeviceInfo? = null

    var uIRelatedClass = UIRelatedClass()

    lateinit var frameContent: FrameLayout

    lateinit var sharedPreferences: SharedPreferences

    var selectedAppliance = "2"//by default tvp

    var tvSelectedBrand: String = ""
    var tvRemoteId: String = "0"

    var tvpSelectedBrand = ""
    var tvpRemoteId = ""


    var acSelectedBrand = ""
    var acRemoteId = ""


    var modelTvRemoteDetails = ModelRemoteDetails()

    var modelTvpRemoteDetails = ModelRemoteDetails()

    var modelAcRemoteDetails = ModelRemoteDetails()

    var gson: Gson? = Gson()


    var selectedTabIndex: Int = 0//by default selecting tvp

    lateinit var apiViewModel: ApiViewModel

    companion object {
        var irAddRemoteVPActivity: IRAddRemoteVPActivity? = null
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
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddRemoteVPActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRAddRemoteVPActivity)
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_add_remote_vp)


        apiViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ApiViewModel::class.java)


        irAddRemoteVPActivity = this

        bundle = intent.extras!!

        buildrogressDialog()

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
            selectedTabIndex = bundle.getInt("selectedTabIndex", 0)
        }

        frameContent = findViewById(R.id.frameContent)


        frameContent.visibility = View.GONE

        sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)


        setApplianceInfoDetails()


    }


    fun setTabLayout() {
        titles.add("Set Top Box")

        titles.add("Television")

        titles.add("Ac")


        appliancesSelectionVp.adapter = ApplianceFragmentAdapter(this@IRAddRemoteVPActivity, titles.toList())

        // attaching tab mediator
        TabLayoutMediator(tabLayout, appliancesSelectionVp) { tab, position ->
            tab.setCustomView(R.layout.custom_tab_view_layout)

            val tvApplianceType = tab.customView?.findViewById<AppCompatTextView>(R.id.tvApplianceType)
            val tvBrandName = tab.customView?.findViewById<AppCompatTextView>(R.id.tvBrandName)

            val ivShowBrands = tab.customView?.findViewById<AppCompatImageView>(R.id.ivShowBrands)

            tvApplianceType?.text = titles[position]

            tvApplianceType?.isSelected = true
            tvBrandName?.isSelected = true

            when (position) {
                0 -> {
                    //tvp
                    tvBrandName?.text = tvpSelectedBrand
                    Log.d(TAG, "tvpBrand ".plus(tvpSelectedBrand))
                    ivShowBrands?.setOnClickListener {
                        val intent = Intent(this@IRAddRemoteVPActivity, IRTvpBrandActivity::class.java)
                        var bundle = Bundle()
                        bundle.putSerializable("deviceInfo", deviceInfo)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }
                1 -> {
                    //tv
                    tvBrandName?.text = tvSelectedBrand
                    ivShowBrands?.setOnClickListener {
                        val intent = Intent(this@IRAddRemoteVPActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
                        val bundle = Bundle()
                        bundle.putSerializable("deviceInfo", deviceInfo)
                        bundle.putBoolean("isTv", true)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }
                2 -> {
                    //ac
                    tvBrandName?.text = acSelectedBrand
                    ivShowBrands?.setOnClickListener {
                        val intent = Intent(this@IRAddRemoteVPActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
                        val bundle = Bundle()
                        bundle.putSerializable("deviceInfo", deviceInfo);
                        bundle.putBoolean("isAc", true)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }
            }
        }.attach()


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        selectedAppliance = "2" //tvp
                    }
                    1 -> {
                        selectedAppliance = "1"//tv
                    }

                    2 -> {
                        selectedAppliance = "3"//ac
                    }
                }
            }
        })

        appliancesSelectionVp.currentItem = selectedTabIndex
    }

    fun hideTabLayout() {
        tabLayout.visibility = View.GONE
    }


    fun setApplianceInfoDetails() {
        //getting from the local storage

        setDefaultApplianceInfo()

        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {
            //user has added data //might be tv.tvp.or.ac

            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()


            modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            if (modelRemoteSubAndMacDetils.mac == deviceInfo?.usn) {
                //data is present for the mavid device user has selected
                for (modelRemoteDetails: ModelRemoteDetails in modelRemoteSubAndMacDetils.modelRemoteDetailsList) {

                    if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {

                        modelTvRemoteDetails = modelRemoteDetails

                        tvRemoteId = modelTvRemoteDetails.remoteId

                        tvSelectedBrand = modelTvRemoteDetails.selectedBrandName
                        //tv data is there check if we have remoteButtons in internal storage
//                        if (checkIfWeHaveWorkingTVRemoteButtonnsEmpty()) {
//                            showProgressBar()
//                            Log.d(TAG, "tvRemoteId: $tvRemoteId" + "selectedAppliance" + modelRemoteDetails.selectedAppliance)
//
//                            timerTaskToReadDeviceStatusForTvAppliance(deviceInfo?.ipAddress, tvRemoteId.toInt(), modelRemoteDetails.selectedAppliance)
//                        }

                    } else if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {

                        modelTvpRemoteDetails = modelRemoteDetails

                        tvpRemoteId = modelTvpRemoteDetails.remoteId

                        tvpSelectedBrand = modelTvpRemoteDetails.selectedBrandName

//                        //tvp data is there check if we have remoteButtons in internal storage
//                        if (checkIfWeHaveWorkingTVPRemoteButtonsEmpty()) {
//                            showProgressBar()
//                            Log.d(TAG, "tvpRemoteId: $tvpRemoteId" + "selectedAppliance" + modelRemoteDetails.selectedAppliance)
//                            timerTaskToReadDeviceStatusForTvpAppliance(deviceInfo?.ipAddress, tvpRemoteId.toInt(), modelRemoteDetails.selectedAppliance)
//                        }

                    } else if (modelRemoteDetails.selectedAppliance == "3" || modelRemoteDetails.selectedAppliance == "AC") {
                        //ac
                        modelAcRemoteDetails = modelRemoteDetails
                        acRemoteId = modelRemoteDetails.remoteId
                        acSelectedBrand = modelAcRemoteDetails.selectedBrandName

                        //call ldapi#2 if in case ac remote brands are changed
                        //if there are no ac remote buttons in the app
//                        if (checkIfWeHaveWorkingACRemoteButtonsEmpty()) {
//                            showProgressBar()
//                            Log.d(TAG, "acRemoteId: $acRemoteId" + "selectedAppliance" + modelRemoteDetails.selectedAppliance)
//                            timerTaskToReadDeviceStatusForAcAppliance(deviceInfo?.ipAddress, acRemoteId.toInt(), modelRemoteDetails.selectedAppliance)
//                        }
                    }
                }

            } else {
                //diff mavid device ..so data might not be present
                setDefaultApplianceInfo()
            }
        } else {
            //user hasnt added any device
            setDefaultApplianceInfo()
        }

        runOnUiThread {
            setTabLayout()
        }

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


    fun setDefaultApplianceInfo() {
        tvpRemoteId = "0"
        tvpSelectedBrand = "Not Set"

        tvRemoteId = "0"
        tvSelectedBrand = "Not Set"

        acRemoteId = "0"
        acSelectedBrand = "Not Set"
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
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddRemoteVPActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRAddRemoteVPActivity)
                }
            }

            override fun failure(e: java.lang.Exception?) {
                irButtonListACTimerTask?.cancel()
                myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                dismissLoader()
                Log.d(TAG, "exeception:".plus(e.toString()))
                uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddRemoteVPActivity, "There sees to be an error!!.Please try after some time",
                        "Go Back", this@IRAddRemoteVPActivity)
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
        irAddRemoteVPActivity = null
    }

    fun hideApplianceVp() {
        appliancesSelectionVp.visibility = View.GONE
    }

    fun buildrogressDialog() {
        progressDialog = Dialog(this@IRAddRemoteVPActivity)
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


    fun timerTaskToReadDeviceStatusForAcAppliance(ipdAddress: String?, remoteId: Int, selectedApplianceType: String) {

        myHandler.sendEmptyMessageDelayed(LDAPI2_TIMOUT, 25000)
        irButtonListACTimerTask!!.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "calling_Ldapi#2_every_5_secs")

                getDeviceLdapi2WorkingButtonListForAc(ipdAddress!!, remoteId)

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
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddRemoteVPActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", (this@IRAddRemoteVPActivity))
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
                uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddRemoteVPActivity, "There sees to be an error!!.Please try after some time",
                        "Go Back", (this@IRAddRemoteVPActivity))
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
            payloadJsonObject.put("data", dataJsonObject)
            Log.d(TAG, "ldapi#2_payload$payloadJsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return payloadJsonObject
    }

    fun showSucessfullMessage() {
        uIRelatedClass?.buildSnackBarWithoutButton(this@IRAddRemoteVPActivity,
                window?.decorView!!.findViewById(android.R.id.content), "Successfully sent data to the device")
    }

    fun showErrorMessage() {
        uIRelatedClass?.buildSnackBarWithoutButton(this@IRAddRemoteVPActivity,
                window?.decorView!!.findViewById(android.R.id.content), "There was an error while sending data to the device")
    }

    /** LDApi#4*/
    private fun buiidJsonForSendingTheKeyPressed(apiId: Int,
                                                 applianceType: String, remoteId: String, keys: String): JSONObject {
        val paylodJsonObject = JSONObject()
        paylodJsonObject.put("ID", apiId)

        val dataJSONObject = JSONObject()
        dataJSONObject.put("appliance", applianceType.toInt())//type of the appliance
        dataJSONObject.put("rId", remoteId.toInt())//remote if the selected user

        var keysJsonArray = JSONArray()
        keysJsonArray.put(keys)

        dataJSONObject.put("keys", keysJsonArray)//button name ie pressed

        paylodJsonObject.put("data", dataJSONObject)

        return paylodJsonObject
    }


    /** LDApi#4*/
    fun sendTheKeysPressedIntoTheMavid3MDevice(keysPressed: String, onRemoteKeyPressedInterface: OnRemoteKeyPressedInterface) {
        showProgressBar()

        var remoteId = "0"
        //device acknowledged or device sucess response
        when (selectedAppliance) {
            //tv
            "1",
            "TV"
            -> {
                remoteId = tvRemoteId
            }
            "2",
            "TVP"
            -> {
                //tvp
                remoteId = tvpRemoteId
            }
            //ac
            "3",
            "AC"
            -> {
                remoteId = acRemoteId
            }
        }

        Log.d(TAG, "sendingRemoteButton: ".plus(buiidJsonForSendingTheKeyPressed(4,
                selectedAppliance, remoteId, keysPressed.plus(",1")).toString()))

        LibreMavidHelper.sendCustomCommands(deviceInfo?.ipAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buiidJsonForSendingTheKeyPressed(4, selectedAppliance, remoteId, keysPressed.plus(",1")).toString(),
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