package com.libre.irremote

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.libre.irremote.R
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import com.libre.irremote.utility.UIRelatedClass
import kotlinx.android.synthetic.main.mavid_settings_activity.*
import kotlinx.android.synthetic.main.show_all_ir_device_list_activity.progressBar

class MavidSettingsFragment : Fragment() {

    var tv_app_version: AppCompatTextView? = null

    var llLogout: LinearLayout? = null


    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    val uiRelatedClass = UIRelatedClass()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mavid_settings_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_app_version = view.findViewById(R.id.tv_app_version)

        llLogout = view.findViewById(R.id.llLogout)

        if (activity != null) {
            try {
                val pInfo = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
                val version = pInfo.versionName
                tv_app_version?.text = version
                (activity as MavidHomeTabsActivity).ivRefresh.visibility = View.GONE
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

        }


        llLogout?.setOnClickListener {
            removeUserDetails()
            val intent = Intent(context, SplashScreenActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        tvEmail.text = context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)?.getString("email", "")

    }


    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }

    fun removeUserDetails() {

        val sharedPreferences = context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)

        val editor = sharedPreferences?.edit()

        editor?.putString("accessToken", "")
        editor?.putString("tokenType", "")
        editor?.putString("refreshToken", "")
        editor?.putBoolean("userLoggedIn", false)
        editor?.putString("email", "")
        editor?.putString("password", "")

        editor?.apply()

        context?.cacheDir?.deleteRecursively()

    }

//    fun userLogoutApi() {
//
//
//        val url: String = Constants.BASEURL.plus(Constants.RestApis.LOGOUT)
//
//        Log.d("atul_url", url)
//
//        val requestQueue = Volley.newRequestQueue(context)
//
//
//        val bodyObject: JSONObject = JSONObject()
//
//        bodyObject.put("client_id", Constants.dataIds.CLIENT_ID)
//
//        bodyObject.put("client_secret", Constants.dataIds.CLIENT_SECRET)
//
//        bodyObject.put("token_type", "refresh_token")
//
//        bodyObject.put("token", context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)?.getString("refreshToken", ""))
//
//        val requestBody: String = bodyObject.toString()
//
//        Log.d("atul_body", requestBody)
//
//        val stringRequest = @RequiresApi(Build.VERSION_CODES.KITKAT)
//        object : StringRequest(Request.Method.POST, url,
//                Response.Listener { response ->
//
//                    Log.d("atul_response", response.toString())
//
//                    hideProgressBar()
//
//                    val responseObject = JSONObject(response)
//                    val code = responseObject.getString("code")
//                    if (code == "0") {
//
//                       val  sharedPreferences = context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)
//
//                        val editor = sharedPreferences?.edit()
//
//                        editor?.putString("accessToken", "")
//                        editor?.putString("tokenType","")
//                        editor?.putString("refreshToken", "")
//                        editor?.putBoolean("userLoggedIn", false)
//                        editor?.putString("email", "")
//                        editor?.putString("password", "")
//
//                        editor?.commit()
//
//                        emailAutroziationApi.buildSnackBar("User logged out .", true,
//                                context!!, null)
//                        (activity as MavidHomeTabsActivity).bottomNavigation.selectedItemId = R.id.action_discover
//                        val intent = Intent(context, IRLoginActivity::class.java)
//                        startActivity(intent)
//
//                    }else {
//                        //"code": 4, "msg": "'DoesNotExist' object has no attribute 'code'"
//                        if (activity != null) {
//                            uiRelatedClass.buildSnackBarWithoutButton(activity!!,
//                                    activity?.window?.decorView?.findViewById(android.R.id.content)!!, "There seems to be problem,Try logging in again");
//                        }
//                    }
//                },
//
//                Response.ErrorListener { volleyError ->
//
//                    hideProgressBar()
//
//                    Log.d("atul_volley_error", volleyError.networkResponse.statusCode.toString())
//
//
//                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {
//
//
//                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
//                                context!!, null)
//
//                    } else if (volleyError is AuthFailureError) {
//
//                        emailAutroziationApi.buildSnackBar("AuthFailure error occurred, please try again later",
//                                true, context!!, null)
//
//
//                    } else if (volleyError is ServerError) {
//
//                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
//                                true, context!!, null)
//
//                    } else if (volleyError is NetworkError) {
//
//                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
//                                true, context!!, null)
//
//                    } else if (volleyError is ParseError) {
//
//                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true,
//                                context!!, null)
//
//
//                    }
//
//                }) {
//            override fun getHeaders(): MutableMap<String, String> {
//
//                Log.d("atul_token", context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)?.getString("accessToken", ""))
//
//                val params = Hashtable<String, String>()
//
//                params["Content-Type"] = "application/json"
//                params["authorization"] = "Bearer  ${context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)
//                        ?.getString("accessToken", "")}"
//
//                return params
//            }
//
//            override fun getBody(): ByteArray {
//
//                return requestBody.toByteArray(Charsets.UTF_8)
//
//            }
//        }
//
//        stringRequest.retryPolicy = DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
//
//        requestQueue.add(stringRequest)
//
//    }

}