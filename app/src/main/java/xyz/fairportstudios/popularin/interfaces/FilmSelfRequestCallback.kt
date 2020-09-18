package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.FilmSelf

interface FilmSelfRequestCallback {
    fun onSuccess(filmSelf: FilmSelf)
    fun onError(message: String)
}