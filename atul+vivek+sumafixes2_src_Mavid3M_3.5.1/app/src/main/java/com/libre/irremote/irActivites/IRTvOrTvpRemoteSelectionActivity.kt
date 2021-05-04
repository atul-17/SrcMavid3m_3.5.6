package com.libre.irremote.irActivites

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.BaseActivity
import com.libre.irremote.R
import com.libre.irremote.fragments.IRTvOrTvpRemoteSelectionFragment
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.ModelLevelCode
import com.libre.irremote.models.ModelLevelData
import com.libre.irremote.models.ModelSelectRemotePayload
import com.libre.irremote.utility.OnCallingGetApiToGetCustomNames
import com.libre.irremote.utility.OnUserButtonSelection
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import kotlinx.android.synthetic.main.activity_ir_tv_remote_selection.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IRTvOrTvpRemoteSelectionActivity : BaseActivity() {


    val uiRelatedClass = UIRelatedClass()

    lateinit var apiViewModel: ApiViewModel

    val TAG = IRTvOrTvpRemoteSelectionActivity::class.java.simpleName

    var applianceId = 0

    var applianceName = ""

    var LEVEL: Int = 1

    var INDEX: Int = 0

    var ipAddress = ""

    var modelSelectRemotePayload: ModelSelectRemotePayload? = ModelSelectRemotePayload()

    lateinit var progressDialog: Dialog

    var deviceInfo: DeviceInfo? = null

    var selectedApplianceType = ""

    var customNamesHashMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_tv_remote_selection)

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

        getTVOrTVPSelectionData(applianceId, selectedApplianceType)

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

        val requestQueue = Volley.newRequestQueue(this@IRTvOrTvpRemoteSelectionActivity)

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
                uiRelatedClass.buildSnackBarWithoutButton(this@IRTvOrTvpRemoteSelectionActivity,
                        this@IRTvOrTvpRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")
            } else if (volleyError is AuthFailureError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRTvOrTvpRemoteSelectionActivity,
                        this@IRTvOrTvpRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")
            } else if (volleyError is ServerError) {
                if (volleyError.networkResponse.statusCode != 302) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRTvOrTvpRemoteSelectionActivity,
                            this@IRTvOrTvpRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Server error occurred, please try again later")
                }
            } else if (volleyError is NetworkError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRTvOrTvpRemoteSelectionActivity,
                        this@IRTvOrTvpRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Network error occurred, please try again later")
            } else if (volleyError is ParseError) {
                uiRelatedClass.buildSnackBarWithoutButton(this@IRTvOrTvpRemoteSelectionActivity,
                        this@IRTvOrTvpRemoteSelectionActivity.window.decorView.findViewById(android.R.id.content), "Parser error occurred, please try again later")
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
        }


        //Living Room
        popularOptionsHashMap["LIVING ROOM $applianceType"] = deviceInfo!!.usn

        popularOptionsHashMap["${brandName.toUpperCase()} $applianceType"] = deviceInfo!!.usn

        popularOptionsHashMap["BED ROOM $applianceType"] = deviceInfo!!.usn

        popularOptionsHashMap["OFFICE $applianceType"] = deviceInfo!!.usn

        return popularOptionsHashMap
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


    fun buildrogressDialog() {
        progressDialog = Dialog(this@IRTvOrTvpRemoteSelectionActivity)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_bar)
        progressDialog.setCancelable(false)

        val progress_title: AppCompatTextView = progressDialog.findViewById(R.id.progress_title)
        val progress_bar: ProgressBar = progressDialog.findViewById(R.id.progress_bar)
        val progress_message: AppCompatTextView = progressDialog.findViewById(R.id.progress_message)

        progress_message.visibility = View.GONE
        progress_title.text = "Please Wait..."
    }


    fun creteNewFragment(tag: String, modelLevelData: ModelLevelData) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val bundle = Bundle()
        bundle.putSerializable("modelLevelData", modelLevelData)
        val irTelevisionApplianceFragment = IRTvOrTvpRemoteSelectionFragment()
        irTelevisionApplianceFragment.arguments = bundle


        fragmentTransaction.add(R.id.tvRemoteSelectionFrameLayout, irTelevisionApplianceFragment, tag).addToBackStack(tag)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {

        val fm: FragmentManager = supportFragmentManager
        if (fm.backStackEntryCount > 1) {

            fm.popBackStackImmediate()

            val fragment = supportFragmentManager.findFragmentById(R.id.tvRemoteSelectionFrameLayout) as IRTvOrTvpRemoteSelectionFragment?

            fragment?.irButtonListTimerTask?.cancel()
            fragment?.myHandler?.removeCallbacksAndMessages(null)
            //reinit code level index = 0
            fragment?.codeLevelIndex = 0
            fragment?.initViews()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.tvRemoteSelectionFrameLayout) as IRTvOrTvpRemoteSelectionFragment?
            fragment?.irButtonListTimerTask?.cancel()
            fragment?.myHandler?.removeCallbacksAndMessages(null)

            finish()
        }
    }


    fun showCustomAlertForRemoteSelection(buttonName: String, onUserButtonSelection: OnUserButtonSelection) {
        runOnUiThread {
            val alert: Dialog = Dialog(this@IRTvOrTvpRemoteSelectionActivity)
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert.setContentView(R.layout.custom_user_button_selection_alert)

            alert.setCancelable(false)

            val tvAlertTitle = alert.findViewById<AppCompatTextView>(R.id.tvAlertTitle)
            val tvAlertMessage = alert.findViewById<AppCompatTextView>(R.id.tvAlertMessage)
            var applianceType = ""
            when (selectedApplianceType) {
                "1",
                "TV" -> {
                    applianceType = "TV"
                }
                "2",
                "TVP" -> {
                    applianceType = "Set Top Box"
                }

            }
            if (buttonName.equals("POWER", true)) {
                tvAlertMessage.text = "Does the $applianceType Power ON/OFF?"
            } else {
                //for non power button
                tvAlertMessage.text = "Does $applianceType Responds correctly to $buttonName Button?"
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
                incLevel()
                onUserButtonSelection.didTheCommandWork(true)
            }
            alert.show()
        }
    }

    fun parseLevelData(levelJsonArray: JSONArray, modelLevelData: ModelLevelData, level: Int, index: Int): ModelLevelData {

        if (index < levelJsonArray.length()) {

            val levelJsonObject: JSONObject = levelJsonArray[index] as JSONObject

            modelLevelData.index = index

            modelLevelData.level = level

            modelLevelData.key = levelJsonObject.getString("key")

            val codeJsonArray = levelJsonObject.getJSONArray("code")

            modelLevelData.modelLevelCodeList = parseCodeLevelData(codeJsonArray, level)

        } else {

            modelLevelData.key = "No More data"

        }

        return modelLevelData
    }


    fun parseCodeLevelData(codeJsonArray: JSONArray, level: Int): MutableList<ModelLevelCode> {

        var modelLevelCodeList: MutableList<ModelLevelCode> = ArrayList()

        for (j in 0 until codeJsonArray.length()) {

            val codeJsonObject: JSONObject = codeJsonArray[j] as JSONObject

            val modelLevelCode: ModelLevelCode = ModelLevelCode()

            modelLevelCode.codeLevelIndex = j

            val commandJsonArray = codeJsonObject.getJSONArray("command")
            for (k in 0 until commandJsonArray.length()) {
                val value: String = commandJsonArray[k] as String
                modelLevelCode.command = value
            }

            val idJsonArray = codeJsonObject.getJSONArray("id")
            val idList: MutableList<Int> = ArrayList()
            for (m in 0 until idJsonArray.length()) {
                val id: Int = idJsonArray[m] as Int
                idList.add(id)
            }
            modelLevelCode.idList = idList
            val level = level + 1
            if (codeJsonObject.has("level".plus(level))) {
                modelLevelCode.subLevelJsonArray = codeJsonObject.getJSONArray("level".plus(level))
            }

            modelLevelCodeList.add(modelLevelCode)
        }
        return modelLevelCodeList
    }

    fun incLevel(): Int {
        LEVEL + 1
        return LEVEL
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

    fun getTVOrTVPSelectionData(id: Int, selectedApplianceType: String) {

        apiViewModel.getTvSelectJsonDetails(id, selectedApplianceType)?.observe(this, Observer {
            if (it.modelSelectRemotePayload != null) {
                if (it.modelSelectRemotePayload?.applianceBrandId != 0) {
                    if (it.modelSelectRemotePayload?.levelJsonArray != null) {

                        modelSelectRemotePayload = it.modelSelectRemotePayload!!

                        llNoData.visibility = View.GONE

                        creteNewFragment("fragment".plus(LEVEL), parseLevelData(it.modelSelectRemotePayload?.levelJsonArray!!, ModelLevelData(), LEVEL, INDEX))

                    }
                } else {
                    //no data present
                    llNoData.visibility = View.VISIBLE
                    llGoBack.setOnClickListener {
                        finish()
                    }

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRTvOrTvpRemoteSelectionActivity,
                            window.decorView.findViewById(android.R.id.content), "No Data present at the moment for the particular appliance")
                }
            } else {
                //error
                val volleyError = it?.volleyError

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvOrTvpRemoteSelectionActivity, "Seems your internet connection is slow, please try in sometime",
                            "Go Back", this@IRTvOrTvpRemoteSelectionActivity)

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvOrTvpRemoteSelectionActivity, "AuthFailure error occurred, please try again later",
                            "Go Back", this@IRTvOrTvpRemoteSelectionActivity)

                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {

                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvOrTvpRemoteSelectionActivity, "Server error occurred, please try again later",
                                "Go Back", this@IRTvOrTvpRemoteSelectionActivity)
                    }

                } else if (volleyError is NetworkError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvOrTvpRemoteSelectionActivity, "Network error occurred, please try again later",
                            "Go Back", this@IRTvOrTvpRemoteSelectionActivity)


                } else if (volleyError is ParseError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvOrTvpRemoteSelectionActivity, "Parser error occurred, please try again later",
                            "Go Back", this@IRTvOrTvpRemoteSelectionActivity)

                } else {
                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvOrTvpRemoteSelectionActivity, "SomeThing is wrong!!.Please Try after some timer",
                            "Go Back", this@IRTvOrTvpRemoteSelectionActivity)
                }
            }
        })
        dismissLoader()
    }
}