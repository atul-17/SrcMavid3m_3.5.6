package com.libre.irremote.irActivites.irMultipleRemotes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.libre.irremote.Constants.RemoteIndexGetter
import com.libre.irremote.R
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo

import com.libre.irremote.irActivites.irMultipleRemotes.remotelistadapters.RemoteListAdapters
import com.libre.irremote.utility.*
import com.libre.libresdk.LibreMavidHelper
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo
import com.libre.libresdk.Util.LibreLogger

import kotlinx.android.synthetic.main.activity_remote_selection.*
import kotlinx.android.synthetic.main.toolbar_custom_layout.*
import org.json.JSONObject
import java.nio.charset.Charset

class IRAddOrSelectRemotesActivity : AppCompatActivity(), OnButtonClickListViewInterface, OnClickOfDeleteButtonInterface {

    var bundle1 = Bundle()

    var deviceInfo: DeviceInfo? = null;

    var remoteListAdapters: RemoteListAdapters? = null

    var remoteList: ArrayList<String>? = null

    var remoteListStored: MutableList<ModelRemoteDetails> = ArrayList();

    var uiRelatedClass = UIRelatedClass()


    lateinit var progressDialog: Dialog


    var TAG = IRAddOrSelectRemotesActivity::class.java.simpleName


    var modelRemoteSubAndMacDetils = ModelRemoteSubAndMacDetils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_remote_selection)

        bundle1 = intent.extras!!

        if (bundle1 != null) {
            deviceInfo = bundle1.getSerializable("deviceInfo") as DeviceInfo
        }


        buildrogressDialog()

        setRemoteSelectionAdapter()

        id_icon_back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })


        id_add_icon.setOnClickListener {
            val intent = Intent(this@IRAddOrSelectRemotesActivity, IRRemoteSelectionActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }

    }


    fun getSortedRemotesByAppliancesType(remoteListStored: MutableList<ModelRemoteDetails>): List<ModelRemoteDetails> {
        return remoteListStored.sortedBy { it.selectedAppliance.toInt() }
    }

    fun setRemoteSelectionAdapter() {

        val linearLayoutManager = LinearLayoutManager(this@IRAddOrSelectRemotesActivity)

        remoteList = ArrayList<String>()

        var sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val gson = Gson()


        var modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")

        if (modelRemoteDetailsString!!.isNotEmpty()) {


            var modelRemoteSubAndMacDetils = gson?.fromJson<ModelRemoteSubAndMacDetils>(modelRemoteDetailsString,
                    ModelRemoteSubAndMacDetils::class.java) as ModelRemoteSubAndMacDetils

            remoteListStored = modelRemoteSubAndMacDetils.modelRemoteDetailsList

            remoteListStored = getSortedRemotesByAppliancesType(remoteListStored).toMutableList()

            for (i in remoteListStored) {
                remoteList?.add(i.selectedBrandName.plus(" ").plus(i.selectedAppliance.let {
                    var applianceType: String = when (it) {
                        "1",
                        "TV" -> {
                            "TV"
                        }
                        "2",
                        "TVP" -> {
                            "TVP"
                        }
                        "3",
                        "AC" -> {
                            "AC"
                        }
                        else -> {
                            ""
                        }
                    }
                    applianceType
                }))

            }
        }



        remoteListAdapters = RemoteListAdapters(this@IRAddOrSelectRemotesActivity, remoteList!!,
                this, this, remoteListStored)
        remote_list_Recycler_view.adapter = remoteListAdapters
        remote_list_Recycler_view.layoutManager = linearLayoutManager

    }

    override fun onClickOfDelete(modelRemoteDetails: ModelRemoteDetails, pos: Int) {
        showCustomAlertForDeleteConfirmation(modelRemoteDetails, pos)
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


    fun buildrogressDialog() {
        progressDialog = Dialog(this@IRAddOrSelectRemotesActivity)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_bar)
        progressDialog.setCancelable(false)

        val progress_title: AppCompatTextView = progressDialog.findViewById(R.id.progress_title)
        val progress_bar: ProgressBar = progressDialog.findViewById(R.id.progress_bar)
        val progress_message: AppCompatTextView = progressDialog.findViewById(R.id.progress_message)

        progress_message.visibility = View.GONE
        progress_title.text = "Please Wait..."

    }


    private fun showCustomAlertForDeleteConfirmation(modelRemoteDetails: ModelRemoteDetails, pos: Int) {
        uiRelatedClass.showCustomAlertDialogForDeleteConfirmation(this@IRAddOrSelectRemotesActivity, object : OnButtonClickCallback {
            override fun onClick(isSucess: Boolean) {
                if (isSucess) {
                    //call the delete api
                    showProgressBar()


                    deleteRemoteDetailsOnTheDevice(deviceInfo!!.ipAddress, modelRemoteDetails!!,pos)


                }
            }
        }, getDeleteMessageToBeDisplayed(modelRemoteDetails.selectedBrandName, modelRemoteDetails.selectedAppliance))
    }


    private fun getDeleteMessageToBeDisplayed(brandName: String, selectedApplianceType: String): String {

        var displayMessage: String = ""

        when (selectedApplianceType) {
            "1", "TV" -> {
                displayMessage = brandName.plus(" TV")
            }
            "2", "TVP" -> {
                displayMessage = brandName.plus(" TVP")
            }
            "3", "AC" -> {
                displayMessage = brandName.plus(" AC")
            }

        }
        return displayMessage
    }

    fun buildJsonForUserManagmentApis(sub: String, macAddress: String,
                                      modelRemoteDetails: ModelRemoteDetails, operation: String): JSONObject {
        var payLoadObject: JSONObject = JSONObject()


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

            val requestQueue = Volley.newRequestQueue(this@IRAddOrSelectRemotesActivity)

            val url = ApiConstants.BASE_URL_USER_MGT + "Beta/usermangement"

            Log.d(TAG, "deleteBody_$TAG" + buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString())

            var requestBody: String = buildJsonForUserManagmentApis(sub, macAddress, modelRemoteDetails!!, "delete").toString()


            var deleteUserDetailsStringRequest = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->


                val responseObject = JSONObject(response)


                if (responseObject.has("body")) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAddOrSelectRemotesActivity,
                            window?.decorView!!.findViewById(android.R.id.content), responseObject.getString("body"))
                }

                restApiSucessFailureCallbacks.onSucessFailureCallbacks(true, modelRemoteDetails)

                Log.d(TAG, "deleteResponse:".plus(response))


            }, Response.ErrorListener { volleyError ->

                restApiSucessFailureCallbacks.onSucessFailureCallbacks(false, null)

                Log.d(TAG, "Error: ${volleyError.networkResponse.statusCode}")

                dismissLoader()

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAddOrSelectRemotesActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAddOrSelectRemotesActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(this@IRAddOrSelectRemotesActivity,
                                window?.decorView!!.findViewById(android.R.id.content), "Server error occurred, please try again later")
                    }

                } else if (volleyError is NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAddOrSelectRemotesActivity,
                            window?.decorView!!.findViewById(android.R.id.content), "Network error occurred, please try again later")

                } else if (volleyError is ParseError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRAddOrSelectRemotesActivity,
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


    override fun onClickListview(position: Int) {
        if (remoteListStored != null && remoteListStored.size > position) {
            var modelRemoteDetails = remoteListStored[position]
            if (modelRemoteDetails.selectedAppliance == "1" || modelRemoteDetails.selectedAppliance == "TV") {

                gotToTvActivity(position)

            } else if (modelRemoteDetails.selectedAppliance == "2" || modelRemoteDetails.selectedAppliance == "TVP") {
                gotToTVPActivity(position)

            } else if (modelRemoteDetails.selectedAppliance == "3" || modelRemoteDetails.selectedAppliance == "AC") {
                gotToAcActivity(position)

            }
        }

    }


    fun buildRemotePayloadJsonForLDAPI0(modelRemoteDetails: ModelRemoteDetails): JSONObject {

        val payloadJsonObject: JSONObject = JSONObject()

        payloadJsonObject.put("ID", 0)

        val dataJsonObject = JSONObject()


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

    /** LDAPI#0 */
    fun deleteRemoteDetailsOnTheDevice(ipAddress: String, modelRemoteDetails: ModelRemoteDetails, pos: Int) {

        Log.d(TAG, "ldapi0Payload: ".plus(buildRemotePayloadJsonForLDAPI0(modelRemoteDetails)))

        val payloadString = buildRemotePayloadJsonForLDAPI0(modelRemoteDetails).toString()

        LibreMavidHelper.sendCustomCommands(ipAddress, LibreMavidHelper.COMMANDS.SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST, payloadString, object : CommandStatusListenerWithResponse {
            override fun response(messageInfo: MessageInfo?) {
                if (messageInfo != null) {
                    val dataJsonObject = JSONObject(messageInfo?.message)

                    val status = dataJsonObject.getInt("Status")

                    Log.d(TAG, "response_from_ldapi0" + messageInfo?.message)

                    //device acknowledged
                    if (status == 2) {
                        //call the cloud delete api

                        deleteUserDevice(getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("sub", "")!!,
                                deviceInfo!!.usn, modelRemoteDetails, object : RestApiSucessFailureCallbacks {
                            override fun onSucessFailureCallbacks(isSucess: Boolean, modelRemoteDetails: ModelRemoteDetails?) {
                                if (isSucess) {
                                    //remove the data present in the device


                                    deleteApplianceFromSharedPref(deviceInfo!!.usn, modelRemoteDetails)

                                    dismissLoader()

                                    remoteListStored.removeAt(pos)
                                    remoteList?.removeAt(pos)
                                    remoteListAdapters?.notifyDataSetChanged()


                                }
                            }
                        })


                    } else if (status == 3) {
                        //error
                        dismissLoader()
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddOrSelectRemotesActivity, "There sees to be an error!!.Please try after some time",
                                "Go Back", this@IRAddOrSelectRemotesActivity)
                    }
                } else {
                    dismissLoader()
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRAddOrSelectRemotesActivity, "There sees to be an error!!.Please try after some time",
                            "Go Back", this@IRAddOrSelectRemotesActivity)
                }
            }

            override fun failure(e: java.lang.Exception?) {
                dismissLoader()
                Log.d(TAG, "deletingRemoteDetailsException$e")
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
                            Log.d(TAG, "deletedFromSharedPref".plus(storedRemoteDetails.selectedBrandName))
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

    fun gotToTvActivity(pos: Int) {
        val gson = Gson()
        val intent = Intent(this, IRTelevisionApplianceActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putInt("remoteIndex", remoteListStored[pos].index)
        bundle.putString("remoteId", remoteListStored[pos].remoteId)
        bundle.putString("workingRemoteData", gson.toJson(remoteListStored[pos].remotesHashMap))
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun gotToAcActivity(pos: Int) {
        val gson = Gson()
        val intent = Intent(this, IRAcApplianceActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putInt("remoteIndex", remoteListStored[pos].index)
        bundle.putString("remoteId", remoteListStored[pos].remoteId)
        bundle.putString("workingRemoteData", gson.toJson(remoteListStored[pos].ac_remotelist))
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun gotToTVPActivity(pos: Int) {
        val gson = Gson()
        val intent = Intent(this, IRTVPApplianceActivity::class.java)
        var bundle = Bundle()
        bundle.putSerializable("deviceInfo", deviceInfo)
        bundle.putInt("remoteIndex", remoteListStored[pos].index)
        bundle.putString("remoteId", remoteListStored[pos].remoteId)
        bundle.putString("workingRemoteData", gson.toJson(remoteListStored[pos].remotesHashMap))
        intent.putExtras(bundle)
        startActivity(intent)
    }


}