package com.libre.irremote.Constants

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.libre.irremote.models.ModelRemoteDetails
import com.libre.irremote.models.ModelRemoteSubAndMacDetils
import org.json.JSONObject
import java.nio.charset.Charset

class RemoteIndexGetter {
    companion object {
        fun getNextRemoteIndex(context: Context?): Int {
            val remotIndex = 1
            var modelRemoteSubAndMacDetils: ModelRemoteSubAndMacDetils? = null
            val sharedPreferences = context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)
            val gson = Gson()
            val modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")
            if (!modelRemoteDetailsString!!.isEmpty()) {
                modelRemoteSubAndMacDetils = gson.fromJson(modelRemoteDetailsString, ModelRemoteSubAndMacDetils::class.java)
                return getNextRemoteIndex(modelRemoteSubAndMacDetils.modelRemoteDetailsList)
            }
            return remotIndex
        }

        fun getNextRemoteIndex(modelRemoteDetailsList: List<ModelRemoteDetails>?): Int {
            var startIndex = 1
            if (modelRemoteDetailsList != null && modelRemoteDetailsList.size > 0) for (i in modelRemoteDetailsList.indices) {
                if (startIndex == modelRemoteDetailsList[i].index) {
                    startIndex++
                } else return startIndex
            }
            return startIndex
        }

    }
}