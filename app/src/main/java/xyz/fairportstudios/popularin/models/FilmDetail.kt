package xyz.fairportstudios.popularin.models

data class FilmDetail(
    val genreID: Int,
    val runtime: Int,
    val hasOverview: Boolean,
    val hasCast: Boolean,
    val hasCrew: Boolean,
    val originalTitle: String,
    val releaseDate: String,
    val overview: String,
    val posterPath: String,
    val videoKey: String
)