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
import xyz.fairportstudios.popularin.interfaces.CommentAdapterClickListener
import xyz.fairportstudios.popularin.models.Comment

class CommentAdapter(
    private val context: Context,
    private val authID: Int,
    private val commentList: ArrayList<Comment>,
    private val clickListener: CommentAdapterClickListener
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        // Posisi
        val currentItem = commentList[position]

        // Auth
        holder.mImageDelete.visibility = when (currentItem.userID == authID) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        // Isi
        holder.mTextUsername.text = currentItem.username
        holder.mTextCommentTimestamp.text = currentItem.timestamp
        holder.mTextCommentDetail.text = currentItem.commentDetail
        Glide.with(context).load(currentItem.profilePicture).into(holder.mImageProfile)
    }

    override fun getItemCount() = commentList.size

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mImageProfile: ImageView = itemView.image_rc_profile
        val mImageDelete: ImageView = itemView.image_rc_delete
        val mTextUsername: TextView = itemView.text_rc_username
        val mTextCommentTimestamp: TextView = itemView.text_rc_timestamp
        val mTextCommentDetail: TextView = itemView.text_rc_comment

        init {
            mImageProfile.setOnClickListener(this)
            mImageDelete.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v) {
                mImageProfile -> clickListener.onCommentProfileClick(adapterPosition)
                mImageDelete -> clickListener.onCommentDeleteClick(adapterPosition)
            }
        }
    }
}