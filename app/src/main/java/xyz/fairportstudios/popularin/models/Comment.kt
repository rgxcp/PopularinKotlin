package xyz.fairportstudios.popularin.models

data class Comment(
    val id: Int,
    val userID: Int,
    val isSelf: Boolean,
    val commentDetail: String,
    val timestamp: String,
    val username: String,
    val profilePicture: String
)