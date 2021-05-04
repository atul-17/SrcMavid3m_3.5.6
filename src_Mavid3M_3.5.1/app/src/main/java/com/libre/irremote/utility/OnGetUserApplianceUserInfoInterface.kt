package com.libre.irremote.utility

import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo

interface OnGetUserApplianceUserInfoInterface {
    fun onApiResponseCallback(deviceInfo: DeviceInfo,bodyObject: String)
}