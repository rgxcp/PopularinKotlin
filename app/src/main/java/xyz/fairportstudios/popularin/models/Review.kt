package xyz.fairportstudios.popularin.models

data class Review(
    val id: Int,
    val tmdbID: Int,
    val userID: Int,
    var totalLike: Int,
    val totalComment: Int,
    var isLiked: Boolean,
    val rating: Double,
    val reviewDetail: String,
    val timestamp: String,
    val title: String,
    val releaseDate: String,
    val poster: String,
    val username: String,
    val profilePicture: String
)