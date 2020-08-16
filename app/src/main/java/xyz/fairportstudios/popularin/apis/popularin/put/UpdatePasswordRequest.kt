package xyz.fairportstudios.popularin.apis.popularin.put

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.preferences.Auth
import xyz.fairportstudios.popularin.secrets.APIKey
import xyz.fairportstudios.popularin.statics.PopularinAPI

class UpdatePasswordRequest(
    private val context: Context,
    private val currentPassword: String,
    private val newPassword: String,
    private val confirmPassword: String
) {
    interface Callback {
        fun onSuccess()
        fun onInvalidCurrentPassword()
        fun onFailed(message: String)
        fun onError(message: String)
    }

    fun sendRequest(callback: Callback) {
        val requestURL = PopularinAPI.UPDATE_PASSWORD

        val updatePassword = object : StringRequest(Method.PUT, requestURL, Response.Listener { response ->
            val responseObject = JSONObject(response)

            when (responseObject.getInt("status")) {
                303 -> callback.onSuccess()
                616 -> callback.onInvalidCurrentPassword()
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
                params["current_password"] = currentPassword
                params["new_password"] = newPassword
                params["confirm_password"] = confirmPassword
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

        Volley.newRequestQueue(context).add(updatePassword)
    }
}