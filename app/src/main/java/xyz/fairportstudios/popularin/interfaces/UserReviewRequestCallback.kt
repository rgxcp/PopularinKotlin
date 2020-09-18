package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.UserReview

interface UserReviewRequestCallback {
    fun onSuccess(totalPage: Int, userReviewList: ArrayList<UserReview>)
    fun onNotFound()
    fun onError(message: String)
}