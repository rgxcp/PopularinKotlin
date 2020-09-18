package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Cast
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.models.FilmDetail

interface FilmDetailRequestCallback {
    fun onSuccess(filmDetail: FilmDetail, castList: ArrayList<Cast>, crewList: ArrayList<Crew>)
    fun onError(message: String)
}