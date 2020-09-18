package xyz.fairportstudios.popularin.interfaces

interface AddWatchlistRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}