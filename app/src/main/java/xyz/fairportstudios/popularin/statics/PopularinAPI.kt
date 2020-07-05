package xyz.fairportstudios.popularin.statics

class PopularinAPI {
    companion object {
        private const val BASE_URL: String = "https://popularin.fairportstudios.xyz/api"
        const val ADD_COMMENT: String = "$BASE_URL/comment"
        const val ADD_REVIEW: String = "$BASE_URL/review"
        const val COMMENT: String = "$BASE_URL/comment/"
        const val FILM: String = "$BASE_URL/film/"
        const val REVIEW: String = "$BASE_URL/review/"
        const val REVIEWS: String = "$BASE_URL/reviews"
        const val SEARCH_USER: String = "$BASE_URL/user/search/"
        const val SELF: String = "$BASE_URL/user/self"
        const val SIGN_IN: String = "$BASE_URL/user/signin"
        const val SIGN_OUT: String = "$BASE_URL/user/signout"
        const val SIGN_UP: String = "$BASE_URL/user/signup"
        const val TIMELINE: String = "$BASE_URL/reviews/timeline"
        const val UPDATE_PASSWORD: String = "$BASE_URL/user/update/password"
        const val UPDATE_PROFILE: String = "$BASE_URL/user/update/profile"
        const val USER: String = "$BASE_URL/user/"
    }
}