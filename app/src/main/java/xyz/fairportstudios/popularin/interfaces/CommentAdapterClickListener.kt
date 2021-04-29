package xyz.fairportstudios.popularin.interfaces

import android.widget.ImageView

interface CommentAdapterClickListener {
    fun onCommentProfileClick(position: Int)
    fun onCommentNSFWBannerClick(position: Int)
    fun onCommentReportClick(position: Int)
    fun onCommentMoreClick(position: Int, anchor: ImageView)
    fun onCommentDeleteClick(position: Int)
}