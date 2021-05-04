package com.libre.irremote.utility

import com.libre.irremote.models.ModelRemoteDetails

interface OnClickOfDeleteButtonInterface {
    fun onClickOfDelete(modelRemoteDetails: ModelRemoteDetails,pos:Int)
}