package com.libre.irremote.irActivites.irMultipleRemotes

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import com.google.gson.reflect.TypeToken
import com.libre.irremote.R
import com.libre.irremote.irActivites.IRSelectTvOrTVPOrAcRegionalBrandsActivity
import com.libre.irremote.models.ModelLdapi2AcModes
import com.libre.irremote.models.ModelLdapi6Response
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo


import kotlinx.android.synthetic.main.activity_ir_ac_appliace_layout.*
import org.json.JSONObject
import java.lang.Exception

class IRAcApplianceActivity : IRRemoteVPBaseActivity() {

    private var vibe: Vibrator? = null


    var bundle = Bundle()

    var deviceInfo: DeviceInfo? = null;


    var DISABLE_AC_REMOTE_BUTTONS: Int = 0


    var modelLdapi6ResponsModesList: MutableList<ModelLdapi6Response> = ArrayList()

    var preDefinedRemoteButtonsHashmap: HashMap<String, String> = HashMap()

    var remoteIndex: Int = 0

    var remoteId: String? = null


    var workingRemoteButtonsString: String? = null


    var currentMode: String = ""
    var acRemoteId = ""

    val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what: Int = msg!!.what
            when (what) {
                DISABLE_AC_REMOTE_BUTTONS -> {
                    val type = object : TypeToken<MutableList<ModelLdapi2AcModes>?>() {}.type
                    if (workingRemoteButtonsString!!.isNotEmpty()) {
                        modelLdapi2AcModesList = gson?.fromJson(workingRemoteButtonsString, type) as MutableList<ModelLdapi2AcModes>
                        disableNotWorkingButtons(currentMode)
                    }
                }
            }
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_ir_ac_appliace_layout;
    }

    override fun getSelectedAppliance(): Int {
        return 3
    }

    override fun getDeviceInfoData(): DeviceInfo? {
        return deviceInfo
    }

    override fun getSelectedRemoteIndex(): Int {
        return remoteIndex
    }

    override fun getSelectedRemoteId(): String? {
        return remoteId
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundle = intent.extras!!

        buildrogressDialog()

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
            workingRemoteButtonsString = bundle.getString("workingRemoteData")
            remoteIndex = bundle.getInt("remoteIndex")
            remoteId = bundle.getString("remoteId")
        }

        initializeUIOnclick()
        llSelectAc.visibility = View.GONE
        llRemoteUi.visibility = View.VISIBLE
        showProgressBar()
        getDeviceDetailsLdapi6ToReadAcCurrentConfigs(deviceInfo!!.ipAddress)
    }




    private fun initializeUIOnclick() {
        tvSelectAc.setOnClickListener {
            val intent = Intent(this, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            bundle.putBoolean("isAc", true)
            intent.putExtras(bundle)
            startActivity(intent)
        }




        rlPowerOn.setOnClickListener {

            vibrateOnButtonClick()
            showProgressBar()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_POWER_ON_BUTTON, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }

            })

            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }





        rlPowerOff.setOnClickListener {
            vibrateOnButtonClick()
            showProgressBar()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_POWER_OFF_BUTTON, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }



        tvSwing.setOnClickListener {
            vibrateOnButtonClick()
            showProgressBar()
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_SWING, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }




        tvDirectionBtn.setOnClickListener {
            vibrateOnButtonClick()
            showProgressBar()

            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_DIRECTION, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }




        tvModeBtn.setOnClickListener {
            vibrateOnButtonClick()
            showProgressBar()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_MODE, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }



        tvSpeedBtn.setOnClickListener {
            vibrateOnButtonClick()
            showProgressBar()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_FAN_SPEED, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }


        llTempMinus.setOnClickListener({
            vibrateOnButtonClick()
            showProgressBar()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_TEMP_DOWN, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        })



        llTempPlus.setOnClickListener {
            vibrateOnButtonClick()
            showProgressBar()
            sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex, LibreMavidHelper.AC_REMOTE_CONTROLS.AC_TEMP_UP, object : OnRemoteKeyPressedInterface {
                override fun onKeyPressed(isSuccess: Boolean) {
                    dismissLoader()
                    if (isSuccess) {
                        showSucessfullMessage()
                    } else {
                        showErrorMessage()
                    }
                }
            })
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs()
        }
    }

    private fun getDeviceDetailsLdapi6ToReadAcCurrentConfigs() {
        Handler().postDelayed({
            getDeviceDetailsLdapi6ToReadAcCurrentConfigs(deviceInfo!!.ipAddress)
        }, 500)
    }


    private fun vibrateOnButtonClick() {
        if (Build.VERSION.SDK_INT >= 26) {
            vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibe?.vibrate(VibrationEffect.createOneShot(150, 10))
        } else {
            vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibe?.vibrate(150)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myHandler?.removeCallbacksAndMessages(null)
    }


    fun getDeviceDetailsLdapi6ToReadAcCurrentConfigs(ip: String) {
        LibreMavidHelper.sendCustomCommands(ip, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST,
                buildPayloadForLdapi6().toString(), object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {

                dismissLoader()

                val dataJsonObject = JSONObject(messageInfo?.message)

                Log.d(TAG, "ldapi_#6_Response".plus(dataJsonObject).toString())


                var status = dataJsonObject.getString("Status")

                if (status == "3") {
                    //sucess

                    var payloadJSONObject = dataJsonObject.getJSONObject("payload")

                    var currentMode = payloadJSONObject.getString("current_mode")

                    var modesJsonArray = payloadJSONObject.getJSONArray("modes")

                    modelLdapi6ResponsModesList = ArrayList()

                    for (i in 0 until modesJsonArray.length()) {
                        modelLdapi6ResponsModesList.add(parseTheLdapi6ModeResponse(modesJsonArray[i] as JSONObject))
                    }

                    setTheAcCurrentSettings(currentMode)
                } else {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAcApplianceActivity,
                            this@IRAcApplianceActivity.window?.decorView!!.findViewById(android.R.id.content), "There seems to be an error")
                }


            }

            override fun failure(e: Exception?) {
                dismissLoader()
                Log.d(TAG, "ldapi6_exception".plus(e.toString()))
                dismissLoader()
            }

            override fun success() {

            }
        })
    }


    fun parseTheLdapi6ModeResponse(jsonObject: JSONObject): ModelLdapi6Response {
        var modelLdapi6Response = ModelLdapi6Response()

        modelLdapi6Response.mode = jsonObject.getString("mode")
        modelLdapi6Response.temperature = jsonObject.getString("temperature")

        modelLdapi6Response.fanSpeed = jsonObject.getString("fan_speed")
        modelLdapi6Response.swing = jsonObject.getString("swing")

        modelLdapi6Response.direction = jsonObject.getString("direction")

        return modelLdapi6Response
    }

    fun setTheAcCurrentSettings(currentMode: String) {
        for (modelLdapi6Response: ModelLdapi6Response in modelLdapi6ResponsModesList) {
            /** current mode is equal to the one
             * ie current mode = Cooling and
             * modelLdapi6Response.mode is also the same
             * */
            if (modelLdapi6Response.mode == currentMode) {
                this.runOnUiThread {
                    setTheUIForAc(modelLdapi6Response.temperature,
                            modelLdapi6Response.mode, modelLdapi6Response.fanSpeed, modelLdapi6Response.swing, modelLdapi6Response.direction)

                }
                break
            }
        }
    }


    fun setTheUIForAc(temp: String, acMode: String, acFanSpeed: String, acSwing: String, acDirection: String) {

        if (temp.equals("default", true)) {
            tvTempUnit.visibility = View.GONE
            tvCurrentTemp.text = "NA"
        } else {
            tvTempUnit.visibility = View.VISIBLE
            tvCurrentTemp.text = temp
        }

        when (acMode) {
            "Heat" -> {
                ivModeIcon.setImageDrawable(this.resources?.getDrawable(R.drawable.ic_sun_white))
                frameBg.setBackgroundColor(resources.getColor(R.color.brand_orange))
            }

            "Dehumidify" -> {
                ivModeIcon.setImageDrawable(this.resources?.getDrawable(R.drawable.ic_ac_cooling))
                frameBg.setBackgroundColor(resources.getColor(R.color.alexaBlue))
            }
            "Auto" -> {
                ivModeIcon.setImageDrawable(this.resources?.getDrawable(R.drawable.ic_iv_ac_mode_fan))
                frameBg.setBackgroundColor(resources.getColor(R.color.alexaBlue))
            }
            "Cooling" -> {
                ivModeIcon.setImageDrawable(this.resources?.getDrawable(R.drawable.ic_ac_cooling))
                frameBg.setBackgroundColor(resources.getColor(R.color.alexaBlue))
            }

            //default
            else -> {
                ivModeIcon.setImageDrawable(this.resources?.getDrawable(R.drawable.ic_sun_white))
                frameBg.setBackgroundColor(resources.getColor(R.color.alexaBlue))
            }
        }

        tvAcMode.text = acMode

        if (acFanSpeed.equals("default", true)) {
            tvFanSpeed.text = "NA"
        } else {
            tvFanSpeed.text = acFanSpeed
        }

        if (acDirection.equals("default", true)) {
            tvAcDirection.text = "NA"
        } else {
            tvAcDirection.text = acDirection
        }

        if (acSwing.equals("default", true)) {
            tvAcSwing.text = "NA"
        } else {
            tvAcSwing.text = acSwing
        }

        currentMode = acMode

        //if there is only one mode for that ac then disable the mode.
        tvModeBtn.isEnabled = modelLdapi6ResponsModesList.size != 1

        mHandler.sendEmptyMessage(DISABLE_AC_REMOTE_BUTTONS)

    }


    fun buildPayloadForLdapi6(): JSONObject {
        val paylodJsonObject = JSONObject()
        paylodJsonObject.put("ID", 6)

        val dataJSONObject = JSONObject()
        dataJSONObject.put("appliance", 3)//type of the appliance
        dataJSONObject.put("rId", getSelectedRemoteId())//remote if the selected user
        //dataJSONObject.put("group", modelAcRemoteDetails?.groupId)
        dataJSONObject.put("index", remoteIndex)

        paylodJsonObject.put("data", dataJSONObject)

        Log.d(TAG, "ldapi6_payload_data".plus(paylodJsonObject.toString()))

        return paylodJsonObject
    }


    fun setMaxAndMinTemp(minTemp: Int, maxTemp: Int, currentTemp: Int) {

        when (currentTemp) {
            minTemp -> {
                ivTempMinus.isEnabled = false
                llTempMinus.isEnabled = false

                ivTempPlus.isEnabled = true
                llTempPlus.isEnabled = true
            }
            maxTemp -> {
                ivTempMinus.isEnabled = true
                llTempMinus.isEnabled = true

                ivTempPlus.isEnabled = false
                llTempPlus.isEnabled = false
            }
            else -> {
                ivTempMinus.isEnabled = true
                llTempMinus.isEnabled = true

                ivTempPlus.isEnabled = true
                llTempPlus.isEnabled = true
            }
        }
    }

    fun disableNotWorkingButtons(currentMode: String) {
        for (modelLdapi2AcModes: ModelLdapi2AcModes in modelLdapi2AcModesList) {

            Log.d(TAG.toString(), "workingModesOfAc: ".plus(modelLdapi2AcModes.mode))

            if (modelLdapi2AcModes.mode.equals(currentMode)) {

                tvDirectionBtn.isEnabled = modelLdapi2AcModes.directionAllowed

                if (modelLdapi2AcModes.tempAllowed) {
                    ivTempMinus.isEnabled = true
                    ivTempPlus.isEnabled = true

                    if (tvCurrentTemp.text.toString().isNotEmpty() && tvCurrentTemp.text.toString().isNotEmpty()) {
                        setMaxAndMinTemp(modelLdapi2AcModes.minTemp, modelLdapi2AcModes.maxTemp, tvCurrentTemp.text.toString().toInt())
                    }
                } else {
                    ivTempMinus.isEnabled = false
                    ivTempPlus.isEnabled = false
                }

                tvSpeedBtn.isEnabled = modelLdapi2AcModes.speedAllowed

                tvSwing.isEnabled = modelLdapi2AcModes.swingAllowed
            }
        }
    }


}