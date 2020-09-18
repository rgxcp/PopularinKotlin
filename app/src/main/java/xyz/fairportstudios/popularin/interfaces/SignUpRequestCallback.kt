package xyz.fairportstudios.popularin.interfaces

interface SignUpRequestCallback {
    fun onSuccess(authID: Int, authToken: String)
    fun onFailed(message: String)
    fun onError(message: String)
}