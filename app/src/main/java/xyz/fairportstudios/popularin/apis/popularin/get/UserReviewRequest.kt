package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.UserReviewRequestCallback
import xyz.fairportstudios.popularin.models.UserReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class UserReviewRequest(private val context: Context, private val userID: Int) {
    fun sendRequest(page: Int, callback: UserReviewRequestCallback) {
        val requestURL = "${PopularinAPI.USER}$userID/reviews?page=$page"

        val userReview = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val userReviewList = ArrayList<UserReview>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")

                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val filmObject = indexObject.getJSONObject("film")
                        val userReview = UserReview(
                            indexObject.getInt("id"),
                            filmObject.getInt("tmdb_id"),
                            indexObject.getInt("total_like"),
                            indexObject.getInt("total_comment"),
                            indexObject.getInt("total_report"),
                            indexObject.getBoolean("is_liked"),
                            indexObject.getBoolean("is_nsfw"),
                            indexObject.getDouble("rating"),
                            indexObject.getString("review_detail"),
                            indexObject.getString("timestamp"),
                            filmObject.getString("title"),
                            filmObject.getString("release_date"),
                            filmObject.getString("poster")
                        )
                        userReviewList.add(userReview)
                    }

                    callback.onSuccess(totalPage, userReviewList)
                }
                606 -> callback.onNotFound()
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
            override fun getHeaders(): MutableMap<String, String?> {
                val headers = HashMap<String, String?>()
                headers["API-Key"] = APIKey.POPULARIN_API_KEY
                headers["Auth-Token"] = Auth(context).getAuthToken()
                return headers
            }
        }

        Volley.newRequestQueue(context).add(userReview)
    }
}