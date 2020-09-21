package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.databinding.ItemFilmReviewBinding
import xyz.fairportstudios.popularin.interfaces.FilmReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.FilmReview

class FilmReviewAdapter(
    private val filmReviewList: ArrayList<FilmReview>,
    private val clickListener: FilmReviewAdapterClickListener
) : RecyclerView.Adapter<FilmReviewAdapter.FilmReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmReviewViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFilmReviewBinding.inflate(layoutInflater, parent, false)
        return FilmReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = filmReviewList[position]

        // Binding
        holder.binding.filmReview = currentItem

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

    override fun getItemCount() = filmReviewList.size

    inner class FilmReviewViewHolder(val binding: ItemFilmReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onFilmReviewItemClick(adapterPosition)
            }
            binding.userProfilePicture.setOnClickListener {
                clickListener.onFilmReviewUserProfileClick(adapterPosition)
            }
            binding.likeImage.setOnClickListener {
                clickListener.onFilmReviewLikeClick(adapterPosition)
            }
            binding.commentImage.setOnClickListener {
                clickListener.onFilmReviewCommentClick(adapterPosition)
            }
        }
    }
}