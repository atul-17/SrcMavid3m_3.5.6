package com.mavid.fragments.irMultipleRemotes


import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.gson.reflect.TypeToken
import com.libre.irremote.R
import com.libre.irremote.fragments.IRRemoteControlNumberFragment
import com.libre.irremote.irActivites.IRTvpBrandActivity
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo

import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.*
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibBackButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibSourceButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvExitButtonAlt
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibFastForwardButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibInfoButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibMoreButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibNextButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibPauseButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibPlayButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibPowerButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibPrevButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibRewindButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibStopButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ibVolumeMuteButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ivDownButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ivLeftButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ivRightButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.ivUpButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.llRemoteUi
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.rlRecButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvBlueButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvChDownButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvChUpButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvGreenButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvGuideButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvHomeButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvLangButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvMenuButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvOkButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvOptionButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvRedButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvVolMinusButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvVolPlusButton
import kotlinx.android.synthetic.main.fragment_ir_tvp_layout.tvYellowButton

class IRTVPApplianceActivity : IRRemoteVPBaseActivity() {

    private var vibe: Vibrator? = null
    var bundle = Bundle()

    var deviceInfo: DeviceInfo? = null;

    var workingTvpRemoteButtonsHashMap: HashMap<String, String> = HashMap()


    var preDefinedRemoteButtonsHashmap: HashMap<String, String> = HashMap()

    var remoteIndex: Int = 1
    var remoteId: String? = ""

