package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.LikeFromAllRequestCallback
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class LikeFromAllRequest(private val context: Context, private val reviewID: Int) {
    fun sendRequest(page: Int, callback: LikeFromAllRequestCallback) {
        val requestURL = "${PopularinAPI.REVIEW}$reviewID/likes/from/all?page=$page"

        val likeFromAll = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val userList = ArrayList<User>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")

                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val userObject = indexObject.getJSONObject("user")
                        val user = User(
                            userObject.getInt("id"),
                            userObject.getString("full_name"),
                            userObject.getString("username"),
                            userObject.getString("profile_picture")
                        )
                        userList.add(user)
                    }

                    callback.onSuccess(totalPage, userList)
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

        Volley.newRequestQueue(context).add(likeFromAll)
    }
}