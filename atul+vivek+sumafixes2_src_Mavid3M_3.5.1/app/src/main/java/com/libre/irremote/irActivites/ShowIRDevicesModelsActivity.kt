package com.libre.irremote.irActivites

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.adapters.ShowIRDeviceModelTypeAdapter
import com.libre.irremote.BaseActivity
import com.libre.irremote.Constants.Constants
import com.libre.irremote.R
import com.libre.irremote.utility.ApiSucessCallback
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import com.libre.irremote.utility.MaterialDesignIconView
import com.libre.irremote.utility.RecyclerItemClickListener
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.ModelIRDeviceModelsTypes
import kotlinx.android.synthetic.main.show_ir_device_model_activity.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ShowIRDevicesModelsActivity : BaseActivity() {


    var showIRDeviceModelTypeAdapter: ShowIRDeviceModelTypeAdapter? = null


    var modelTypesList: MutableList<ModelIRDeviceModelsTypes> = ArrayList()

    var bundle = Bundle()

    var selectedAppliance = ""

    var selectedMakersName = ""

    var selectedApplianceIcon = ""

    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    var customDialog: Dialog? = null

//    var macAddress: String = "a6:50:df:86:1c:54"

    var modelCode: String = ""


    internal var emailAuthAndTokenRefreshApis = EmailAuthAndTokenRefreshApis()

     var deviceInfo : DeviceInfo?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_ir_device_model_activity)

        bundle = intent.extras!!
        if (bundle != null) {
            selectedAppliance = bundle.getString("selectedAppliance", "")
            selectedMakersName = bundle.getString("selectedMakersName", "")

            selectedApplianceIcon = bundle.getString("selectedApplianceIcon", "")

            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo

        }

        showProgressBar()

        getModelTypeList()
    }


    fun setModelsTypesAdapter() {

        rvModelsTypesList.setHasFixedSize(true)

        showIRDeviceModelTypeAdapter = ShowIRDeviceModelTypeAdapter(this@ShowIRDevicesModelsActivity, modelTypesList)

        val linearLayoutManager = LinearLayoutManager(this@ShowIRDevicesModelsActivity)

        rvModelsTypesList.layoutManager = linearLayoutManager

        rvModelsTypesList.adapter = showIRDeviceModelTypeAdapter





        rvModelsTypesList.addOnItemTouchListener(RecyclerItemClickListener(this@ShowIRDevicesModelsActivity,
                rvModelsTypesList,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        //show a dialog for the name
                        modelCode = modelTypesList[position].modelTypeCode

                        showDialogForIrDeviceName()
                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        Log.d("tag", "do nothing")

                    }
                }))
    }




    fun showDialogForIrDeviceName() {
        if (!this@ShowIRDevicesModelsActivity.isFinishing) {
            customDialog = Dialog(this@ShowIRDevicesModelsActivity)
            customDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            customDialog?.setContentView(R.layout.custom_set_ir_device_name_dialog)
            customDialog?.setCancelable(false)

            if (customDialog != null) {
                val miApplianceIcon: MaterialDesignIconView = customDialog!!.findViewById(R.id.miApplianceIcon)
                val tvApplianceName: AppCompatTextView = customDialog!!.findViewById(R.id.tvApplianceName)
                val etIRDeviceName: AppCompatEditText = customDialog!!.findViewById(R.id.etIRDeviceName)
                val btnCancel: AppCompatButton = customDialog!!.findViewById(R.id.btnCancel)
                val btnSubmit: AppCompatButton = customDialog!!.findViewById(R.id.btnSubmit)


                tvApplianceName.text = selectedAppliance

                btnCancel.setOnClickListener {
                    customDialog?.dismiss()
                }

                btnSubmit.setOnClickListener {
                    if (etIRDeviceName.text.toString().isNotEmpty()) {
                        //call the post api
                        showProgressBar()
                        postIrDeviceApi(etIRDeviceName.text.toString())
                        customDialog?.dismiss()
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    miApplianceIcon.text = Html.fromHtml("&#x$selectedApplianceIcon;", Html.FROM_HTML_MODE_LEGACY)
                } else {
                    miApplianceIcon.text = Html.fromHtml("&#x$selectedApplianceIcon;")
                }


                customDialog?.show()
            }
        }
    }

    fun postIrDeviceApi(IRDeviceName: String) {

        val url: String = Constants.BASEURL.plus(Constants.RestApis.ADD_DEVICE_TO_USER)

        Log.d("atul_url", url)

        val requestQueue = Volley.newRequestQueue(this@ShowIRDevicesModelsActivity)


        val bodyObject: JSONObject = JSONObject()

        bodyObject.put("nameSpace", IRDeviceName)

        bodyObject.put("action", "add")


        val payloadObject = JSONObject()
        //irblaster mac id
        payloadObject.put("deviceId", deviceInfo?.usn)

        //mavidDevice we get in m-search friendlyName
        payloadObject.put("friendlyName", deviceInfo?.friendlyName)


        bodyObject.put("payload", payloadObject)


        val endpointObject: JSONObject = JSONObject()
        //genrate the exact fomat as per instructins

        //Endpointid =  <Macid of the Mavid3 device without colon>::<code of the model>::<Name of the endpoint>
        endpointObject.put("id", setUpEndPointId(modelCode, IRDeviceName))

        //name given by the user in the dialog
        endpointObject.put("friendlyName", IRDeviceName)

        endpointObject.put("type", selectedAppliance)

        endpointObject.put("manufacturerName", "Libre")

        bodyObject.put("endpointDetails", endpointObject)

        val requestBody: String = bodyObject.toString();

        Log.d("atul_body", requestBody)


        val stringRequest = @RequiresApi(Build.VERSION_CODES.KITKAT)
        object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->

                    Log.d("atul_response", response.toString())

                    hideProgressBar()

                    val responseJsonObject: JSONObject = JSONObject(response)
                    val code: String = responseJsonObject.getString("code")

                    val msg = responseJsonObject.getString("msg")

                    if (code == "0") {
                        //data posted
                        val intent = Intent(this@ShowIRDevicesModelsActivity, ShowAllIrDeviceListActivity::class.java)
                        startActivity(intent)
                    } else {
                        //failure
                        emailAutroziationApi.buildSnackBar(msg, true, this@ShowIRDevicesModelsActivity, null)
                    }

                },

                Response.ErrorListener { volleyError ->

                    hideProgressBar()

                    Log.d("atul_volley_error", volleyError.networkResponse.statusCode.toString())


                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
                                this@ShowIRDevicesModelsActivity, null)

                    } else if (volleyError is AuthFailureError) {

//                        emailAutroziationApi.buildSnackBar("AuthFailure error occurred, please try again later",
//                                true, this@ShowIRDevicesModelsActivity, null)

                        //token is expired call refresh token
                        emailAuthAndTokenRefreshApis.getAccessTokenApi(
                                getSharedPreferences("Mavid", Context.MODE_PRIVATE).getString("refreshToken", "")!!,
                                this@ShowIRDevicesModelsActivity, object : ApiSucessCallback {
                            override fun onSucess(code: String?, message: String?) {
                                hideProgressBar()
                                if (code == "0") {
                                    postIrDeviceApi(IRDeviceName)
                                } else {
                                    emailAutroziationApi.buildSnackBar("Something went wrong, please try again later",
                                            true, this@ShowIRDevicesModelsActivity, null)
                                }
                            }
                        })

                    } else if (volleyError is ServerError) {

                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
                                true, this@ShowIRDevicesModelsActivity, null)

                    } else if (volleyError is NetworkError) {

                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
                                true, this@ShowIRDevicesModelsActivity, null)

                    } else if (volleyError is ParseError) {

                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true, this@ShowIRDevicesModelsActivity, null)

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

        requestQueue.add(stringRequest)


    }


    fun removeColonsFromMacAddress(macAddress: String?): String {
        if (macAddress!=null) {
            var macAddressEdited = macAddress?.replace(Regex("""[:,.]"""), "")

            Log.d("atul_macAddress", macAddressEdited)

            return macAddressEdited!!
        }else{
            return ""
        }
    }

    fun setUpEndPointId(modelCode: String, endpointName: String): String {
        return removeColonsFromMacAddress(deviceInfo?.usn).plus("::").plus(modelCode).plus("::").plus(endpointName)
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }


    fun getModelTypeList() {


        val url: String = Constants.RestApis.MANUFACTURESLIST + selectedAppliance + "&make=" + selectedMakersName

        Log.d("atul_url", url)

        val requestQueue = Volley.newRequestQueue(this@ShowIRDevicesModelsActivity)

        val stringRequest = @RequiresApi(Build.VERSION_CODES.KITKAT)
        object : StringRequest(Request.Method.GET, url,
                Response.Listener { response ->

                    val responseJsonObject: JSONObject = JSONObject(response)

                    val applianceJsonArray: JSONArray = responseJsonObject.getJSONArray("MODELS")


                    Log.d("atul_volley", applianceJsonArray.toString())


                    if (applianceJsonArray.length() > 0) {

                        tvHeadingLabel.visibility = View.VISIBLE

                        llNoDevices.visibility = View.GONE

                        for (i in 0 until applianceJsonArray.length()) {

                            val modelTypeObject: JSONObject = applianceJsonArray[i] as JSONObject

                            val modelTypeName: String = modelTypeObject.getString("name")
                            val modelTypeCode: String = modelTypeObject.getString("code")

                            modelTypesList.add(ModelIRDeviceModelsTypes(modelTypeName, modelTypeCode))
                        }

                        setModelsTypesAdapter()

                    } else {
                        tvHeadingLabel.visibility = View.GONE

                        llNoDevices.visibility = View.VISIBLE


                        emailAutroziationApi.buildSnackBar("Seems like they are no  data present.", true,
                                this@ShowIRDevicesModelsActivity, null)
                    }
                    hideProgressBar()
                },

                Response.ErrorListener { volleyError ->

                    hideProgressBar()

                    Log.d("atul_volley_error", volleyError.toString())


                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
                                this@ShowIRDevicesModelsActivity, null)

                    } else if (volleyError is AuthFailureError) {

                        emailAutroziationApi.buildSnackBar("AuthFailure error occurred, please try again later",
                                true, this@ShowIRDevicesModelsActivity, null)


                    } else if (volleyError is ServerError) {

                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
                                true, this@ShowIRDevicesModelsActivity, null)

                    } else if (volleyError is NetworkError) {

                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
                                true, this@ShowIRDevicesModelsActivity, null)

                    } else if (volleyError is ParseError) {

                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true, this@ShowIRDevicesModelsActivity, null)

                    }

                }) {

        }

        requestQueue.add(stringRequest)

    }

}