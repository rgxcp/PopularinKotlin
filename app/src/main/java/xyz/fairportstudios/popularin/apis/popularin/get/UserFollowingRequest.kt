package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.User
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class UserFollowingRequest(private val context: Context, private val userID: Int) {
    interface Callback {
        fun onSuccess(totalPage: Int, userList: ArrayList<User>)

        fun onNotFound()

        fun onError(message: String)
    }

    fun sendRequest(page: Int, callback: Callback) {
        val requestURL = "${PopularinAPI.USER}$userID/followings?page=$page"

        val userFollowing = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val userList = ArrayList<User>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")
                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val followingObject = indexObject.getJSONObject("following")
                        val user = User(
                            followingObject.getInt("id"),
                            followingObject.getString("full_name"),
                            followingObject.getString("username"),
                            followingObject.getString("profile_picture")
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

        Volley.newRequestQueue(context).add(userFollowing)
    }
}