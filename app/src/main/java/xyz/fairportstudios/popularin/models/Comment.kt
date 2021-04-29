package xyz.fairportstudios.popularin.models

data class Comment(
    val id: Int,
    val userID: Int,
    val totalReport: Int,
    val isSelf: Boolean,
    var isNSFW: Boolean,
    val commentDetail: String,
    val timestamp: String,
    val username: String,
    val profilePicture: String
)