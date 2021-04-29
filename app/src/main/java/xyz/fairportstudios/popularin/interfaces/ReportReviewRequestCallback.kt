package xyz.fairportstudios.popularin.interfaces

interface ReportReviewRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}