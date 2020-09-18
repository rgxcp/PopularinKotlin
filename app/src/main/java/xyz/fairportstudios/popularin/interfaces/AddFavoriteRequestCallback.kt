package xyz.fairportstudios.popularin.interfaces

interface AddFavoriteRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}