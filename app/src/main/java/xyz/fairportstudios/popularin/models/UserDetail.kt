package xyz.fairportstudios.popularin.models

data class UserDetail(
    val isSelf: Boolean,
    val isFollower: Boolean,
    var isFollowing: Boolean,
    val hasRecentFavorite: Boolean,
    val hasRecentReview: Boolean,
    val totalReview: Int,
    val totalFavorite: Int,
    val totalWatchlist: Int,
    var totalFollower: Int,
    val totalFollowing: Int,
    val fullName: String,
    val username: String,
    val profilePicture: String
)