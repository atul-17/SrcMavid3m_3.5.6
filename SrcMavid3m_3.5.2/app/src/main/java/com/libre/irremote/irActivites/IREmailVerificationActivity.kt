package com.libre.irremote.irActivites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.Constants.Constants
import com.libre.irremote.MavidHomeTabsActivity
import com.libre.irremote.R
import com.libre.irremote.utility.ApiSucessCallback
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import kotlinx.android.synthetic.main.ir_email_verification_activity_layout.*
import org.json.JSONObject
import java.util.*

class IREmailVerificationActivity : AppCompatActivity() {


    var bundle = Bundle()

    var email: String = ""

    var password: String = ""

    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    var countDownTimer: CountDownTimer? = null

    var sharedPreferences: SharedPreferences? = null

    var editor: SharedPreferences.Editor? = null

    var isCountDownTimed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ir_email_verification_activity_layout)
        if (intent.extras!=null) {
            bundle = intent.extras!!
        }

        if (bundle != null) {
            email = bundle.getString("email", "")
            password = bundle.getString("password", "")
        }

        tvResendVerificationCode.setOnClickListener {
            showProgressBar()
            emailAutroziationApi.emailAutroziationApi(email, this@IREmailVerificationActivity, apiSucessCallback = object : ApiSucessCallback {
                override fun onSucess(code: String?, message: String?) {
                    hideProgressBar()
                    when (code) {
                        Constants.ErrorCodes.SUCCESS -> { //success
                            emailAutroziationApi.buildSnackBar("Email verification code has been sent your email id",
                                    true, this@IREmailVerificationActivity, sucessCallback = null)
                        }
                        Constants.ErrorCodes.ERR_PARAS_ERROR -> {
                            //ERR_PARAS_ERROR
                            btnVerify.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Something went wrong,Please try again", true, this@IREmailVerificationActivity, null)
                        }


                        Constants.ErrorCodes.ERR_SERVER_ERROR -> {
                            //ERR_SERVER_ERROR
                            btnVerify.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Something went wrong,Please try again", true, this@IREmailVerificationActivity, null)
                        }

                        Constants.ErrorCodes.ERR_ACCOUNT_EMAIL_ALREADY_REGISTERED -> {
                            btnVerify.revertAnimation()
                            emailAutroziationApi.buildSnackBar("The entered email is not registered.", true, this@IREmailVerificationActivity, null)
                        }

                        Constants.ErrorCodes.ERR_OAUTH_TYPE_NOT_SUPPORT -> {
                            btnVerify.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Email or password is wrong please check", true, this@IREmailVerificationActivity, sucessCallback = null)
                        }

                        "111" -> {
                            if (message != null)
                                emailAutroziationApi.buildSnackBar(message!!, true, this@IREmailVerificationActivity, sucessCallback = null)
                            btnVerify.revertAnimation()
                        }

                        else -> //failure
                            btnVerify.revertAnimation()

                    }
                }
            })
        }



        setupCountDownTimer()



        btnVerify.setOnClickListener {
            if (squarePinFieldOtp.text.toString().length == 6) {
                showProgressBar()
                userSignUp(email, password, squarePinFieldOtp.text.toString())
            } else {
                emailAutroziationApi.buildSnackBar("Six digits are mandatory for verification code.", true, this@IREmailVerificationActivity, null)
            }
        }

    }


    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }


    fun setupCountDownTimer() {
        if (!isCountDownTimed) {
            countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tvCountdownTimer.text = "Time Remaining: " + millisUntilFinished / 1000
                }

                override fun onFinish() {
                    tvCountdownTimer.visibility = View.GONE
                    tvResendVerificationCode.visibility = View.VISIBLE
                    isCountDownTimed = true
                    countDownTimer?.cancel()
                }

            }
            countDownTimer?.start()
        } else {
            countDownTimer?.cancel()
        }
    }


    fun userSignUp(email: String, password: String, emailCode: String) {

        val url: String = Constants.BASEURL + Constants.RestApis.SIGN_UP


        val bodyObject: JSONObject = JSONObject()

        bodyObject.put("client_id", Constants.dataIds.CLIENT_ID)

        bodyObject.put("client_secret", Constants.dataIds.CLIENT_SECRET)

        bodyObject.put("email", email)

        bodyObject.put("country_code_id", "235")

        bodyObject.put("password1", password)

        bodyObject.put("password2", password)

        bodyObject.put("email_code", emailCode)

        val requestBody: String = bodyObject.toString()

        val requestQueue = Volley.newRequestQueue(this@IREmailVerificationActivity)

        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->

                    hideProgressBar()

                    Log.d("atul_volley", response)

                    btnVerify.revertAnimation()

                    val reponseObject: JSONObject = JSONObject(response)

                    val code: String = reponseObject.getString("code")
                    val msg: String = reponseObject.getString("msg")

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
                            val intent = Intent(this@IREmailVerificationActivity, MavidHomeTabsActivity::class.java)
                            startActivity(intent)
                            finish()

                        }

                        Constants.ErrorCodes.ERR_PARAS_ERROR -> {
                            //ERR_PARAS_ERROR
                            btnVerify.revertAnimation()
                            emailAutroziationApi.buildSnackBar("Error : Parameter missing", true, this@IREmailVerificationActivity, null)
                        }


                        Constants.ErrorCodes.ERR_SERVER_ERROR -> {
                            //ERR_SERVER_ERROR
                            emailAutroziationApi.buildSnackBar("Error : Server Down!!", true, this@IREmailVerificationActivity, null)
                        }

                        Constants.ErrorCodes.ERR_ACCOUNT_EMAIL_ALREADY_REGISTERED -> {
                            //email acc already registred
                            emailAutroziationApi.buildSnackBar("This email is already registered!!", true, this@IREmailVerificationActivity, null)

                        }

                        Constants.ErrorCodes.ERR_VERIFY_CODE_WRONG -> {
                            //verification code is wrong
                            emailAutroziationApi.buildSnackBar("The entered verification code is wrong!!", true, this@IREmailVerificationActivity, null)

                        }

                    }

                },

                Response.ErrorListener { volleyError ->
                    Log.d("atul_volley_error", volleyError.toString())
                }) {


            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }

            override fun getHeaders(): MutableMap<String, String> {

                val params = Hashtable<String, String>()

                params["Content-Type"] = "application/json"

                return params
            }

//            override fun getParams(): MutableMap<String, String> {
//                val params = HashMap<String, String>()
//                params["client_id"] = Constants.dataIds.CLIENT_ID
//                params["client_secret"] = Constants.dataIds.CLIENT_SECRET
//                params["email"] = email
//                params["country_code_id"] = "235"
//                params["password1"] = password
//                params["password2"] = password
//                params["email_code"] = emailCode
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


}