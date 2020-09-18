package xyz.fairportstudios.popularin.interfaces

interface DeleteReviewRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}