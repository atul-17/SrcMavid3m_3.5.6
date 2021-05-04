package com.libre.irremote.irActivites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.libre.irremote.R
import org.json.JSONObject
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.Constants.Constants
import com.libre.irremote.MavidHomeTabsActivity
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import kotlinx.android.synthetic.main.ir_login_activity.*
import kotlinx.android.synthetic.main.ir_login_activity.tvSignUp
import java.util.*


class IRLoginActivity : AppCompatActivity() {

    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    var sharedPreferences: SharedPreferences? = null

    var editor: SharedPreferences.Editor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ir_login_activity)

        tvSignUp.setOnClickListener {
            val intent = Intent(this@IRLoginActivity, IRRegistrationActivity::class.java)
            startActivity(intent)
        }


        btnLogin.setOnClickListener {
            validateUserDetails()
        }


    }


    fun validateUserDetails() {

        when {

            et_email.text.toString().isEmpty() -> {

                emailAutroziationApi.buildSnackBar("Email field is mandatory", true, this@IRLoginActivity, null);
            }

            et_password.text.toString().isEmpty() -> {

                emailAutroziationApi.buildSnackBar("Password field is mandatory", true, this@IRLoginActivity, null);
            }

            et_email.text.toString().isNotEmpty() && et_password.text!!.isNotEmpty() -> {

                btnLogin.startAnimation()

                val bitmap: Bitmap = BitmapFactory.decodeResource(resources,
                        R.drawable.ic_black_tick_mark)

                btnLogin.doneLoadingAnimation(resources.getColor(R.color.white), bitmap)

                userLoginApi(et_email.text.toString(), et_password.text.toString())

            }
        }
    }

    fun userLoginApi(email: String, password: String) {
        val url: String = Constants.BASEURL + Constants.RestApis.LOGIN

        val requestQueue = Volley.newRequestQueue(this@IRLoginActivity)


        val bodyObject: JSONObject = JSONObject()

        bodyObject.put("client_id", Constants.dataIds.CLIENT_ID)

        bodyObject.put("client_secret", Constants.dataIds.CLIENT_SECRET)

        bodyObject.put("grant_type", "password")

        bodyObject.put("password", password)

        bodyObject.put("device_id","android_123456789")

        bodyObject.put("email",email)

        val requestBody: String = bodyObject.toString()

        Log.d("atul_login_body",requestBody.toString())

        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->

                    val reponseObject: JSONObject = JSONObject(response)

                    val code: String = reponseObject.getString("code")


                    Log.d("atul_volley", response)

                    when (code) {

                        Constants.ErrorCodes.SUCCESS -> {
                            sharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

                            editor = sharedPreferences?.edit()

                            editor?.putString("accessToken", reponseObject.getString("access_token"))
                            editor?.putString("tokenType", reponseObject.getString("token_type"))
                            editor?.putString("refreshToken", reponseObject.getString("refresh_token"))
                            editor?.putBoolean("userLoggedIn", true)
                            editor?.putString("email", email)
                            editor?.putString("password", password)
                            editor?.commit()


                            //success
                            val intent = Intent(this@IRLoginActivity, MavidHomeTabsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        Constants.ErrorCodes.ERR_PARAS_ERROR -> {
                            //ERR_PARAS_ERROR
                            btnLogin.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Something went wrong,Please try again.", true, this@IRLoginActivity, null)
                        }


                        Constants.ErrorCodes.ERR_SERVER_ERROR -> {
                            //ERR_SERVER_ERROR
                            btnLogin.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Something went wrong,Please try again.", true, this@IRLoginActivity, null)
                        }

                        Constants.ErrorCodes.ERR_ACCOUNT_NOT_REGISTERED -> {
                            btnLogin.revertAnimation()
                            emailAutroziationApi.buildSnackBar("The entered email is not registered.", true, this@IRLoginActivity, null)
                        }

                        Constants.ErrorCodes.ERR_OAUTH_TYPE_NOT_SUPPORT -> {
                            btnLogin.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Email or password is wrong please check", true, this@IRLoginActivity, sucessCallback = null)
                        }
                        else -> {
                            btnLogin.revertAnimation()
                        }
                    }

                },

                Response.ErrorListener { volleyError ->
                    btnLogin.revertAnimation()
                    Log.d("atul_volley_error", volleyError.networkResponse.statusCode.toString())


                    if (volleyError is TimeoutError || volleyError is NoConnectionError) {


                        emailAutroziationApi.buildSnackBar("Seems your internet connection is slow, please try in sometime.", true,
                                this@IRLoginActivity, null)

                    } else if (volleyError is AuthFailureError) {

                        emailAutroziationApi.buildSnackBar("AuthFailure error occurred, please try again later",
                                true, this@IRLoginActivity, null)


                    } else if (volleyError is ServerError) {

                        emailAutroziationApi.buildSnackBar("Server error occurred, please try again later",
                                true, this@IRLoginActivity, null)

                    } else if (volleyError is NetworkError) {

                        emailAutroziationApi.buildSnackBar("Network error occurred, please try again later",
                                true, this@IRLoginActivity, null)

                    } else if (volleyError is ParseError) {

                        emailAutroziationApi.buildSnackBar("Parser error occurred, please try again later", true, this@IRLoginActivity, null)

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


    override fun onBackPressed() {
       this@IRLoginActivity.finishAffinity()
    }

}