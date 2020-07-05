package xyz.fairportstudios.popularin.preferences

import android.content.Context
import android.content.SharedPreferences
import xyz.fairportstudios.popularin.statics.Popularin

class Auth(private val context: Context) {
    fun isAuth(): Boolean {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(Popularin.AUTH, Context.MODE_PRIVATE)
        val authID: Int = sharedPrefs.getInt(Popularin.AUTH_ID, 0)
        val authToken: String? = sharedPrefs.getString(Popularin.AUTH_TOKEN, "")
        return authID != 0 && authToken != ""
    }

    fun getAuthID(): Int {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(Popularin.AUTH, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(Popularin.AUTH_ID, 0)
    }

    fun getAuthToken(): String? {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(Popularin.AUTH, Context.MODE_PRIVATE)
        return sharedPrefs.getString(Popularin.AUTH_TOKEN, "")
    }

    fun setAuth(authID: Int, authToken: String) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(Popularin.AUTH, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putInt(Popularin.AUTH_ID, authID)
        editor.putString(Popularin.AUTH_TOKEN, authToken)
        editor.apply()
    }

    fun delAuh() {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(Popularin.AUTH, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.clear()
        editor.apply()
    }
}