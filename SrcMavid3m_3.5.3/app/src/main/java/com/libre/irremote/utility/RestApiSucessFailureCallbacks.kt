package com.libre.irremote.utility

import com.libre.irremote.models.ModelRemoteDetails

interface RestApiSucessFailureCallbacks {
    fun onSucessFailureCallbacks(isSucess:Boolean,modelRemoteDetails: ModelRemoteDetails?)
}