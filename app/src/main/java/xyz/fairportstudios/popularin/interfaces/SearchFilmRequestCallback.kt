package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Film

interface SearchFilmRequestCallback {
    fun onSuccess(filmList: ArrayList<Film>)
    fun onNotFound()
    fun onError(message: String)
}