package xyz.fairportstudios.popularin.models

data class Report(
    val id: Int,
    val userId: Int,
    val timestamp: String,
    val category: String,
    val fullName: String,
    val profilePicture: String
)