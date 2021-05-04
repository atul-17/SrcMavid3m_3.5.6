package com.libre.irremote.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.libre.irremote.Constants.RemoteIndexGetter
import com.libre.irremote.R
import com.libre.irremote.irActivites.IRAddRemoteVPActivity
import com.libre.irremote.irActivites.IRSelectTvOrTVPOrAcRegionalBrandsActivity
import com.libre.irremote.irActivites.IRTvOrTvpRemoteSelectionActivity
import com.libre.irremote.irActivites.IRTvpBrandActivity
import com.libre.irremote.irActivites.irMultipleRemotes.IRAcApplianceActivity
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo
import com.libre.irremote.models.*
import com.libre.irremote.utility.*
import com.libre.irremote.irActivites.irMultipleRemotes.IRAddOrSelectRemotesActivity
import com.libre.irremote.irActivites.irMultipleRemotes.IRTVPApplianceActivity
import com.libre.irremote.irActivites.irMultipleRemotes.IRTelevisionApplianceActivity
import com.libre.libresdk.Util.LibreLogger
import kotlinx.android.synthetic.main.fragment_tv_remote_selection.*
import kotlinx.android.synthetic.main.fragment_tv_remote_selection.tvHeading
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IRTvOrTvpRemoteSelectionFragment : Fragment() {

    var bundle = Bundle()

    var modelLevelData = ModelLevelData()

    var codeLevelIndex: Int = 0

    var level: Int = 1

    var remoteIndex = 1;


    val TAG = IRTvOrTvpRemoteSelectionFragment::class.java.simpleName

    var modelLevelCode: ModelLevelCode? = null

    var uiRelatedClass = UIRelatedClass()

    var irButtonListTimerTask: Timer? = null

    var handler = Handler()

    private val DO_UPDATE_TEXT = 0

    private var LDAPI_TIMOUT = 1

    var modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

    var workingRemoteButtonsHashMap: HashMap<String, String> = HashMap()

    /**TODO dont allow user to go  when he clicks back after sending the data and the device is dowloading button list
     * */

    val myHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what: Int = msg.what
            when (what) {
                DO_UPDATE_TEXT -> updateTheCommandToBeSentTodDevice()

                LDAPI_TIMOUT -> {
                    getActivityObject()?.runOnUiThread {
                        getActivityObject()?.dismissLoader()
                        irButtonListTimerTask?.cancel()
                        Log.d(TAG, "Error")
                        uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try after some time",
                                "Go Back", activity!! as AppCompatActivity)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tv_remote_selection, container, false)

        bundle = arguments!!
        if (bundle != null) {
            modelLevelData = bundle.getSerializable("modelLevelData") as ModelLevelData
        }

        return view
    }

    init {
        codeLevelIndex = 0
    }

    fun updateTheCommandToBeSentTodDevice() {
        if (modelLevelData.modelLevelCodeList != null) {
            if (codeLevelIndex + 1 <= modelLevelData.modelLevelCodeList!!.size) {
                tvSendingCommand.text = "Checking available configurations ".plus(codeLevelIndex + 1).plus("/").plus(modelLevelData.modelLevelCodeList?.size)
                tvSendingCommand.visibility = View.VISIBLE
            } else {
                tvSendingCommand.visibility = View.GONE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (modelLevelData.key == "No More data") {
            btnRemoteButtonName.visibility = View.GONE
            uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try different brand",
                    "Go Back", activity!! as AppCompatActivity)
        } else {
            initViews()
        }

        btnRemoteButtonName.setOnClickListener {
            /** send the commmand
             *  wait for the acknowledgement from the device
             * */
            //wait for two seconds
            getActivityObject()?.showProgressBar()
            //need to check id
            if (getActivityObject() != null && modelLevelCode != null) {
                sendCommandToMavidDevice(getActivityObject()!!.applianceId, modelLevelCode?.command!!, getActivityObject()!!.ipAddress, object : OnMavid3mAckTheCommandInterface {
                    override fun onAcknowledgment(status: String) {
                        getActivityObject()?.dismissLoader()
                        if (status == "2") {
                            /** the device has successfully reached the command
                             * show a alert to ask whether the command worked
                             * */
                            getActivityObject()?.showCustomAlertForRemoteSelection(btnRemoteButtonName.text.toString(), object : OnUserButtonSelection {
                                override fun didTheCommandWork(value: Boolean) {
                                    modelLevelCode?.isCommandWorking = value
                                    myHandler.sendEmptyMessage(DO_UPDATE_TEXT)
                                    if (value) {
                                        //it worked
                                        /** The command
                                         * worked so inc the level and check whether there is a next level in the json * */
                                        level += 1//inc level
                                        //and add one more fragment on top of the old fragment
                                        if (modelLevelCode?.subLevelJsonArray != null) {
                                            (activity as IRTvOrTvpRemoteSelectionActivity?)?.creteNewFragment("fragment".plus(level),
                                                    getActivityObject()!!.parseLevelData(modelLevelCode?.subLevelJsonArray!!, ModelLevelData(), level, 0))
                                        } else {
                                            //send the id to the cloud/device
                                            //call the LDAPI#1
                                            tvSendingCommand.visibility = View.GONE
                                            btnRemoteButtonName.visibility = View.GONE
                                            if (getActivityObject() != null) {

//                                                if (!checkIfTheUserSelectedRemoteIsPrevSelected(getActivityObject()!!.selectedApplianceType,
//                                                                getModelCodeLevelId(modelLevelCode!!), getActivityObject()!!.applianceId, getActivityObject()?.deviceInfo!!.usn)) {

                                                getActivityObject()?.getAppliancesListFromAllDevicesTheUserHasConfigured(getActivityObject()?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("sub", "")!!,
                                                        object : OnCallingGetApiToGetCustomNames {
                                                            override fun onResponse(userAddedCustomNamesHashMap: HashMap<String, String>) {
                                                                /**
                                                                 * Filter out names which
                                                                 * are already used by the
                                                                 * user in another device
                                                                 * */
                                                                /**
                                                                 * show the user an options to edit custom for the appliance he has configured
                                                                 * *
                                                                 * */
                                                                val filtredHashMap: HashMap<String, String> = getActivityObject()!!
                                                                        .filterOutDuplicateNamesWhichAreUsedByTheUser(userAddedCustomNamesHashMap, getActivityObject()!!.addPreDefinedPopularOptionsCustomName(getActivityObject()!!
                                                                                .selectedApplianceType, getActivityObject()!!.applianceName))

                                                                getActivityObject()?.dismissLoader()

                                                                //show the dialog to edit custom name
                                                                uiRelatedClass.showBottomDialogForAddingCustomName(getActivityObject()!!, object : OnButtonClickCallbackWithStringParams {
                                                                    override fun onUserClicked(userPassedInfo: String) {

                                                                        Log.d(TAG, "LDAPI#1 data: " + buildRemotePayloadJson(getModelCodeLevelId(modelLevelCode!!)).toString())

                                                                        sendRemoteDetailsToMavid3mLDAPI1(getActivityObject()!!.ipAddress, getModelCodeLevelId(modelLevelCode!!), userPassedInfo)
                                                                    }
                                                                }, filtredHashMap, userAddedCustomNamesHashMap,
                                                                        getActivityObject()!!.selectedApplianceType, getActivityObject()!!.applianceName, getActivityObject()?.deviceInfo!!.usn)
                                                            }
                                                        })

                                                //}
                                                // else {
//                                                    /** show a dialog to inform the user
//                                                     * he has prev selected the same remote id
//                                                     * */
//                                                    getActivityObject()?.runOnUiThread {
//                                                        getActivityObject()?.dismissLoader()
//                                                        uiRelatedClass.showUserCustomDialogForPrevSelectedRemote(getActivityObject()!!, object : OnButtonClickCallback {
//                                                            override fun onClick(isSucess: Boolean) {
//                                                                gotoNextActivity(getActivityObject()!!.selectedApplianceType,
//                                                                        null, 0, null, true)
//                                                            }
//                                                        })
//                                                    }
                                                //}

                                            }
                                        }

                                    } else {
                                        //did not work
                                        //wait for few seconds
                                        codeLevelIndex++//inc Command level array to next object

                                        modelLevelCode = getModelCodeLevel(codeLevelIndex)

                                        if (modelLevelCode?.command != null) {
                                            Log.d(TAG, "loggingTheCommand: ".plus(modelLevelCode?.command))
                                        } else {
                                            //is all the commands  for the button is false
                                            //and they are no more levels in that code json object
                                            //and button level should be greater/equal to  that of level1

                                            getActivityObject()?.LEVEL = modelLevelData.level
                                            getActivityObject()?.showProgressBar()
                                            if (isAllFalse(addingCommandValuesToArray(modelLevelData)) && modelLevelData.level > 1) {
                                                /**TBD
                                                 * for now showing a error message to the user
                                                 * and sending the last id index to the device
                                                 * */
                                                tvSendingCommand.visibility = View.GONE
                                                btnRemoteButtonName.visibility = View.GONE
                                                if (activity != null) {

                                                    modelLevelCode = getModelCodeLevel(modelLevelData.modelLevelCodeList?.size!!.minus(1))//sending the last index to the device

                                                    Log.d(TAG, "LDAPI#1 data: " + buildRemotePayloadJson(getModelCodeLevelId(modelLevelCode!!)).toString())


                                                    getActivityObject()?.runOnUiThread {

                                                        getActivityObject()?.dismissLoader()

                                                        uiRelatedClass.buidCustomSnackBarWithButton(getActivityObject()!!,
                                                                "No suitable remotes found, contact Customer care",
                                                                "OK",
                                                                getActivityObject()!!
                                                        )
                                                    }
                                                }
                                            } else {
                                                //there is no next index in level1
                                                if (getActivityObject()?.modelSelectRemotePayload?.levelJsonArray?.length()?.minus(1) == modelLevelData.index
                                                        && modelLevelData.level == 1) {

                                                    modelLevelCode = getModelCodeLevel(modelLevelData.modelLevelCodeList?.size!!.minus(1))

                                                    tvSendingCommand.visibility = View.GONE
                                                    btnRemoteButtonName.visibility = View.GONE

                                                    Log.d(TAG, "index: " + getActivityObject()?.modelSelectRemotePayload?.levelJsonArray?.length()?.minus(1)
                                                            + "level: " + modelLevelData.level + "id: ".plus(modelLevelCode?.idList.toString()))


                                                    getActivityObject()?.runOnUiThread {

                                                        getActivityObject()?.dismissLoader()

                                                        uiRelatedClass.buidCustomSnackBarWithButton(getActivityObject()!!,
                                                                "No suitable remotes found, contact Customer care",
                                                                "OK",
                                                                getActivityObject()!!
                                                        )
                                                    }


                                                } else {
                                                    //inc the topLevel Index
                                                    getActivityObject()?.INDEX = modelLevelData.index!!

                                                    getActivityObject()?.INDEX = modelLevelData.index!! + 1

                                                    if (getActivityObject()?.modelSelectRemotePayload?.levelJsonArray != null) {

                                                        getActivityObject()?.creteNewFragment("fragment".plus(getActivityObject()?.LEVEL),
                                                                getActivityObject()!!
                                                                        .parseLevelData(getActivityObject()?.modelSelectRemotePayload?.levelJsonArray!!,
                                                                                ModelLevelData(), getActivityObject()?.LEVEL!!, getActivityObject()?.INDEX!!))

                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                })
            }
        }
    }

    fun checkifPrevLevelButtonIsWorkingAndGetTheId(modelLevelData: ModelLevelData): String? {
        val modelLevelCodeList = modelLevelData.modelLevelCodeList
        if (modelLevelCodeList != null) {
            for (modelLevelCode in modelLevelCodeList!!) {
                if (modelLevelCode.isCommandWorking != null) {
                    if (modelLevelCode.isCommandWorking!!) {
                        return modelLevelCode.idList.toString()
                    }
                }
            }
        }
        return null
    }


    fun addingCommandValuesToArray(modelLevelData: ModelLevelData): Array<Boolean> {

        var isCommandWorkingArray = arrayOf<Boolean>()
        if (modelLevelData.modelLevelCodeList != null) {
            for (modelLevelCode in modelLevelData.modelLevelCodeList!!) {
                if (modelLevelCode.isCommandWorking != null && modelLevelCode.codeLevelIndex != null) {
                    isCommandWorkingArray = arrayOf(modelLevelCode.isCommandWorking!!)
                }
            }

        }
        return isCommandWorkingArray
    }

    fun isAllFalse(array: Array<Boolean>): Boolean {
        for (b in array) if (!b) return true
        return false
    }

    fun initViews() {

        when (getActivityObject()?.selectedApplianceType) {
            "1",
            "TV" -> {
                //tv
                tvHeading.text = "TV Remote Selection"
                tvRemoteSelectionInstrs.text = "Tap the below button and confirm whether TV responds"
                tvMessageNonPowerButton.text = "If TV is not powered on , please power it on using the original remote"
            }
            "2",
            "TVP" -> {
                //tvp
                //tv
                tvHeading.text = "Set Top Box Remote Selection"
                tvRemoteSelectionInstrs.text = "Tap the below button and confirm whether Set Top Box responds"
                tvMessageNonPowerButton.text = "If Set Top Box is not powered on , please power it on using the original remote"
            }
            "3",
            "AC" -> {

            }
        }
        level = modelLevelData.level

        modelLevelCode = getModelCodeLevel(codeLevelIndex)

        val command = modelLevelCode?.command

        if (modelLevelData.key!!.equals("POWER", true)) {
            tvMessageNonPowerButton.visibility = View.GONE
        } else {
            tvMessageNonPowerButton.visibility = View.VISIBLE
        }

        btnRemoteButtonName.text = modelLevelData.key

        btnRemoteButtonName.visibility = View.VISIBLE
        getActivityObject()?.dismissLoader()
        Log.d(TAG, "CommandToBeSent".plus(command))

        myHandler.sendEmptyMessage(DO_UPDATE_TEXT)
    }


    /** LDAPI#1 */
    fun sendRemoteDetailsToMavid3mLDAPI1(ipAddress: String, remoteId: Int, customName: String) {
        getActivityObject()?.showProgressBar()
        val payloadString = buildRemotePayloadJson(remoteId).toString()
        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST, payloadString,
                object : CommandStatusListenerWithResponse {
                    override fun response(messageInfo: MessageInfo?) {
                        if (messageInfo != null) {

                            val dataJsonObject = JSONObject(messageInfo?.message)

                            val status = dataJsonObject.getInt("Status")

                            if (status == 2) {//device acknowledged
                                //calling get api from with
                                //all the devices present

                                /**need to call the LDAPI#1
                                 * */
                                getActivityObject()?.showProgressBar()
                                timerTaskToReadDeviceStatus(ipAddress, remoteId, customName)

                            } else if (status == 3) {
                                //error
                                getActivityObject()?.dismissLoader()
                                uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try after some time",
                                        "Go Back", activity!! as AppCompatActivity)
                            }
                        } else {
                            getActivityObject()?.dismissLoader()
                            uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try after some time",
                                    "Go Back", activity!! as AppCompatActivity)
                        }
                    }


                    override fun failure(e: Exception?) {
                        getActivityObject()?.dismissLoader()
                        Log.d(TAG, "sendingRemoteDetailsException$e")
                    }

                    override fun success() {
                        getActivityObject()?.dismissLoader()
                    }
                })
    }


    /** Call the LDAPI#2 every 3 seconds and check the status */
    fun timerTaskToReadDeviceStatus(ipdAddress: String, remoteId: Int, customName: String) {
        irButtonListTimerTask = Timer()
        myHandler.sendEmptyMessageDelayed(LDAPI_TIMOUT, 25000);
        irButtonListTimerTask?.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "callingLdapi#2 every 5 secs")
                getButtonPayload(remoteIndex, ipdAddress, remoteId, customName)
            }

        }, 0, 5000)
    }

    /** LDAPI#2 */
    fun getButtonPayload(remoteIndex: Int, ipdAddress: String, remoteId: Int, customName: String) {
        getActivityObject()?.showProgressBar()
        LibreMavidHelper.sendCustomCommands(ipdAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForRemoteJsonList(remoteIndex, remoteId).toString(), object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {
                Log.d(TAG, "ldapi#2_".plus(messageInfo?.message))
                try {
                    val responseJSONObject = JSONObject(messageInfo?.message)
                    val statusCode = responseJSONObject.getInt("Status")
                    when (statusCode) {

                        3 -> {
                            myHandler.removeCallbacksAndMessages(LDAPI_TIMOUT)
                            myHandler?.removeCallbacksAndMessages(null)
                            irButtonListTimerTask?.cancel()
                            /** get the button list from the data json object */

                            val payloadJsonObject = responseJSONObject.getJSONObject("payload")

                            val buttonJsonArray = payloadJsonObject.getJSONArray("keys")

                            Log.d(TAG, "buttonList: ".plus(buttonJsonArray.toString()))

                            for (i in 0 until buttonJsonArray.length()) {
                                val buttonNameString = buttonJsonArray[i] as String
                                workingRemoteButtonsHashMap[buttonNameString] = "1"
                            }


                            postUserManagment(getActivityObject()?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("sub", "")!!,
                                    getActivityObject()?.deviceInfo!!.usn, buidlRenoteDetails(true, remoteIndex, remoteId, customName))


                        }

                    }
                } catch (e: JSONException) {
                    irButtonListTimerTask?.cancel()
                    myHandler.removeCallbacksAndMessages(LDAPI_TIMOUT)
                    getActivityObject()?.dismissLoader()
                    Log.d(TAG, "exeception:".plus(e.toString()))
                    uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try after some time",
                            "Go Back", activity!! as AppCompatActivity)
                }

            }

            override fun failure(e: java.lang.Exception?) {
                getActivityObject()?.dismissLoader()
                Log.d(TAG, "sendingRemoteDetailsException$e")
            }

            override fun success() {

            }
        })
    }


    fun getModelCodeLevel(index: Int): ModelLevelCode {

        if (modelLevelData.modelLevelCodeList != null) {
            if (index < modelLevelData.modelLevelCodeList!!.size) {
                return modelLevelData.modelLevelCodeList!![index]
            }
            //if they are no more command objects avail
            //inc top level
            //they are no more buttons in this
        }
        return ModelLevelCode()
    }


    fun getModelCodeLevelId(modelLevelCode: ModelLevelCode): Int {
        return if (modelLevelCode.idList != null) {
            modelLevelCode.idList!![0]
        } else
            0
    }

    fun getActivityObject(): IRTvOrTvpRemoteSelectionActivity? {
        if (activity != null) {
            return activity as IRTvOrTvpRemoteSelectionActivity
        }
        return null
    }


    /** LDAPI#3*/
    fun sendCommandToMavidDevice(brandId: Int, remoteCommand: String, ipdAddress: String, onMavid3mAckTheCommandInterface: OnMavid3mAckTheCommandInterface) {
        getActivityObject()?.showProgressBar()
        LibreMavidHelper.sendCustomCommands(ipdAddress,
                LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadToSendCommandToTheDevice(brandId, remoteCommand),
                object : CommandStatusListenerWithResponse {
                    override fun response(messageInfo: MessageInfo?) {
                        getActivityObject()?.dismissLoader()
                        if (messageInfo != null) {
                            val dataJsonObject = JSONObject(messageInfo?.message)

                            Log.d(TAG, "ldapi#3 response".plus(dataJsonObject).toString())
                            val status = dataJsonObject.getInt("Status")

                            if (status == 2) {//device acknowledged

                                onMavid3mAckTheCommandInterface.onAcknowledgment(status.toString())

                            } else if (status == 3) {
                                onMavid3mAckTheCommandInterface.onAcknowledgment(status.toString())
                                //error
                                uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try after some time",
                                        "Go Back", activity!! as AppCompatActivity)
                            }
                        } else {
                            onMavid3mAckTheCommandInterface.onAcknowledgment("3")//error
                            uiRelatedClass.buidCustomSnackBarWithButton(activity!!, "There sees to be an error!!.Please try after some time",
                                    "Go Back", activity!! as AppCompatActivity)
                        }
                    }

                    override fun failure(e: java.lang.Exception?) {
                        getActivityObject()?.dismissLoader()
                        Log.d(TAG, "sendingRemoteDetailsException$e")
                    }

                    override fun success() {

                    }

                })
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


    //deleting the old appliance which is selected by the user
    fun deleteUserDevice(sub: String, macAddress: String,
                         modelRemoteDetails: ModelRemoteDetails?, restApiSucessFailureCallbacks: RestApiSucessFailureCallbacks) {

        if (modelRemoteDetails != null) {

            val requestQueue = Volley.newRequestQueue(context)

            val url = ApiConstants.BASE_URL_USER_MGT + "Beta/usermangement"

            Log.d(TAG, "deleteBody" + buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString())

            var requestBody: String = buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString()


            var deleteUserDetailsStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->


                restApiSucessFailureCallbacks.onSucessFailureCallbacks(true, modelRemoteDetails)

                Log.d(TAG, "deleteResponse:".plus(response))

            }, Response.ErrorListener { volleyError ->

                restApiSucessFailureCallbacks.onSucessFailureCallbacks(false, null)

                Log.d(TAG, "Error: ${volleyError.networkResponse.statusCode}")

                getActivityObject()?.dismissLoader()

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                            getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                            getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                                getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Server error occurred, please try again later")
                    }

                } else if (volleyError is NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                            getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Network error occurred, please try again later")

                } else if (volleyError is ParseError) {

                    uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                            getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Parser error occurred, please try again later")
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

        val requestQueue = Volley.newRequestQueue(context)

        val url = ApiConstants.BASE_URL_USER_MGT + "Beta/usermangement"

        var requestBody: String = buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails, "create").toString()


        LibreLogger.d(this, "atul_in_post_data: ".plus(requestBody))

        val postUserDetailsStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->

            Log.d(TAG, "responsePostUserMgt: $response")

            val responseObject: JSONObject = JSONObject(response)

            var status = responseObject.getString("statusCode")

            if (status == "200") {


                updateApplianceInfoInSharedPref(modelRemoteDetails.index, modelRemoteDetails.remoteId.toInt(),
                        getActivityObject()?.deviceInfo!!.usn, modelRemoteDetails.customName, modelRemoteDetails.index, modelRemoteDetails.remotesHashMap)//tv or tvp or ac

                gotoNextActivity(modelRemoteDetails.selectedAppliance, modelRemoteDetails.remotesHashMap, modelRemoteDetails.index, modelRemoteDetails.remoteId, false)


            } else {
                uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                        getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), responseObject.getString("body"))
            }
            Log.d(TAG, "modelPostUserMgt: statusCode".plus(status))

        }, Response.ErrorListener { volleyError ->

            Log.d(TAG, "Error: ${volleyError.networkResponse.statusCode}")

            getActivityObject()?.dismissLoader()

            if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                        getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

            } else if (volleyError is AuthFailureError) {

                uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                        getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


            } else if (volleyError is ServerError) {
                if (volleyError.networkResponse.statusCode != 302) {
                    uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                            getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Server error occurred, please try again later")
                }

            } else if (volleyError is NetworkError) {
                uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                        getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Network error occurred, please try again later")

            } else if (volleyError is ParseError) {

                uiRelatedClass.buildSnackBarWithoutButton(getActivityObject()!!,
                        getActivityObject()?.window?.decorView!!.findViewById(android.R.id.content), "Parser error occurred, please try again later")
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


    fun gotoNextActivity(selectedAppliance: String, workingRemoteData: HashMap<String, String>?, remoteIndex: Int, remoteId: String?, isUserPrevSelectedRemote: Boolean) {
        getActivityObject()?.dismissLoader()

        IRSelectTvOrTVPOrAcRegionalBrandsActivity.irSelectTvOrTVPOrAcRegionalBrandsActivity?.finish()
        IRTvpBrandActivity.irTvpBrandActivity?.finish()

        var bundle = Bundle()

        var intent: Intent? = null

        val gson = Gson()

        if (isUserPrevSelectedRemote) {
            intent = Intent(getActivityObject()!!, IRAddOrSelectRemotesActivity::class.java)
        } else {

            bundle.putInt("remoteIndex", remoteIndex)
            bundle.putString("remoteId", remoteId)
            bundle.putString("workingRemoteData", gson.toJson(workingRemoteData))

            when (selectedAppliance) {
                "1", "TV" -> {
                    //tv
                    intent = Intent(getActivityObject()!!, IRTelevisionApplianceActivity::class.java)

                }
                "2", "TVP" -> {
                    //tvp
                    intent = Intent(getActivityObject()!!, IRTVPApplianceActivity::class.java)
                }
                else -> {
                    intent = Intent(getActivityObject()!!, IRAddOrSelectRemotesActivity::class.java)
                }
            }

        }


        bundle.putSerializable("deviceInfo", getActivityObject()?.deviceInfo)
        intent?.putExtras(bundle)
        startActivity(intent)
        getActivityObject()?.finish()
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

        dataJsonObject.put("appliance", getActivityObject()!!.selectedApplianceType.toInt())//tv//tvp//ac

        dataJsonObject.put("bName", getActivityObject()?.applianceName)
        dataJsonObject.put("bId", getActivityObject()?.applianceId)
        dataJsonObject.put("rId", remoteId)

        remoteIndex = RemoteIndexGetter.getNextRemoteIndex(getActivityObject(), getActivityObject()!!.selectedApplianceType)

        dataJsonObject.put("index", remoteIndex)

        payloadJsonObject.put("data", dataJsonObject)

        return payloadJsonObject
    }


    /** value: 2 for LDAPI#2 ie sending the remote api*/
    fun buildPayloadForRemoteJsonList(remoteIndex: Int, remoteId: Int): JSONObject {
        val payloadJsonObject = JSONObject()

        payloadJsonObject.put("ID", 2)

        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        dataJsonObject.put("appliance", getActivityObject()!!.selectedApplianceType.toInt())//tv//tvp//ac
        dataJsonObject.put("rId", remoteId)
        dataJsonObject.put("index", remoteIndex)

        payloadJsonObject.put("data", dataJsonObject)

        Log.d(TAG, "ldapi#2_payload".plus(payloadJsonObject.toString()))
        return payloadJsonObject

    }


    /** value: 3 for LDAPI#3 ie sending the remote api*/
    fun buildPayloadToSendCommandToTheDevice(brandId: Int, remoteCommand: String): String {
        val payloadJsonObject: JSONObject = JSONObject()
        payloadJsonObject.put("ID", 3)


        val dataJsonObject = JSONObject()
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        dataJsonObject.put("appliance", getActivityObject()!!.selectedApplianceType.toInt())//tv/tvp//ac

        dataJsonObject.put("selectId", brandId)
        dataJsonObject.put("IrCode", remoteCommand)

        payloadJsonObject.put("data", dataJsonObject)

        return payloadJsonObject.toString()
    }

    fun buidlRenoteDetails(isUpdate: Boolean, remoteIndex: Int, remoteId: Int, customName: String): ModelRemoteDetails {
        var modelRemoteDetails = ModelRemoteDetails()
        modelRemoteDetails.selectedAppliance = getActivityObject()!!.selectedApplianceType

        modelRemoteDetails.customName = customName

        //modelRemoteDetails.groupId = 1
        modelRemoteDetails.groupdName = "Scene1"

        modelRemoteDetails.remoteId = remoteId.toString()
        modelRemoteDetails.selectedBrandName = getActivityObject()?.applianceName!!
        modelRemoteDetails.brandId = getActivityObject()?.applianceId.toString()

        if (isUpdate) {
            modelRemoteDetails.index = remoteIndex
        } else
            modelRemoteDetails.index = RemoteIndexGetter.getNextRemoteIndex(getActivityObject(), modelRemoteDetails.selectedAppliance)

        modelRemoteDetails.remotesHashMap = workingRemoteButtonsHashMap

        return modelRemoteDetails
    }

    fun checkIfTheUserSelectedRemoteIsPrevSelected(userSelectedAppliance: String, userSelectedRemoteId: Int, userSelectedBrandId: Int, macId: String): Boolean {
        var sharedPreferences = getActivityObject()?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)
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
                        if (modelRemoteDetails.selectedAppliance == userSelectedAppliance && modelRemoteDetails.remoteId == userSelectedRemoteId.toString()) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }


    fun updateApplianceInfoInSharedPref(remoteIndex: Int, remoteId: Int, macId: String, customName: String, index: Int, remotesHashMap: HashMap<String, String>) {


        var sharedPreferences = getActivityObject()?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()

        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {

            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            if (modelRemoteSubAndMacDetils.mac == macId) {
                //update the appliance list  details in the list to the exsting device
                modelRemoteSubAndMacDetils.modelRemoteDetailsList.add(buidlRenoteDetails(true, remoteIndex, remoteId, customName))
            } else {
                //new device
                modelRemoteSubAndMacDetils.sub = sharedPreferences!!.getString("sub", "")!!
                modelRemoteSubAndMacDetils.mac = macId
                val appllianceInfoList: MutableList<ModelRemoteDetails> = ArrayList()
                appllianceInfoList.add(buidlRenoteDetails(true, remoteIndex, remoteId, customName))
                modelRemoteSubAndMacDetils.modelRemoteDetailsList = appllianceInfoList
            }
        } else {
            //new user and first device
            modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

            modelRemoteSubAndMacDetils.sub = sharedPreferences!!.getString("sub", "")!!
            modelRemoteSubAndMacDetils.mac = macId
            var appllianceInfoList: MutableList<ModelRemoteDetails> = ArrayList()
            appllianceInfoList.add(buidlRenoteDetails(true, remoteIndex, remoteId, customName))
            modelRemoteSubAndMacDetils.modelRemoteDetailsList = appllianceInfoList
        }

        modelRemoteDetailsString = gson.toJson(modelRemoteSubAndMacDetils)

        val workingRemoteButtonsString = gson.toJson(workingRemoteButtonsHashMap)

        var editor: SharedPreferences.Editor
        editor = sharedPreferences!!.edit()
        editor.putString("applianceInfoList", modelRemoteDetailsString)

        if (getActivityObject()?.selectedApplianceType == "1" || getActivityObject()?.selectedApplianceType == "TV") {
            editor.putString("workingTVRemoteButtons", workingRemoteButtonsString)
        } else if (getActivityObject()?.selectedApplianceType == "2" || getActivityObject()?.selectedApplianceType == "TVP") {
            editor.putString("\\", workingRemoteButtonsString)
        }

        editor.apply()
    }

    private fun deleteApplianceFromSharedPref(macId: String, modelRemoteDetails: ModelRemoteDetails?) {
        if (modelRemoteDetails != null) {

            var sharedPreferences = getActivityObject()?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)

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

    fun getRemoteDetailsFromTheApplianceList(userSelectedAppliance: String, modelRemoteDetailsList: MutableList<ModelRemoteDetails>): ModelRemoteDetails? {
        for (modelRemoteDetails: ModelRemoteDetails in modelRemoteDetailsList) {
            if (userSelectedAppliance == modelRemoteDetails.selectedAppliance) {
                return modelRemoteDetails
            }
        }
        return null
    }

    fun getRemoteDetailsFromSharedPrefThatNeedsToBeDeleted(userSelectedAppliance: String, macId: String): ModelRemoteDetails? {
        var sharedPreferences = getActivityObject()?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)

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

}
