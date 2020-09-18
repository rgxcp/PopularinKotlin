package xyz.fairportstudios.popularin.interfaces

interface LikeReviewRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}