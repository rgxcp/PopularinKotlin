package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_user_review.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.UserReview
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.TMDbAPI

class UserReviewAdapter(
    private val context: Context,
    private val userReviewList: ArrayList<UserReview>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder>() {
    interface OnClickListener {
        fun onUserReviewItemClick(position: Int)

        fun onUserReviewFilmPosterClick(position: Int)

        fun onUserReviewFilmPosterLongClick(position: Int)

        fun onUserReviewLikeClick(position: Int)

        fun onUserReviewCommentClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewViewHolder {
        return UserReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_user_review, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: UserReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = userReviewList[position]

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
        holder.textReviewDetail.text = currentItem.reviewDetail
        holder.textTotalLike.text = currentItem.totalLike.toString()
        holder.textTotalComment.text = currentItem.totalComment.toString()
        holder.textReviewTimestamp.text = currentItem.timestamp
        holder.imageReviewStar.setImageResource(reviewStar!!)
        Glide.with(context).load(filmPoster).into(holder.imageFilmPoster)
    }

    override fun getItemCount(): Int {
        return userReviewList.size
    }

    class UserReviewViewHolder(
        itemView: View,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val imageReviewStar: ImageView = itemView.image_rur_star
        val imageFilmPoster: ImageView = itemView.image_rur_poster
        val imageLike: ImageView = itemView.image_rur_like
        val textFilmTitleYear: TextView = itemView.text_rur_title_year
        val textReviewDetail: TextView = itemView.text_rur_review
        val textTotalLike: TextView = itemView.text_rur_total_like
        val textTotalComment: TextView = itemView.text_rur_total_comment
        val textReviewTimestamp: TextView = itemView.text_rur_timestamp
        val border: View = itemView.border_rur_layout
        private val imageComment: ImageView = itemView.image_rur_comment
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            when (v) {
                itemView -> mOnClickListener.onUserReviewItemClick(adapterPosition)
                imageFilmPoster -> mOnClickListener.onUserReviewFilmPosterClick(adapterPosition)
                imageLike -> mOnClickListener.onUserReviewLikeClick(adapterPosition)
                imageComment -> mOnClickListener.onUserReviewCommentClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == imageFilmPoster) {
                mOnClickListener.onUserReviewFilmPosterLongClick(adapterPosition)
            }
            return true
        }
    }
}