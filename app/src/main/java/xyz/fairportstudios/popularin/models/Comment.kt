package xyz.fairportstudios.popularin.models

data class Comment(
    val id: Int,
    val userID: Int,
    val commentDetail: String,
    val timestamp: String,
    val username: String,
    val profilePicture: String
)