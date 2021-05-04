package com.libre.irremote.models

import java.io.Serializable

class ModelLevelData : Serializable{
     var level : Int=1
     var key  :String? = null
     var index : Int? = null
     var modelLevelCodeList : List<ModelLevelCode>?=null
}