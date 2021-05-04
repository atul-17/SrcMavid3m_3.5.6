package com.libre.irremote.models

import org.json.JSONArray
import java.io.Serializable

class ModelLevelCode : Serializable {
    var isCommandWorking : Boolean? = null
    var command: String? = null
    var idList: MutableList<Int>? = null
    var subLevelJsonArray: JSONArray? = null
    var codeLevelIndex: Int? = null
}