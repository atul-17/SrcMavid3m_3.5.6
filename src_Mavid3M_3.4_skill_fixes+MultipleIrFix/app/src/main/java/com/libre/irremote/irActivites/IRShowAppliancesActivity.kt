package com.libre.irremote.irActivites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.libre.irremote.adapters.IRAddDeviceAdapter
import com.libre.irremote.R
import kotlinx.android.synthetic.main.show_ir_appliances_activity_layout.*
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.Constants.Constants
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import com.libre.irremote.utility.RecyclerItemClickListener
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.ModelApplianceList
import kotlinx.android.synthetic.main.show_ir_appliances_activity_layout.progressBar
import org.json.JSONArray
import org.json.JSONObject


class IRShowAppliancesActivity : AppCompatActivity() {


    var irAddDeviceAdapter: IRAddDeviceAdapter? = null

    val appliancesList: MutableList<ModelApplianceList> = ArrayList()

    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()



    var bundle = Bundle()



    lateinit var deviceInfo : DeviceInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_ir_appliances_activity_layout)

        bundle = intent.extras!!
        if (bundle!=null){
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
        }

        showProgressBar()
        getAppliancesList()


    }


    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }

    fun setAppliancesAdapter() {

        rvAppliancesList.setHasFixedSize(true)


        irAddDeviceAdapter = IRAddDeviceAdapter(this@IRShowAppliancesActivity, appliancesList)

        val gridLayoutManager = GridLayoutManager(this@IRShowAppliancesActivity, 3)

        rvAppliancesList.layoutManager = gridLayoutManager

        rvAppliancesList.adapter = irAddDeviceAdapter




        rvAppliancesList.addOnItemTouchListener(RecyclerItemClickListener(this@IRShowAppliancesActivity,
                rvAppliancesList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val intent = Intent(this@IRShowAppliancesActivity,ShowIRDevicesManufacturesMakesActivity::class.java)
                val bundle = Bundle()
                bundle.putString("selectedAppliance",appliancesList[position].applianceName)
                bundle.putString("selectedApplianceIcon",appliancesList[position].materialDesignCode)
                bundle.putSerializable("deviceInfo",deviceInfo)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            override fun onLongItemClick(view: View?, position: Int) {
                Log.d("tag", "do nothing")
            }

        }))




    }

    fun getAppliancesList() {


        val url: String = Constants.BASEURL2 + Constants.RestApis.APPLIANCESLIST+"all"

        Log.d("atul_url", url)


        val requestQueue = Volley.newRequestQueue(this@IRShowAppliancesActivity)

        val stringRequest = @RequiresApi(Build.VERSION_CODES.KITKAT)
        object : StringRequest(Request.Method.GET, url,
                Response.Listener { response ->

                    val responseJsonObject: JSONObject = JSONObject(response)

                    val applianceJsonArray: JSONArray = responseJsonObject.getJSONArray("APPLIANCES")


                    Log.d("atul_volley", applianceJsonArray.toString())


                    if (applianceJsonArray.length() > 0) {

                        llNoDevices.visibility = View.GONE

                        tvHeadingLabel.visibility = View.VISIBLE

                        for (i in 0 until applianceJsonArray.length()) {

                            val applianceJsonObject: JSONObject = applianceJsonArray[i] as JSONObject

                            appliancesList.add(ModelApplianceList(applianceJsonObject.getString("name"),
                                    applianceJsonObject.getString("md_code")))
                        }

                        setAppliancesAdapter()

                    } else {

                        llNoDevices.visibility = View.VISIBLE

                        tvHeadingLabel.visibility = View.GONE

                        emailAutroziationApi.buildSnackBar("Seems like they are no appliances present.", true,
                                this@IRShowAppliancesActivity, null)
                    }

                    hideProgressBar()
                },

                Response.ErrorListener { volleyError ->

                    hideProgressBar()

                    Log.d("atul_volley_error", volleyError.toString())


                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
                                this@IRShowAppliancesActivity, null)

                    } else if (volleyError is AuthFailureError) {

                        emailAutroziationApi.buildSnackBar("AuthFailure error occurred, please try again later",
                                true, this@IRShowAppliancesActivity, null)


                    } else if (volleyError is ServerError) {

                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
                                true, this@IRShowAppliancesActivity, null)



                    } else if (volleyError is NetworkError) {

                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
                                true, this@IRShowAppliancesActivity, null)

                    } else if (volleyError is ParseError) {

                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true, this@IRShowAppliancesActivity, null)

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