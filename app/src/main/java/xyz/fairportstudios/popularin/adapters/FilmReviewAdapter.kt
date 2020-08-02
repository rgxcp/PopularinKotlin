package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_film_review.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.services.ConvertRating

class FilmReviewAdapter(
    private val context: Context,
    private val filmReviewList: ArrayList<FilmReview>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<FilmReviewAdapter.FilmReviewViewHolder>() {
    interface OnClickListener {
        fun onFilmReviewItemClick(position: Int)

        fun onFilmReviewUserProfileClick(position: Int)

        fun onFilmReviewLikeClick(position: Int)

        fun onFilmReviewCommentClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmReviewViewHolder {
        return FilmReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_film_review, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: FilmReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = filmReviewList[position]

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

        // Isi
        holder.textUsername.text = currentItem.username
        holder.textReviewDetail.text = currentItem.reviewDetail
        holder.textTotalLike.text = currentItem.totalLike.toString()
        holder.textTotalComment.text = currentItem.totalComment.toString()
        holder.textReviewTimestamp.text = currentItem.timestamp
        holder.imageReviewStar.setImageResource(reviewStar!!)
        Glide.with(context).load(currentItem.profilePicture).into(holder.imageUserProfile)
    }

    override fun getItemCount(): Int {
        return filmReviewList.size
    }

    class FilmReviewViewHolder(itemView: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageUserProfile: ImageView = itemView.image_rfr_profile
        val imageReviewStar: ImageView = itemView.image_rfr_star
        val imageLike: ImageView = itemView.image_rfr_like
        val textUsername: TextView = itemView.text_rfr_username
        val textReviewDetail: TextView = itemView.text_rfr_review
        val textTotalLike: TextView = itemView.text_rfr_total_like
        val textTotalComment: TextView = itemView.text_rfr_total_comment
        val textReviewTimestamp: TextView = itemView.text_rfr_timestamp
        val border: View = itemView.border_rfr_layout
        private val imageComment: ImageView = itemView.image_rfr_comment
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            when (v) {
                itemView -> mOnClickListener.onFilmReviewItemClick(adapterPosition)
                imageUserProfile -> mOnClickListener.onFilmReviewUserProfileClick(adapterPosition)
                imageLike -> mOnClickListener.onFilmReviewLikeClick(adapterPosition)
                imageComment -> mOnClickListener.onFilmReviewCommentClick(adapterPosition)
            }
        }
    }
}