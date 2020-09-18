package xyz.fairportstudios.popularin.interfaces

interface DeleteFavoriteRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}