    var workingRemoteButtonsString: String? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_ir_tvp_layout;
    }

    override fun getSelectedAppliance(): Int {
        return 2
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


        llSelectTvp.visibility = View.GONE
        llRemoteUi.visibility = View.VISIBLE

        bundle = intent.extras!!

        buildrogressDialog()

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
            workingRemoteButtonsString = bundle.getString("workingRemoteData")
            remoteIndex = bundle.getInt("remoteIndex")
            remoteId = bundle.getString("remoteIndex")
        }

        val type = object : TypeToken<HashMap<String?, String?>?>() {}.type

        if (workingRemoteButtonsString!!.isNotEmpty()) {
            addPreDefinedButtonsToHashmap()
            workingTvpRemoteButtonsHashMap = gson?.fromJson(workingRemoteButtonsString, type) as HashMap<String, String>
            disableNotWorkingButtons()
        }


        tvSelectTVP.setOnClickListener {
            val intent = Intent(this, IRTvpBrandActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        initClickListnersForRemoteButtons()
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


    fun disableNotWorkingButtons() {

        Log.d(TAG, "workingRemoteButtonsTVP: ".plus(workingTvpRemoteButtonsHashMap))

        for (preDefinedRemoteButtonObject: Map.Entry<String, String> in preDefinedRemoteButtonsHashmap) {
            if (!workingTvpRemoteButtonsHashMap.containsKey(preDefinedRemoteButtonObject.key)) {

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
                        if (tvpSelectedBrand == "Tata Sky") {
                            if (!workingTvpRemoteButtonsHashMap.containsKey(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SELECT_BUTTON)) {
                                tvOkButton.isEnabled = false
                                tvOkButton.setTextColor(resources.getColor(R.color.light_gray))
                            } else {
                                tvOkButton.isEnabled = true
                                tvOkButton.setTextColor(resources.getColor(R.color.black))
                            }
                        } else {
                            tvOkButton.isEnabled = false
                            tvOkButton.setTextColor(resources.getColor(R.color.light_gray))
                        }
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


        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.ZERO_NOS_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.ONE_NOS_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.TWO_NOS_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.THREE_NOS_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.FOUR_NOS_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.FIVE_NOS_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SIX_NOS_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.SEVEN_NOS_BUTTON, "1")

        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.EIGHT_NOS_BUTTON, "1")
        preDefinedRemoteButtonsHashmap.put(LibreMavidHelper.REMOTECONTROLBUTTONNAME.NINE_NOS_BUTTON, "1")

    }


    private fun initClickListnersForRemoteButtons() {

        //var buttom = findViewById<Button>(R.id.ibPowerButton)
        ibPowerButton.setOnClickListener {
            ibPowerButtonOnClickListener()
        }


        ibMoreButton.setOnClickListener({ ibMoreButtonOnClickListener() })

        ibSourceButton.setOnClickListener({ ibSourceButtonOnClickListener() })

        tvHomeButton.setOnClickListener({ tvHomeButtonOnClickListener() })

        tvMenuButton.setOnClickListener({ tvMenuButtonOnClickListener() })

        tvOptionButton.setOnClickListener({ tvOptionButtonOnClickListener() })

        tvGuideButton.setOnClickListener({ tvGuideButtonOnClickListener() })

        tvLangButton.setOnClickListener({ tvLangButtonOnClickListener() })

        ibVolumeMuteButton.setOnClickListener({ ibVolumeMuteButtonOnClickListener() })

        ibBackButton.setOnClickListener({ ibBackButtonOnClickListener() })

        ibPrevButton.setOnClickListener({ ibPrevButtonOnClickListener() })

        ibNextButton.setOnClickListener({ ibNextButtonOnClickListener() })

        tvVolPlusButton.setOnClickListener({ tvVolPlusButtonOnClickListener() })

        tvVolMinusButton.setOnClickListener({ tvVolMinusButtonOnClickListener() })


        ivUpButton.setOnClickListener({ ivUpButtonOnClickListener() })

        ivDownButton.setOnClickListener({ ivDownButtonOnClickListener() })


        ivLeftButton.setOnClickListener({ ivLeftButtonOnClickListener() })

        ivRightButton.setOnClickListener({ ivRightButtonOnClickListener() })


        tvChUpButton.setOnClickListener({ tvChUpButtonOnClickListener() })
        tvChDownButton.setOnClickListener({ tvChDownButtonOnClickListener() })


        tvOkButton.setOnClickListener({ tvOkButtonOnClickListener() })
        tvBlueButton.setOnClickListener({ tvBlueButtonOnClickListener() })

        tvGreenButton.setOnClickListener({ tvGreenButtonOnClickListener() })
        tvYellowButton.setOnClickListener({ tvYellowButtonOnClickListener() })

        tvRedButton.setOnClickListener({ tvRedButtonOnClickListener() })
        ibRewindButton.setOnClickListener({ ibRewindButtonOnClickListener() })

        ibPlayButton.setOnClickListener({ ibPlayButtonOnClickListener() })
        ibPauseButton.setOnClickListener({ ibPauseButtonOnClickListener() })

        ibFastForwardButton.setOnClickListener({ ibFastForwardButtonOnClickListener() })
        ibInfoButton.setOnClickListener({ ibInfoButtonOnClickListener() })

        ibStopButton.setOnClickListener({ ibStopButtonOnClickListener() })
        tvExitButtonAlt.setOnClickListener({ tvExitButtonAltOnClickListener() })

        rlRecButton.setOnClickListener({ rlRecButtonOnClickListener() })

    }


    fun showRemoteControlNumberButton() {
        hideApplianceVp()

        hideTabLayout()
        frameContent?.visibility = View.VISIBLE

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()

        fragmentTransaction?.setCustomAnimations(R.anim.fade_in, R.anim.fade_in, R.anim.fade_out, R.anim.fade_out);


        bundle.putInt("remoteIndex", remoteIndex)
        val iRRemoteControlNumberFragment = IRRemoteControlNumberFragment()


        fragmentTransaction?.add(R.id.frameContent, iRRemoteControlNumberFragment, TAG)
                ?.addToBackStack(TAG)
        fragmentTransaction?.commit()
    }


    fun ibPowerButtonOnClickListener() {
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

    fun ibMoreButtonOnClickListener() {
        dismissLoader()
        showRemoteControlNumberButton()
    }

    fun ibSourceButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvHomeButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvMenuButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvOptionButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvGuideButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvLangButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ibVolumeMuteButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ibBackButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ibPrevButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ibNextButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvVolPlusButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvVolMinusButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ivUpButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ivDownButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ivLeftButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ivRightButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvChUpButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvChDownButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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


    fun tvOkButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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


    fun tvBlueButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvGreenButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvYellowButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun tvRedButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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

    fun ibRewindButtonOnClickListener() {
        vibrateOnButtonClick()
        showProgressBar()
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
        vibrateOnButtonClick()
        showProgressBar()
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
        vibrateOnButtonClick()
        showProgressBar()
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
        vibrateOnButtonClick()
        showProgressBar()
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
        vibrateOnButtonClick()
        showProgressBar()
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
        vibrateOnButtonClick()
        showProgressBar()
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
        vibrateOnButtonClick()
        showProgressBar()
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


}