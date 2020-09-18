package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Film

interface DiscoverFilmRequestCallback {
    fun onSuccess(totalPage: Int, filmList: ArrayList<Film>)
    fun onError(message: String)
}