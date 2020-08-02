package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_comment.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Comment

class CommentAdapter(
    private val context: Context,
    private val authID: Int,
    private val commentList: ArrayList<Comment>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    interface OnClickListener {
        fun onCommentProfileClick(position: Int)

        fun onCommentDeleteClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_comment, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        // Posisi
        val currentItem = commentList[position]

        // Auth
        when (currentItem.userID == authID) {
            true -> holder.imageDelete.visibility = View.VISIBLE
            false -> holder.imageDelete.visibility = View.GONE
        }

        // Isi
        holder.textUsername.text = currentItem.username
        holder.textCommentTimestamp.text = currentItem.timestamp
        holder.textCommentDetail.text = currentItem.commentDetail
        Glide.with(context).load(currentItem.profilePicture).into(holder.imageProfile)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    class CommentViewHolder(itemView: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageProfile: ImageView = itemView.image_rc_profile
        val imageDelete: ImageView = itemView.image_rc_delete
        val textUsername: TextView = itemView.text_rc_username
        val textCommentTimestamp: TextView = itemView.text_rc_timestamp
        val textCommentDetail: TextView = itemView.text_rc_comment
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            when (v) {
                imageProfile -> mOnClickListener.onCommentProfileClick(adapterPosition)
                imageDelete -> mOnClickListener.onCommentDeleteClick(adapterPosition)
            }
        }
    }
}