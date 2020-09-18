package xyz.fairportstudios.popularin.interfaces

interface AddReviewRequestCallback {
    fun onSuccess()
    fun onFailed(message: String)
    fun onError(message: String)
}