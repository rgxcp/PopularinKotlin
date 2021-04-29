package xyz.fairportstudios.popularin.interfaces

interface ReportCommentRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}