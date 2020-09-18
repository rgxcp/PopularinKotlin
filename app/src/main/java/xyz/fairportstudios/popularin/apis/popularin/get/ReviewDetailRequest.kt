package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.ReviewDetailRequestCallback
import xyz.fairportstudios.popularin.models.ReviewDetail
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class ReviewDetailRequest(private val context: Context, private val reviewID: Int) {
    fun sendRequest(callback: ReviewDetailRequestCallback) {
        val requestURL = "${PopularinAPI.REVIEW}$reviewID"

        val reviewDetail = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val resultObject = response.getJSONObject("result")
                    val filmObject = resultObject.getJSONObject("film")
                    val userObject = resultObject.getJSONObject("user")
                    val reviewDetail = ReviewDetail(
                        filmObject.getInt("tmdb_id"),
                        userObject.getInt("id"),
                        resultObject.getInt("total_like"),
                        resultObject.getBoolean("is_liked"),
                        resultObject.getDouble("rating"),
                        resultObject.getString("review_detail"),
                        resultObject.getString("review_date"),
                        resultObject.getString("watch_date"),
                        filmObject.getString("title"),
                        filmObject.getString("release_date"),
                        filmObject.getString("poster"),
                        userObject.getString("username"),
                        userObject.getString("profile_picture")
                    )
                    callback.onSuccess(reviewDetail)
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
            override fun getHeaders(): MutableMap<String, String?> {
                val headers = HashMap<String, String?>()
                headers["API-Key"] = APIKey.POPULARIN_API_KEY
                headers["Auth-Token"] = Auth(context).getAuthToken()
                return headers
            }
        }

        Volley.newRequestQueue(context).add(reviewDetail)
    }
}