package xyz.fairportstudios.popularin.statics

class TMDbAPI {
    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3"
        const val BASE_SMALL_IMAGE_URL = "https://image.tmdb.org/t/p/w154"
        const val BASE_LARGE_IMAGE_URL = "https://image.tmdb.org/t/p/w780"
        const val AIRING = "$BASE_URL/movie/now_playing"
        const val CREDIT = "$BASE_URL/person/"
        const val DISCOVER = "$BASE_URL/discover/movie"
        const val FILM = "$BASE_URL/movie/"
        const val SEARCH_FILM = "$BASE_URL/search/movie"
    }
}