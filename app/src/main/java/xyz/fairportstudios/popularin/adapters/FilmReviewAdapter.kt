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
import xyz.fairportstudios.popularin.interfaces.FilmReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.FilmReview
import xyz.fairportstudios.popularin.services.ConvertRating

class FilmReviewAdapter(
    private val context: Context,
    private val filmReviewList: ArrayList<FilmReview>,
    private val clickListener: FilmReviewAdapterClickListener
) : RecyclerView.Adapter<FilmReviewAdapter.FilmReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmReviewViewHolder {
        return FilmReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_film_review, parent, false))
    }

    override fun onBindViewHolder(holder: FilmReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = filmReviewList[position]

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

        // Isi
        holder.mTextUsername.text = currentItem.username
        holder.mTextReviewDetail.text = currentItem.reviewDetail
        holder.mTextTotalLike.text = currentItem.totalLike.toString()
        holder.mTextTotalComment.text = currentItem.totalComment.toString()
        holder.mTextReviewTimestamp.text = currentItem.timestamp
        reviewStar?.let { holder.mImageReviewStar.setImageResource(it) }
        Glide.with(context).load(currentItem.profilePicture).into(holder.mImageUserProfile)
    }

    override fun getItemCount() = filmReviewList.size

    inner class FilmReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mImageUserProfile: ImageView = itemView.image_rfr_profile
        val mImageReviewStar: ImageView = itemView.image_rfr_star
        val mImageLike: ImageView = itemView.image_rfr_like
        val mTextUsername: TextView = itemView.text_rfr_username
        val mTextReviewDetail: TextView = itemView.text_rfr_review
        val mTextTotalLike: TextView = itemView.text_rfr_total_like
        val mTextTotalComment: TextView = itemView.text_rfr_total_comment
        val mTextReviewTimestamp: TextView = itemView.text_rfr_timestamp
        val mBorder: View = itemView.border_rfr_layout
        private val mImageComment: ImageView = itemView.image_rfr_comment

        init {
            itemView.setOnClickListener(this)
            mImageUserProfile.setOnClickListener(this)
            mImageLike.setOnClickListener(this)
            mImageComment.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v) {
                itemView -> clickListener.onFilmReviewItemClick(adapterPosition)
                mImageUserProfile -> clickListener.onFilmReviewUserProfileClick(adapterPosition)
                mImageLike -> clickListener.onFilmReviewLikeClick(adapterPosition)
                mImageComment -> clickListener.onFilmReviewCommentClick(adapterPosition)
            }
        }
    }
}