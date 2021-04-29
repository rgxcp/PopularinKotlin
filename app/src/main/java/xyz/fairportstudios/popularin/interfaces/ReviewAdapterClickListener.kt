package xyz.fairportstudios.popularin.interfaces

interface ReviewAdapterClickListener {
    fun onReviewItemClick(position: Int)
    fun onReviewUserProfileClick(position: Int)
    fun onReviewFilmPosterClick(position: Int)
    fun onReviewFilmPosterLongClick(position: Int)
    fun onReviewNSFWBannerClick(position: Int)
    fun onReviewLikeClick(position: Int)
    fun onReviewCommentClick(position: Int)
    fun onReviewReportClick(position: Int)
}