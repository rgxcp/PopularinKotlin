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
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class SignUpRequest(
    private val context: Context,
    private val fullName: String,
    private val username: String,
    private val email: String,
    private val password: String
) {
    interface Callback {
        fun onSuccess(authID: Int, authToken: String)
        fun onFailed(message: String)
        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = PopularinAPI.SIGN_UP

        val signUp = object : StringRequest(Method.POST, requestURL, Response.Listener { response ->
            val responseObject = JSONObject(response)

            when (responseObject.getInt("status")) {
                505 -> {
                    val resultObject = responseObject.getJSONObject("result")
                    val authID = resultObject.getInt("id")
                    val authToken = resultObject.getString("api_token")
                    callback.onSuccess(authID, authToken)
                }
                626 -> callback.onFailed(responseObject.getJSONArray("result").getString(0))
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
                params["full_name"] = fullName
                params["username"] = username
                params["email"] = email
                params["password"] = password
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["API-Key"] = APIKey.POPULARIN_API_KEY
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(signUp)
    }
}