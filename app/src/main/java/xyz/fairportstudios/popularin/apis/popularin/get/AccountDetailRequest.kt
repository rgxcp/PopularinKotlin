package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.AccountDetail
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class AccountDetailRequest(private val context: Context, private val userID: Int) {
    interface Callback {
        fun onSuccess(accountDetail: AccountDetail)
        fun onHasRecentFavorite(recentFavoriteList: ArrayList<RecentFavorite>)
        fun onHasRecentReview(recentReviewList: ArrayList<RecentReview>)
        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = "${PopularinAPI.USER}$userID"

        val accountDetail = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val resultObject = response.getJSONObject("result")
                    val userObject = resultObject.getJSONObject("user")
                    val metadataObject = resultObject.getJSONObject("metadata")
                    val activityObject = resultObject.getJSONObject("activity")
                    val totalFavorite = metadataObject.getInt("total_favorite")
                    val totalReview = metadataObject.getInt("total_review")
                    val hasRecentFavorite = totalFavorite > 0
                    val hasRecentReview = totalReview > 0

                    // Detail
                    val accountDetail = AccountDetail(
                        totalReview,
                        totalFavorite,
                        metadataObject.getInt("total_watchlist"),
                        metadataObject.getInt("total_follower"),
                        metadataObject.getInt("total_following"),
                        hasRecentFavorite,
                        hasRecentReview,
                        userObject.getString("full_name"),
                        userObject.getString("username"),
                        userObject.getString("profile_picture")
                    )
                    callback.onSuccess(accountDetail)

                    // Favorite
                    if (hasRecentFavorite) {
                        val recentFavoriteList = ArrayList<RecentFavorite>()
                        val recentFavoriteArray = activityObject.getJSONArray("recent_favorites")

                        for (index in 0 until recentFavoriteArray.length()) {
                            val indexObject = recentFavoriteArray.getJSONObject(index)
                            val filmObject = indexObject.getJSONObject("film")
                            val recentFavorite = RecentFavorite(
                                filmObject.getInt("tmdb_id"),
                                filmObject.getString("title"),
                                filmObject.getString("release_date"),
                                filmObject.getString("poster")
                            )
                            recentFavoriteList.add(recentFavorite)
                        }

                        callback.onHasRecentFavorite(recentFavoriteList)
                    }

                    // Review
                    if (hasRecentReview) {
                        val recentReviewList = ArrayList<RecentReview>()
                        val recentReviewArray = activityObject.getJSONArray("recent_reviews")

                        for (index in 0 until recentReviewArray.length()) {
                            val indexObject = recentReviewArray.getJSONObject(index)
                            val filmObject = indexObject.getJSONObject("film")
                            val recentReview = RecentReview(
                                indexObject.getInt("id"),
                                filmObject.getInt("tmdb_id"),
                                indexObject.getDouble("rating"),
                                filmObject.getString("title"),
                                filmObject.getString("release_date"),
                                filmObject.getString("poster")
                            )
                            recentReviewList.add(recentReview)
                        }

                        callback.onHasRecentReview(recentReviewList)
                    }
                }
                else -> callback.onError(context.getString(R.string.general_error))
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            when (error) {
                is NetworkError, is TimeoutError -> callback.onError(context.getString(R.string.network_error))
                is ServerError -> callback.onError(context.getString(R.string.server_error))
                else -> callback.onError(context.getString(R.string.general_error))
            }
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["API-Key"] = APIKey.POPULARIN_API_KEY
                return headers
            }
        }

        Volley.newRequestQueue(context).add(accountDetail)
    }
}