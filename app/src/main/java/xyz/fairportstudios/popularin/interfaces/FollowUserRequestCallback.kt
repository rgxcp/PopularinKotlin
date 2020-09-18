package xyz.fairportstudios.popularin.interfaces

interface FollowUserRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}