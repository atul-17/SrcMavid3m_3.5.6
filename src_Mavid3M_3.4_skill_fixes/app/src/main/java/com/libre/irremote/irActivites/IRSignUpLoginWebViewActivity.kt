package com.libre.irremote.irActivites

import android.app.ProgressDialog
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import com.auth0.android.jwt.JWT
import com.libre.irremote.MavidHomeTabsActivity
import com.libre.irremote.R
import com.libre.irremote.models.AuthURLRepoModel
import com.libre.irremote.models.TokenRepoModel
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import kotlinx.android.synthetic.main.activity_webview_ir_sign_up_login.*


class IRSignUpLoginWebViewActivity : AppCompatActivity() {

    val uiRelatedClass = UIRelatedClass()

    lateinit var apiViewModel: ApiViewModel

    var loginAuthCode: String? = null

    var progressDialog: ProgressDialog? = null

    var TAG = IRSignUpLoginWebViewActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_ir_sign_up_login)


        initWebViewClient()

        progressDialog = ProgressDialog(this@IRSignUpLoginWebViewActivity)
        progressDialog?.setMessage("Please wait...")
        progressDialog?.setCancelable(false)

        if(checkWifiConnect()) {
            showLoader()
            getAuthorizeURl()
        }else{
            uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                    window.decorView.findViewById(android.R.id.content), "Please check your internet connection")
        }

        irBlasterWebView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d("My Webview", url!!)
                val url = Uri.parse(url)
                return if (url.host == "127.0.0.1") {
                    loginAuthCode = url.getQueryParameter("code")
                    Log.d("atul_redirect_url", loginAuthCode.toString())

                    showLoader()
                    getTokenData()
                    true
                } else {
                    false//Allow WebView to load url
                }
            }
        }
    }


    fun  initWebViewClient(){
        irBlasterWebView?.webViewClient = WebViewClient()

        irBlasterWebView?.settings?.loadsImagesAutomatically = true

        irBlasterWebView?.settings?.javaScriptEnabled = true

        irBlasterWebView?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY


        apiViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ApiViewModel::class.java)
    }

    private fun checkWifiConnect(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifi: NetworkInfo = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!

        val mobile: NetworkInfo = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!

        return !(!wifi.isConnected && !mobile.isConnected)
    }


    private fun registerWifiReceiver() {
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(mWifiReceiver, filter)
    }

    private fun unregisterWifiReceiver() {
        unregisterReceiver(mWifiReceiver)
    }


    override fun onResume() {
        super.onResume()
        registerWifiReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterWifiReceiver()
    }

    var mWifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (checkWifiConnect()) {
                // TODO
                if (checkWifiConnect()) {
                    showLoader()

                    getAuthorizeURl()
                } else {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Please check your internet connection")
                }
            }
        }
    }

    fun showLoader() {
        progressDialog?.show()
    }

    fun dismissLoader() {
        progressDialog?.dismiss()
    }

    fun getTokenData() {
        apiViewModel.getTokenData(loginAuthCode!!, null, "authorization_code")?.observe(this, Observer<TokenRepoModel?> {

            if (it!!.tokenSucessRepoModel != null) {

                val sharedPref: SharedPreferences = getSharedPreferences("Mavid", Context.MODE_PRIVATE)

                val editor: SharedPreferences.Editor = sharedPref.edit()

                val jwt = JWT(it.tokenSucessRepoModel?.idToken!!)

                val claim = jwt.getClaim("cognito:username")

                val email = jwt.getClaim("email")

                val sub = jwt.getClaim("sub")

                Log.d(TAG, "email: " + email.asString() + "sub: ${sub.asString()}")

                editor.putString("idToken", it?.tokenSucessRepoModel?.idToken)

                editor.putString("accessToken", it?.tokenSucessRepoModel?.accessToken)

                editor.putString("refreshToken", it?.tokenSucessRepoModel?.refreshToken)

                editor.putString("expiresIn", it?.tokenSucessRepoModel?.expiresIn)

                editor.putString("tokenType", it?.tokenSucessRepoModel?.tokenType)

                editor.putString("email", email.asString())

                editor.putString("sub", sub.asString())

                editor.putBoolean("userLoggedIn", true)

                editor.apply()


                Log.d(TAG, "sub: ".plus(sub))

                Log.d("atul_token_username", claim.asString()!!)

                dismissLoader()
                val intent = Intent(this@IRSignUpLoginWebViewActivity, MavidHomeTabsActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                //volley error
                val volleyError = it?.volleyError

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                                window.decorView.findViewById(android.R.id.content), "Server error occurred, please try again later")
                    }

                } else if (volleyError is NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Network error occurred, please try again later")

                } else if (volleyError is ParseError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Parser error occurred, please try again later")
                }
            }
        })
    }


    fun getAuthorizeURl() {
        apiViewModel.getAuthURl(this@IRSignUpLoginWebViewActivity)?.observe(this, Observer<AuthURLRepoModel?> {

            if (it?.authUrl != null) {

                irBlasterWebView.loadUrl(it?.authUrl)
            } else {
                val volleyError = it?.volleyError

                if (volleyError?.networkResponse != null) {
                    if (volleyError?.networkResponse.statusCode == 302) {
                        //url gets redirected

                        val redirectedUrl: String? =
                                volleyError?.networkResponse.headers["Location"]

                    }
                }

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Seems your internet connection is slow, please try in sometime")

                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                                window.decorView.findViewById(android.R.id.content), "Server error occurred, please try again later")
                    }

                } else if (volleyError is NetworkError) {
                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Network error occurred, please try again later")

                } else if (volleyError is ParseError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSignUpLoginWebViewActivity,
                            window.decorView.findViewById(android.R.id.content), "Parser error occurred, please try again later")
                }
            }
            dismissLoader()
        })
    }
}






