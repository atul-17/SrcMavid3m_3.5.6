package com.libre.irremote.models

import java.io.Serializable

class TvBrandsSucessRepoModel : Serializable {

    var id : Int = 0
    var name : String = ""
    var remoteList : MutableList<Int> = ArrayList()
}