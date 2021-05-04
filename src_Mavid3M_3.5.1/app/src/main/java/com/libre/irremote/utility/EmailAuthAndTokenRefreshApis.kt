package com.libre.irremote.utility

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.danimahardhika.cafebar.CafeBar
import com.libre.irremote.Constants.Constants
import com.libre.irremote.R
import org.json.JSONObject
import java.util.*

class EmailAuthAndTokenRefreshApis {

    var sharedPreferences: SharedPreferences? = null

    var editor: SharedPreferences.Editor? = null


    fun emailAutroziationApi(email: String, context: Context?, apiSucessCallback: ApiSucessCallback?) {

        val url: String = Constants.BASEURL + Constants.RestApis.EMAIL_VERIFICATION


        val requestQueue = Volley.newRequestQueue(context)


        val bodyObject: JSONObject = JSONObject()

        bodyObject.put("client_id", Constants.dataIds.CLIENT_ID)

        bodyObject.put("email", email)

        bodyObject.put("verify_type","signup")

        val requestBody : String = bodyObject.toString()


        val stringRequest = object : StringRequest(Request.Method.POST, url,

                Response.Listener { response ->

                    Log.d("atul_volley", response)

                    val reponseObject = JSONObject(response)

                    if (reponseObject.has("code")) {
                        val code: String = reponseObject.getString("code")
                        var msg: String? = ""
                        if (reponseObject.has("msg")) {
                            msg = reponseObject.getString("msg")
                        }


                        apiSucessCallback?.onSucess(code, msg)

                    }
                },

                Response.ErrorListener { volleyError ->
                    if (volleyError != null) {
                        Log.d("atul_volley_error", volleyError.networkResponse.statusCode.toString())

                        if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                            apiSucessCallback?.onSucess("111", "Seems your internet connection is slow, please try in sometime.")

                        } else if (volleyError is AuthFailureError) {


                            apiSucessCallback?.onSucess("111", "AuthFailure error occurred, please try again later")


                        } else if (volleyError is ServerError) {

                            apiSucessCallback?.onSucess("111", "Server error occurred, please try again later")

                        } else if (volleyError is NetworkError) {


                            apiSucessCallback?.onSucess("111", "Network error occurred, please try again later")


                        } else if (volleyError is ParseError) {

                            apiSucessCallback?.onSucess("111", "Parser error occurred, please try again later")

                        }

                    }

                }) {

            override fun getHeaders(): MutableMap<String, String> {

                val params = Hashtable<String, String>()

                params["Content-Type"] = "application/json"

                return params
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }

//            override fun getParams(): MutableMap<String, String> {
//                val params = HashMap<String, String>()
//                params["client_id"] = Constants.dataIds.CLIENT_ID
//                params["email"] = email
//                params["verify_type"] = "signup"
//
//                return params
//
//            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(stringRequest)
    }


    interface SucessCallback {

        fun onSucess()

    }

    fun buildSnackBar(message: String, isAutoDismiss: Boolean, context: Context, sucessCallback: SucessCallback?) {
        val builder = CafeBar.builder(context)
        builder.customView(R.layout.custom_snackbar_layout)

        if (isAutoDismiss) {
            builder.autoDismiss(true)
            builder.duration(8000)

        } else {
            builder.autoDismiss(false)
        }

        val cafeBar = builder.build()

        val tv_message: AppCompatTextView = cafeBar?.cafeBarView!!.findViewById(R.id.tv_message)
        tv_message.text = message


        val btnOk: AppCompatButton = cafeBar?.cafeBarView!!.findViewById<AppCompatButton>(R.id.btnOk)

        if (!isAutoDismiss) {
            btnOk.visibility = View.VISIBLE
        } else {
            btnOk.visibility = View.GONE
        }

        btnOk.setOnClickListener {
            sucessCallback?.onSucess()
        }

        cafeBar?.show()
    }


    fun getAccessTokenApi(refreshToken: String, context: Context, apiSucessCallback: ApiSucessCallback?) {

        val url = Constants.BASEURL + Constants.RestApis.REFRESH_TOKEN


        val requestQueue = Volley.newRequestQueue(context)


        val bodyObject: JSONObject = JSONObject()

        bodyObject.put("client_id", Constants.dataIds.CLIENT_ID)

        bodyObject.put("client_secret", Constants.dataIds.CLIENT_SECRET)

        bodyObject.put("grant_type", "refresh_token")

        bodyObject.put("refresh_token", refreshToken)

        bodyObject.put("device_id","android_123456789")


        Log.d("atul_token_url",url)

        val requestBody: String = bodyObject.toString()

        Log.d("atul_token_body",requestBody)


        val stringRequest = object : StringRequest(Request.Method.POST, url,

                Response.Listener { response ->

                    Log.d("atul_volley_token", response)

                    val reponseObject = JSONObject(response)

                    if (reponseObject.has("code")) {
                        val code: String = reponseObject.getString("code")

                        if (code == "0") {
                            sharedPreferences = context.getSharedPreferences("Mavid", Context.MODE_PRIVATE)

                            editor = sharedPreferences?.edit()

                            editor?.putString("accessToken", reponseObject.getString("access_token"))
                            editor?.putString("tokenType", reponseObject.getString("token_type"))
                            editor?.putString("refreshToken", reponseObject.getString("refresh_token"))

                            editor?.commit()


                        }
                        apiSucessCallback?.onSucess(code, "Success")

                    }


                },

                Response.ErrorListener { volleyError ->
                    if (volleyError != null) {
//                        Log.d("atul_volley_error", volleyError.networkResponse.statusCode.toString())

                        if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                            apiSucessCallback?.onSucess("111", "Seems your internet connection is slow, please try in sometime.")

                        } else if (volleyError is AuthFailureError) {


                            apiSucessCallback?.onSucess("111", "AuthFailure error occurred, please try again later")


                        } else if (volleyError is ServerError) {

                            apiSucessCallback?.onSucess("111", "Server error occurred, please try again later")

                        } else if (volleyError is NetworkError) {


                            apiSucessCallback?.onSucess("111", "Network error occurred, please try again later")


                        } else if (volleyError is ParseError) {

                            apiSucessCallback?.onSucess("111", "Parser error occurred, please try again later")

                        }

                    }

                }) {

            override fun getHeaders(): MutableMap<String, String> {

                val params = Hashtable<String, String>()

                params["Content-Type"] = "application/json"

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