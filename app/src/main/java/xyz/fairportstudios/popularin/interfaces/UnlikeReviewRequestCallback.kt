package xyz.fairportstudios.popularin.interfaces

interface UnlikeReviewRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}