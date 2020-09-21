package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemRecentReviewBinding
import xyz.fairportstudios.popularin.interfaces.RecentReviewAdapterClickListener
import xyz.fairportstudios.popularin.models.RecentReview
import xyz.fairportstudios.popularin.services.ConvertPixel

class RecentReviewAdapter(
    private val recentReviewList: ArrayList<RecentReview>,
    private val clickListener: RecentReviewAdapterClickListener
) : RecyclerView.Adapter<RecentReviewAdapter.RecentReviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentReviewViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRecentReviewBinding.inflate(layoutInflater, parent, false)
        return RecentReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentReviewViewHolder, position: Int) {
        // Posisi
        val currentItem = recentReviewList[position]

        // Binding
        holder.binding.recentReview = currentItem

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

    override fun getItemCount() = recentReviewList.size

    inner class RecentReviewViewHolder(val binding: ItemRecentReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onRecentReviewItemClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                clickListener.onRecentReviewItemLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }
}