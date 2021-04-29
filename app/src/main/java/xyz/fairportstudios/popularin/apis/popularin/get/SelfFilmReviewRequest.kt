package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.SelfFilmReviewRequestCallback
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class SelfFilmReviewRequest(private val context: Context, private val filmID: Int) {
    fun sendRequest(page: Int, callback: SelfFilmReviewRequestCallback) {
        val requestURL = "${PopularinAPI.FILM}$filmID/reviews/self?page=$page"

        val selfFilmReview = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val filmReviewList = ArrayList<FilmReview>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")

                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val userObject = indexObject.getJSONObject("user")
                        val filmReview = FilmReview(
                            indexObject.getInt("id"),
                            userObject.getInt("id"),
                            indexObject.getInt("total_like"),
                            indexObject.getInt("total_comment"),
                            indexObject.getInt("total_report"),
                            indexObject.getBoolean("is_liked"),
                            indexObject.getBoolean("is_nsfw"),
                            indexObject.getDouble("rating"),
                            indexObject.getString("review_detail"),
                            indexObject.getString("timestamp"),
                            userObject.getString("username"),
                            userObject.getString("profile_picture")
                        )
                        filmReviewList.add(filmReview)
                    }

                    callback.onSuccess(totalPage, filmReviewList)
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

        Volley.newRequestQueue(context).add(selfFilmReview)
    }
}