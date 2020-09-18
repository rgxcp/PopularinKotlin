package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Comment

interface AddCommentRequestCallback {
    fun onSuccess(comment: Comment)
    fun onFailed(message: String)
    fun onError(message: String)
}