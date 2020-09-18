package xyz.fairportstudios.popularin.interfaces

interface UserReviewAdapterClickListener {
    fun onUserReviewItemClick(position: Int)
    fun onUserReviewFilmPosterClick(position: Int)
    fun onUserReviewFilmPosterLongClick(position: Int)
    fun onUserReviewLikeClick(position: Int)
    fun onUserReviewCommentClick(position: Int)
}