package xyz.fairportstudios.popularin.apis.tmdb.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.CreditDetailRequestCallback
import xyz.fairportstudios.popularin.models.CreditDetail
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.TMDbAPI

class CreditDetailRequest(private val context: Context, private val creditID: Int) {
    fun sendRequest(callback: CreditDetailRequestCallback) {
        val requestURL = "${TMDbAPI.CREDIT}$creditID?api_key=${APIKey.TMDB_API_KEY}&language=id&append_to_response=movie_credits"

        val creditDetail = JsonObjectRequest(Request.Method.GET, requestURL, null, { response ->
            val movieCreditObject = response.getJSONObject("movie_credits")
            val castArray = movieCreditObject.getJSONArray("cast")
            val crewArray = movieCreditObject.getJSONArray("crew")

            // Bio
            val creditDetail = CreditDetail(
                response.getString("name"),
                response.getString("known_for_department"),
                response.getString("birthday"),
                response.getString("place_of_birth"),
                response.getString("profile_path")
            )

            // Cast
            val filmAsCastList = ArrayList<Film>()
            if (!castArray.isNull(0)) {
                for (index in 0 until castArray.length()) {
                    val indexObject = castArray.getJSONObject(index)
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
                        filmAsCastList.add(film)
                    }
                }
            }

            // Crew
            val filmAsCrewList = ArrayList<Film>()
            if (!crewArray.isNull(0)) {
                for (index in 0 until crewArray.length()) {
                    val indexObject = crewArray.getJSONObject(index)
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
                        filmAsCrewList.add(film)
                    }
                }
            }

            callback.onSuccess(creditDetail, filmAsCastList, filmAsCrewList)
        }, { error ->
            error.printStackTrace()
            when (error) {
                is NetworkError, is TimeoutError -> callback.onError(context.getString(R.string.network_error))
                is ServerError -> callback.onError(context.getString(R.string.server_error))
                else -> callback.onError(context.getString(R.string.general_error))
            }
        })

        Volley.newRequestQueue(context).add(creditDetail)
    }
}