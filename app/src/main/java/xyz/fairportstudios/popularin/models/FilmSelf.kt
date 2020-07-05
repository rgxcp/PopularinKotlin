package xyz.fairportstudios.popularin.models

data class FilmSelf(
    val inReview: Boolean,
    val inFavorite: Boolean,
    val inWatchlist: Boolean,
    val lastRate: Double
)