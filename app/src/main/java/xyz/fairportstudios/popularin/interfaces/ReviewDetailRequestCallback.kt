package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.ReviewDetail

interface ReviewDetailRequestCallback {
    fun onSuccess(reviewDetail: ReviewDetail)
    fun onError(message: String)
}