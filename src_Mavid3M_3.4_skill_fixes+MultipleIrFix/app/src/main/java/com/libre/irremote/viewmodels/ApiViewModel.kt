package com.libre.irremote.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.libre.irremote.R
import com.libre.libresdk.Util.LibreLogger
import com.libre.irremote.models.*
import com.libre.irremote.utility.ApiConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class ApiViewModel(application: Application) : AndroidViewModel(application) {

    //this is the data that we will fetch asynchronously
    private var getAuthURLRepoModel: MutableLiveData<AuthURLRepoModel>? = null

    private var getTokenRepoModel: MutableLiveData<TokenRepoModel>? = null;

    var redirectURI: String = "https://127.0.0.1"

    private var getTvBrandDetailsRepoModel: MutableLiveData<TvBrandsDetailsRepoModel>? = null

    private var getSelectRemoteJson: MutableLiveData<ModelRepoBody>? = null

    private var getSelecAcRemoteJsonLiveData: MutableLiveData<ModelAcRepoBody>? = null

    private var getUserMgtDetailsSucessError: MutableLiveData<ModelGetUserMgtDetailsSucessError>? = null


    private var getTvpBrandsLiveData: MutableLiveData<ModelGetTvpBodyResponse>? = null

    private var getTvpRegionalBrandLiveData: MutableLiveData<ModelGetRegionalBodyResponse>? = null

    private var getAcBrandsLiveData: MutableLiveData<AcBrandsDetailsRepoModel>? = null

    //tv select json list
    val modelSelectRemotePayload = ModelSelectRemotePayload()
    val modelLevelDataList: MutableList<ModelLevelData> = ArrayList()

    var TAG = ApiViewModel::class.java.simpleName

    //we will call this method to get the data
    public fun getAuthURl(context: Context): LiveData<AuthURLRepoModel?>? {
        //if the list is null
        if (getAuthURLRepoModel == null) {
            getAuthURLRepoModel = MutableLiveData<AuthURLRepoModel>()
            //we will load it asynchronously from server in this method
            volleyAuthURL(context)
        }

        //finally we will return the list
        return getAuthURLRepoModel
    }


    fun volleyAuthURL(context: Context) {

        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        val baseURL = ApiConstants.BASE_URL + "oauth2/authorize?"

        val state = UUID.randomUUID().toString()


        var uri = Uri.parse(baseURL)
                .buildUpon()
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", context.getString(R.string.client_id))
//                .appendQueryParameter("client_secret", context.getString(R.string.client_secret))
                .appendQueryParameter("redirect_uri", redirectURI)
                .appendQueryParameter("scope", "openid")
                .appendQueryParameter("state", state)
                .build()
        Log.d("atul_getAuthURl", uri.toString())

        val getAuthUrlRequest = object : StringRequest(Request.Method.GET, uri.toString(), Response.Listener { response ->

            Log.d("volley_getauth", response)

            val authURLRepoModel = AuthURLRepoModel()
            authURLRepoModel.authUrl = uri.toString();
            authURLRepoModel.volleyError = null;
            getAuthURLRepoModel?.value = authURLRepoModel


        }, Response.ErrorListener { volleyError ->
            val authURLRepoModel = AuthURLRepoModel()
            authURLRepoModel.authUrl = null
            authURLRepoModel.volleyError = volleyError;
            getAuthURLRepoModel?.value = authURLRepoModel
        }) {}
        getAuthUrlRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getAuthUrlRequest)
    }


    fun getTokenData(code: String, refreshToken: String?, grantType: String): LiveData<TokenRepoModel?>? {
        //if the list is null
        if (getTokenRepoModel == null) {
            getTokenRepoModel = MutableLiveData<TokenRepoModel>()
            //we will load it asynchronously from server in this method
            getToken(code, refreshToken, grantType)
        }

        //finally we will return the list
        return getTokenRepoModel
    }

    fun getTvBrandsDetails(): LiveData<TvBrandsDetailsRepoModel>? {
        if (getTvBrandDetailsRepoModel == null) {
            getTvBrandDetailsRepoModel = MutableLiveData<TvBrandsDetailsRepoModel>()
            //we will load it asynchronously from server in this method
            getTvBrandsList()
        }
        return getTvBrandDetailsRepoModel
    }

    fun getTvSelectJsonDetails(id: Int, selectedApplianceType: String): LiveData<ModelRepoBody>? {
        if (getSelectRemoteJson == null) {
            getSelectRemoteJson = MutableLiveData<ModelRepoBody>()
            getTvOrTvpelectJsonData(id, selectedApplianceType)
        }
        return getSelectRemoteJson
    }

    fun getAcSelectJsonDetails(id: Int): LiveData<ModelAcRepoBody>? {
        if (getSelecAcRemoteJsonLiveData == null) {
            getSelecAcRemoteJsonLiveData = MutableLiveData<ModelAcRepoBody>()
            getAcSelectJsonData(id)
        }
        return getSelecAcRemoteJsonLiveData
    }

    fun getUserMgtDetailsList(id: String): LiveData<ModelGetUserMgtDetailsSucessError>? {
        if (getUserMgtDetailsSucessError == null) {
            getUserMgtDetailsSucessError = MutableLiveData<ModelGetUserMgtDetailsSucessError>()
            getUserManagementDetails(id)
        }
        return getUserMgtDetailsSucessError
    }

    fun getUserManagementDetails(id: String) {
        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        var baseUrl = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/usermangement?".plus("id=").plus(id)

        val getUserMgtDetailsRequest = object : StringRequest(Request.Method.GET, baseUrl, Response.Listener { response ->

            Log.d(TAG, "getUserManagementDetails: response".plus(response))

            val responseObject: JSONObject = JSONObject(response)

            val modelGetUserMgtDetailsSucessError = ModelGetUserMgtDetailsSucessError()

            val bodyObject = responseObject.optJSONObject("body")

            if (bodyObject != null) {
                //body is a json object
                val modelGetUserDetailsBodySucess = ModelGetUserDetailsBodySucess()

                modelGetUserDetailsBodySucess.sub = bodyObject.getString("sub")
                modelGetUserDetailsBodySucess.mac = bodyObject.getString("Mac")

                var modelGetUserDetailsApplianceList: MutableList<ModelGetUserDetailsAppliance> = ArrayList()

                var applianceObject: JSONObject? = bodyObject.optJSONObject("Appliance")
                if (applianceObject != null) {
                    //it is a json object
                    modelGetUserDetailsApplianceList.add(parseApplianceJsonObject(applianceObject))
                } else {
                    //it might be an array
                    var applianceJsonArray: JSONArray? = bodyObject.optJSONArray("Appliance")
                    if (applianceJsonArray != null) {
                        for (i in 0 until applianceJsonArray.length()) {
                            modelGetUserDetailsApplianceList.add(parseApplianceJsonObject(applianceJsonArray[i] as JSONObject))
                        }
                    }
                }
                modelGetUserDetailsBodySucess.modelGetUserDetailsApplianceList = modelGetUserDetailsApplianceList

                modelGetUserMgtDetailsSucessError.modelGetUserDetailsAppliance = modelGetUserDetailsBodySucess

                getUserMgtDetailsSucessError?.value = modelGetUserMgtDetailsSucessError

            } else {
                val bodyObjectString = responseObject.optString("body")

                val modelGetUserDetailsBodySucess = ModelGetUserDetailsBodySucess()
                modelGetUserDetailsBodySucess.mac = "00"
                modelGetUserDetailsBodySucess.sub = bodyObjectString
                modelGetUserDetailsBodySucess.modelGetUserDetailsApplianceList = null

                modelGetUserMgtDetailsSucessError.modelGetUserDetailsAppliance = modelGetUserDetailsBodySucess

                getUserMgtDetailsSucessError?.value = modelGetUserMgtDetailsSucessError
            }


        }, Response.ErrorListener { volleyError ->
            val modelGetUserMgtDetailsSucessError = ModelGetUserMgtDetailsSucessError()
            modelGetUserMgtDetailsSucessError.volleyError = volleyError
            getUserMgtDetailsSucessError?.value = modelGetUserMgtDetailsSucessError
        }) {

        }

        getUserMgtDetailsRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getUserMgtDetailsRequest)
    }

    fun parseApplianceJsonObject(applianceObject: JSONObject): ModelGetUserDetailsAppliance {

        var modelGetUserDetailsAppliance = ModelGetUserDetailsAppliance()

        modelGetUserDetailsAppliance.brandName = applianceObject.getString("BrandName")
        modelGetUserDetailsAppliance.remoteID = applianceObject.getString("RemoteID")

        modelGetUserDetailsAppliance.brandId = applianceObject.getString("BrandId")
        modelGetUserDetailsAppliance.appliance = applianceObject.getString("Appliance")

        modelGetUserDetailsAppliance.customName = applianceObject.getString("CustomName")

        return modelGetUserDetailsAppliance
    }

    fun getToken(code: String, refreshToken: String?, grantType: String) {
        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        val baseURL = ApiConstants.BASE_URL + "oauth2/token"

        val getTokenRequest = object : StringRequest(Request.Method.POST, baseURL, Response.Listener { response ->

            LibreLogger.d(this, "getToken response$response")

            val responseObject = JSONObject(response)

            val tokenRepoModel = TokenRepoModel()
            tokenRepoModel.tokenSucessRepoModel = TokenSucessRepoModel()

            tokenRepoModel.tokenSucessRepoModel?.idToken = responseObject.getString("id_token")
            tokenRepoModel.tokenSucessRepoModel?.accessToken = responseObject.getString("access_token")
            tokenRepoModel.tokenSucessRepoModel?.refreshToken = responseObject.getString("refresh_token")
            tokenRepoModel.tokenSucessRepoModel?.expiresIn = responseObject.getString("expires_in")
            tokenRepoModel.tokenSucessRepoModel?.tokenType = responseObject.getString("token_type")


            getTokenRepoModel?.value = tokenRepoModel

        }, Response.ErrorListener { volleyError ->

            val tokenRepoModel = TokenRepoModel()
            tokenRepoModel.volleyError = volleyError
            getTokenRepoModel?.value = tokenRepoModel

        }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()

                val clientIdString =
                        context.resources.getString(R.string.client_id)

                val clientSecret =
                        context.resources.getString(R.string.client_secret)

                val authHeaderDataString = clientIdString.plus(":").plus(clientSecret)

                val authHeaderByteData = authHeaderDataString.toByteArray()


                val authoriationHeaderBase64 =
                        Base64.encodeToString(authHeaderByteData, Base64.NO_WRAP)


                params["Authorization"] = "Basic".plus(" ").plus(authoriationHeaderBase64)

                params["Content-Type"] = "application/x-www-form-urlencoded"

                LibreLogger.d(this, "header$params")

                return params

            }

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()

                params["grant_type"] = grantType
                params["redirect_uri"] = redirectURI

                params["code"] = code

                if (refreshToken != null) {
                    params["refresh_token"] = refreshToken
                }
                return params
            }
        }
        getTokenRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getTokenRequest)
    }

    fun getTvBrandsList() {
        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        val url = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/TV"

        val getTvBrandsList = object : StringRequest(Request.Method.GET, url, Response.Listener { response ->

            LibreLogger.d(this, "getTvBrandList response$response")

            val responseObject: JSONObject = JSONObject(response)

            val bodyObject = responseObject.getJSONObject("body")

            val payloadArrayObject = bodyObject.getJSONArray("payload")

            if (payloadArrayObject.length() > 0) {

                val tvBrandsDetailsRepoModelList: MutableList<TvBrandsSucessRepoModel> = ArrayList()

                val tvBrandsDetailsRepoModel = TvBrandsDetailsRepoModel()

                for (i in 0 until payloadArrayObject.length()) {

                    val payloadJsonObject: JSONObject = payloadArrayObject[i] as JSONObject

                    val tvBrandsSucessRepoModel = TvBrandsSucessRepoModel()

                    tvBrandsSucessRepoModel.id = payloadJsonObject.getInt("id")
                    tvBrandsSucessRepoModel.name = payloadJsonObject.getString("name")

                    val remoteJsonArray = payloadJsonObject.getJSONArray("remotes")

                    if (remoteJsonArray.length() > 0) {
                        val remoteList: MutableList<Int> = ArrayList()
                        for (j in 0 until remoteJsonArray.length()) {
                            val value: Int = remoteJsonArray.getInt(j)
                            remoteList.add(value)
                        }
                        tvBrandsSucessRepoModel.remoteList = remoteList
                    }

                    tvBrandsDetailsRepoModelList.add(tvBrandsSucessRepoModel)
                }

                tvBrandsDetailsRepoModel.tvBrandsDetailsRepoModelList = tvBrandsDetailsRepoModelList
                getTvBrandDetailsRepoModel?.value = tvBrandsDetailsRepoModel

            }
        }, Response.ErrorListener { volleyError ->
            val tvBrandsDetailsRepoModel = TvBrandsDetailsRepoModel()
            tvBrandsDetailsRepoModel.volleyError = volleyError
            getTvBrandDetailsRepoModel?.value = tvBrandsDetailsRepoModel
        }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                val params = HashMap<String, String>()
//                params["Authorization"] =
//                return params
//            }
        }
        getTvBrandsList.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getTvBrandsList)
    }


    fun getTVPBrandsGetApiResponse(): MutableLiveData<ModelGetTvpBodyResponse> {
        if (getTvpBrandsLiveData == null) {
            getTvpBrandsLiveData = MutableLiveData<ModelGetTvpBodyResponse>()
            getTVPBrandsList()
        }
        return getTvpBrandsLiveData!!
    }

    fun getAcBrandsGetApiResponse(): MutableLiveData<AcBrandsDetailsRepoModel> {
        if (getAcBrandsLiveData == null) {
            getAcBrandsLiveData = MutableLiveData<AcBrandsDetailsRepoModel>()
            getAcBrandsList()
        }
        return getAcBrandsLiveData!!
    }

    fun getAcBrandsList() {
        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        val acBrandsListUrl = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/AC"

        val getAcBrandsListStringRequest = object : StringRequest(Request.Method.GET, acBrandsListUrl, Response.Listener { response ->

            Log.d(TAG, "getTvBrandList response$response")

            val responseObject: JSONObject = JSONObject(response)

            val bodyObject = responseObject.getJSONObject("body")

            val payloadArrayObject = bodyObject.getJSONArray("payload")

            if (payloadArrayObject.length() > 0) {
                val acBrandsDetailsRepoModel = AcBrandsDetailsRepoModel()
                val acBrandsDetailsRepoModelList: MutableList<AcBrandsSucessRepoModel> = ArrayList()
                for (i in 0 until payloadArrayObject.length()) {
                    val payloadJsonObject: JSONObject = payloadArrayObject[i] as JSONObject
                    val acBrandsSucessRepoModel = AcBrandsSucessRepoModel()
                    acBrandsSucessRepoModel.id = payloadJsonObject.getInt("id")
                    acBrandsSucessRepoModel.name = payloadJsonObject.getString("name")

                    val remoteJsonArray = payloadJsonObject.getJSONArray("remotes")
                    if (remoteJsonArray.length() > 0) {
                        val remoteList: MutableList<Int> = ArrayList()
                        for (j in 0 until remoteJsonArray.length()) {
                            val value: Int = remoteJsonArray.getInt(j)
                            remoteList.add(value)
                        }
                        acBrandsSucessRepoModel.remoteList = remoteList
                    }
                    acBrandsDetailsRepoModelList.add(acBrandsSucessRepoModel)
                }
                acBrandsDetailsRepoModel.acBrandsSucessRepoModelList = acBrandsDetailsRepoModelList

                getAcBrandsLiveData?.value = acBrandsDetailsRepoModel
            }
        }, Response.ErrorListener { volleyError ->
            val acBrandsDetailsRepoModel = AcBrandsDetailsRepoModel()
            acBrandsDetailsRepoModel.volleyError = volleyError
            getAcBrandsLiveData?.value = acBrandsDetailsRepoModel
        }) {}
        getAcBrandsListStringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getAcBrandsListStringRequest)
    }

    fun getTVPBrandsList() {

        val tvpBrandListUrl = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/TVP"

        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)


        val getTvpBrandsListStringRequest = object : StringRequest(Request.Method.GET, tvpBrandListUrl, Response.Listener { response ->

            Log.d(TAG, " getTVPBrandsList response$response")

            var modelGetTvpBodyResponse = ModelGetTvpBodyResponse()


            val responseObject: JSONObject = JSONObject(response)

            var statusCode = responseObject.getString("statuscode")

            if (statusCode == "200") {

                var responseBody = responseObject.getJSONObject("body")

                var responsePayload = responseBody.getJSONObject("payload")

                var responseArray = responsePayload.getJSONArray("result")

                var modelGetTvpBrandsSucessResponseList: MutableList<ModelGetTvpBrandsSucessResponse> = ArrayList()

                for (i in 0 until responseArray.length()) {

                    var modelGetTvpBrandsSucessResponse = ModelGetTvpBrandsSucessResponse()

                    var responseTVpJsonObject = responseArray[i] as JSONObject

                    modelGetTvpBrandsSucessResponse.id = responseTVpJsonObject.getInt("id")
                    modelGetTvpBrandsSucessResponse.title = responseTVpJsonObject.getString("title")
                    modelGetTvpBrandsSucessResponse.isDth = responseTVpJsonObject.getBoolean("is_dth")
                    modelGetTvpBrandsSucessResponse.providerGroup = responseTVpJsonObject.getString("provider_group")

                    modelGetTvpBrandsSucessResponseList.add(modelGetTvpBrandsSucessResponse)
                }
                modelGetTvpBodyResponse.modelGetTvpBrandsSucessResponseList = modelGetTvpBrandsSucessResponseList

                getTvpBrandsLiveData?.value = modelGetTvpBodyResponse
            }


        }, Response.ErrorListener { volleyError ->
            var modelGetTvpBodyResponse = ModelGetTvpBodyResponse()

            modelGetTvpBodyResponse.volleyError = volleyError
            getTvpBrandsLiveData?.value = modelGetTvpBodyResponse
        }) {}
        getTvpBrandsListStringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(getTvpBrandsListStringRequest)
    }


    fun getRegionalTvpBrandListApiResponse(id: Int): MutableLiveData<ModelGetRegionalBodyResponse>? {
        if (getTvpRegionalBrandLiveData == null) {
            getTvpRegionalBrandLiveData = MutableLiveData<ModelGetRegionalBodyResponse>()
            getRegionalTvpBrandsList(id)
        }
        return getTvpRegionalBrandLiveData
    }


    fun getRegionalTvpBrandsList(id: Int) {

        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        val tvpRegionalBrandsListtUrl = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/TVP?id=".plus(id)


        val tvRegionalBrandsStringRequest: StringRequest = object : StringRequest(Request.Method.GET, tvpRegionalBrandsListtUrl, Response.Listener {

            response ->

            val responseObject: JSONObject = JSONObject(response)


            Log.d(TAG, "response$response")

            val responseBodyObject: JSONObject? = responseObject.optJSONObject("body")

            if (responseBodyObject != null) {

                var modelGetRegionalBodyResponse = ModelGetRegionalBodyResponse()

                var modelGetRegionalTvpPayloadSucessList: MutableList<ModelGetRegionalTvpPayloadSucess> = ArrayList()

                var statusCode: String = responseObject.getString("statuscode")

                if (statusCode == "200") {

                    val payloadObject: JSONObject = responseBodyObject.getJSONObject("payload")

                    val completedJsonArray: JSONArray = payloadObject.getJSONArray("completed")

                    for (i in 0 until completedJsonArray.length()) {

                        var modelGetRegionalTvpPayloadSucess = ModelGetRegionalTvpPayloadSucess()

                        var completedJsonObject: JSONObject = completedJsonArray[i] as JSONObject

                        modelGetRegionalTvpPayloadSucess.remoteIds = completedJsonObject.getString("remote_ids")

                        modelGetRegionalTvpPayloadSucess.regionalId = completedJsonObject.getInt("id")

                        modelGetRegionalTvpPayloadSucess.state = completedJsonObject.getString("state")

                        modelGetRegionalTvpPayloadSucess.title = completedJsonObject.getString("title")

                        modelGetRegionalTvpPayloadSucess.region = completedJsonObject.getString("region")


                        modelGetRegionalTvpPayloadSucess.slug = completedJsonObject.getString("slug")

                        modelGetRegionalTvpPayloadSucess.group = completedJsonObject.getString("group")

                        //brand json object
                        var modelTvpGetRegionalBrand = ModelTvpGetRegionalBrand()
                        var brandJsonObject = completedJsonObject.getJSONObject("brand")

                        modelTvpGetRegionalBrand.tvpBrandId = brandJsonObject.getInt("id")
                        modelTvpGetRegionalBrand.tvpBrandTitle = brandJsonObject.getString("title")

                        modelTvpGetRegionalBrand.isDth = brandJsonObject.getBoolean("is_dth")
                        modelTvpGetRegionalBrand.providerGroup = brandJsonObject.getString("provider_group")

                        modelGetRegionalTvpPayloadSucess.modelTvpGetRegionalBrand = modelTvpGetRegionalBrand

                        modelGetRegionalTvpPayloadSucessList.add(modelGetRegionalTvpPayloadSucess)
                    }
                }

                modelGetRegionalBodyResponse.modelGetTvpBrandsSucessResponseList = modelGetRegionalTvpPayloadSucessList

                getTvpRegionalBrandLiveData?.value = modelGetRegionalBodyResponse
            } else {
                var modelGetRegionalBodyResponse = ModelGetRegionalBodyResponse()

                var modelGetRegionalTvpPayloadSucessList: MutableList<ModelGetRegionalTvpPayloadSucess> = ArrayList()

                val modelGetRegionalTvpPayloadSucess = ModelGetRegionalTvpPayloadSucess()
                modelGetRegionalTvpPayloadSucess.remoteIds = "-1"
                modelGetRegionalTvpPayloadSucess.title = "No data found"

                modelGetRegionalTvpPayloadSucessList.add(modelGetRegionalTvpPayloadSucess)

                modelGetRegionalBodyResponse.modelGetTvpBrandsSucessResponseList = modelGetRegionalTvpPayloadSucessList

                getTvpRegionalBrandLiveData?.value = modelGetRegionalBodyResponse
            }


        }, Response.ErrorListener { volleyError ->
            var modelGetRegionalBodyResponse = ModelGetRegionalBodyResponse()
            modelGetRegionalBodyResponse.volleyError = volleyError

            getTvpRegionalBrandLiveData?.value = modelGetRegionalBodyResponse
        }) {}

        tvRegionalBrandsStringRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(tvRegionalBrandsStringRequest)

    }


    fun getAcSelectJsonData(id: Int) {

        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        var url: String? = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/ACSELECT?id=".plus(id) //select json for ac

        Log.d(TAG,"ac_selection_url".plus(url))

        val getAcBrandList = object : StringRequest(Request.Method.GET, url, Response.Listener { response ->

            Log.d(TAG, "getAcSelectJsonData response$response")


            val responseObject = JSONObject(response)

            val modelAcRepoModel = ModelAcRepoBody()

            var modelSelectAcRemotePayloadList: MutableList<ModelSelectAcRemotePayload> = ArrayList()

            var bodyObject: JSONObject? = null
            try {
                bodyObject = responseObject.getJSONObject("body") as JSONObject
            } catch (e: JSONException) {
                //body might be a string
                val modelSelectAcRemotePayload = ModelSelectAcRemotePayload()
                modelSelectAcRemotePayload.remoteId = "0"
                modelSelectAcRemotePayload.powerOff = "No Data present"
                modelSelectAcRemotePayload.powerOnOrIrCommand = "No Data present"

                modelSelectAcRemotePayloadList.add(modelSelectAcRemotePayload)

                modelAcRepoModel.modelSelecAcRemotePayload = modelSelectAcRemotePayloadList

                getSelecAcRemoteJsonLiveData?.value = modelAcRepoModel
            }

            if (bodyObject != null) {
                var payloadObject: JSONObject = bodyObject!!.getJSONObject("payload")

                var remotesJsonArray: JSONArray = payloadObject.getJSONArray("remotes")

                for (i in 0 until remotesJsonArray.length()) {
                    val remoteJsonObject: JSONObject = remotesJsonArray[i] as JSONObject
                    val modelSelectAcRemotePayload = ModelSelectAcRemotePayload()
                    modelSelectAcRemotePayload.remoteId = remoteJsonObject.getInt("id").toString()
                    modelSelectAcRemotePayload.powerOff = remoteJsonObject.getString("Power Off")
                    modelSelectAcRemotePayload.powerOnOrIrCommand = remoteJsonObject.getString("ircommand")

                    modelSelectAcRemotePayloadList.add(modelSelectAcRemotePayload)
                }

                modelAcRepoModel.modelSelecAcRemotePayload = modelSelectAcRemotePayloadList

                getSelecAcRemoteJsonLiveData?.value = modelAcRepoModel
            }

        }, Response.ErrorListener { volleyError ->
            val modelAcRepoModel = ModelAcRepoBody()
            modelAcRepoModel.volleyError = volleyError
            getSelecAcRemoteJsonLiveData?.value = modelAcRepoModel
        }) {}
        getAcBrandList.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getAcBrandList)
    }

    fun getTvOrTvpelectJsonData(id: Int, selectedApplianceType: String) {

        val context = getApplication<Application>().applicationContext

        val requestQueue = Volley.newRequestQueue(context)

        var url: String? = null

        //select json for tv
        when (selectedApplianceType) {
            "1" -> {
                //tv
                url = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/TVSELECT?id=".plus(id)//select json for tv
            }
            "2" -> {
                //tvp
                url = "https://op4w1ojeh4.execute-api.us-east-1.amazonaws.com/Beta/TVPSELECT?id=".plus(id)//select json for tvp
            }
        }

        Log.d(TAG, "selectJson request url ".plus(url))

//        val url = "https://api.jsonbin.io/b/5f157b1ac1edc466175a9635/1"

        val getTvBrandsList = object : StringRequest(Request.Method.GET, url, Response.Listener { response ->

            Log.d(TAG, "getTvSelectJsonData response$response")

            val responseObject: JSONObject = JSONObject(response)
            val modelRepoBody = ModelRepoBody()

            var bodyObject: Any? = null
            try {
                bodyObject = responseObject.getJSONObject("body")
            } catch (e: JSONException) {
                //no data is present
                modelSelectRemotePayload.applianceBrandId = 0
                modelSelectRemotePayload.applianceName = "No Data present"
                modelSelectRemotePayload.levelJsonArray = JSONArray()
                modelRepoBody.modelSelectRemotePayload = modelSelectRemotePayload

                getSelectRemoteJson?.value = modelRepoBody
            }

            if (bodyObject != null) {

                bodyObject as JSONObject

                val payLoadObject = bodyObject.getJSONObject("payload")

                //io and appliance name
                modelSelectRemotePayload.applianceBrandId = bodyObject.getInt("ID")
                modelSelectRemotePayload.applianceName = bodyObject.getString("Appliance")


                modelSelectRemotePayload.levelJsonArray = payLoadObject.getJSONArray("level1")
                //
                modelRepoBody.modelSelectRemotePayload = modelSelectRemotePayload

                getSelectRemoteJson?.value = modelRepoBody
            }

        }, Response.ErrorListener { volleyError ->
            val modelRepoBody = ModelRepoBody()
            modelRepoBody.volleyError = volleyError
            getSelectRemoteJson?.value = modelRepoBody
        }) {}
        getTvBrandsList.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(getTvBrandsList)
    }


}


