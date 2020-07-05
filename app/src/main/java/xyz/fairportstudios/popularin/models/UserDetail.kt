package xyz.fairportstudios.popularin.models

data class UserDetail(
    val isFollower: Boolean,
    val isFollowing: Boolean,
    val totalReview: Int,
    val totalFavorite: Int,
    val totalWatchlist: Int,
    val totalFollower: Int,
    val totalFollowing: Int,
    val fullName: String,
    val username: String,
    val profilePicture: String
)