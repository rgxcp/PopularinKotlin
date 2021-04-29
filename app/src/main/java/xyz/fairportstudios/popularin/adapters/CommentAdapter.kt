package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemCommentBinding
import xyz.fairportstudios.popularin.interfaces.CommentAdapterClickListener
import xyz.fairportstudios.popularin.models.Comment

class CommentAdapter(
    private val commentList: ArrayList<Comment>,
    private val clickListener: CommentAdapterClickListener
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(layoutInflater, parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        // Posisi
        val currentItem = commentList[position]

        // Binding
        holder.binding.comment = currentItem

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

        // Auth
        when (currentItem.isSelf) {
            true -> {
                holder.binding.deleteImage.visibility = View.VISIBLE
                holder.binding.moreImage.visibility = View.GONE
            }
            false -> {
                holder.binding.deleteImage.visibility = View.GONE
                holder.binding.moreImage.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = commentList.size

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.userProfilePicture.setOnClickListener {
                clickListener.onCommentProfileClick(adapterPosition)
            }
            binding.nsfwBanner.setOnClickListener {
                clickListener.onCommentNSFWBannerClick(adapterPosition)
            }
            binding.reportImage.setOnClickListener {
                clickListener.onCommentReportClick(adapterPosition)
            }
            binding.moreImage.setOnClickListener {
                clickListener.onCommentMoreClick(adapterPosition, binding.moreImage)
            }
            binding.deleteImage.setOnClickListener {
                clickListener.onCommentDeleteClick(adapterPosition)
            }
        }
    }
}