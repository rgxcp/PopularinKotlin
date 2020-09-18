package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.models.UserDetail

interface UserDetailRequestCallback {
    fun onSuccess(userDetail: UserDetail)
    fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>)
    fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>)
    fun onError(message: String)
}