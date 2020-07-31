package xyz.fairportstudios.popularin.apis.popularin.post

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Comment
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class AddCommentRequest(private val context: Context, private val reviewID: Int, private val commentDetail: String) {
    interface Callback {
        fun onSuccess(comment: Comment)

        fun onFailed(message: String)

        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = PopularinAPI.ADD_COMMENT

        val addComment = object : StringRequest(Method.POST, requestURL, Response.Listener { response ->
            val responseObject = JSONObject(response)

            when (responseObject.getInt("status")) {
                202 -> {
                    val resultObject = responseObject.getJSONObject("result")
                    val userObject = resultObject.getJSONObject("user")
                    val comment = Comment(
                        resultObject.getInt("id"),
                        userObject.getInt("id"),
                        resultObject.getString("comment_detail"),
                        resultObject.getString("timestamp"),
                        userObject.getString("username"),
                        userObject.getString("profile_picture")
                    )
                    callback.onSuccess(comment)
                }
                626 -> {
                    val resultArray = responseObject.getJSONArray("result")
                    val message = resultArray.getString(0)
                    callback.onFailed(message)
                }
                else -> callback.onError(context.getString(R.string.general_error))
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            if (error is NetworkError || error is TimeoutError) {
                callback.onError(context.getString(R.string.network_error))
            } else if (error is ServerError) {
                callback.onError(context.getString(R.string.server_error))
            } else {
                callback.onError(context.getString(R.string.general_error))
            }
        }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["review_id"] = reviewID.toString()
                params["comment_detail"] = commentDetail
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

        Volley.newRequestQueue(context).add(addComment)
    }
}