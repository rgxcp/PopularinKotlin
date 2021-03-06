package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.FilmSelfRequestCallback
import xyz.fairportstudios.popularin.models.FilmSelf
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class FilmSelfRequest(private val context: Context, private val filmID: Int) {
    fun sendRequest(callback: FilmSelfRequestCallback) {
        val requestURL = "${PopularinAPI.FILM}$filmID/self"

        val filmSelf = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val resultObject = response.getJSONObject("result")
                    val filmSelf = FilmSelf(
                        resultObject.getBoolean("in_review"),
                        resultObject.getBoolean("in_favorite"),
                        resultObject.getBoolean("in_watchlist"),
                        resultObject.getDouble("last_rate")
                    )
                    callback.onSuccess(filmSelf)
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

        Volley.newRequestQueue(context).add(filmSelf)
    }
}