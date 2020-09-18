package xyz.fairportstudios.popularin.interfaces

interface FilmReviewAdapterClickListener {
    fun onFilmReviewItemClick(position: Int)
    fun onFilmReviewUserProfileClick(position: Int)
    fun onFilmReviewLikeClick(position: Int)
    fun onFilmReviewCommentClick(position: Int)
}