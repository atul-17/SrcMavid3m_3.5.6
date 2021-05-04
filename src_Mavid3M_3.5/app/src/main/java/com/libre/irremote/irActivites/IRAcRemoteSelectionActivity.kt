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
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.libre.irremote.Constants.RemoteIndexGetter
import com.libre.irremote.R
import com.libre.irremote.irActivites.irMultipleRemotes.IRAcApplianceActivity
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo
import com.libre.irremote.models.ModelLdapi2AcModes
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import com.libre.irremote.models.ModelSelectAcRemotePayload
import com.libre.irremote.utility.*
import com.libre.irremote.viewmodels.ApiViewModel
import com.libre.irremote.irActivites.irMultipleRemotes.IRAddOrSelectRemotesActivity
import com.libre.irremote.irActivites.irMultipleRemotes.IRTVPApplianceActivity
import com.libre.irremote.irActivites.irMultipleRemotes.IRTelevisionApplianceActivity
import kotlinx.android.synthetic.main.activity_ir_ac_remote_selection.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IRAcRemoteSelectionActivity : AppCompatActivity() {

    var applianceId = 0

    var applianceName = ""

    lateinit var progressDialog: Dialog

    lateinit var apiViewModel: ApiViewModel

    val uiRelatedClass = UIRelatedClass()

    var deviceInfo: DeviceInfo? = null

    var selectedApplianceType = ""

    var customNamesHashMap: HashMap<String, String> = HashMap()

    var ipAddress = ""

    var index: Int = 0

    var modelAcRemoteConfigurationsList: MutableList<ModelSelectAcRemotePayload> = ArrayList()

    val TAG = IRAcRemoteSelectionActivity::class.java.simpleName

    var modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

    var isUserClickedOnPowerOn = false

    var irButtonListTimerTask: Timer? = null

    var remoteIndex: Int = 1


//    var workingRemoteButtonsHashMap: HashMap<String, String> = HashMap()

    var modelLdapi2AcModesList: MutableList<ModelLdapi2AcModes> = ArrayList()


    private var LDAPI2_TIMOUT = 1

    val myHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what: Int = msg.what
            when (what) {
                LDAPI2_TIMOUT -> {
                    runOnUiThread {
                        dismissLoader()
                        irButtonListTimerTask?.cancel()
                        Log.d(TAG, "Error")
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                                "Go Back", this@IRAcRemoteSelectionActivity)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_ac_remote_selection)
        initializeUI()
    }

    fun initializeUI() {

        if (intent.extras != null) {
            applianceId = intent!!.extras!!.getInt("applianceId")
            applianceName = intent!!.extras!!.getString("applianceBrandName", "")
            ipAddress = intent!!.extras!!.getString("ipAddress", "")

            deviceInfo = intent!!.extras!!.getSerializable("deviceInfo") as DeviceInfo

            selectedApplianceType = intent!!.extras!!.getString("selectedApplianceType", "1")//tv

        }
        apiViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ApiViewModel::class.java)



        buildrogressDialog()
        showProgressBar()

        getAcRemoteSelectionData(applianceId)


        btnRemoteButtonName.setOnClickListener {
            addOnclickListener(btnRemoteButtonName.text.toString())
        }


        llGoBack.setOnClickListener {
            finish()
        }
    }

    fun addOnclickListener(text: String) {
        showProgressBar()
        if (index < modelAcRemoteConfigurationsList.size) {
            if (!isUserClickedOnPowerOn) {
                sendCommandToMavidDevice(applianceId, modelAcRemoteConfigurationsList[index].powerOnOrIrCommand, ipAddress, object : OnMavid3mAckTheCommandInterface {
                    override fun onAcknowledgment(status: String) {
                        if (status == "2") {
                            //sucess
                            showCustomAlertForACRemoteSelection(text, object : OnUserButtonSelection {
                                override fun didTheCommandWork(value: Boolean) {
                                    dismissLoader()
                                    if (value) {
                                        //ie the power on button workked
                                        //then wait for the user to check power off button in the same configuration
                                        isUserClickedOnPowerOn = true
                                        setNosOfConfigs(index, "Power Off", false)
                                    } else {

                                        //powerOnOff ir command did not work
                                        //check agai with another config
                                        isUserClickedOnPowerOn = false
                                        index++
                                        setNosOfConfigs(index, "Power On", false)
                                    }
                                }
                            })
                        }
                    }
                })
            } else {
                sendCommandToMavidDevice(applianceId, modelAcRemoteConfigurationsList[index].powerOff, ipAddress, object : OnMavid3mAckTheCommandInterface {
                    override fun onAcknowledgment(status: String) {
                        showCustomAlertForACRemoteSelection(btnRemoteButtonName.text.toString(), object : OnUserButtonSelection {
                            override fun didTheCommandWork(value: Boolean) {
                                dismissLoader()
                                if (value) {
                                    //if the second command ie the power off also worked
                                    //now pass the id to the device
                                    isUserClickedOnPowerOn = true
                                    btnRemoteButtonName.visibility = View.GONE
                                    tvNosOfConfigs.visibility = View.GONE

//                                    if (!checkIfTheUserSelectedRemoteIsPrevSelected(this@IRAcRemoteSelectionActivity.selectedApplianceType,
//                                                    modelAcRemoteConfigurationsList[index].remoteId.toInt(), this@IRAcRemoteSelectionActivity.applianceId, deviceInfo!!.usn)) {

                                        /**show the user an options to edit custom for the appliance he has configured
                                         * *
                                         * */
                                        getAppliancesListFromAllDevicesTheUserHasConfigured(getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("sub", "")!!,
                                                object : OnCallingGetApiToGetCustomNames {
                                                    override fun onResponse(userAddedCustomNamesHashMap: HashMap<String, String>) {
                                                        /**
                                                         * Filter out names which
                                                         * are already used by the
                                                         * user in another device
                                                         * */
                                                        val filtredHashMap: HashMap<String, String> =
                                                                filterOutDuplicateNamesWhichAreUsedByTheUser(userAddedCustomNamesHashMap, addPreDefinedPopularOptionsCustomName(selectedApplianceType,
                                                                        applianceName))
                                                        dismissLoader()
                                                        //show the dialog to edit custom name
                                                        uiRelatedClass.showBottomDialogForAddingCustomName(this@IRAcRemoteSelectionActivity, object : OnButtonClickCallbackWithStringParams {
                                                            override fun onUserClicked(userPassedInfo: String) {
                                                                /**need to call the ldapi#1
                                                                 * */

                                                                sendRemoteDetailsToMavidDevUsingLdapi1(ipAddress, modelAcRemoteConfigurationsList[index].remoteId.toInt(), userPassedInfo)
                                                            }
                                                        }, filtredHashMap, userAddedCustomNamesHashMap,
                                                                selectedApplianceType, applianceName, deviceInfo!!.usn)
                                                    }
                                                })
                                   // }


//                                    else {
//                                        /** show a dialog to inform the user
//                                         * he has prev selected the same remote id
//                                         * */
//                                        runOnUiThread {
//                                            dismissLoader()
//                                            uiRelatedClass.showUserCustomDialogForPrevSelectedRemote(this@IRAcRemoteSelectionActivity, object : OnButtonClickCallback {
//                                                override fun onClick(isSucess: Boolean) {
//                                                    gotoNextActivity(null, 0, null, true)
//                                                }
//                                            })
//                                        }
//                                    }


                                } else {
                                    //power off did not work
                                    //inc the index
                                    //check again with another config start from power on
                                    isUserClickedOnPowerOn = false
                                    index++
                                    setNosOfConfigs(index, "Power On", false)
                                }
                            }
                        })
                    }
                })
            }
        }
    }


    fun setNosOfConfigs(index: Int, buttonName: String, isCalledOnCreate: Boolean) {
        llRemoteSelection.visibility = View.VISIBLE
        llNoData.visibility = View.GONE
        llGoBack.visibility = View.GONE
        if (index < modelAcRemoteConfigurationsList.size) {
            tvNosOfConfigs.visibility = View.VISIBLE
            btnRemoteButtonName.visibility = View.VISIBLE

            btnRemoteButtonName.text = buttonName

            tvNosOfConfigs.text = "Checking available configurations ${index.plus(1)}/${modelAcRemoteConfigurationsList.size}"

            if (!isCalledOnCreate) {
                if (buttonName.equals("Power On", true)) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Try again with the next configuration")
                } else {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Now,try Power Off button")
                }
            }

        } else {
            btnRemoteButtonName.visibility = View.GONE
            tvNosOfConfigs.visibility = View.GONE
            uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity,
                    "No suitable remotes found, contact Customer care",
                    "OK",
                    this@IRAcRemoteSelectionActivity
            )
        }
    }


    /** LDAPI#3*/
    fun sendCommandToMavidDevice(brandId: Int, iRCommand: String, ipdAddress: String, onMavid3mAckTheCommandInterface: OnMavid3mAckTheCommandInterface) {
        showProgressBar()
        LibreMavidHelper.sendCustomCommands(ipdAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadToSendCommandToTheDevice(brandId, iRCommand),
                object : CommandStatusListenerWithResponse {
                    override fun response(messageInfo: MessageInfo?) {
                        dismissLoader()
                        if (messageInfo != null) {
                            val dataJsonObject = JSONObject(messageInfo?.message)

                            Log.d(TAG, "ldapi#3_Response".plus(dataJsonObject))
                            val status = dataJsonObject.getInt("Status")

                            if (status == 2) {//device acknowledged

                                onMavid3mAckTheCommandInterface.onAcknowledgment(status.toString())

                            } else if (status == 3) {
                                onMavid3mAckTheCommandInterface.onAcknowledgment(status.toString())
                                //error
                                uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                                        "Go Back", this@IRAcRemoteSelectionActivity)
                            }
                        } else {
                            onMavid3mAckTheCommandInterface.onAcknowledgment("3")//error
                            uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                                    "Go Back", this@IRAcRemoteSelectionActivity)
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


    /** value: 3 for LDAPI#3 ie sending the remote api*/
    fun buildPayloadToSendCommandToTheDevice(brandId: Int, remoteCommand: String): String {
        val payloadJsonObject: JSONObject = JSONObject()
        payloadJsonObject.put("ID", 3)


        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        dataJsonObject.put("appliance", 3)//tv/tvp//ac

        dataJsonObject.put("index", remoteIndex)

        dataJsonObject.put("selectId", brandId)
        dataJsonObject.put("IrCode", remoteCommand)

        payloadJsonObject.put("data", dataJsonObject)

        Log.d(TAG, "ldapi3_data_params".plus(payloadJsonObject.toString()))

        return payloadJsonObject.toString()
    }


    fun getDeviceLdapi2WorkingButtonListForAc(ipdAddress: String, remoteId: Int, customName: String) {
        LibreMavidHelper.sendCustomCommands(ipdAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForLdapi2(remoteId).toString(), object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {

                Log.d(TAG, "ldapi#2_response".plus(messageInfo?.message))

                try {
                    val responseJSONObject = JSONObject(messageInfo?.message)
                    val statusCode = responseJSONObject.getInt("Status")

                    when (statusCode) {
                        2 -> {

                            myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                            myHandler?.removeCallbacksAndMessages(null)
                            irButtonListTimerTask?.cancel()

                            val payloadJsonObject = responseJSONObject.getJSONObject("payload")

                            var modesJsonArray = payloadJsonObject.getJSONArray("modes")

                            Log.d(TAG, "modes".plus(modesJsonArray.toString()))

                            modelLdapi2AcModesList = ArrayList()

                            for (i in 0 until modesJsonArray.length()) {
                                modelLdapi2AcModesList.add(parseLdapi2Response(modesJsonArray[i] as JSONObject))
                            }

                            postUserManagment(getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("sub", "")!!,
                                    deviceInfo!!.usn, buidlRenoteDetails(remoteId, customName))

                            /* //deleting from the app storage
                             deleteUserDevice(getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("sub", "")!!,
                                     deviceInfo!!.usn,
                                     getRemoteDetailsFromSharedPrefThatNeedsToBeDeleted(selectedApplianceType, deviceInfo!!.usn),
                                     object : RestApiSucessFailureCallbacks {
                                         override fun onSucessFailureCallbacks(isSucess: Boolean, modelRemoteDetails: ModelRemoteDetails?) {

                                             Log.d(TAG, "newSelectedAppliance".plus(selectedApplianceType))

                                             //need to delete the old appliance data ie tv/tvp/ac
                                             //post the newly selected applaince

                                             //deleteApplianceFromSharedPref(deviceInfo!!.usn, modelRemoteDetails)

                                             postUserManagment(getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("sub", "")!!,
                                                     deviceInfo!!.usn, buidlRenoteDetails(remoteId, customName))
                                         }
                                     })*/
                        }
                    }

                } catch (e: JSONException) {
                    irButtonListTimerTask?.cancel()
                    myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                    dismissLoader()
                    Log.d(TAG, "exeception:".plus(e.toString()))
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRAcRemoteSelectionActivity)
                }
            }

            override fun failure(e: java.lang.Exception?) {
                irButtonListTimerTask?.cancel()
                myHandler.removeCallbacksAndMessages(LDAPI2_TIMOUT)
                dismissLoader()
                Log.d(TAG, "exeception:".plus(e.toString()))
                uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                        "Go Back", this@IRAcRemoteSelectionActivity)
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

    override fun onBackPressed() {
        myHandler.removeCallbacksAndMessages(null)
        super.onBackPressed()
    }

    fun buildPayloadForLdapi2(remoteId: Int): JSONObject {
        val payloadJsonObject: JSONObject = JSONObject()

        payloadJsonObject.put("ID", 2)

        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        dataJsonObject.put("appliance", selectedApplianceType.toInt())//tv//tvp//ac

        dataJsonObject.put("rId", remoteId)
        dataJsonObject.put("index", remoteIndex)

        payloadJsonObject.put("data", dataJsonObject)

        Log.d(TAG, "ldapi#2_payload".plus(payloadJsonObject.toString()))

        return payloadJsonObject
    }

    fun getAcRemoteSelectionData(applianceId: Int) {
        apiViewModel.getAcSelectJsonDetails(applianceId)?.observe(this, Observer {
            if (it.modelSelecAcRemotePayload != null) {
                if (it.modelSelecAcRemotePayload!!.size > 0) {
                    llRemoteSelection.visibility = View.VISIBLE
                    llNoData.visibility = View.GONE

                    modelAcRemoteConfigurationsList = it.modelSelecAcRemotePayload!!

                    if (modelAcRemoteConfigurationsList.size == 1) {
                        //no data
                        if (modelAcRemoteConfigurationsList[index].remoteId == "0") {
                            //no data present
                            llNoData.visibility = View.VISIBLE
                            llGoBack.visibility = View.VISIBLE
                            llRemoteSelection.visibility = View.GONE
                        } else {
                            setNosOfConfigs(index, "Power On", true)//intially 0
                        }
                    } else {
                        setNosOfConfigs(index, "Power On", true)//intially 0
                    }

                    dismissLoader()

                } else {
                    llNoData.visibility = View.VISIBLE
                    llGoBack.setOnClickListener {
                        finish()
                    }

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window.decorView.findViewById(android.R.id.content), "No Data present at the moment for the particular appliance")
                }
            } else {
                val volleyError = it?.volleyError

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "Seems your internet connection is slow, please try in sometime",
                            "Go Back", this@IRAcRemoteSelectionActivity)

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "AuthFailure error occurred, please try again later",
                            "Go Back", this@IRAcRemoteSelectionActivity)

                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {

                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "Server error occurred, please try again later",
                                "Go Back", this@IRAcRemoteSelectionActivity)
                    }

                } else if (volleyError is NetworkError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "Network error occurred, please try again later",
                            "Go Back", this@IRAcRemoteSelectionActivity)


                } else if (volleyError is ParseError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "Parser error occurred, please try again later",
                            "Go Back", this@IRAcRemoteSelectionActivity)

                } else {
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "SomeThing is wrong!!.Please Try after some timer",
                            "Go Back", this@IRAcRemoteSelectionActivity)
                }
            }
        })
    }

    fun buildrogressDialog() {
        progressDialog = Dialog(this@IRAcRemoteSelectionActivity)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_bar)
        progressDialog.setCancelable(false)

        val progress_title: AppCompatTextView = progressDialog.findViewById(R.id.progress_title)
        val progress_bar: ProgressBar = progressDialog.findViewById(R.id.progress_bar)
        val progress_message: AppCompatTextView = progressDialog.findViewById(R.id.progress_message)

        progress_message.visibility = View.GONE
        progress_title.text = "Please Wait..."
    }


    fun showCustomAlertForACRemoteSelection(buttonName: String, onUserButtonSelection: OnUserButtonSelection) {
        runOnUiThread {
            val alert = Dialog(this@IRAcRemoteSelectionActivity)
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert.setContentView(R.layout.custom_user_button_selection_alert)

            alert.setCancelable(false)


            val tvAlertMessage = alert.findViewById<AppCompatTextView>(R.id.tvAlertMessage)
            var applianceType = "AC"


            if (buttonName.equals("Power Off", true)) {

                tvAlertMessage.text = "Does the $applianceType Power OFF?"
            } else {
                //for non power button
                tvAlertMessage.text = "Does $applianceType Power On?"
            }
            val btnNo = alert.findViewById<AppCompatButton>(R.id.btnNo)
            btnNo.setOnClickListener {
                alert.dismiss()
                onUserButtonSelection.didTheCommandWork(false)
            }

            val btnYes = alert.findViewById<AppCompatButton>(R.id.btnYes)
            btnYes.setOnClickListener {
                //button worked
                //inc level
                alert.dismiss()
                onUserButtonSelection.didTheCommandWork(true)
            }
            alert.show()
        }
    }


    /** value: 1 for LDAPI#1 ie sending the remote api*/
    fun buildRemotePayloadJson(remoteId: Int): JSONObject {
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

        remoteIndex = RemoteIndexGetter.getNextRemoteIndex(this, "3")

        dataJsonObject.put("appliance", 3)//tv//tvp//ac
        dataJsonObject.put("index", remoteIndex)

        dataJsonObject.put("bName", applianceName)
        dataJsonObject.put("bId", applianceId)
        dataJsonObject.put("rId", remoteId)

        payloadJsonObject.put("data", dataJsonObject)

        Log.d(TAG, "ldapi#1_response".plus(payloadJsonObject))

        return payloadJsonObject
    }


    fun checkIfTheUserSelectedRemoteIsPrevSelected(userSelectedAppliance: String, userSelectedRemoteId: Int, userSelectedBrandId: Int, macId: String): Boolean {
        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)
        var gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {
            //data is present

            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            if (modelRemoteSubAndMacDetils.mac == macId) {
                for (modelRemoteDetails: ModelRemoteDetails in modelRemoteSubAndMacDetils.modelRemoteDetailsList) {
                    if (modelRemoteDetails.brandId == userSelectedBrandId.toString()) {
                        if (modelRemoteDetails.selectedAppliance == userSelectedAppliance
                                && modelRemoteDetails.remoteId == userSelectedRemoteId.toString()) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /** Call the LDAPI#2 every 5 seconds and check the status */
    fun timerTaskToReadDeviceStatus(ipdAddress: String, remoteId: Int, customName: String) {
        irButtonListTimerTask = Timer()
        myHandler.sendEmptyMessageDelayed(LDAPI2_TIMOUT, 25000);
        showProgressBar()

        irButtonListTimerTask?.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "callingLdapi#2 every 5 secs")
                getDeviceLdapi2WorkingButtonListForAc(ipdAddress, remoteId, customName)
            }

        }, 0, 5000)
    }

    /** LDAPI#1 */
    fun sendRemoteDetailsToMavidDevUsingLdapi1(ipAddress: String, remoteId: Int, customName: String) {
        val payloadString = buildRemotePayloadJson(remoteId).toString()
        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST, payloadString,
                object : CommandStatusListenerWithResponse {
                    override fun response(messageInfo: MessageInfo?) {
                        if (messageInfo != null) {

                            val dataJsonObject = JSONObject(messageInfo?.message)

                            val status = dataJsonObject.getInt("Status")

                            if (status == 2) {//device acknowledged
                                //call ldapi2 to get button list

                                timerTaskToReadDeviceStatus(ipAddress, remoteId, customName)

                            } else if (status == 3) {
                                //error
                                dismissLoader()
                                uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                                        "Go Back", this@IRAcRemoteSelectionActivity)
                            }
                        } else {
                            dismissLoader()
                            uiRelatedClass.buidCustomSnackBarWithButton(this@IRAcRemoteSelectionActivity, "There sees to be an error!!.Please try after some time",
                                    "Go Back", this@IRAcRemoteSelectionActivity)
                        }
                    }


                    override fun failure(e: Exception?) {
                        dismissLoader()
                        Log.d(TAG, "sendingRemoteDetailsException$e")
                    }

                    override fun success() {
                        dismissLoader()
                    }
                })
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
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    fun gotoNextActivity(ac_remotelist: MutableList<ModelLdapi2AcModes>?, remoteIndex: Int, remoteId: String?, isUserPrevSelectedRemote: Boolean) {
        dismissLoader()

        IRSelectTvOrTVPOrAcRegionalBrandsActivity.irSelectTvOrTVPOrAcRegionalBrandsActivity?.finish()
        IRTvpBrandActivity.irTvpBrandActivity?.finish()


        var bundle = Bundle()

        bundle.putSerializable("deviceInfo", deviceInfo)

        var intent: Intent? = null

        val gson = Gson()

        if (isUserPrevSelectedRemote) {

            intent = Intent(this@IRAcRemoteSelectionActivity, IRAddOrSelectRemotesActivity::class.java)

        } else {

            bundle.putInt("remoteIndex", remoteIndex)
            bundle.putString("remoteId", remoteId)
            bundle.putString("workingRemoteData", gson.toJson(ac_remotelist))

            intent = Intent(this@IRAcRemoteSelectionActivity, IRAcApplianceActivity::class.java)

        }

        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }


    fun buidlRenoteDetails(remoteId: Int, customName: String): ModelRemoteDetails {
        var modelRemoteDetails = ModelRemoteDetails()

        modelRemoteDetails.selectedAppliance = selectedApplianceType

        modelRemoteDetails.customName = customName

        //modelRemoteDetails.groupId = 1
        modelRemoteDetails.groupdName = "Scene1"

        modelRemoteDetails.remoteId = remoteId.toString()
        modelRemoteDetails.selectedBrandName = applianceName!!
        modelRemoteDetails.brandId = applianceId.toString()
        modelRemoteDetails.ac_remotelist = modelLdapi2AcModesList
        modelRemoteDetails.index = remoteIndex

        return modelRemoteDetails
    }

    fun addUserCustomNamesToHashMap(bodyJsonObject: JSONObject?, bodyJsonArray: JSONArray?): HashMap<String, String> {

        if (bodyJsonObject != null) {
            //this means ther user has only one device in his account
            val applianceJsonObject: JSONObject? = bodyJsonObject.optJSONObject("Appliance")
            if (applianceJsonObject != null) {
                //user has only one appliance
                customNamesHashMap[getCustomName(applianceJsonObject)] = bodyJsonObject.getString("Mac")
            } else {
                val applianceJsonArray: JSONArray? = bodyJsonObject.optJSONArray("Appliance")
                if (applianceJsonArray != null) {
                    for (i in 0 until applianceJsonArray.length()) {
                        //user has more than one device
                        customNamesHashMap[getCustomName(applianceJsonArray[i] as JSONObject)] = bodyJsonObject.getString("Mac")
                    }
                }
            }

        } else if (bodyJsonArray != null) {
            //this means user has more than device in
            //his account
            for (j in 0 until bodyJsonArray.length()) {
                val bodyJsonObject = bodyJsonArray[j] as JSONObject
                addUserCustomNamesToHashMap(bodyJsonObject, null)
            }
        }
        return customNamesHashMap
    }


    fun getAppliancesListFromAllDevicesTheUserHasConfigured(sub: String, onCallingGetApiToGetCustomNames: OnCallingGetApiToGetCustomNames) {

        val requestQueue = Volley.newRequestQueue(this@IRAcRemoteSelectionActivity)

        val baseUrl = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/usermangement?sub=$sub"

        Log.d(TAG, "requestedURl: $baseUrl")

        var userAddedCustomNamesHashMap: HashMap<String, String> = HashMap()

        val stringRequest = StringRequest(Request.Method.GET, baseUrl, Response.Listener { response ->

            Log.d(TAG, "getUserManagementDetailsAllDevices: response: $response")


            val responseObject = JSONObject(response)

            val bodyJsonObject: JSONObject? = responseObject.optJSONObject("body")

            if (bodyJsonObject != null) {

                userAddedCustomNamesHashMap = addUserCustomNamesToHashMap(bodyJsonObject, null)

            } else {
                //body key value is a json array
                //user has added more than one device
                val bodyJsonArray: JSONArray? = responseObject.optJSONArray("body")

                if (bodyJsonArray != null) {
                    userAddedCustomNamesHashMap = addUserCustomNamesToHashMap(null, bodyJsonArray)
                }

            }

            Log.d(TAG, "customHashMap: ".plus(customNamesHashMap))

            onCallingGetApiToGetCustomNames.onResponse(userAddedCustomNamesHashMap)
        }, Response.ErrorListener { volleyError ->
            if (volleyError is TimeoutError || volleyError is NoConnectionError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        this@IRAcRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")
            } else if (volleyError is AuthFailureError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        this@IRAcRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")
            } else if (volleyError is ServerError) {
                if (volleyError.networkResponse.statusCode != 302) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            this@IRAcRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Server error occurred, please try again later")
                }
            } else if (volleyError is NetworkError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        this@IRAcRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Network error occurred, please try again later")
            } else if (volleyError is ParseError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        this@IRAcRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Parser error occurred, please try again later")
            }
        })

        stringRequest.setRetryPolicy(DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))

        requestQueue.add<String>(stringRequest)
    }

    fun addPreDefinedPopularOptionsCustomName(selectedAppliace: String, brandName: String): HashMap<String, String> {
        var popularOptionsHashMap: HashMap<String, String> = HashMap()
        var applianceType = "TV"
        when (selectedAppliace) {
            "1",
            "TV"
            -> {
                //tv
                popularOptionsHashMap[applianceType] = deviceInfo!!.usn
                applianceType = "TV"
            }
            "2",
            "TVP"
            -> {
                //tvp
                popularOptionsHashMap["SET TOP BOX"] = deviceInfo!!.usn
                applianceType = "SET TOP BOX"
            }
            "3",
            "AC"
            -> {
                //ac
                popularOptionsHashMap["AC"] = deviceInfo!!.usn
                applianceType = "AC"
            }
        }


        //Living Room
        popularOptionsHashMap["LIVING ROOM $applianceType"] = deviceInfo!!.usn

        popularOptionsHashMap["${brandName.toUpperCase()} $applianceType"] = deviceInfo!!.usn

        popularOptionsHashMap["BED ROOM $applianceType"] = deviceInfo!!.usn

        popularOptionsHashMap["OFFICE $applianceType"] = deviceInfo!!.usn

        return popularOptionsHashMap
    }


    fun getCustomName(applianceInfoJsonObject: JSONObject?): String {
        if (applianceInfoJsonObject != null) {
            try {
                return applianceInfoJsonObject.getString("CustomName")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return ""
    }


    //deleting the old appliance which is selected by the user
    fun deleteUserDevice(sub: String, macAddress: String,
                         modelRemoteDetails: ModelRemoteDetails?, restApiSucessFailureCallbacks: RestApiSucessFailureCallbacks) {

        if (modelRemoteDetails != null) {

            val requestQueue = Volley.newRequestQueue(this@IRAcRemoteSelectionActivity)

            val url = ApiConstants.BASE_URL_USER_MGT + "Beta/usermangement"

            Log.d(TAG, "deleteBody" + buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString())

            var requestBody: String = buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString()


            var deleteUserDetailsStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->


                restApiSucessFailureCallbacks.onSucessFailureCallbacks(true, modelRemoteDetails)

                Log.d(TAG, "deleteResponse:".plus(response))

            }, Response.ErrorListener { volleyError ->

                restApiSucessFailureCallbacks.onSucessFailureCallbacks(false, null)

                Log.d(TAG, "Error: ${volleyError.networkResponse.statusCode}")

                dismissLoader()

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                                window?.decorView!!.findViewById(android.R.id.content), "Server error occurred, please try again later")
                    }

                } else if (volleyError is NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Network error occurred, please try again later")

                } else if (volleyError is ParseError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
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

    fun postUserManagment(sub: String, macAddress: String,
                          modelRemoteDetails: ModelRemoteDetails) {

        val requestQueue = Volley.newRequestQueue(this@IRAcRemoteSelectionActivity)

        val url = ApiConstants.BASE_URL_USER_MGT + "Beta/usermangement"

        var requestBody: String = buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails, "create").toString()


        val postUserDetailsStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->

            Log.d(TAG, "responsePostUserMgt: $response")

            val responseObject: JSONObject = JSONObject(response)

            var status = responseObject.getString("statusCode")

            if (status == "200") {

                updateApplianceInfoInSharedPref(modelRemoteDetails.remoteId.toInt(),
                        deviceInfo!!.usn, modelRemoteDetails.customName)//tv or tvp or ac

                gotoNextActivity(modelRemoteDetails.ac_remotelist, modelRemoteDetails.index,
                        modelRemoteDetails.remoteId, false)

            } else {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        window?.decorView!!.findViewById(android.R.id.content), responseObject.getString("body"))
            }
            Log.d(TAG, "modelPostUserMgt: statusCode".plus(status))

        }, Response.ErrorListener { volleyError ->

            Log.d(TAG, "Error: ${volleyError.networkResponse.statusCode}")

            dismissLoader()

            if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        window?.decorView!!.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

            } else if (volleyError is AuthFailureError) {

                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        window?.decorView!!.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


            } else if (volleyError is ServerError) {
                if (volleyError.networkResponse.statusCode != 302) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Server error occurred, please try again later")
                }

            } else if (volleyError is NetworkError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
                        window?.decorView!!.findViewById(android.R.id.content), "Network error occurred, please try again later")

            } else if (volleyError is ParseError) {

                uiRelatedClass.buildSnackBarWithoutButton(this@IRAcRemoteSelectionActivity,
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
        postUserDetailsStringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(postUserDetailsStringRequest)
    }


    fun filterOutDuplicateNamesWhichAreUsedByTheUser(customNamesUserIsUsingList:
                                                     HashMap<String, String>, preDefinedPopularOptionsHashMap: HashMap<String, String>): HashMap<String, String> {

        var mutableIterator = preDefinedPopularOptionsHashMap.iterator()


        for (preDefinedHashMapObject: Map.Entry<String, String> in mutableIterator) {

            for (customNamesHashMap: Map.Entry<String, String> in customNamesUserIsUsingList) {
                if (customNamesHashMap.key == (preDefinedHashMapObject.key)) {
                    //check if the mac is diff from the ones present in the
                    if (customNamesHashMap.value != deviceInfo?.usn) {
                        //ie then the user that name for the appliance ie tv.tvp
                        //for a diffrent device then only then remove it
                        Log.d(TAG, "deletedCustomName: ".plus(preDefinedHashMapObject.key))
                        mutableIterator.remove()
                    }
                }
            }
        }
        return preDefinedPopularOptionsHashMap
    }

    fun getRemoteDetailsFromTheApplianceList(userSelectedAppliance: String, modelRemoteDetailsList: MutableList<ModelRemoteDetails>): ModelRemoteDetails? {
        for (modelRemoteDetails: ModelRemoteDetails in modelRemoteDetailsList) {
            if (userSelectedAppliance == modelRemoteDetails.selectedAppliance) {
                return modelRemoteDetails
            }
        }
        return null
    }


    fun buildJsonForUserManagmentApis(sub: String, macAddress: String,
                                      modelRemoteDetails: ModelRemoteDetails, operation: String): JSONObject {
        var payLoadObject: JSONObject = JSONObject()

        when (modelRemoteDetails.selectedAppliance) {
            "1",
            "TV"
            -> {
                payLoadObject.put("Appliance", "TV")//tv
            }

            "2",
            "TVP"
            -> {
                payLoadObject.put("Appliance", "TVP")//tvp
            }

            "3",
            "AC"
            -> {
                payLoadObject.put("Appliance", "AC")//ac
            }
        }

        payLoadObject.put("RemoteID", modelRemoteDetails.remoteId)
        payLoadObject.put("BrandID", modelRemoteDetails.brandId)

        //payLoadObject.put("GroupID", modelRemoteDetails.groupId.toString())

        payLoadObject.put("GroupName", modelRemoteDetails.groupdName)

        payLoadObject.put("BrandName", modelRemoteDetails.selectedBrandName)
        payLoadObject.put("CustomName", modelRemoteDetails.customName)
        payLoadObject.put("index", modelRemoteDetails.index.toString())

        var bodyObject: JSONObject = JSONObject()


        bodyObject.put("operation", operation)
        bodyObject.put("sub", sub)
        bodyObject.put("Mac", macAddress)
        bodyObject.put("payload", payLoadObject)

        Log.d(TAG, "body: ".plus(bodyObject))

        return bodyObject
    }

    fun getRemoteDetailsFromSharedPrefThatNeedsToBeDeleted(userSelectedAppliance: String, macId: String): ModelRemoteDetails? {
        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        var gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {
            //if data is present
            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            if (modelRemoteSubAndMacDetils.mac == macId) {
                //and the appliance should be from that particular mavid device
                return getRemoteDetailsFromTheApplianceList(userSelectedAppliance, modelRemoteSubAndMacDetils?.modelRemoteDetailsList)
            }
        }
        return null
    }


    fun updateApplianceInfoInSharedPref(remoteId: Int, macId: String, customName: String) {


        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {

            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            if (modelRemoteSubAndMacDetils.mac == macId) {
                //update the appliance list  details in the list to the exsting device
                modelRemoteSubAndMacDetils.modelRemoteDetailsList.add(buidlRenoteDetails(remoteId, customName))
            } else {
                //new device
                modelRemoteSubAndMacDetils.sub = sharedPreferences!!.getString("sub", "")!!
                modelRemoteSubAndMacDetils.mac = macId
                var appllianceInfoList: MutableList<ModelRemoteDetails> = ArrayList()
                appllianceInfoList.add(buidlRenoteDetails(remoteId, customName))

                modelRemoteSubAndMacDetils.modelRemoteDetailsList = appllianceInfoList
            }
        } else {
            //new user and first device
            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils.sub = sharedPreferences!!.getString("sub", "")!!
            modelRemoteSubAndMacDetils.mac = macId
            var appllianceInfoList: MutableList<ModelRemoteDetails> = ArrayList()
            appllianceInfoList.add(buidlRenoteDetails(remoteId, customName))
            modelRemoteSubAndMacDetils.modelRemoteDetailsList = appllianceInfoList
        }

        modelRemoteDetailsString = gson.toJson(modelRemoteSubAndMacDetils)


        val workingRemoteButtonsString = gson.toJson(modelLdapi2AcModesList)

        var editor: SharedPreferences.Editor
        editor = sharedPreferences!!.edit()
        editor.putString("applianceInfoList", modelRemoteDetailsString)


        if (selectedApplianceType == "3" || selectedApplianceType == "AC") {
            editor.putString("workingACRemoteButtons", workingRemoteButtonsString)
        }

        editor.apply()
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

                            Log.d(TAG, "deletedFromSharedPref".plus(storedRemoteDetails.remoteId.plus("CustomaName:").plus(storedRemoteDetails.customName)))

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

}
