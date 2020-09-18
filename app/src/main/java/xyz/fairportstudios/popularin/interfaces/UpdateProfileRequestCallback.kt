package xyz.fairportstudios.popularin.interfaces

interface UpdateProfileRequestCallback {
    fun onSuccess()
    fun onFailed(message: String)
    fun onError(message: String)
}