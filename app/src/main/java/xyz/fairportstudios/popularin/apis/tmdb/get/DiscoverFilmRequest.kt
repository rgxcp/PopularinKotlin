package xyz.fairportstudios.popularin.apis.tmdb.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.DiscoverFilmRequestCallback
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.TMDbAPI

class DiscoverFilmRequest(private val context: Context, private val genreID: Int) {
    fun sendRequest(page: Int, callback: DiscoverFilmRequestCallback) {
        val requestURL = "${TMDbAPI.DISCOVER}?api_key=${APIKey.TMDB_API_KEY}&language=id&region=ID&sort_by=popularity.desc&page=$page&with_genres=$genreID&with_original_language=id"

        val discoverFilm = JsonObjectRequest(Request.Method.GET, requestURL, null, { response ->
            val filmList = ArrayList<Film>()
            val resultArray = response.getJSONArray("results")
            val totalPage = response.getInt("total_pages")

            for (index in 0 until resultArray.length()) {
                val indexObject = resultArray.getJSONObject(index)
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

            callback.onSuccess(totalPage, filmList)
        }, { error ->
            error.printStackTrace()
            when (error) {
                is NetworkError, is TimeoutError -> callback.onError(context.getString(R.string.network_error))
                is ServerError -> callback.onError(context.getString(R.string.server_error))
                else -> callback.onError(context.getString(R.string.general_error))
            }
        })

        Volley.newRequestQueue(context).add(discoverFilm)
    }
}