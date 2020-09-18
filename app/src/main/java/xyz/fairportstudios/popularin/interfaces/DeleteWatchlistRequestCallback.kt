package xyz.fairportstudios.popularin.interfaces

interface DeleteWatchlistRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}