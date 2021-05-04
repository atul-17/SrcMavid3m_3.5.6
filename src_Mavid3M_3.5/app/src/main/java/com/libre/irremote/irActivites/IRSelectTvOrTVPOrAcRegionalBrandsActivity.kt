package com.libre.irremote.irActivites

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.*
import com.libre.irremote.R
import com.libre.irremote.adapters.IRSelectAcBrandsAdapter
import com.libre.irremote.adapters.IRSelectRegionalTvpAdapter
import com.libre.irremote.adapters.IRSelectTvBrandsAdapter
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.AcBrandsSucessRepoModel
import com.libre.irremote.models.ModelGetRegionalBodyResponse
import com.libre.irremote.models.ModelGetRegionalTvpPayloadSucess
import com.libre.irremote.models.TvBrandsSucessRepoModel
import com.libre.irremote.utility.RecyclerItemClickListener
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import kotlinx.android.synthetic.main.activity_ir_select_tv_or_tvp_regional_brands.*
import kotlinx.android.synthetic.main.activity_ir_select_tv_or_tvp_regional_brands.progressBar


class IRSelectTvOrTVPOrAcRegionalBrandsActivity : AppCompatActivity() {

    lateinit var apiViewModel: ApiViewModel

    val uiRelatedClass = UIRelatedClass()

    var deviceInfo: DeviceInfo? = null

    var bundle = Bundle()

    var isTvp: Boolean = false

    var tvpBrandId: String = ""

    var tvpBrandName: String = ""

    var isAc: Boolean = false

    var isTv: Boolean = false

    companion object {
        var irSelectTvOrTVPOrAcRegionalBrandsActivity: IRSelectTvOrTVPOrAcRegionalBrandsActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_select_tv_or_tvp_regional_brands)

        irSelectTvOrTVPOrAcRegionalBrandsActivity = this


        bundle = intent.extras!!

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
            isTvp = bundle.getBoolean("isTvp", false)
            tvpBrandId = bundle.getString("tvpBrandId", "")
            tvpBrandName = bundle.getString("tvpBrandName", "")

            isAc = bundle.getBoolean("isAc", false)

