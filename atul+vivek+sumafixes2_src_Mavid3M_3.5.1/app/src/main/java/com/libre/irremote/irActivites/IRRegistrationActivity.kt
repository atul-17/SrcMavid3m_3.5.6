package com.libre.irremote.irActivites

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.danimahardhika.cafebar.CafeBar
import com.libre.irremote.Constants.Constants
import com.libre.irremote.R
import com.libre.irremote.utility.ApiSucessCallback
import com.libre.irremote.utility.EmailAuthAndTokenRefreshApis
import kotlinx.android.synthetic.main.ir_registration_activity.*
import java.util.regex.Pattern


class IRRegistrationActivity : AppCompatActivity() {

    var cafeBar: CafeBar? = null

    val emailAutroziationApi = EmailAuthAndTokenRefreshApis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ir_registration_activity)

        btnRegister.setOnClickListener {
            validateUserDetails()
        }

        tvSignIn.setOnClickListener {
            onBackPressed()
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

    fun validateUserDetails() {

        when {
            etEmail.text.toString().isEmpty() -> {

                emailAutroziationApi.buildSnackBar("Email field is mandatory", true, this@IRRegistrationActivity, null);
            }
            etPassword.text.toString().isEmpty() -> {

                emailAutroziationApi.buildSnackBar("Password field is mandatory", true, this@IRRegistrationActivity, null);
            }
            etConfirmPassword.text.toString().isEmpty() -> {

                emailAutroziationApi.buildSnackBar("Confirm Password field is mandatory", true, this@IRRegistrationActivity, null)
            }

            ((etEmail.text.toString().isNotEmpty() && etPassword.text!!.isNotEmpty()
                    && etConfirmPassword.text!!.isNotEmpty())) -> {

                if (etConfirmPassword.text.toString() == etPassword.text.toString() && isEmailValid(etEmail.text.toString())) {


                    if (etPassword.text.toString().length >= 8) {

                        btnRegister.startAnimation()

                        val bitmap: Bitmap = BitmapFactory.decodeResource(resources,
                                R.drawable.ic_black_tick_mark)

                        btnRegister.doneLoadingAnimation(resources.getColor(R.color.white), bitmap)

                        showProgressBar()
                        emailAutroziationApi.emailAutroziationApi(etEmail.text.toString(),
                                this@IRRegistrationActivity, apiSucessCallback = object : ApiSucessCallback {
                            override fun onSucess(code: String?, message: String?) {
                                hideProgressBar()
                                when (code) {

                                    Constants.ErrorCodes.SUCCESS -> {
                                        //success

                                        emailAutroziationApi.buildSnackBar("Email verification code has been sent your email id",
                                                false, this@IRRegistrationActivity, sucessCallback = object : EmailAuthAndTokenRefreshApis.SucessCallback {
                                            override fun onSucess() {
                                                continueToEmailVerification(etEmail.text.toString(), etPassword.text.toString())
                                            }
                                        })
                                }

                                    Constants.ErrorCodes.ERR_PARAS_ERROR -> {
                                        //ERR_PARAS_ERROR
                                        btnRegister.revertAnimation()
                                        emailAutroziationApi.buildSnackBar("Something went wrong,Please try again", true, this@IRRegistrationActivity, null)
                                    }


                                    Constants.ErrorCodes.ERR_SERVER_ERROR -> {
                                        //ERR_SERVER_ERROR
                                        btnRegister.revertAnimation()
                                        emailAutroziationApi.buildSnackBar("Something went wrong,Please try again", true, this@IRRegistrationActivity, null)
                                    }


                                    Constants.ErrorCodes.ERR_ACCOUNT_EMAIL_ALREADY_REGISTERED -> {
                                        btnRegister.revertAnimation()
                                        emailAutroziationApi.buildSnackBar("The entered email is not registered.", true, this@IRRegistrationActivity, null)
                                    }

                                    Constants.ErrorCodes.ERR_OAUTH_TYPE_NOT_SUPPORT -> {
                                        btnRegister.revertAnimation()
                                        emailAutroziationApi.buildSnackBar("Email or password is wrong please check", true, this@IRRegistrationActivity, sucessCallback = null)
                                    }

                                    "111" -> {

                                        emailAutroziationApi.buildSnackBar(message!!, true, this@IRRegistrationActivity, sucessCallback = null)

                                        btnRegister.revertAnimation()
                                    }

                                    else -> //failure
                                        btnRegister.revertAnimation()
                                }


                            }

                        })
                    } else {

                        emailAutroziationApi.buildSnackBar("Password should be min 8 chars", true, this@IRRegistrationActivity, null)

                    }
                } else if (etConfirmPassword.text.toString() != etPassword.text.toString()) {

                    emailAutroziationApi.buildSnackBar("Password and confirm Password fields should be same", true, this@IRRegistrationActivity, null)

                } else if (!isEmailValid(etEmail.text.toString())) {

                    emailAutroziationApi.buildSnackBar("The entered email is not valid", true, this@IRRegistrationActivity, null);
                }
            }

        }
    }


    fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun continueToEmailVerification(email: String, password: String) {
        val intent = Intent(this@IRRegistrationActivity, IREmailVerificationActivity::class.java)
        val bundle = Bundle()
        bundle.putString("email", email)
        bundle.putString("password", password)
        intent.putExtras(bundle)
        startActivity(intent)
    }



}