package xyz.fairportstudios.popularin.models

data class Film(
    val id: Int,
    val genreID: Int,
    val originalTitle: String,
    val releaseDate: String,
    val posterPath: String
)