package com.mavid.fragments.irMultipleRemotes




import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import com.google.gson.reflect.TypeToken
import com.libre.irremote.R
import com.libre.irremote.fragments.IRRemoteControlNumberFragment
import com.libre.irremote.irActivites.IRSelectTvOrTVPOrAcRegionalBrandsActivity
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo

import kotlinx.android.synthetic.main.fragment_ir_television_appliance.*


class IRTelevisionApplianceActivity : IRRemoteVPBaseActivity() {

    private var vibe: Vibrator? = null


    var workingTvRemoteButtonsHashMap: HashMap<String, String> = HashMap()

    var bundle: Bundle? = null
    var deviceInfo: DeviceInfo? = null


    var preDefinedRemoteButtonsHashmap: HashMap<String, String> = HashMap()

    var DISABLE_REMOTE_BUTTONS: Int = 0

    var workingRemoteButtonsString: String? = null

    var remoteIndex: Int = 0

    var remoteId: String? = ""

    val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val what: Int = msg!!.what
            when (what) {
                DISABLE_REMOTE_BUTTONS -> {
                    val workingRemoteButtonsString = getSharedPreferences("Mavid", Context.MODE_PRIVATE)!!.getString("workingTVRemoteButtons", "")
                    val type = object : TypeToken<HashMap<String?, String?>?>() {}.type

                    if (workingRemoteButtonsString!!.isNotEmpty()) {
                        addPreDefinedButtonsToHashmap()
                        workingTvRemoteButtonsHashMap = gson?.fromJson(workingRemoteButtonsString, type) as HashMap<String, String>
                        disableNotWorkingButtons()
                    }
                }
            }
        }
    }

    var preDefinedNosButtons: MutableList<String> = ArrayList()


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_ir_television_appliance;
    }

    override fun getSelectedAppliance(): Int {
        return 1
    }

    override fun getDeviceInfoData(): DeviceInfo? {
        return deviceInfo
    }

    override fun getSelectedRemoteIndex(): Int {
        return remoteIndex
    }

    override fun getSelectedRemoteId(): String?{
        return remoteId
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        llSelectTv.visibility = View.GONE
        llRemoteUi.visibility = View.VISIBLE

        bundle = intent.extras!!

        buildrogressDialog()

        if (bundle != null) {
            deviceInfo = bundle?.getSerializable("deviceInfo") as DeviceInfo
            workingRemoteButtonsString = bundle!!.getString("workingRemoteData")
            remoteIndex = bundle!!.getInt("remoteIndex")
            remoteId = bundle!!.getString("remoteId")
        }

        val type = object : TypeToken<HashMap<String?, String?>?>() {}.type

        if (workingRemoteButtonsString!!.isNotEmpty()) {
            addPreDefinedButtonsToHashmap()
            workingTvRemoteButtonsHashMap = gson?.fromJson(workingRemoteButtonsString, type) as HashMap<String, String>
            disableNotWorkingButtons()
        }

        //mHandler.sendEmptyMessage(DISABLE_REMOTE_BUTTONS)

        addRemoteNosButtons()

        tvSelectTV.setOnClickListener {
            val intent = Intent(this, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            bundle.putBoolean("isTv", true)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        initClickListnersForRemoteButtons()

    }


    override fun onDestroy() {
        super.onDestroy()
        mHandler?.removeCallbacksAndMessages(null)
    }


    fun disableNotWorkingButtons() {

        Log.d(TAG, "workingRemoteButtonsTV: ".plus(workingTvRemoteButtonsHashMap))

        for (preDefinedRemoteButtonObject: Map.Entry<String, String> in preDefinedRemoteButtonsHashmap) {
            if (!workingTvRemoteButtonsHashMap.containsKey(preDefinedRemoteButtonObject.key)) {
                //if the working remote buttons does not contain the preDefined button
                //then disable that button
                when (preDefinedRemoteButtonObject.key) {

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.SOURCE_BUTTON -> {
                        ibSourceButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.BACK_BUTTON -> {
                        ibBackButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_DOWN -> {
                        tvChDownButton.isEnabled = false
                    }


                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.REC_BUTTON -> {
                        rlRecButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.EXIT_BUTTON -> {
                        tvExitButtonAlt.isEnabled = false
                    }
                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.STOP_BUTTON -> {
                        ibStopButton.isEnabled = false
                    }


                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.INFO_BUTTON -> {
                        ibInfoButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.FAST_FORWARD_BUTTON -> {
                        ibFastForwardButton.isEnabled = false
                    }
                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.PAUSE_BUTTON -> {
                        ibPauseButton.isEnabled = false
                    }
                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.PLAY_BUTTON -> {
                        ibPlayButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.REWIND_BUTTON -> {
                        ibRewindButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.RED_BUTTON -> {
                        tvRedButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.YELLOW_BUTTON -> {
                        tvYellowButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.GREEN_BUTTON -> {
                        tvGreenButton.isEnabled = false
                    }
                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.BLUE_BUTTON -> {
                        tvBlueButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.OK_BUTTON
                    -> {
                        tvOkButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_UP -> {
                        tvChUpButton.isEnabled = false
                    }


                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.RIGHT_BUTTON -> {
                        ivRightButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.LEFT_BUTTON -> {
                        ivLeftButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.DOWN_BUTTON -> {
                        ivDownButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.UP_BUTTON -> {
                        ivUpButton.isEnabled = false
                    }


                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_DOWN -> {
                        tvVolMinusButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_MUTE_BUTTON -> {
                        ibVolumeMuteButton.isEnabled = false
                    }


                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_UP -> {
                        tvVolPlusButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.NEXT_BUTTON -> {
                        ibNextButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.PREV_BUTTON -> {
                        ibPrevButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.LANG_BUTTON -> {
                        tvLangButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.GUIDE_BUTTON -> {
                        tvGuideButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.OPTION_BUTTON -> {
                        tvOptionButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.MENU_BUTTON -> {
                        tvMenuButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.HOME_BUTTON -> {
                        tvHomeButton.isEnabled = false
                    }

                    LibreMavidHelper.REMOTECONTROLBUTTONNAME.POWER_BUTTON -> {
                        ibPowerButton.isEnabled = false
                    }
                }
            }
        }
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

    fun addPreDefinedButtonsToHashmap() {
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SOURCE_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.BACK_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_DOWN, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.REC_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.EXIT_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.STOP_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.INFO_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.FAST_FORWARD_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.PAUSE_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.PLAY_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.REWIND_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.RED_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.YELLOW_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.GREEN_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.BLUE_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.OK_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SELECT_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_UP, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_DOWN, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.RIGHT_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.LEFT_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.DOWN_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.UP_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_DOWN, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_MUTE_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_UP, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.NEXT_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.PREV_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.LANG_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.GUIDE_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.OPTION_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.MENU_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.HOME_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.POWER_BUTTON, "1")

    }

    fun addRemoteNosButtons() {
        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.ZERO_NOS_BUTTON)

        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.ONE_NOS_BUTTON)

        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.TWO_NOS_BUTTON)

        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.THREE_NOS_BUTTON)


        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.FOUR_NOS_BUTTON)

        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.FIVE_NOS_BUTTON)


        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SIX_NOS_BUTTON)

        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SEVEN_NOS_BUTTON)


        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.EIGHT_NOS_BUTTON)
        preDefinedNosButtons.add(LibreMavidHelper.REMOTECONTROLBUTTONNAME.NINE_NOS_BUTTON)
    }


    private fun initClickListnersForRemoteButtons() {


        ibPowerButton.setOnClickListener {
            onPowerButtonClick()
        }

        ibMoreButton.setOnClickListener({
            dismissLoader()
            showRemoteControlNumberButton()
        })

        ibSourceButton.setOnClickListener({
            ibStopButtonOnClickListener()
        })

        tvHomeButton.setOnClickListener({
            tvHomeButtonOnClick()
        })

        tvMenuButton.setOnClickListener({
            tvMenuButtonOnClickListsener()
        })

        tvOptionButton.setOnClickListener({
            tvOptionButtononClickListsener()
        })

        tvGuideButton.setOnClickListener({
            tvGuideButtononListsener()
        })


        tvLangButton.setOnClickListener({
            tvLangButtononListsener()
        })

        ibVolumeMuteButton.setOnClickListener({
            ibVolumeMuteButtononListsener()
        })

        ibBackButton.setOnClickListener({
            ibBackButtononListsener()
        })

        ibPrevButton.setOnClickListener({
            ibBackButtononListsener()
        })

        ibNextButton.setOnClickListener({
            ibNextButtononListsener()
        })

        tvVolPlusButton.setOnClickListener({
            tvVolPlusButtononListsener()
        })

        tvVolMinusButton.setOnClickListener({
            tvVolMinusButtononListsener()
        })


        ivUpButton.setOnClickListener({
            ivUpButtononListsener()
        })

        ivDownButton.setOnClickListener({
            ivDownButtononListsener()
        })


        ivLeftButton.setOnClickListener({
            ivLeftButtononListsener()
        })

        ivRightButton.setOnClickListener({
            ivRightButtononListsener()
        })


        tvChUpButton.setOnClickListener({
            tvChUpButtononListsener()
        })

        tvChDownButton.setOnClickListener({
            tvChDownButtononListsener()
        })


        tvOkButton.setOnClickListener({
            tvOkButtonListsener()
        })


        tvBlueButton.setOnClickListener({
            tvBlueButtonListener()
        })

        tvGreenButton.setOnClickListener({
            tvGreenButtonListener()
        })

        tvYellowButton.setOnClickListener({
            tvYellowButtonListener()
        })

        tvRedButton.setOnClickListener({
            tvRedButtonListener()
        })

        ibRewindButton.setOnClickListener({
            ibRewindButtonListener()
        })

        ibPlayButton.setOnClickListener({
            ibPlayButtonOnClickListener()
        })


        ibPauseButton.setOnClickListener({
            ibPauseButtonOnClickListener()
        })

        ibFastForwardButton.setOnClickListener({
            ibFastForwardButtonOnClickListener()
        })

        ibInfoButton.setOnClickListener({
            ibInfoButtonOnClickListener()
        })

        ibStopButton.setOnClickListener({
            ibStopButtonOnClickListener()
        })

        tvExitButtonAlt.setOnClickListener({
            tvExitButtonAltOnClickListener()
        })

        rlRecButton.setOnClickListener({
            rlRecButtonOnClickListener()
        })

    }


    fun onPowerButtonClick() {
        vibrateOnButtonClick()
        showProgressBar()
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.POWER_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvHomeButtonOnClick() {

        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.HOME_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })

    }


    fun sourceButtonOnClick() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.SOURCE_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvMenuButtonOnClickListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.MENU_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvOptionButtononClickListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.OPTION_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvGuideButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.GUIDE_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvLangButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.LANG_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ibVolumeMuteButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_MUTE_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ibBackButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.BACK_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ibPrevButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.PREV_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ibNextButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.NEXT_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvVolPlusButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_UP, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvVolMinusButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.VOLUME_DOWN, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ivUpButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.UP_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ivDownButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.DOWN_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ivLeftButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.LEFT_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ivRightButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.RIGHT_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvChUpButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_UP, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun tvChDownButtononListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.CHANNEL_DOWN, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvOkButtonListsener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.OK_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvBlueButtonListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.BLUE_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvGreenButtonListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.GREEN_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvYellowButtonListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.YELLOW_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvRedButtonListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.RED_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun ibRewindButtonListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.REWIND_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun ibPlayButtonOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.PLAY_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun ibPauseButtonOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.PAUSE_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }

    fun ibFastForwardButtonOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.FAST_FORWARD_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun ibInfoButtonOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.INFO_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun ibStopButtonOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.STOP_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun tvExitButtonAltOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.EXIT_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun rlRecButtonOnClickListener() {
        sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                LibreMavidHelper.REMOTECONTROLBUTTONNAME.REC_BUTTON, object : OnRemoteKeyPressedInterface {
            override fun onKeyPressed(isSuccess: Boolean) {
                dismissLoader()
                if (isSuccess) {
                    showSucessfullMessage()
                } else {
                    showErrorMessage()
                }
            }
        })
    }


    fun showRemoteControlNumberButton() {
        hideApplianceVp()

        hideTabLayout()

        frameContent?.visibility = View.VISIBLE

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()

        fragmentTransaction?.setCustomAnimations(R.anim.fade_in, R.anim.fade_in, R.anim.fade_out, R.anim.fade_out);
        val bundle = Bundle()
        bundle.putInt("remoteIndex", remoteIndex)

        val iRRemoteControlNumberFragment = IRRemoteControlNumberFragment()

        iRRemoteControlNumberFragment.setArguments(bundle);


        fragmentTransaction?.add(R.id.frameContent, iRRemoteControlNumberFragment, TAG)
                ?.addToBackStack(TAG)
        fragmentTransaction?.commit()
    }


}
