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
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.libre.irremote.R
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo
import com.libre.irremote.models.ModelLdapi2AcModes
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import com.libre.irremote.utility.ApiConstants
import com.libre.irremote.utility.OnButtonClickCallback
import com.libre.irremote.utility.RestApiSucessFailureCallbacks
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.irActivites.irMultipleRemotes.IRAddOrSelectRemotesActivity
import kotlinx.android.synthetic.main.activity_layout_ir_restore_selection.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IRRestoreSelectionActivity : AppCompatActivity() {

    var bundle = Bundle()

    var deviceInfo: DeviceInfo? = null

    var applianceInfo: String? = null


    var gson = Gson()

    var modelRemoteDetailsList: MutableList<ModelRemoteDetails> = ArrayList()

    var index: Int = 0

    var selectedApplianceToViewOnTextView = ""

    lateinit var progressDialog: Dialog

    var uiRelatedClass = UIRelatedClass()

    val TAG = IRRestoreSelectionActivity::class.java.simpleName

    var modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

    var irButtonListTimerTask: Timer? = null


    private var LDAPI2_TIMOUT = 1

    var workingRemoteButtonsHashMap: HashMap<String, String> = HashMap()


    var modelLdapi2AcModesList: MutableList<ModelLdapi2AcModes> = ArrayList()


    val myHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what: Int = msg.what
            when (what) {
                LDAPI2_TIMOUT -> {
                    runOnUiThread {
                        dismissLoader()
                        irButtonListTimerTask?.cancel()
                        Log.d(TAG, "Error")
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRRestoreSelectionActivity, "There sees to be an error!!.Please try after some time",
                                "Go Back", this@IRRestoreSelectionActivity)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_ir_restore_selection)

        bundle = intent.extras!!

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
            applianceInfo = bundle.getString("applianceInfo")
        }

        var bodyJsonObject = JSONObject(applianceInfo)

        var sub = bodyJsonObject.getString("sub")

        Log.d(TAG, sub)

        if (bodyJsonObject.has("Appliance")) {

            val applianceObject: JSONObject? = bodyJsonObject.optJSONObject("Appliance")

            if (applianceObject != null) {
                //it is a json object
                val modelRemoteDetails: ModelRemoteDetails = parseApplianceJsonObject(applianceObject)!!
                modelRemoteDetailsList.add(modelRemoteDetails)

            } else {
                //it might be an array
                val applianceJsonArray: JSONArray? = bodyJsonObject.optJSONArray("Appliance")
                if (applianceJsonArray != null) {
                    for (i in 0 until applianceJsonArray.length()) {
                        //updating the tv or tvp details
                        val modelRemoteDetails: ModelRemoteDetails = parseApplianceJsonObject(applianceJsonArray[i] as JSONObject)!!
                        modelRemoteDetailsList.add(modelRemoteDetails)

                    }
                    Log.d(TAG, "remoteData".plus(modelRemoteDetailsList.size.toString()))
                }
            }
        }



        buildrogressDialog()


        updateSelectedApplianceTextView()


        btnNo.setOnClickListener {
            if (index < modelRemoteDetailsList.size) {
                showCustomAlertForDeleteConfirmation(modelRemoteDetailsList[index])
            }
        }


        btnYes.setOnClickListener {
            if (index < modelRemoteDetailsList.size) {
                sendRemoteDetailsToMavid3m(deviceInfo!!.ipAddress, modelRemoteDetailsList[index])
            }
        }


        btnProceed.setOnClickListener {
            val intent = Intent(this@IRRestoreSelectionActivity, IRAddOrSelectRemotesActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }

    }


    private fun updateSelectedApplianceTextView() {

        Log.d(TAG, "seletedAppliance".plus(index))

        if (index < modelRemoteDetailsList.size) {
            if ((modelRemoteDetailsList[index].selectedAppliance) == "TV" || (modelRemoteDetailsList[index].selectedAppliance == "1")) {
                selectedApplianceToViewOnTextView = "TV"
            } else if ((modelRemoteDetailsList[index].selectedAppliance == "TVP") || (modelRemoteDetailsList[index].selectedAppliance == "2")) {

                selectedApplianceToViewOnTextView = "Set Top Box"

            } else if ((modelRemoteDetailsList[index].selectedAppliance == "AC") || (modelRemoteDetailsList[index].selectedAppliance == "3")) {
                selectedApplianceToViewOnTextView = "AC"
            }

            selectedApplianceAndBrand.text = modelRemoteDetailsList[index].selectedBrandName.plus(" $selectedApplianceToViewOnTextView")
        }
    }

    private fun showCustomAlertForDeleteConfirmation(modelRemoteDetails: ModelRemoteDetails) {
        uiRelatedClass.showCustomAlertDialogForDeleteConfirmation(this@IRRestoreSelectionActivity, object : OnButtonClickCallback {
            override fun onClick(isSucess: Boolean) {
                if (isSucess) {
                    //call the delete api
                    showProgressBar()
                    deleteUserDevice(getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", "")!!,
                            deviceInfo!!.usn, modelRemoteDetails, object : RestApiSucessFailureCallbacks {
                        override fun onSucessFailureCallbacks(isSucess: Boolean, modelRemoteDetails: ModelRemoteDetails?) {
                            if (isSucess) {
                                //remove the data present in the app

                                deleteApplianceFromSharedPref(deviceInfo!!.usn, modelRemoteDetailsList[index])

                                //inc to appliance present in the list
                                index += 1

                                dismissLoader()
                                //check whether the remote config is done
                                checkWhetherRemoteConfigurationIsDone()

                                updateSelectedApplianceTextView()
                            }
                        }
                    })
                }
            }
        })
    }


    /** Call the LDAPI#2 every 3 seconds and check the status */
    fun timerTaskToReadDeviceStatus(remoteIndex: Int, ipdAddress: String, remoteId: Int, selectedApplianceType: String) {
        irButtonListTimerTask = Timer()
        myHandler.sendEmptyMessageDelayed(LDAPI2_TIMOUT, 25000);
        irButtonListTimerTask?.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "callingLdapi#2 every 5 secs")
                getButtonPayload(remoteIndex, ipdAddress, remoteId, selectedApplianceType)
            }

        }, 0, 5000)
    }


    /** value: 2 for LDAPI#2 ie sending the remote api*/
    fun buildPayloadForRemoteJsonListForLdapi2(remoteIndex: Int, remoteId: Int, selectedApplianceType: String): JSONObject {
        val payloadJsonObject: JSONObject = JSONObject()

        payloadJsonObject.put("ID", 2)

        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        if (selectedApplianceType == "1" || selectedApplianceType == "TV") {
            dataJsonObject.put("appliance", 1)//tv//tvp//ac
        } else if (selectedApplianceType == "2" || selectedApplianceType == "TVP") {
            dataJsonObject.put("appliance", 2)//tv//tvp//ac
        } else if (selectedApplianceType == "3" || selectedApplianceType == "AC") {
            dataJsonObject.put("appliance", 3)//tv//tvp//ac
        }

        dataJsonObject.put("rId", remoteId)
        dataJsonObject.put("index", remoteIndex)


        payloadJsonObject.put("data", dataJsonObject)

        Log.d(TAG, "ldapi#2_payload".plus(payloadJsonObject.toString()))

        return payloadJsonObject

    }


    /** LDAPI#2 */
    fun getButtonPayload(remoteIndex: Int, ipdAddress: String, remoteId: Int, selectedApplianceType: String) {
        LibreMavidHelper.sendCustomCommands(ipdAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForRemoteJsonListForLdapi2(remoteIndex, remoteId, selectedApplianceType).toString(), object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {
                Log.d(TAG, "ldapi#2_".plus(messageInfo?.message))
                try {
                    val responseJSONObject = JSONObject(messageInfo?.message)
                    val statusCode = responseJSONObject.getInt("Status")
                    when (statusCode) {

                        3,
                        2
                        -> {
                            myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                            myHandler?.removeCallbacksAndMessages(null)
                            irButtonListTimerTask?.cancel()
                            /** get the button list from the data json object */

                            val payloadJsonObject = responseJSONObject.getJSONObject("payload")

                            if (payloadJsonObject.has("keys")) {

                                val buttonJsonArray = payloadJsonObject.getJSONArray("keys")

                                Log.d(TAG, "buttonList: ".plus(buttonJsonArray.toString()))

                                workingRemoteButtonsHashMap = HashMap()

                                for (i in 0 until buttonJsonArray.length()) {
                                    val buttonNameString = buttonJsonArray[i] as String
                                    workingRemoteButtonsHashMap[buttonNameString] = "1"
                                }

                            } else if (payloadJsonObject.has("modes")) {

                                var modesJsonArray = payloadJsonObject.getJSONArray("modes")

                                Log.d(TAG, "modes".plus(modesJsonArray.toString()))

                                modelLdapi2AcModesList = ArrayList()

                                for (i in 0 until modesJsonArray.length()) {
                                    modelLdapi2AcModesList.add(parseLdapi2AcResponse(modesJsonArray[i] as JSONObject))
                                }
                            }
                            //updating the appliance info in the app

                            updateApplianceInfoInSharedPref(modelRemoteDetailsList[index], deviceInfo!!.usn)

                            //inc to next appliance from the list
                            index += 1

                            dismissLoader()

                            //check if the config is done
                            checkWhetherRemoteConfigurationIsDone()

                            updateSelectedApplianceTextView()
                        }

                    }
                } catch (e: JSONException) {
                    irButtonListTimerTask?.cancel()
                    myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                    dismissLoader()
                    Log.d(TAG, "exeception:".plus(e.toString()))
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRRestoreSelectionActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRRestoreSelectionActivity)
                }

            }

            override fun failure(e: java.lang.Exception?) {
                dismissLoader()
                Log.d(TAG, "sendingRemoteDetailsException$e")
            }

            override fun success() {

            }
        })
    }


    fun parseLdapi2AcResponse(jsonObject: JSONObject): ModelLdapi2AcModes {
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

    fun updateApplianceInfoInSharedPref(modelRemoteDetails: ModelRemoteDetails, macId: String) {

        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {

            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            if (modelRemoteSubAndMacDetils.mac == macId) {
                //update the appliance list  details in the list to the exsting device
                var index: Int = getRemoteIndexInPrefs(modelRemoteDetails)
                if (index >= 0) {
                    modelRemoteSubAndMacDetils.modelRemoteDetailsList.removeAt(index)
                    modelRemoteSubAndMacDetils.modelRemoteDetailsList.add(index, buidlRemoteDetails(modelRemoteDetails))
                } else
                    modelRemoteSubAndMacDetils.modelRemoteDetailsList.add(buidlRemoteDetails(modelRemoteDetails))
            } else {
                //new device
                modelRemoteSubAndMacDetils.sub = sharedPreferences!!.getString("sub", "")!!
                modelRemoteSubAndMacDetils.mac = macId
                var appllianceInfoList: MutableList<ModelRemoteDetails> = ArrayList()
                appllianceInfoList.add(buidlRemoteDetails(modelRemoteDetails))
                modelRemoteSubAndMacDetils.modelRemoteDetailsList = appllianceInfoList
            }
        } else {
            //new user and first device
            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils.sub = sharedPreferences!!.getString("sub", "")!!
            modelRemoteSubAndMacDetils.mac = macId
            val appllianceInfoList: MutableList<ModelRemoteDetails> = ArrayList()
            appllianceInfoList.add(buidlRemoteDetails(modelRemoteDetails))
            modelRemoteSubAndMacDetils.modelRemoteDetailsList = appllianceInfoList
        }



        var editor: SharedPreferences.Editor
        editor = sharedPreferences!!.edit()


        var workingRemoteButtonsString = ""

        if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {
            workingRemoteButtonsString = gson.toJson(workingRemoteButtonsHashMap)
            editor.putString("workingTVRemoteButtons", workingRemoteButtonsString)
        } else if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {
            workingRemoteButtonsString = gson.toJson(workingRemoteButtonsHashMap)
            editor.putString("workingTVPRemoteButtons", workingRemoteButtonsString)
        } else if (modelRemoteDetails.selectedAppliance == "3" || modelRemoteDetails.selectedAppliance == "AC") {
            workingRemoteButtonsString = gson.toJson(modelLdapi2AcModesList)
            editor.putString("workingACRemoteButtons", workingRemoteButtonsString)
        }



        modelRemoteDetailsString = gson.toJson(modelRemoteSubAndMacDetils)
        editor.putString("applianceInfoList", modelRemoteDetailsString)
        editor.apply()
    }

    private fun getRemoteIndexInPrefs(modelRemoteDetails: ModelRemoteDetails): Int {
        var index: Int = -1
        for (i in 0 until modelRemoteSubAndMacDetils.modelRemoteDetailsList.size) {
            if (modelRemoteDetails.index == modelRemoteSubAndMacDetils.modelRemoteDetailsList[i].index)
                return i
        }

        return index
    }


    fun buidlRemoteDetails(modelRemoteDetails: ModelRemoteDetails): ModelRemoteDetails {
        modelRemoteDetails.selectedAppliance = modelRemoteDetails.selectedAppliance
        if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {
            modelRemoteDetails.customName = "TV"//for now hardcoding the customa name
            modelRemoteDetails.remotesHashMap = workingRemoteButtonsHashMap
        } else if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {
            modelRemoteDetails.customName = "My Box"//for now hardcoding the customa name
            modelRemoteDetails.remotesHashMap = workingRemoteButtonsHashMap
        } else if (modelRemoteDetails.selectedAppliance == "3" || modelRemoteDetails.selectedAppliance == "AC") {
            modelRemoteDetails.customName = "AC"
            modelRemoteDetails.ac_remotelist = modelLdapi2AcModesList
        }

        modelRemoteDetails.groupdName = "Scene1"

        modelRemoteDetails.remoteId = modelRemoteDetails.remoteId


        modelRemoteDetails.selectedBrandName = modelRemoteDetails.selectedBrandName!!

        modelRemoteDetails.brandId = modelRemoteDetails.brandId

        return modelRemoteDetails
    }


    fun checkWhetherRemoteConfigurationIsDone() {
        runOnUiThread {
            if (index < modelRemoteDetailsList.size) {
                //show restore selection
                llRestoreAppliance.visibility = View.VISIBLE
                rlRemoteSuccessful.visibility = View.GONE
            } else {
                rlRemoteSuccessful.visibility = View.VISIBLE
                llRestoreAppliance.visibility = View.GONE
            }
        }
    }

    fun buildJsonForUserManagmentApis(sub: String, macAddress: String,
                                      modelRemoteDetails: ModelRemoteDetails, operation: String): JSONObject {
        var payLoadObject: JSONObject = JSONObject()


        //payLoadObject.put("GroupID", modelRemoteDetails.groupId.toString())


        payLoadObject.put("GroupName", modelRemoteDetails.groupdName)
        payLoadObject.put("BrandName", modelRemoteDetails.selectedBrandName)
        if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {
            payLoadObject.put("Appliance", "TV")
        } else if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {
            payLoadObject.put("Appliance", "TVP")
        } else if ((modelRemoteDetails.selectedAppliance == "3" || modelRemoteDetails.selectedAppliance == "AC")) {
            payLoadObject.put("Appliance", "AC")
        }
        payLoadObject.put("CustomName", modelRemoteDetails.customName)
        payLoadObject.put("RemoteID", modelRemoteDetails.remoteId)
        payLoadObject.put("index", modelRemoteDetails.index.toString())
        payLoadObject.put("BrandID", modelRemoteDetails.brandId)



        var bodyObject: JSONObject = JSONObject()



        bodyObject.put("operation", operation)
        bodyObject.put("sub", sub)
        bodyObject.put("Mac", macAddress)
        bodyObject.put("payload", payLoadObject)

        Log.d(TAG, "body: ".plus(bodyObject))

        return bodyObject
    }


    fun deleteUserDevice(sub: String, macAddress: String,
                         modelRemoteDetails: ModelRemoteDetails?, restApiSucessFailureCallbacks: RestApiSucessFailureCallbacks) {

        if (modelRemoteDetails != null) {

            val requestQueue = Volley.newRequestQueue(this@IRRestoreSelectionActivity)

            val url = ApiConstants.BASE_URL_USER_MGT + "Beta/usermangement"

            Log.d(TAG, "deleteBody_IR_RestoreSelectionActivity" + buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString())

            var requestBody: String = buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString()


            var deleteUserDetailsStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->


                val responseObject = JSONObject(response)


                if (responseObject.has("body")) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRRestoreSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), responseObject.getString("body"))
                }

                restApiSucessFailureCallbacks.onSucessFailureCallbacks(true, modelRemoteDetails)

                Log.d(TAG, "deleteResponse:".plus(response))


            }, Response.ErrorListener { volleyError ->

                restApiSucessFailureCallbacks.onSucessFailureCallbacks(false, null)

                Log.d(TAG, "Error: ${volleyError.networkResponse.statusCode}")

                dismissLoader()

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRRestoreSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRRestoreSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(this@IRRestoreSelectionActivity,
                                window?.decorView!!.findViewById(android.R.id.content), "Server error occurred, please try again later")
                    }

                } else if (volleyError is NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRRestoreSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Network error occurred, please try again later")

                } else if (volleyError is ParseError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRRestoreSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Parser error occurred, please try again later")
                }
            }) {
                override fun getBodyContentType(): String? {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.forName("utf-8"));
                }
            }

            deleteUserDetailsStringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue.add(deleteUserDetailsStringRequest)
        } else {
            restApiSucessFailureCallbacks.onSucessFailureCallbacks(true, modelRemoteDetails)
        }
    }

    fun buildrogressDialog() {
        progressDialog = Dialog(this@IRRestoreSelectionActivity)
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
            progressDialog.show()
        }
    }

    fun dismissLoader() {
        runOnUiThread {
            progressDialog.dismiss()
        }

    }


    /** value: 1 for LDAPI#1 ie sending the remote api*/
    fun buildRemotePayloadJson(modelRemoteDetails: ModelRemoteDetails): JSONObject {
        val payloadJsonObject: JSONObject = JSONObject()
        payloadJsonObject.put("ID", 1)

        val dataJsonObject = JSONObject()

        /**Now we have only one set of devices,
        tomorrow we might give provision for user
        to add another group, say group#2 with second TV, STB..etc. For now keep it as 1*/
        //dataJsonObject.put("group", 1)

        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        when (modelRemoteDetails.selectedAppliance) {
            "TV",
            "1"
            -> {
                dataJsonObject.put("appliance", 1)
            }
            "TVP",
            "2"
            -> {
                dataJsonObject.put("appliance", 2)
            }
            "AC",
            "3"
            -> {
                dataJsonObject.put("appliance", 3)
            }
        }

        dataJsonObject.put("bName", modelRemoteDetails.selectedBrandName)
        dataJsonObject.put("bId", modelRemoteDetails.brandId.toInt())
        dataJsonObject.put("rId", modelRemoteDetails.remoteId.toInt())
        dataJsonObject.put("index", modelRemoteDetails.index)
        payloadJsonObject.put("data", dataJsonObject)


        return payloadJsonObject
    }

    /** LDAPI#1 */
    fun sendRemoteDetailsToMavid3m(ipAddress: String, modelRemoteDetails: ModelRemoteDetails) {
        showProgressBar()
        Log.d(TAG, "ldapiPayload: ".plus(buidlRemoteDetails(modelRemoteDetails)))

        val payloadString = buildRemotePayloadJson(modelRemoteDetails).toString()
        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST, payloadString,
                object : CommandStatusListenerWithResponse {
                    override fun response(messageInfo: MessageInfo?) {
                        if (messageInfo != null) {

                            val dataJsonObject = JSONObject(messageInfo?.message)

                            val status = dataJsonObject.getInt("Status")

                            //device acknowledged
                            if (status == 2) {
                                Log.d(TAG, "response_from_ldapi1" + messageInfo?.message)

                                timerTaskToReadDeviceStatus(modelRemoteDetails.index, ipAddress, modelRemoteDetails.remoteId.toInt(), modelRemoteDetails.selectedAppliance)

                            } else if (status == 3) {
                                //error
                                dismissLoader()
                                uiRelatedClass.buidCustomSnackBarWithButton(this@IRRestoreSelectionActivity, "There sees to be an error!!.Please try after some time",
                                        "Go Back", this@IRRestoreSelectionActivity)
                            }
                        } else {
                            dismissLoader()
                            uiRelatedClass.buidCustomSnackBarWithButton(this@IRRestoreSelectionActivity, "There sees to be an error!!.Please try after some time",
                                    "Go Back", this@IRRestoreSelectionActivity)
                        }
                    }


                    override fun failure(e: Exception?) {
                        dismissLoader()
                        Log.d(TAG, "sendingRemoteDetailsException$e")
                    }

                    override fun success() {

                    }
                })
    }

    private fun deleteApplianceFromSharedPref(macId: String, modelRemoteDetails: ModelRemoteDetails?) {
        if (modelRemoteDetails != null) {

            var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

            var gson = Gson()

            var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

            if (modelRemoteDetailsString!!.isNotEmpty()) {
                //if data is present
                modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()


                modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                        ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils


                if (modelRemoteSubAndMacDetils.mac == macId) {
                    val appllianceList = modelRemoteSubAndMacDetils.modelRemoteDetailsList

                    val iterator = appllianceList.iterator()

                    while (iterator.hasNext()) {
                        val storedRemoteDetails: ModelRemoteDetails = iterator.next()

                        if (storedRemoteDetails.remoteId == modelRemoteDetails?.remoteId
                                && storedRemoteDetails.brandId == modelRemoteDetails?.brandId) {
                            Log.d(TAG, "deletedFromSharedPref".plus(storedRemoteDetails.remoteId))
                            iterator.remove()
                        }
                    }
                }
                //saving the details
                modelRemoteDetailsString = gson.toJson(modelRemoteSubAndMacDetils)
                var editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putString("applianceInfoList", modelRemoteDetailsString)
                editor.apply()
            }
        }
    }


    private fun parseApplianceJsonObject(applianceObject: JSONObject): ModelRemoteDetails? {
        val modelRemoteDetails = ModelRemoteDetails()
        try {
            modelRemoteDetails.selectedBrandName = applianceObject.getString("BrandName")
            modelRemoteDetails.remoteId = applianceObject.getString("RemoteID")
            modelRemoteDetails.brandId = applianceObject.getString("BrandID")
            modelRemoteDetails.index = applianceObject.getString("index").toInt()
            when {
                applianceObject["Appliance"] == "TV" -> {
                    modelRemoteDetails.selectedAppliance = "1"
                }
                applianceObject["Appliance"] == "TVP" -> {
                    modelRemoteDetails.selectedAppliance = "2"
                }
                applianceObject["Appliance"] == "AC" -> {
                    modelRemoteDetails.selectedAppliance = "3"
                }
            }
            modelRemoteDetails.customName = applianceObject.getString("CustomName")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return modelRemoteDetails
    }

}
