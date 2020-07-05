package xyz.fairportstudios.popularin.models

data class RecentReview(
    val id: Int,
    val tmdbID: Int,
    val rating: Double,
    val title: String,
    val releaseDate: String,
    val poster: String
)