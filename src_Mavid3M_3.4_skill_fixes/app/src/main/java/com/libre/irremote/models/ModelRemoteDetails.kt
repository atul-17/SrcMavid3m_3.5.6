package com.libre.irremote.models

import java.io.Serializable

data class ModelRemoteDetails(var selectedAppliance: String = "",
                              var selectedBrandName: String = "",
                              var remoteId: String = "",
                              var brandId: String = "",
                              var customName: String = "",
                              var groupId: Int = 1,//by default
                              var groupdName: String = "Scene1",
                              var index: Int = 1,
                              var remotesHashMap: HashMap<String, String> = HashMap(),
                              var ac_remotelist: MutableList<ModelLdapi2AcModes>? = ArrayList()
) : Serializable {


    override fun equals(other: Any?): Boolean {
        if (other is ModelRemoteDetails) {
            if (this.selectedAppliance == other.selectedAppliance
                    && this.selectedBrandName == other.selectedBrandName
                    && this.remoteId == other.remoteId
                    && this.brandId == other.brandId)

                return true;
        }
        return false
    }

    override fun hashCode(): Int {
        return remoteId.toInt()
    }
}
