package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.databinding.ItemReviewBinding
import xyz.fairportstudios.popularin.interfaces.ReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.Review
import xyz.fairportstudios.popularin.services.ParseDate

class ReviewAdapter(
    private val reviewList: ArrayList<Review>,
    private val clickListener: ReviewAdapterClickListener
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewBinding.inflate(layoutInflater, parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = reviewList[position]

        // Binding
        holder.binding.review = currentItem
        holder.binding.filmYear = ParseDate.getYear(currentItem.releaseDate)

        // NSFW status
        when (currentItem.isNSFW) {
            true -> {
                holder.binding.nsfwBanner.visibility = View.VISIBLE
                holder.binding.detail.visibility = View.GONE
            }
            false -> {
                holder.binding.nsfwBanner.visibility = View.GONE
                holder.binding.detail.visibility = View.VISIBLE
            }
        }

        // Like status
        holder.binding.likeImage.setImageResource(
            when (currentItem.isLiked) {
                true -> R.drawable.ic_fill_heart
                false -> R.drawable.ic_outline_heart
            }
        )

        // Border
        holder.binding.border.visibility = when (position == itemCount - 1) {
            true -> View.INVISIBLE
            false -> View.VISIBLE
        }
    }

    override fun getItemCount() = reviewList.size

    inner class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onReviewItemClick(adapterPosition)
            }
            binding.userProfilePicture.setOnClickListener {
                clickListener.onReviewUserProfileClick(adapterPosition)
            }
            binding.filmPoster.setOnClickListener {
                clickListener.onReviewFilmPosterClick(adapterPosition)
            }
            binding.filmPoster.setOnLongClickListener {
                clickListener.onReviewFilmPosterLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
            binding.nsfwBanner.setOnClickListener {
                clickListener.onReviewNSFWBannerClick(adapterPosition)
            }
            binding.likeImage.setOnClickListener {
                clickListener.onReviewLikeClick(adapterPosition)
            }
            binding.commentImage.setOnClickListener {
                clickListener.onReviewCommentClick(adapterPosition)
            }
            binding.reportImage.setOnClickListener {
                clickListener.onReviewReportClick(adapterPosition)
            }
        }
    }
}