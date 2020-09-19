package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_recent_review.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.RecentReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.services.ConvertRating
import xyz.fairportstudios.popularin.statics.TMDbAPI

class RecentReviewAdapter(
    private val context: Context,
    private val recentReviewList: ArrayList<RecentReview>,
    private val clickListener: RecentReviewAdapterClickListener
) : RecyclerView.Adapter<RecentReviewAdapter.RecentReviewViewHolder>() {
    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentReviewViewHolder {
        return RecentReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recent_review, parent, false))
    }

    override fun onBindViewHolder(holder: RecentReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = recentReviewList[position]

        // Parsing
        val reviewStar = ConvertRating.getStar(currentItem.rating)
        val filmPoster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.poster}"

        // Isi
        reviewStar?.let { holder.mImageReviewStar.setImageResource(it) }
        Glide.with(context).load(filmPoster).into(holder.mImageFilmPoster)

        // Margin
        val left = when (position == 0) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val right = when (position == itemCount - 1) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = recentReviewList.size

    inner class RecentReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val mImageFilmPoster: ImageView = itemView.image_rrr_poster
        val mImageReviewStar: ImageView = itemView.image_rrr_star

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) clickListener.onRecentReviewItemClick(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == itemView) clickListener.onRecentReviewItemLongClick(adapterPosition)
            return true
        }
    }
}