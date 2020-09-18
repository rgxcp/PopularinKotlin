package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Film

interface UserFavoriteRequestCallback {
    fun onSuccess(totalPage: Int, filmList: ArrayList<Film>)
    fun onNotFound()
    fun onError(message: String)
}