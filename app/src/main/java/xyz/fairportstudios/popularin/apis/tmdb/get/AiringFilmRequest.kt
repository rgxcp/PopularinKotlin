package xyz.fairportstudios.popularin.apis.tmdb.get

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.TMDbAPI

class AiringFilmRequest(private val context: Context) {
    interface Callback {
        fun onSuccess(filmList: ArrayList<Film>)

        fun onNotFound()

        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = "${TMDbAPI.AIRING}?api_key=${APIKey.TMDB_API_KEY}&language=id&region=ID"

        val airingFilm = JsonObjectRequest(Request.Method.GET, requestURL, null, Response.Listener { response ->
            val totalResult = response.getInt("total_results")

            if (totalResult > 0) {
                val filmList = ArrayList<Film>()
                val resultArray = response.getJSONArray("results")

                for (index in 0 until resultArray.length()) {
                    val indexObject = resultArray.getJSONObject(index)
                    val language = indexObject.getString("original_language")
                    if (language == "id") {
                        val genreArray = indexObject.getJSONArray("genre_ids")
                        var genreID = 0
                        if (!genreArray.isNull(0)) {
                            genreID = genreArray.getInt(0)
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

                if (filmList.size > 0) {
                    callback.onSuccess(filmList)
                } else {
                    callback.onNotFound()
                }
            } else {
                callback.onNotFound()
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            when (error) {
                is NetworkError, is TimeoutError -> callback.onError(context.getString(R.string.network_error))
                is ServerError -> callback.onError(context.getString(R.string.server_error))
                else -> callback.onError(context.getString(R.string.general_error))
            }
        })

        Volley.newRequestQueue(context).add(airingFilm)
    }
}