package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.databinding.ItemUserReviewBinding
import xyz.fairportstudios.popularin.interfaces.UserReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.UserReview
import xyz.fairportstudios.popularin.services.ParseDate

class UserReviewAdapter(
    private val userReviewList: ArrayList<UserReview>,
    private val clickListener: UserReviewAdapterClickListener
) : RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserReviewBinding.inflate(layoutInflater, parent, false)
        return UserReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = userReviewList[position]

        // Binding
        holder.binding.userReview = currentItem
        holder.binding.filmYear = ParseDate.getYear(currentItem.releaseDate)

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

    override fun getItemCount() = userReviewList.size

    inner class UserReviewViewHolder(val binding: ItemUserReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onUserReviewItemClick(adapterPosition)
            }
            binding.filmPoster.setOnClickListener {
                clickListener.onUserReviewFilmPosterClick(adapterPosition)
            }
            binding.filmPoster.setOnLongClickListener {
                clickListener.onUserReviewFilmPosterLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
            binding.likeImage.setOnClickListener {
                clickListener.onUserReviewLikeClick(adapterPosition)
            }
            binding.commentImage.setOnClickListener {
                clickListener.onUserReviewCommentClick(adapterPosition)
            }
        }
    }
}