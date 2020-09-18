package xyz.fairportstudios.popularin.interfaces

interface UpdatePasswordRequestCallback {
    fun onSuccess()
    fun onInvalidCurrentPassword()
    fun onFailed(message: String)
    fun onError(message: String)
}