package xyz.fairportstudios.popularin.preferences

import android.content.Context
import xyz.fairportstudios.popularin.statics.Popularin

class Auth(context: Context) {
    // Variable member
    private val mSharedPreferences = context.getSharedPreferences(Popularin.AUTH, Context.MODE_PRIVATE)

    fun isAuth(): Boolean {
        val authID = mSharedPreferences.getInt(Popularin.AUTH_ID, 0)
        val authToken = mSharedPreferences.getString(Popularin.AUTH_TOKEN, "")
        return authID != 0 && authToken != ""
    }

    fun isSelf(userID: Int, authID: Int): Boolean {
        return userID == authID
    }

    fun getAuthID(): Int {
        return mSharedPreferences.getInt(Popularin.AUTH_ID, 0)
    }

    fun getAuthToken(): String? {
        return mSharedPreferences.getString(Popularin.AUTH_TOKEN, "")
    }

    fun setAuth(authID: Int, authToken: String) {
        val editor = mSharedPreferences.edit()
        editor.putInt(Popularin.AUTH_ID, authID)
        editor.putString(Popularin.AUTH_TOKEN, authToken)
        editor.apply()
    }

    fun delAuh() {
        val editor = mSharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}