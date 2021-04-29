package xyz.fairportstudios.popularin.apis.popularin.get

import android.content.Context
import com.android.volley.NetworkError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.ReviewReportsRequestCallback
import xyz.fairportstudios.popularin.models.Report
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class ReviewReportsRequest(private val context: Context, private val reviewId: Int) {
    fun sendRequest(page: Int, callback: ReviewReportsRequestCallback) {
        val requestURL = "${PopularinAPI.REVIEW}$reviewId/reports?page=$page"

        val reviewReports = object : JsonObjectRequest(Method.GET, requestURL, null, Response.Listener { response ->
            when (response.getInt("status")) {
                101 -> {
                    val reports = mutableListOf<Report>()
                    val resultObject = response.getJSONObject("result")
                    val dataArray = resultObject.getJSONArray("data")
                    val totalPage = resultObject.getInt("last_page")

                    for (index in 0 until dataArray.length()) {
                        val indexObject = dataArray.getJSONObject(index)
                        val reportCategoryObject = indexObject.getJSONObject("report_category")
                        val userObject = indexObject.getJSONObject("user")
                        val report = Report(
                            indexObject.getInt("id"),
                            indexObject.getInt("user_id"),
                            indexObject.getString("timestamp"),
                            reportCategoryObject.getString("name"),
                            userObject.getString("full_name"),
                            userObject.getString("profile_picture")
                        )
                        reports.add(report)
                    }

                    callback.onSuccess(totalPage, reports)
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

        Volley.newRequestQueue(context).add(reviewReports)
    }
}