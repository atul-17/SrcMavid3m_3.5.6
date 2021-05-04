package com.libre.irremote.irActivites

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.adapters.ShowIRDeviceManufactureMakersAdapter
import com.libre.irremote.Constants.Constants
import com.libre.irremote.R
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import com.libre.irremote.utility.RecyclerItemClickListener
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.ModelIRDeviceManufacturerNames
import kotlinx.android.synthetic.main.show_ir_appliances_activity_layout.progressBar
import kotlinx.android.synthetic.main.show_ir_devices_manufactures_types_activity.*
import org.json.JSONArray
import org.json.JSONObject

class ShowIRDevicesManufacturesMakesActivity : AppCompatActivity() {

    var showIRDevicesManufacturesMakesAdapter: ShowIRDeviceManufactureMakersAdapter? = null

    var irMakersList: MutableList<ModelIRDeviceManufacturerNames> = ArrayList()

    var bundle = Bundle()

    var selectedAppliance = ""

    var selectedApplianceIcon = ""

    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    lateinit var deviceInfo : DeviceInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_ir_devices_manufactures_types_activity)

        bundle = intent.extras!!
        selectedAppliance = bundle.getString("selectedAppliance", "")
        selectedApplianceIcon = bundle.getString("selectedApplianceIcon","")

        deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo


        showProgressBar()
        getIRMakersList()
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }


    fun setManufactureMakersAdapter() {

        rvManufactureTypesList.setHasFixedSize(true)

        showIRDevicesManufacturesMakesAdapter = ShowIRDeviceManufactureMakersAdapter(this@ShowIRDevicesManufacturesMakesActivity, irMakersList)

        val linearLayoutManager = LinearLayoutManager(this@ShowIRDevicesManufacturesMakesActivity)

        rvManufactureTypesList.layoutManager = linearLayoutManager

        rvManufactureTypesList.adapter = showIRDevicesManufacturesMakesAdapter





        rvManufactureTypesList.addOnItemTouchListener(RecyclerItemClickListener(this@ShowIRDevicesManufacturesMakesActivity,
                rvManufactureTypesList,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val intent = Intent(this@ShowIRDevicesManufacturesMakesActivity, ShowIRDevicesModelsActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("selectedAppliance",selectedAppliance)
                        bundle.putString("selectedMakersName", irMakersList[position].makersName)
                        bundle.putString("selectedApplianceIcon",selectedApplianceIcon)
                        bundle.putSerializable("deviceInfo",deviceInfo)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        Log.d("tag", "do nothing")
                    }

                }))
    }

    fun getIRMakersList() {

        Log.d("atul_",selectedAppliance)



        val url: String = Constants.RestApis.MANUFACTURESLIST+selectedAppliance+"&make=all"

        Log.d("atul_url", url)

        val requestQueue = Volley.newRequestQueue(this@ShowIRDevicesManufacturesMakesActivity)


        val stringRequest = @RequiresApi(Build.VERSION_CODES.KITKAT)
        object : StringRequest(Request.Method.GET, url,
                Response.Listener { response ->

                    val responseJsonObject: JSONObject = JSONObject(response)

                    val applianceJsonArray: JSONArray = responseJsonObject.getJSONArray("MAKE")


                    Log.d("atul_volley", applianceJsonArray.toString())


                    if (applianceJsonArray.length() > 0) {

                        tvHeadingLabel.visibility = View.VISIBLE

                        llNoDevices.visibility = View.GONE

                        for (i in 0 until applianceJsonArray.length()) {

                            val makersString: String = applianceJsonArray[i] as String

                            irMakersList.add(ModelIRDeviceManufacturerNames(makersString))
                        }

                        setManufactureMakersAdapter()

                    } else {

                        tvHeadingLabel.visibility = View.GONE

                        llNoDevices.visibility = View.VISIBLE

                        emailAutroziationApi.buildSnackBar("Seems like they are no  data present.", true,
                                this@ShowIRDevicesManufacturesMakesActivity, null)
                    }
                    hideProgressBar()
                },

                Response.ErrorListener { volleyError ->

                    hideProgressBar()

                    Log.d("atul_volley_error", volleyError.toString())


                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
                                this@ShowIRDevicesManufacturesMakesActivity, null)

                    } else if (volleyError is AuthFailureError) {

                        emailAutroziationApi.buildSnackBar("AuthFailure error occurred, please try again later",
                                true, this@ShowIRDevicesManufacturesMakesActivity, null)


                    } else if (volleyError is ServerError) {

                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
                                true, this@ShowIRDevicesManufacturesMakesActivity, null)

                    } else if (volleyError is NetworkError) {

                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
                                true, this@ShowIRDevicesManufacturesMakesActivity, null)

                    } else if (volleyError is ParseError) {

                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true, this@ShowIRDevicesManufacturesMakesActivity, null)

                    }

                }) {

        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        requestQueue.add(stringRequest)

    }

}