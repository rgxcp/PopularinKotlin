package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.FilmMetadataRequestCallback
import xyz.fairportstudios.popularin.models.FilmMetadata
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class FilmMetadataRequest(private val context: Context, private val filmID: Int) {
    fun sendRequest(callback: FilmMetadataRequestCallback) {
        val requestURL = "${PopularinAPI.FILM}$filmID"

        val filmMetadata = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val resultObject = response.getJSONObject("result")
                    val metadataObject = resultObject.getJSONObject("metadata")
                    val filmMetadata = FilmMetadata(
                        metadataObject.getDouble("average_rating"),
                        metadataObject.getInt("total_review"),
                        metadataObject.getInt("total_favorite"),
                        metadataObject.getInt("total_watchlist")
                    )
                    callback.onSuccess(filmMetadata)
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

        Volley.newRequestQueue(context).add(filmMetadata)
    }
}