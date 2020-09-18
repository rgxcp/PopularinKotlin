package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.User

interface LikeFromAllRequestCallback {
    fun onSuccess(totalPage: Int, userList: ArrayList<User>)
    fun onNotFound()
    fun onError(message: String)
}