package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.UserFavoriteRequestCallback
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class UserFavoriteRequest(private val context: Context, private val userID: Int) {
    fun sendRequest(page: Int, callback: UserFavoriteRequestCallback) {
        val requestURL = "${PopularinAPI.USER}$userID/favorites?page=$page"

        val userFavorite = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val filmList = ArrayList<Film>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")

                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val filmObject = indexObject.getJSONObject("film")
                        val film = Film(
                            filmObject.getInt("tmdb_id"),
                            filmObject.getInt("genre_id"),
                            filmObject.getString("title"),
                            filmObject.getString("release_date"),
                            filmObject.getString("poster")
                        )
                        filmList.add(film)
                    }

                    callback.onSuccess(totalPage, filmList)
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
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["API-Key"] = APIKey.POPULARIN_API_KEY
                return headers
            }
        }

        Volley.newRequestQueue(context).add(userFavorite)
    }
}