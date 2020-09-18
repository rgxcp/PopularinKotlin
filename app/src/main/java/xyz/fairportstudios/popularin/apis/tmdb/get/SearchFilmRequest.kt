package xyz.fairportstudios.popularin.apis.tmdb.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.SearchFilmRequestCallback
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.TMDbAPI

class SearchFilmRequest(private val context: Context) {
    fun sendRequest(query: String, callback: SearchFilmRequestCallback) {
        val requestURL = "${TMDbAPI.SEARCH_FILM}?api_key=${APIKey.TMDB_API_KEY}&language=id&query=$query&region=ID"

        val searchFilm = JsonObjectRequest(Request.Method.GET, requestURL, null, { response ->
            when (response.getInt("total_results") > 0) {
                true -> {
                    val filmList = ArrayList<Film>()
                    val resultArray = response.getJSONArray("results")

                    for (index in 0 until resultArray.length()) {
                        val indexObject = resultArray.getJSONObject(index)
                        val language = indexObject.getString("original_language")
                        if (language == "id") {
                            val genreArray = indexObject.getJSONArray("genre_ids")
                            val genreID = when (!genreArray.isNull(0)) {
                                true -> genreArray.getInt(0)
                                false -> 0
                            }
                            val film = Film(
                                indexObject.getInt("id"),
                                genreID,
                                indexObject.getString("original_title"),
                                indexObject.getString("release_date"),
                                indexObject.getString("poster_path")
                            )
                            filmList.add(film)
                        }
                    }

                    when (filmList.size > 0) {
                        true -> callback.onSuccess(filmList)
                        false -> callback.onNotFound()
                    }
                }
                false -> callback.onNotFound()
            }
        }, { error ->
            error.printStackTrace()
            when (error) {
                is NetworkError, is TimeoutError -> callback.onError(context.getString(R.string.network_error))
                is ServerError -> callback.onError(context.getString(R.string.server_error))
                else -> callback.onError(context.getString(R.string.general_error))
            }
        })

        Volley.newRequestQueue(context).add(searchFilm)
    }
}