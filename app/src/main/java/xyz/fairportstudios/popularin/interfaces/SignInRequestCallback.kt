package xyz.fairportstudios.popularin.interfaces

interface SignInRequestCallback {
    fun onSuccess(authID: Int, authToken: String)
    fun onInvalidUsername()
    fun onInvalidPassword()
    fun onFailed(message: String)
    fun onError(message: String)
}