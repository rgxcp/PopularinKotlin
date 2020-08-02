package xyz.fairportstudios.popularin.apis.popularin.put

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class UpdateReviewRequest(
    private val context: Context,
    private val reviewID: Int,
    private val rating: Float,
    private val reviewDetail: String,
    private val watchDate: String
) {
    interface Callback {
        fun onSuccess()

        fun onFailed(message: String)

        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = "${PopularinAPI.REVIEW}$reviewID"

        val updateReview = object : StringRequest(Method.PUT, requestURL, Response.Listener { response ->
            val responseObject = JSONObject(response)

            when (responseObject.getInt("status")) {
                303 -> callback.onSuccess()
                626 -> {
                    val resultArray = responseObject.getJSONArray("result")
                    val message = resultArray.getString(0)
                    callback.onFailed(message)
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
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["rating"] = rating.toString()
                params["review_detail"] = reviewDetail
                params["watch_date"] = watchDate
                return params
            }

            override fun getHeaders(): MutableMap<String, String?> {
                val headers = HashMap<String, String?>()
                headers["API-Key"] = APIKey.POPULARIN_API_KEY
                headers["Auth-Token"] = Auth(context).getAuthToken()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(updateReview)
    }
}