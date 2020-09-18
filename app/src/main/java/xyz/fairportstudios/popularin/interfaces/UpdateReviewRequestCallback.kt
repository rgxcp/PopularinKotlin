package xyz.fairportstudios.popularin.interfaces

interface UpdateReviewRequestCallback {
    fun onSuccess()
    fun onFailed(message: String)
    fun onError(message: String)
}