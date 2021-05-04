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
import com.libre.irremote.adapters.IrSelectTvpBrandsAdapter
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo
import com.libre.irremote.models.ModelGetTvpBrandsSucessResponse
import com.libre.irremote.utility.RecyclerItemClickListener
import com.libre.irremote.utility.UIRelatedClass
import com.libre.irremote.viewmodels.ApiViewModel
import kotlinx.android.synthetic.main.ir_tvp_brand_activity_layout.*
import kotlinx.android.synthetic.main.ir_tvp_brand_activity_layout.progressBar


class IRTvpBrandActivity : AppCompatActivity() {


    lateinit var apiViewModel: ApiViewModel

    val uiRelatedClass = UIRelatedClass()

    var deviceInfo: DeviceInfo? = null

    var bundle = Bundle()

    companion object {
        var irTvpBrandActivity: IRTvpBrandActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ir_tvp_brand_activity_layout)

        irTvpBrandActivity = this

        bundle = intent.extras!!

        if (bundle != null) {
            deviceInfo = bundle.getSerializable("deviceInfo") as DeviceInfo
        }

        apiViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ApiViewModel::class.java)

        showProgressBar()
        getTvpBrandList()
    }

    override fun onDestroy() {
        super.onDestroy()
        irTvpBrandActivity = null
    }

    fun getTvpBrandList() {
        apiViewModel.getTVPBrandsGetApiResponse().observe(this, Observer {
            if (it.modelGetTvpBrandsSucessResponseList != null) {

                val sortedList: MutableList<ModelGetTvpBrandsSucessResponse> = it.modelGetTvpBrandsSucessResponseList!!.sortedBy { it.title }
                        as MutableList<ModelGetTvpBrandsSucessResponse>

                setRecyclerViewAdapter(sortedList)
            } else {
                val volleyError = it?.volleyError

                if (volleyError is TimeoutError || volleyError is NoConnectionError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvpBrandActivity, "Seems your internet connection is slow, please try in sometime",
                            "Go Back", this@IRTvpBrandActivity)


                } else if (volleyError is AuthFailureError) {

                    uiRelatedClass.buildSnackBarWithoutButton(this@IRTvpBrandActivity,
                            window.decorView.findViewById(android.R.id.content), "AuthFailure error occurred, please try again later")


                } else if (volleyError is ServerError) {
                    if (volleyError.networkResponse.statusCode != 302) {
                        uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvpBrandActivity, "Server error occurred, please try again later",
                                "Go Back", this@IRTvpBrandActivity)
                    }

                } else if (volleyError is NetworkError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvpBrandActivity, "Network error occurred, please try again later",
                            "Go Back", this@IRTvpBrandActivity)


                } else if (volleyError is ParseError) {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvpBrandActivity, "Parser error occurred, please try again later",
                            "Go Back", this@IRTvpBrandActivity)

                } else {

                    uiRelatedClass.buidCustomSnackBarWithButton(this@IRTvpBrandActivity, "SomeThing is wrong!!.Please Try after some time",
                            "Go Back", this@IRTvpBrandActivity)

                }
            }
            dismissLoader()
        })
    }

    fun setRecyclerViewAdapter(modelGetTvpBrandsSucessResponseList: MutableList<ModelGetTvpBrandsSucessResponse>) {
        val itemDecorator = DividerItemDecoration(this@IRTvpBrandActivity, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this@IRTvpBrandActivity, R.drawable.orange_divider)!!)



        rvTvpBrandsList.layoutManager = LinearLayoutManager(this@IRTvpBrandActivity)

        rvTvpBrandsList.adapter = IrSelectTvpBrandsAdapter(this@IRTvpBrandActivity, modelGetTvpBrandsSucessResponseList)
        rvTvpBrandsList.addItemDecoration(itemDecorator)



        rvTvpBrandsList.addOnItemTouchListener(RecyclerItemClickListener(this@IRTvpBrandActivity, rvTvpBrandsList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onLongItemClick(view: View?, position: Int) {
            }

            override fun onItemClick(view: View?, position: Int) {
                //taking the user for regional tvp selection screen
                val intent = Intent(this@IRTvpBrandActivity, IRSelectTvOrTVPOrAcRegionalBrandsActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("deviceInfo", deviceInfo)
                bundle.putString("tvpBrandId", modelGetTvpBrandsSucessResponseList[position].id.toString())
                bundle.putString("tvpBrandName", modelGetTvpBrandsSucessResponseList[position].title)
                bundle.putBoolean("isTvp", true)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }))
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