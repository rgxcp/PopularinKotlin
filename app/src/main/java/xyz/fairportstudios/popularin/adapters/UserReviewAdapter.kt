package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_user_review.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.UserReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.UserReview
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.TMDbAPI

class UserReviewAdapter(
    private val context: Context,
    private val userReviewList: ArrayList<UserReview>,
    private val clickListener: UserReviewAdapterClickListener
) : RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewViewHolder {
        return UserReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user_review, parent, false))
    }

    override fun onBindViewHolder(holder: UserReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = userReviewList[position]

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
        holder.mTextReviewDetail.text = currentItem.reviewDetail
        holder.mTextTotalLike.text = currentItem.totalLike.toString()
        holder.mTextTotalComment.text = currentItem.totalComment.toString()
        holder.mTextReviewTimestamp.text = currentItem.timestamp
        reviewStar?.let { holder.mImageReviewStar.setImageResource(it) }
        Glide.with(context).load(filmPoster).into(holder.mImageFilmPoster)
    }

    override fun getItemCount() = userReviewList.size

    inner class UserReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val mImageReviewStar: ImageView = itemView.image_rur_star
        val mImageFilmPoster: ImageView = itemView.image_rur_poster
        val mImageLike: ImageView = itemView.image_rur_like
        val mTextFilmTitleYear: TextView = itemView.text_rur_title_year
        val mTextReviewDetail: TextView = itemView.text_rur_review
        val mTextTotalLike: TextView = itemView.text_rur_total_like
        val mTextTotalComment: TextView = itemView.text_rur_total_comment
        val mTextReviewTimestamp: TextView = itemView.text_rur_timestamp
        val mBorder: View = itemView.border_rur_layout
        private val mImageComment: ImageView = itemView.image_rur_comment

        init {
            itemView.setOnClickListener(this)
            mImageFilmPoster.setOnClickListener(this)
            mImageFilmPoster.setOnLongClickListener(this)
            mImageLike.setOnClickListener(this)
            mImageComment.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v) {
                itemView -> clickListener.onUserReviewItemClick(adapterPosition)
                mImageFilmPoster -> clickListener.onUserReviewFilmPosterClick(adapterPosition)
                mImageLike -> clickListener.onUserReviewLikeClick(adapterPosition)
                mImageComment -> clickListener.onUserReviewCommentClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == mImageFilmPoster) clickListener.onUserReviewFilmPosterLongClick(adapterPosition)
            return true
        }
    }
}