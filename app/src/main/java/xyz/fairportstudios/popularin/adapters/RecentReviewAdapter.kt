package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_recent_review.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.statics.TMDbAPI

class RecentReviewAdapter(
    private val context: Context,
    private val recentReviewList: ArrayList<RecentReview>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<RecentReviewAdapter.RecentReviewViewHolder>() {
    interface OnClickListener {
        fun onRecentReviewItemClick(position: Int)

        fun onRecentReviewItemLongClick(position: Int)
    }

    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentReviewViewHolder {
        return RecentReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_recent_review, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: RecentReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = recentReviewList[position]

        // Parsing
        val reviewStar = ConvertRating.getStar(currentItem.rating)
        val filmPoster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.poster}"

        // Isi
        holder.imageReviewStar.setImageResource(reviewStar!!)
        Glide.with(context).load(filmPoster).into(holder.imageFilmPoster)

        // Margin
        var left = getDensity(4)
        var right = getDensity(4)
        val isEdgeLeft = position == 0
        val isEdgeRight = position == itemCount - 1
        if (isEdgeLeft) {
            left = getDensity(16)
        }
        if (isEdgeRight) {
            right = getDensity(16)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return recentReviewList.size
    }

    class RecentReviewViewHolder(
        itemView: View,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val imageFilmPoster: ImageView = itemView.image_rrr_poster
        val imageReviewStar: ImageView = itemView.image_rrr_star
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            if (v == itemView) {
                mOnClickListener.onRecentReviewItemClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == itemView) {
                mOnClickListener.onRecentReviewItemLongClick(adapterPosition)
            }
            return true
        }
    }
}