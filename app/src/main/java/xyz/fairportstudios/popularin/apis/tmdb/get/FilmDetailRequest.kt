package xyz.fairportstudios.popularin.apis.tmdb.get

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Cast
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.models.FilmDetail
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.TMDbAPI

class FilmDetailRequest(private val context: Context, private val filmID: Int) {
    interface Callback {
        fun onSuccess(filmDetail: FilmDetail, castList: ArrayList<Cast>, crewList: ArrayList<Crew>)

        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = "${TMDbAPI.FILM}$filmID?api_key=${APIKey.TMDB_API_KEY}&language=id&append_to_response=credits%2Cvideos"

        val filmDetail = JsonObjectRequest(Request.Method.GET, requestURL, null, Response.Listener { response ->
            val creditObject = response.getJSONObject("credits")
            val videoObject = response.getJSONObject("videos")
            val genreArray = response.getJSONArray("genres")
            val castArray = creditObject.getJSONArray("cast")
            val crewArray = creditObject.getJSONArray("crew")
            val videoArray = videoObject.getJSONArray("results")
            var genreID = 0
            var videoKey = ""
            val runtime = try {
                response.getInt("runtime")
            } catch (nullRuntime: JSONException) {
                0
            }
            if (!genreArray.isNull(0)) {
                genreID = genreArray.getJSONObject(0).getInt("id")
            }
            if (!videoArray.isNull(0)) {
                videoKey = videoArray.getJSONObject(0).getString("key")
            }

            // Detail
            val filmDetail = FilmDetail(
                genreID,
                runtime,
                response.getString("original_title"),
                response.getString("release_date"),
                response.getString("overview"),
                response.getString("poster_path"),
                videoKey
            )

            // Cast
            val castList = ArrayList<Cast>()
            if (!castArray.isNull(0)) {
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
            if (!crewArray.isNull(0)) {
                for (index in 0 until crewArray.length()) {
                    val indexObject = crewArray.getJSONObject(index)
                    val crew = Crew(
                        indexObject.getInt("id"),
                        indexObject.getString("name"),
                        indexObject.getString("character"),
                        indexObject.getString("profile_path")
                    )
                    crewList.add(crew)
                }
            }

            callback.onSuccess(filmDetail, castList, crewList)
        }, Response.ErrorListener { error ->
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