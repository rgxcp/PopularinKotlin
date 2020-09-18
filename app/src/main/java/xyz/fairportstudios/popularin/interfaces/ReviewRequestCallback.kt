package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.Review

interface ReviewRequestCallback {
    fun onSuccess(totalPage: Int, reviewList: ArrayList<Review>)
    fun onNotFound()
    fun onError(message: String)
}