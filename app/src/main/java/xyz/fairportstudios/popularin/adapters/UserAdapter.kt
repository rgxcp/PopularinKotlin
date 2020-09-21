package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemUserBinding
import xyz.fairportstudios.popularin.interfaces.UserAdapterClickListener
import xyz.fairportstudios.popularin.models.User

class UserAdapter(
    private val userList: ArrayList<User>,
    private val clickListener: UserAdapterClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Posisi
        val currentItem = userList[position]

        // Binding
        holder.binding.user = currentItem
    }

    override fun getItemCount() = userList.size

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onUserItemClick(adapterPosition)
            }
        }
    }
}