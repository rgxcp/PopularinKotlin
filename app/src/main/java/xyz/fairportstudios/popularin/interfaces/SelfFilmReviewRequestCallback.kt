package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.FilmReview

interface SelfFilmReviewRequestCallback {
    fun onSuccess(totalPage: Int, filmReviewList: ArrayList<FilmReview>)
    fun onNotFound()
    fun onError(message: String)
}