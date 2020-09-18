package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.User

interface SearchUserRequestCallback {
    fun onSuccess(userList: ArrayList<User>)
    fun onNotFound()
    fun onError(message: String)
}