package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.SelfDetail

interface SelfDetailRequestCallback {
    fun onSuccess(selfDetail: SelfDetail)
    fun onError(message: String)
}