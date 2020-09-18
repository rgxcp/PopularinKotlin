package xyz.fairportstudios.popularin.interfaces

interface DeleteCommentRequestCallback {
    fun onSuccess()
    fun onError(message: String)
}