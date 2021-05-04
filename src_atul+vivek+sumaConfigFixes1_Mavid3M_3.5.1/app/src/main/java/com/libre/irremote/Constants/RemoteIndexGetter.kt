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
        fun getNextRemoteIndex(context: Context?, selectedAppliance: String): Int {
            val remotIndex = 1
            var modelRemoteSubAndMacDetils: ModelRemoteSubAndMacDetils? = null
            val sharedPreferences = context?.getSharedPreferences("Mavid", Context.MODE_PRIVATE)
            val gson = Gson()
            val modelRemoteDetailsString = sharedPreferences?.getString("applianceInfoList", "")
            if (!modelRemoteDetailsString!!.isEmpty()) {
                modelRemoteSubAndMacDetils = gson.fromJson(modelRemoteDetailsString, ModelRemoteSubAndMacDetils::class.java)
                return getNextRemoteIndex(modelRemoteSubAndMacDetils.modelRemoteDetailsList, selectedAppliance)
            }
            return remotIndex
        }

        fun getNextRemoteIndex(modelRemoteDetailsList: List<ModelRemoteDetails>?, selectedAppliance: String): Int {
            var startIndex = 1
            val modelRemoteDetailsListFiltered = getSelectedModelRemoteList(selectedAppliance, modelRemoteDetailsList)
            if (modelRemoteDetailsListFiltered != null && modelRemoteDetailsListFiltered.size > 0)
                for (i in modelRemoteDetailsListFiltered.indices) {
                    if (startIndex == modelRemoteDetailsListFiltered[i].index) {
                        startIndex++
                    } else if (isIndexAlreadyPresentInModelRemoteList(startIndex, modelRemoteDetailsListFiltered)) {
                        startIndex++
                    } else
                        return startIndex
                }
            return startIndex
        }

        fun getSelectedModelRemoteList(appliance: String, modelRemoteDetailsList: List<ModelRemoteDetails>?): ArrayList<ModelRemoteDetails> {
            val modelRemoteList = ArrayList<ModelRemoteDetails>()
            if (modelRemoteDetailsList != null && modelRemoteDetailsList.size > 0)
                for (i in modelRemoteDetailsList!!.indices) {
                    if (modelRemoteDetailsList[i].selectedAppliance.equals(appliance)) {
                        modelRemoteList.add(modelRemoteDetailsList[i])
                    }
                }
            return modelRemoteList
        }

        fun isIndexAlreadyPresentInModelRemoteList(index: Int, modelRemoteDetailsList: List<ModelRemoteDetails>?): Boolean {
            if (modelRemoteDetailsList != null && modelRemoteDetailsList.size > 0)
                for (i in modelRemoteDetailsList!!.indices) {
                    if (modelRemoteDetailsList[i].index == index) {
                        return true
                    }
                }
            return false
        }

    }
}