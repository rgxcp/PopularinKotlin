package xyz.fairportstudios.popularin.models

data class FilmReview(
    val id: Int,
    val userID: Int,
    var totalLike: Int,
    val totalComment: Int,
    val totalReport: Int,
    var isLiked: Boolean,
    var isNSFW: Boolean,
    val rating: Double,
    val reviewDetail: String,
    val timestamp: String,
    val username: String,
    val profilePicture: String
)