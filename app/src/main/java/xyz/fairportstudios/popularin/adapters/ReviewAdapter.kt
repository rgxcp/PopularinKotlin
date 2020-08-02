package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_review.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Review
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.TMDbAPI

class ReviewAdapter(
    private val context: Context,
    private val reviewList: ArrayList<Review>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    interface OnClickListener {
        fun onReviewItemClick(position: Int)

        fun onReviewUserProfileClick(position: Int)

        fun onReviewFilmPosterClick(position: Int)

        fun onReviewFilmPosterLongClick(position: Int)

        fun onReviewLikeClick(position: Int)

        fun onReviewCommentClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        return ReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_review, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = reviewList[position]

        // Like status
        when (currentItem.isLiked) {
            true -> holder.imageLike.setImageResource(R.drawable.ic_fill_heart)
            false -> holder.imageLike.setImageResource(R.drawable.ic_outline_heart)
        }

        // Border
        if (position == itemCount - 1) {
            holder.border.visibility = View.INVISIBLE
        } else {
            holder.border.visibility = View.VISIBLE
        }

        // Parsing
        val reviewStar = ConvertRating.getStar(currentItem.rating)
        val filmYear = ParseDate.getYear(currentItem.releaseDate)
        val filmPoster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.poster}"

        // Isi
        holder.textFilmTitleYear.text = String.format("%s (%s)", currentItem.title, filmYear)
        holder.textUsername.text = currentItem.username
        holder.textReviewDetail.text = currentItem.reviewDetail
        holder.textTotalLike.text = currentItem.totalLike.toString()
        holder.textTotalComment.text = currentItem.totalComment.toString()
        holder.textReviewTimestamp.text = currentItem.timestamp
        holder.imageReviewStar.setImageResource(reviewStar!!)
        Glide.with(context).load(currentItem.profilePicture).into(holder.imageUserProfile)
        Glide.with(context).load(filmPoster).into(holder.imageFilmPoster)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ReviewViewHolder(
        itemView: View,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val imageUserProfile: ImageView = itemView.image_rr_profile
        val imageReviewStar: ImageView = itemView.image_rr_star
        val imageFilmPoster: ImageView = itemView.image_rr_poster
        val imageLike: ImageView = itemView.image_rr_like
        val textFilmTitleYear: TextView = itemView.text_rr_title_year
        val textUsername: TextView = itemView.text_rr_username
        val textReviewDetail: TextView = itemView.text_rr_review
        val textTotalLike: TextView = itemView.text_rr_total_like
        val textTotalComment: TextView = itemView.text_rr_total_comment
        val textReviewTimestamp: TextView = itemView.text_rr_timestamp
        val border: View = itemView.border_rr_layout
        private val imageComment: ImageView = itemView.image_rr_comment
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            when (v) {
                itemView -> mOnClickListener.onReviewItemClick(adapterPosition)
                imageUserProfile -> mOnClickListener.onReviewUserProfileClick(adapterPosition)
                imageFilmPoster -> mOnClickListener.onReviewFilmPosterClick(adapterPosition)
                imageLike -> mOnClickListener.onReviewLikeClick(adapterPosition)
                imageComment -> mOnClickListener.onReviewCommentClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == imageFilmPoster) {
                mOnClickListener.onReviewFilmPosterLongClick(adapterPosition)
            }
            return true
        }
    }
}