            isTv = bundle.getBoolean("isTv", false)
        }

        apiViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ApiViewModel::class.java)

        showProgressBar()
        when {
            isTvp -> {
                //tvp
                activityHeading.text = "Select your Regional Set Top Box"
                getTVPBrandList(tvpBrandId.toInt())
            }
            isTv -> {
                activityHeading.text = "Select your TV Manufacturer"
                getTvBrandsList()
            }
            isAc -> {
                //ac
                activityHeading.text = "Select your Ac Manufacturer"
                getAcBrandsList()
            }
        }
        llGoBack.setOnClickListener {
            finish()
        }
    }


    fun getAcBrandsList() {
        apiViewModel.getAcBrandsGetApiResponse()?.observe(this, Observer {
            if (it.acBrandsSucessRepoModelList != null) {
                val sortedList = it.acBrandsSucessRepoModelList?.sortedBy { it.name } as MutableList<AcBrandsSucessRepoModel>
                setAcAdapter(sortedList)
            } else {
                val volleyError = it.volleyError

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Seems your internet connection is slow, please try in sometime",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)


                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity,
                            window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Server error occurred, please try again later",
                                "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)
                    }

                } else if (volleyError is NetworkError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Network error occurred, please try again later",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)


                } else if (volleyError is ParseError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Parser error occurred, please try again later",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

                } else {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "SomeThing is wrong!!.Please Try after some time",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

                }
            }
            dismissLoader()
        })
    }


    fun setAcAdapter(acBrandsDetailsRepoModelList: MutableList<AcBrandsSucessRepoModel>) {
        //ac--> 3
        val itemDecorator = DividerItemDecoration(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, R.drawable.orange_divider)!!)

        rvTvOrTvpOrAcBrandsList.layoutManager = LinearLayoutManager(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

        rvTvOrTvpOrAcBrandsList.adapter = IRSelectAcBrandsAdapter(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, acBrandsDetailsRepoModelList)
        rvTvOrTvpOrAcBrandsList.addItemDecoration(itemDecorator)

        rvTvOrTvpOrAcBrandsList.addOnItemTouchListener(RecyclerItemClickListener(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, rvTvOrTvpOrAcBrandsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
            }

            override fun onItemClick(view: View?, position: Int) {
                //taking the user for remote selection instructions
                val intent = Intent(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, IRRemoteSelectionInstrActivity::class.java)
                val bundle = Bundle()
                bundle.putInt("applianceId", acBrandsDetailsRepoModelList[position].id)
                bundle.putString("applianceBrandName", acBrandsDetailsRepoModelList[position].name)
                bundle.putString("ipAddress", deviceInfo?.ipAddress)
                bundle.putSerializable("deviceInfo", deviceInfo)
                bundle.putString("selectedApplianceType", "3")
                intent.putExtras(bundle)
                startActivity(intent)

            }
        }))
    }


    fun inflateRecyclerView(it: ModelGetRegionalBodyResponse) {
        val sortedList = it.modelGetTvpBrandsSucessResponseList?.sortedBy { it.title } as MutableList<ModelGetRegionalTvpPayloadSucess>
        rvTvOrTvpOrAcBrandsList.visibility = View.VISIBLE
        llNoDevices.visibility = View.GONE
        setTVPAdapter(sortedList)
    }

    fun getTVPBrandList(id: Int) {
        apiViewModel.getRegionalTvpBrandListApiResponse(id)?.observe(this, Observer {

            if (it.modelGetTvpBrandsSucessResponseList != null) {
                if (it.modelGetTvpBrandsSucessResponseList?.size == 1) {
                    if (it.modelGetTvpBrandsSucessResponseList!![0].remoteIds == "-1") {
                        //no data present
                        //show empty error
                        llNoDevices.visibility = View.VISIBLE
                        rvTvOrTvpOrAcBrandsList.visibility = View.GONE
                    } else {
                        inflateRecyclerView(it)
                    }
                } else {
                    inflateRecyclerView(it)
                }
            } else {


                val volleyError = it?.volleyError

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Seems your internet connection is slow, please try in sometime",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)


                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity,
                            window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Server error occurred, please try again later",
                                "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)
                    }

                } else if (volleyError is NetworkError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Network error occurred, please try again later",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)


                } else if (volleyError is ParseError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Parser error occurred, please try again later",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

                } else {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "SomeThing is wrong!!.Please Try after some time",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

                }
            }
            dismissLoader()
        })
    }


    fun getTvBrandsList() {
        apiViewModel.getTvBrandsDetails()?.observe(this, Observer {
            if (it.tvBrandsDetailsRepoModelList != null) {
                //they are tv brands

                val sortedList: MutableList<TvBrandsSucessRepoModel> = it.tvBrandsDetailsRepoModelList!!.sortedBy { it.name } as MutableList<TvBrandsSucessRepoModel>

                setTVAdapter(sortedList)
            } else {

                val volleyError = it?.volleyError


                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Seems your internet connection is slow, please try in sometime",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)


                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity,
                            window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Server error occurred, please try again later",
                                "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)
                    }

                } else if (volleyError is NetworkError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Network error occurred, please try again later",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)


                } else if (volleyError is ParseError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "Parser error occurred, please try again later",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

                } else {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, "SomeThing is wrong!!.Please Try after some time",
                            "Go Back", this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

                }
            }
            dismissLoader()
        })
    }

    fun setTVAdapter(tvBrandsList: MutableList<TvBrandsSucessRepoModel>) {
        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        val itemDecorator = DividerItemDecoration(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, R.drawable.orange_divider)!!)

        rvTvOrTvpOrAcBrandsList.layoutManager = LinearLayoutManager(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

        rvTvOrTvpOrAcBrandsList.adapter = IRSelectTvBrandsAdapter(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, tvBrandsList)
        rvTvOrTvpOrAcBrandsList.addItemDecoration(itemDecorator)

        rvTvOrTvpOrAcBrandsList.addOnItemTouchListener(RecyclerItemClickListener(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, rvTvOrTvpOrAcBrandsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
            }

            override fun onItemClick(view: View?, position: Int) {
                //taking the user for remote selection instructions
                val intent = Intent(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, IRRemoteSelectionInstrActivity::class.java)
                val bundle = Bundle()
                bundle.putInt("applianceId", tvBrandsList[position].id)
                bundle.putString("applianceBrandName", tvBrandsList[position].name)
                bundle.putString("ipAddress", deviceInfo?.ipAddress)
                bundle.putSerializable("deviceInfo", deviceInfo)
                bundle.putString("selectedApplianceType", "1")
                intent.putExtras(bundle)
                startActivity(intent)

            }
        }))
    }

    fun setTVPAdapter(modelGetTvpBrandsSucessResponseList: MutableList<ModelGetRegionalTvpPayloadSucess>) {

        /** <ID>: 1 : TV
        2 : STB
        3 : AC*/

        val itemDecorator = DividerItemDecoration(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, R.drawable.orange_divider)!!)

        rvTvOrTvpOrAcBrandsList.layoutManager = LinearLayoutManager(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity)

        rvTvOrTvpOrAcBrandsList.adapter = IRSelectRegionalTvpAdapter(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, modelGetTvpBrandsSucessResponseList)
        rvTvOrTvpOrAcBrandsList.addItemDecoration(itemDecorator)

        rvTvOrTvpOrAcBrandsList.addOnItemTouchListener(RecyclerItemClickListener(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, rvTvOrTvpOrAcBrandsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
            }

            override fun onItemClick(view: View?, position: Int) {
                //taking the user for remote selection screen
                val intent = Intent(this@IRSelectTvOrTVPOrAcRegionalBrandsActivity, IRRemoteSelectionInstrActivity::class.java)
                val bundle = Bundle()
                bundle.putInt("applianceId", modelGetTvpBrandsSucessResponseList[position].regionalId)
                bundle.putString("applianceBrandName", modelGetTvpBrandsSucessResponseList[position].modelTvpGetRegionalBrand.tvpBrandTitle)
                bundle.putString("ipAddress", deviceInfo?.ipAddress)
                bundle.putSerializable("deviceInfo", deviceInfo)
                bundle.putString("selectedApplianceType", "2")
                intent.putExtras(bundle)
                startActivity(intent)

            }
        }))
    }


    override fun onDestroy() {
        super.onDestroy()
        irSelectTvOrTVPOrAcRegionalBrandsActivity = null
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.show()
    }

    fun dismissLoader() {
        progressBar.visibility = View.GONE
        progressBar.hide()
    }

}