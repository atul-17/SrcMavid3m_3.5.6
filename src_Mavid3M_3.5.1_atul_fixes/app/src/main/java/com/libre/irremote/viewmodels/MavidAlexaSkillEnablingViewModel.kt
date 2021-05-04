package com.libre.irremote.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.libre.irremote.Constants.Constants
import com.libre.irremote.models.ModelAlexaSkillResponse
import com.libre.irremote.models.ModelAlexaSkillResponseError
import com.libre.irremote.models.ModelAlexaSkillResponseSucess
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo
import com.libre.libresdk.Util.LibreLogger
import org.json.JSONException
import org.json.JSONObject

class MavidAlexaSkillEnablingViewModel(application: Application) : AndroidViewModel(application) {


    var getModelAlexaSkillResponse: MutableLiveData<ModelAlexaSkillResponse>? = null


    fun getAlexaStatus(status: Int, authCode: String?, ipAddress: String): LiveData<ModelAlexaSkillResponse?>? {

        getModelAlexaSkillResponse = MutableLiveData()

        when (status) {

            /** Disable */
            0 -> {
                LibreLogger.d(this, "atul calling disable api :")
                enableApiOrDisableApiOrStatusApiForAlexaSkillLinking(buildPayloadForAlexaSkillDisableOrCheckStatus(status), ipAddress)
            }

            /**Enable */
            1 -> {
                LibreLogger.d(this, "atul calling Enable api ")
                enableApiOrDisableApiOrStatusApiForAlexaSkillLinking(buildPayloadForAlexaSkillEnablement(authCode!!, status), ipAddress)
            }

            /** status*/
            2 -> {
                LibreLogger.d(this, "atul calling status api :")
                enableApiOrDisableApiOrStatusApiForAlexaSkillLinking(buildPayloadForAlexaSkillDisableOrCheckStatus(status), ipAddress)
            }
        }


        return getModelAlexaSkillResponse
    }

    fun enableApiOrDisableApiOrStatusApiForAlexaSkillLinking(payloadData: String, ipAddress: String?) {


        LibreLogger.d(this, "atul_alexa_status_data:".plus(payloadData))

        LibreMavidHelper.sendCustomCommands(ipAddress,
                LibreMavidHelper.COMMANDS.ALEXA_SKILL_LINKING, payloadData, object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {
                var dataJsonObject: JSONObject? = null

                LibreLogger.d(this, "atul_alexa_status_data: ".plus(messageInfo?.message))

                var modelAlexaSkillResponse = ModelAlexaSkillResponse()

                modelAlexaSkillResponse.modelAlexaSkillResponseSucess = ModelAlexaSkillResponseSucess()

                try {
                    dataJsonObject = JSONObject(messageInfo!!.message)

                    val statusCode = dataJsonObject.getInt("Status")

                    if (dataJsonObject.has("payload")) {

                        val payloadJsonObject = dataJsonObject.getJSONObject("payload")

                        if (payloadJsonObject.has("status")) {

                            val enabledStatus = payloadJsonObject.getString("status")

                            modelAlexaSkillResponse.modelAlexaSkillResponseSucess?.enabledAlexaStatus = enabledStatus
                        }
                    }

                    modelAlexaSkillResponse.modelAlexaSkillResponseSucess?.alexaSkillStatus = statusCode



                    getModelAlexaSkillResponse?.postValue(modelAlexaSkillResponse)


                } catch (e: JSONException) {

                    LibreLogger.d(this, "atul_in_exception: " + e.toString())

                    var modelAlexaSkillResponseError = ModelAlexaSkillResponseError()

                    modelAlexaSkillResponseError.errorMessage = e.toString()


                    getModelAlexaSkillResponse?.postValue(modelAlexaSkillResponse)
                }
            }

            override fun failure(e: Exception?) {

                LibreLogger.d(this, "atul_exception_alexa_skill".plus(e?.message))

                var modelAlexaSkillResponse = ModelAlexaSkillResponse()
                var modelAlexaSkillResponseError = ModelAlexaSkillResponseError()



                modelAlexaSkillResponseError.errorMessage = e.toString()


                getModelAlexaSkillResponse?.postValue(modelAlexaSkillResponse)
            }

            override fun success() {

            }
        })
    }


    fun buildPayloadForAlexaSkillEnablement(authCode: String, alexaSkillType: Int): String {
        val payloadObject = JSONObject()


        val skillId = Constants.AlexaSkillLinking.SKILL_ID

        var skillPath = Uri.parse("/v0/skills/$skillId/enablement");


        payloadObject.put("id", alexaSkillType)

        payloadObject.put("baseUrl", Constants.AlexaSkillLinking.BASE_URL)

        payloadObject.put("endpointPath", Constants.AlexaSkillLinking.END_POINT_PATH)

        payloadObject.put("skillPath", skillPath)
        payloadObject.put("skillId", skillId)

        payloadObject.put("stage", Constants.AlexaSkillLinking.SKILL_STAGE)

        payloadObject.put("authCode", authCode)

        payloadObject.put("redirectUri", Constants.AlexaSkillLinking.REDIRECT_URI)


        return jsonUrlCorrector(payloadObject.toString())
    }


    private fun jsonUrlCorrector(json_data: String): String {
        var json_data = json_data
        json_data = json_data.replace("\\", "")
        return json_data
    }

    fun buildPayloadForAlexaSkillDisableOrCheckStatus(alexaSkillType: Int): String {


        val payloadObject = JSONObject()


        val skillId = Constants.AlexaSkillLinking.SKILL_ID

        val skillPath = "/v0/skills/$skillId/enablement";

        payloadObject.put("id", alexaSkillType)

        payloadObject.put("baseUrl", Constants.AlexaSkillLinking.BASE_URL)

        payloadObject.put("endpointPath", Constants.AlexaSkillLinking.END_POINT_PATH)
        payloadObject.put("skillPath", skillPath)
        payloadObject.put("skillId", skillId)
        payloadObject.put("stage", Constants.AlexaSkillLinking.SKILL_STAGE)




        return jsonUrlCorrector(payloadObject.toString())
    }
}