package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_user.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.UserAdapterClickListener
import xyz.fairportstudios.popularin.models.User

class UserAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val clickListener: UserAdapterClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Posisi
        val currentItem = userList[position]

        // Parsing
        val username = String.format("@%s", currentItem.username)

        // Isi
        holder.mTextFullName.text = currentItem.fullName
        holder.mTextUsername.text = username
        Glide.with(context).load(currentItem.profilePicture).into(holder.mImageProfile)
    }

    override fun getItemCount() = userList.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mImageProfile: ImageView = itemView.image_ru_profile
        val mTextFullName: TextView = itemView.text_ru_full_name
        val mTextUsername: TextView = itemView.text_ru_username

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) clickListener.onUserItemClick(adapterPosition)
        }
    }
}