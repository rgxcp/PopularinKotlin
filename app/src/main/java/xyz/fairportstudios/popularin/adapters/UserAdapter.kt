package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_user.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.User

class UserAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    interface OnClickListener {
        fun onUserItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_user, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Posisi
        val currentItem = userList[position]

        // Isi
        holder.textFullName.text = currentItem.fullName
        holder.textUsername.text = String.format("@%s", currentItem.username)
        Glide.with(context).load(currentItem.profilePicture).into(holder.imageProfile)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageProfile: ImageView = itemView.image_ru_profile
        val textFullName: TextView = itemView.text_ru_full_name
        val textUsername: TextView = itemView.text_ru_username
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            if (v == itemView) {
                mOnClickListener.onUserItemClick(adapterPosition)
            }
        }
    }
}