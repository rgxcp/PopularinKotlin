package xyz.fairportstudios.popularin.models

data class ReviewDetail(
    val tmdbID: Int,
    val userID: Int,
    val totalLike: Int,
    val isLiked: Boolean,
    val rating: Double,
    val reviewDetail: String,
    val reviewDate: String,
    val watchDate: String,
    val title: String,
    val releaseDate: String,
    val poster: String,
    val username: String,
    val profilePicture: String
)