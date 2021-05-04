package com.libre.irremote.irActivites

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.adapters.ShowIRDeviceListItemAdapter
import com.libre.irremote.BaseActivity
import com.libre.irremote.Constants.Constants
import com.libre.irremote.R
import com.libre.irremote.utility.ApiSucessCallback
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.ModelEndPointObject
import com.libre.irremote.models.ModelIRDevice
import kotlinx.android.synthetic.main.show_all_ir_device_list_activity.*
import org.json.JSONArray
import org.json.JSONException

import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class ShowAllIrDeviceListActivity : BaseActivity() {


    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    var modelIRDeviceList: MutableList<ModelIRDevice> = ArrayList()

    var endPointObjectList: MutableList<ModelEndPointObject> = ArrayList()


    var showIRDeviceAdapterType: ShowIRDeviceListItemAdapter? = null

    internal var emailAuthAndTokenRefreshApis = EmailAuthAndTokenRefreshApis()


    var bundle = Bundle()

    lateinit var deviceInfo: DeviceInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_all_ir_device_list_activity)

        bundle = intent.extras!!

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
        }



        llAddDevice.setOnClickListener {
            val intent = Intent(this@ShowAllIrDeviceListActivity, IRShowAppliancesActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("deviceInfo", deviceInfo)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        VolleyLog.DEBUG = true;

        showProgressBar()
        getAllIRDevices()

    }


    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }

    fun setupAdapter() {

        for (modelIRDevice in modelIRDeviceList) {

            if (modelIRDevice.deviceId == deviceInfo.usn) {
                if (modelIRDevice.modelEndPointObjectList.size > 0) {
                    Log.d("atul_models", modelIRDevice.modelEndPointObjectList.size.toString())
                    tvNoDevices.visibility = View.GONE
                    showIRDeviceAdapterType = ShowIRDeviceListItemAdapter(this@ShowAllIrDeviceListActivity, modelIRDevice.modelEndPointObjectList)
                } else {
                    tvNoDevices.visibility = View.VISIBLE
                }
            }
        }
        rvIrDeviceList.layoutManager = LinearLayoutManager(this@ShowAllIrDeviceListActivity)

        rvIrDeviceList.adapter = showIRDeviceAdapterType

    }

    fun getAllIRDevices() {

        val url: String = Constants.BASEURL.plus(Constants.RestApis.ADD_DEVICE_TO_USER)

        Log.d("atul_url", url)

        val requestQueue = Volley.newRequestQueue(this@ShowAllIrDeviceListActivity)


        val bodyObject: JSONObject = JSONObject()

        bodyObject.put("nameSpace", "IR_BLASTER")
        bodyObject.put("action", "getAll")

        val payloadObject: JSONObject = JSONObject()

        payloadObject.put("emailId", getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("email", ""))

        bodyObject.put("payload", payloadObject)


        val requestBody: String = bodyObject.toString()

        Log.d("atul_body", requestBody)

        val stringRequest = @RequiresApi(Build.VERSION_CODES.KITKAT)
        object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->

                    Log.d("atul_response", response.toString())


                    hideProgressBar()

                    try {
                        val deviceJsonArray: JSONArray = JSONArray(response)

                        if (deviceJsonArray.length() > 0) {

                            for (i in 0 until deviceJsonArray.length()) {

                                val deviceJsonObject: JSONObject = deviceJsonArray[i] as JSONObject

                                endPointObjectList = ArrayList()

                                val modelIRDevice = ModelIRDevice()

                                modelIRDevice.deviceId = deviceJsonObject.getString("deviceId")

                                modelIRDevice.friendlyName = deviceJsonObject.getString("friendlyName")

                                val endpointJsonArray = deviceJsonObject.getJSONArray("endpointList")


                                for (j in 0 until endpointJsonArray.length()) {

                                    val endpointJsonObject = endpointJsonArray[j] as JSONObject

                                    val modelEndPointObject = ModelEndPointObject()

                                    modelEndPointObject.id = endpointJsonObject.getString("id")
                                    modelEndPointObject.friendlyName = endpointJsonObject.getString("friendlyName")
                                    modelEndPointObject.type = endpointJsonObject.getString("type")

                                    endPointObjectList.add(modelEndPointObject)
                                }

                                modelIRDevice.modelEndPointObjectList = endPointObjectList
                                modelIRDeviceList.add(modelIRDevice)
                            }

                            Log.d("atul_irDeviceList", modelIRDeviceList.toString())

                            setupAdapter()
                        }
                    } catch (ex: JSONException) {
                        val responseObject = JSONObject(response)
                        val code: String = responseObject.getString("code")
                        emailAutroziationApi.buildSnackBar("An Error has occured, please try again after some time", true, this@ShowAllIrDeviceListActivity, null);

                        Log.d("atul_error", response)
                    }
                },

                Response.ErrorListener { volleyError ->


                    Log.d("atul_volley_error", volleyError.networkResponse.statusCode.toString())


                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                        hideProgressBar()
                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
                                this@ShowAllIrDeviceListActivity, null)

                    } else if (volleyError is AuthFailureError) {

                        //token is expired call refresh token
                        emailAuthAndTokenRefreshApis.getAccessTokenApi(
                                getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("refreshToken", "")!!,
                                this@ShowAllIrDeviceListActivity, object : ApiSucessCallback {
                            override fun onSucess(code: String?, message: String?) {
                                hideProgressBar()
                                if (code == "0") {
                                    getAllIRDevices()
                                } else {
                                    emailAutroziationApi.buildSnackBar("Something went wrong, please try again later",
                                            true, this@ShowAllIrDeviceListActivity, null)
                                }
                            }
                        })

                    } else if (volleyError is ServerError) {
                        hideProgressBar()
                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
                                true, this@ShowAllIrDeviceListActivity, null)

                    } else if (volleyError is NetworkError) {
                        hideProgressBar()
                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
                                true, this@ShowAllIrDeviceListActivity, null)

                    } else if (volleyError is ParseError) {
                        hideProgressBar()
                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true, this@ShowAllIrDeviceListActivity, null)

                    } else {
                        hideProgressBar()
                    }

                }) {
            override fun getHeaders(): MutableMap<String, String> {

                Log.d("atul_token", getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("accessToken", "")!!)

                val params = Hashtable<String, String>()

                params["Content-Type"] = "application/json"
                params["authorization"] = "Bearer  ${getSharedPreferences("Mavid", Context.MODE_PRIVATE)
                        .getString("accessToken", "")}"

                return params
            }

            override fun getBody(): ByteArray {

                return requestBody.toByteArray(Charsets.UTF_8)

            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        requestQueue.add(stringRequest)


    }

}