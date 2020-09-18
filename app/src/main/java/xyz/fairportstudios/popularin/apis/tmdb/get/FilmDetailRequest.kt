package xyz.fairportstudios.popularin.apis.tmdb.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.FilmDetailRequestCallback
import xyz.fairportstudios.popularin.models.Cast
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.models.FilmDetail
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.TMDbAPI

class FilmDetailRequest(private val context: Context, private val filmID: Int) {
    fun sendRequest(callback: FilmDetailRequestCallback) {
        val requestURL = "${TMDbAPI.FILM}$filmID?api_key=${APIKey.TMDB_API_KEY}&language=id&append_to_response=credits%2Cvideos"

        val filmDetail = JsonObjectRequest(Request.Method.GET, requestURL, null, { response ->
            val creditObject = response.getJSONObject("credits")
            val videoObject = response.getJSONObject("videos")
            val genreArray = response.getJSONArray("genres")
            val castArray = creditObject.getJSONArray("cast")
            val crewArray = creditObject.getJSONArray("crew")
            val videoArray = videoObject.getJSONArray("results")
            val overview = response.getString("overview")
            val hasOverview = overview.isNotEmpty()
            val hasCast = !castArray.isNull(0)
            val hasCrew = !crewArray.isNull(0)
            val runtime = try {
                response.getInt("runtime")
            } catch (nullRuntime: JSONException) {
                0
            }
            val genreID = when (!genreArray.isNull(0)) {
                true -> genreArray.getJSONObject(0).getInt("id")
                false -> 0
            }
            val videoKey = when (!videoArray.isNull(0)) {
                true -> videoArray.getJSONObject(0).getString("key")
                false -> ""
            }

            // Detail
            val filmDetail = FilmDetail(
                genreID,
                runtime,
                hasOverview,
                hasCast,
                hasCrew,
                response.getString("original_title"),
                response.getString("release_date"),
                overview,
                response.getString("poster_path"),
                videoKey
            )

            // Cast
            val castList = ArrayList<Cast>()
            if (hasCast) {
                for (index in 0 until castArray.length()) {
                    val indexObject = castArray.getJSONObject(index)
                    val cast = Cast(
                        indexObject.getInt("id"),
                        indexObject.getString("name"),
                        indexObject.getString("character"),
                        indexObject.getString("profile_path")
                    )
                    castList.add(cast)
                }
            }

            // Crew
            val crewList = ArrayList<Crew>()
            if (hasCrew) {
                for (index in 0 until crewArray.length()) {
                    val indexObject = crewArray.getJSONObject(index)
                    val crew = Crew(
                        indexObject.getInt("id"),
                        indexObject.getString("name"),
                        indexObject.getString("job"),
                        indexObject.getString("profile_path")
                    )
                    crewList.add(crew)
                }
            }

            callback.onSuccess(filmDetail, castList, crewList)
        }, { error ->
            error.printStackTrace()
            when (error) {
                is NetworkError, is TimeoutError -> callback.onError(context.getString(R.string.network_error))
                is ServerError -> callback.onError(context.getString(R.string.server_error))
                else -> callback.onError(context.getString(R.string.general_error))
            }
        })

        Volley.newRequestQueue(context).add(filmDetail)
    }
}