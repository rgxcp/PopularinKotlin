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
        return ReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_review, parent, false))
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = reviewList[position]

        // Like status
        when (currentItem.isLiked) {
            true -> holder.mImageLike.setImageResource(R.drawable.ic_fill_heart)
            false -> holder.mImageLike.setImageResource(R.drawable.ic_outline_heart)
        }

        // Border
        holder.mBorder.visibility = when (position == itemCount - 1) {
            true -> View.INVISIBLE
            false -> View.VISIBLE
        }

        // Parsing
        val reviewStar = ConvertRating.getStar(currentItem.rating)
        val filmYear = ParseDate.getYear(currentItem.releaseDate)
        val filmPoster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.poster}"
        val filmTitleYear = String.format("%s (%s)", currentItem.title, filmYear)

        // Isi
        holder.mTextFilmTitleYear.text = filmTitleYear
        holder.mTextUsername.text = currentItem.username
        holder.mTextReviewDetail.text = currentItem.reviewDetail
        holder.mTextTotalLike.text = currentItem.totalLike.toString()
        holder.mTextTotalComment.text = currentItem.totalComment.toString()
        holder.mTextReviewTimestamp.text = currentItem.timestamp
        reviewStar?.let { holder.mImageReviewStar.setImageResource(it) }
        Glide.with(context).load(currentItem.profilePicture).into(holder.mImageUserProfile)
        Glide.with(context).load(filmPoster).into(holder.mImageFilmPoster)
    }

    override fun getItemCount() = reviewList.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val mImageUserProfile: ImageView = itemView.image_rr_profile
        val mImageReviewStar: ImageView = itemView.image_rr_star
        val mImageFilmPoster: ImageView = itemView.image_rr_poster
        val mImageLike: ImageView = itemView.image_rr_like
        val mTextFilmTitleYear: TextView = itemView.text_rr_title_year
        val mTextUsername: TextView = itemView.text_rr_username
        val mTextReviewDetail: TextView = itemView.text_rr_review
        val mTextTotalLike: TextView = itemView.text_rr_total_like
        val mTextTotalComment: TextView = itemView.text_rr_total_comment
        val mTextReviewTimestamp: TextView = itemView.text_rr_timestamp
        val mBorder: View = itemView.border_rr_layout
        private val mImageComment: ImageView = itemView.image_rr_comment

        init {
            itemView.setOnClickListener(this)
            mImageUserProfile.setOnClickListener(this)
            mImageFilmPoster.setOnClickListener(this)
            mImageFilmPoster.setOnLongClickListener(this)
            mImageLike.setOnClickListener(this)
            mImageComment.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v) {
                itemView -> onClickListener.onReviewItemClick(adapterPosition)
                mImageUserProfile -> onClickListener.onReviewUserProfileClick(adapterPosition)
                mImageFilmPoster -> onClickListener.onReviewFilmPosterClick(adapterPosition)
                mImageLike -> onClickListener.onReviewLikeClick(adapterPosition)
                mImageComment -> onClickListener.onReviewCommentClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == mImageFilmPoster) onClickListener.onReviewFilmPosterLongClick(adapterPosition)
            return true
        }
    }
}