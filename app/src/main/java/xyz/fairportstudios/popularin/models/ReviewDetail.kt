package xyz.fairportstudios.popularin.models

data class ReviewDetail(
    val tmdbID: Int,
    val userID: Int,
    var totalLike: Int,
    val totalReport: Int,
    var isLiked: Boolean,
    val rating: Double,
    var reviewDetail: String,
    val reviewDate: String,
    var watchDate: String,
    val title: String,
    val releaseDate: String,
    val poster: String,
    val username: String,
    val profilePicture: String
)