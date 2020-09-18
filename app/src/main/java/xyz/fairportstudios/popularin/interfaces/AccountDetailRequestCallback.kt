package xyz.fairportstudios.popularin.interfaces

import xyz.fairportstudios.popularin.models.AccountDetail
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.models.RecentReview

interface AccountDetailRequestCallback {
    fun onSuccess(accountDetail: AccountDetail)
    fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>)
    fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>)
    fun onError(message: String)
}