package xyz.fairportstudios.popularin.interfaces

interface UnfollowUserRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}