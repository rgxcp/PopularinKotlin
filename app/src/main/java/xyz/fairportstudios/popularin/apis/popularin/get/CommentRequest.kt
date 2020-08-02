package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Comment
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class CommentRequest(private val context: Context, private val reviewID: Int) {
    interface Callback {
        fun onSuccess(totalPage: Int, commentList: ArrayList<Comment>)

        fun onNotFound()

        fun onError(message: String)
    }

    fun sendRequest(page: Int, callback: Callback) {
        val requestURL = "${PopularinAPI.REVIEW}$reviewID/comments?page=$page"

        val comment = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val commentList = ArrayList<Comment>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")
                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val userObject = indexObject.getJSONObject("user")
                        val comment = Comment(
                            indexObject.getInt("id"),
                            userObject.getInt("id"),
                            indexObject.getString("comment_detail"),
                            indexObject.getString("timestamp"),
                            userObject.getString("username"),
                            userObject.getString("profile_picture")
                        )
                        commentList.add(comment)
                    }
                    callback.onSuccess(totalPage, commentList)
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

        Volley.newRequestQueue(context).add(comment)
    }
}