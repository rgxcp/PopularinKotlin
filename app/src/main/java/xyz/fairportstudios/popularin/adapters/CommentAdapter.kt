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

        // Auth
        holder.binding.deleteImage.visibility = when (currentItem.isSelf) {
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

    override fun getItemCount() = commentList.size

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.userProfilePicture.setOnClickListener {
                clickListener.onCommentProfileClick(adapterPosition)
            }
            binding.deleteImage.setOnClickListener {
                clickListener.onCommentDeleteClick(adapterPosition)
            }
        }
    }
}