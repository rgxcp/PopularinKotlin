package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Comment

interface CommentRequestCallback {
    fun onSuccess(totalPage: Int, commentList: ArrayList<Comment>)
    fun onNotFound()
    fun onError(message: String)
}