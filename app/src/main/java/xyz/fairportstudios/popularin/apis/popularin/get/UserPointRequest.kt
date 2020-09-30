package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.enums.PointType
import xyz.fairportstudios.popularin.interfaces.UserPointRequestCallback
import xyz.fairportstudios.popularin.models.Point
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class UserPointRequest(private val context: Context, private val userID: Int) {
    fun sendRequest(page: Int, callback: UserPointRequestCallback) {
        val requestURL = "${PopularinAPI.USER}$userID/points?page=$page"

        val userPoint = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val pointList = ArrayList<Point>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")

                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val type = when (indexObject.getString("type")) {
                            "FAVORITE" -> PointType.FAVORITE
                            "REVIEW" -> PointType.REVIEW
                            else -> PointType.WATCHLIST
                        }
                        val point = Point(
                            indexObject.getInt("id"),
                            indexObject.getInt("type_id"),
                            indexObject.getBoolean("is_positive"),
                            type,
                            indexObject.getString("total"),
                            indexObject.getString("description"),
                            indexObject.getString("timestamp")
                        )
                        pointList.add(point)
                    }

                    callback.onSuccess(totalPage, pointList)
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

        Volley.newRequestQueue(context).add(userPoint)
    }
}