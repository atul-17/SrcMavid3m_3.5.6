package com.libre.irremote.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.libre.irremote.R
import com.libre.irremote.irActivites.IRAddRemoteVPActivity
import com.libre.irremote.irActivites.irMultipleRemotes.IRRemoteVPBaseActivity
import com.libre.libresdk.LibreMavidHelper
import com.libre.irremote.utility.OnRemoteKeyPressedInterface
import kotlinx.android.synthetic.main.fragment_remote_numbers_layout.*

class IRRemoteControlNumberFragment : Fragment(), View.OnClickListener {

    private var vibe: Vibrator? = null

    var remoteIndex: Int = 0

    var bundle = Bundle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_remote_numbers_layout, container, false)
        bundle = arguments!!
        remoteIndex = bundle.getInt("remoteIndex")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
    }

    override fun onClick(view: View?) {
        vibrateOnButtonClick()

        when (view?.id) {
            R.id.tvRemoteNumberOne -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.ONE_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }

            R.id.tvRemoteNumberTwo -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.TWO_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }

            R.id.tvRemoteNumberThree -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.THREE_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }

            R.id.tvRemoteNumberFour -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.FOUR_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }

            R.id.tvRemoteNumberFive -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.FIVE_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }
            R.id.tvRemoteNumberSix -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.SIX_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }
            R.id.tvRemoteNumberSeven -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.SEVEN_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }
            R.id.tvRemoteNumberEight -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.EIGHT_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }
            R.id.tvRemoteNumberNine -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.NINE_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }
            R.id.tvRemoteNumberZero -> {
                getActivityObject()?.sendTheKeysPressedIntoTheMavid3MDevice(remoteIndex,
                        LibreMavidHelper.REMOTECONTROLBUTTONNAME.ZERO_NOS_BUTTON, object : OnRemoteKeyPressedInterface {
                    override fun onKeyPressed(isSuccess: Boolean) {
                        if (isSuccess) {
                            getActivityObject()?.showSucessfullMessage()
                        } else {
                            getActivityObject()?.showErrorMessage()
                        }
                    }
                })
            }
            R.id.tvRemoteNumberCancel -> {
                getActivityObject()?.onBackPressed()
            }
        }
    }


    private fun vibrateOnButtonClick() {
        if (Build.VERSION.SDK_INT >= 26) {
            vibe = getActivityObject()?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibe?.vibrate(VibrationEffect.createOneShot(150, 10))
        } else {
            vibe = getActivityObject()?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibe?.vibrate(150)
        }
    }

    fun getActivityObject(): IRRemoteVPBaseActivity? {

        return activity as IRRemoteVPBaseActivity
    }

    fun initButton() {
        tvRemoteNumberOne.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberTwo.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberThree.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberFour.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberFive.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberSix.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberSeven.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberEight.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberNine.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberZero.setOnClickListener(this@IRRemoteControlNumberFragment)
        tvRemoteNumberCancel.setOnClickListener(this@IRRemoteControlNumberFragment)
    }
}