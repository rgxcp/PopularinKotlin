package xyz.fairportstudios.popularin.statics

class PopularinAPI {
    companion object {
        private const val BASE_URL = "https://popularin.fairportstudios.xyz/api"
        const val ADD_COMMENT = "$BASE_URL/comment"
        const val ADD_REVIEW = "$BASE_URL/review"
        const val COMMENT = "$BASE_URL/comment/"
        const val FILM = "$BASE_URL/film/"
        const val REVIEW = "$BASE_URL/review/"
        const val REVIEWS = "$BASE_URL/reviews"
        const val SEARCH_USER = "$BASE_URL/user/search/"
        const val SELF = "$BASE_URL/user/self"
        const val SIGN_IN = "$BASE_URL/user/signin"
        const val SIGN_UP = "$BASE_URL/user/signup"
        const val TIMELINE = "$BASE_URL/reviews/timeline"
        const val UPDATE_PASSWORD = "$BASE_URL/user/update/password"
        const val UPDATE_PROFILE = "$BASE_URL/user/update/profile"
        const val USER = "$BASE_URL/user/"
    }
}