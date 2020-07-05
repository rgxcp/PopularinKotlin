package xyz.fairportstudios.popularin.statics

class TMDbAPI {
    companion object {
        private const val BASE_URL: String = "https://api.themoviedb.org/3"
        const val BASE_SMALL_IMAGE_URL: String = "https://image.tmdb.org/t/p/w154"
        const val BASE_LARGE_IMAGE_URL: String = "https://image.tmdb.org/t/p/w780"
        const val AIRING: String = "$BASE_URL/movie/now_playing"
        const val CREDIT: String = "$BASE_URL/person/"
        const val DISCOVER: String = "$BASE_URL/discover/movie"
        const val FILM: String = "$BASE_URL/movie/"
        const val SEARCH_FILM: String = "$BASE_URL/search/movie"
    }
}