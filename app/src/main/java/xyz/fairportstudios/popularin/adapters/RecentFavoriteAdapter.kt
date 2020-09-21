package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemRecentFavoriteBinding
import xyz.fairportstudios.popularin.interfaces.RecentFavoriteAdapterClickListener
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.services.ConvertPixel

class RecentFavoriteAdapter(
    private val recentFavoriteList: ArrayList<RecentFavorite>,
    private val clickListener: RecentFavoriteAdapterClickListener
) : RecyclerView.Adapter<RecentFavoriteAdapter.RecentFavoriteViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentFavoriteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRecentFavoriteBinding.inflate(layoutInflater, parent, false)
        return RecentFavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentFavoriteViewHolder, position: Int) {
        // Posisi
        val currentItem = recentFavoriteList[position]

        // Binding
        holder.binding.recentFavorite = currentItem

        // Margin
        val context = holder.binding.root.context
        val left = when (position == 0) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 4)
        }
        val right = when (position == itemCount - 1) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 4)
        }
        val layoutParams = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.binding.root.layoutParams = layoutParams
    }

    override fun getItemCount() = recentFavoriteList.size

    inner class RecentFavoriteViewHolder(val binding: ItemRecentFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onRecentFavoriteItemClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                clickListener.onRecentFavoriteItemLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }
}