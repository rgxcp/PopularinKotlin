package xyz.fairportstudios.popularin.interfaces

interface FilmAdapterClickListener {
    fun onFilmItemClick(position: Int)
    fun onFilmPosterClick(position: Int)
    fun onFilmPosterLongClick(position: Int)
}