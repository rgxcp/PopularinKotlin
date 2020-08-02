package xyz.fairportstudios.popularin.apis.popularin.delete

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class DeleteFavoriteRequest(private val context: Context, private val filmID: Int) {
    interface Callback {
        fun onSuccess()

        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = "${PopularinAPI.FILM}$filmID/unfavorite"

        val deleteFavorite = object : JsonObjectRequest(Method.DELETE, requestURL, null, Response.Listener { response ->
            val status = response.getInt("status")

            if (status == 404) {
                callback.onSuccess()
            } else {
                callback.onError(context.getString(R.string.general_error))
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

        Volley.newRequestQueue(context).add(deleteFavorite)
    }
}