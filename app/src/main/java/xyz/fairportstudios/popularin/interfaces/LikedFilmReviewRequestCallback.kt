package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.FilmReview

interface LikedFilmReviewRequestCallback {
    fun onSuccess(totalPage: Int, filmReviewList: ArrayList<FilmReview>)
    fun onNotFound()
    fun onError(message: String)
}