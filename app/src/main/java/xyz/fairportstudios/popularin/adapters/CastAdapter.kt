package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemCastBinding
import xyz.fairportstudios.popularin.interfaces.CastAdapterClickListener
import xyz.fairportstudios.popularin.models.Cast
import xyz.fairportstudios.popularin.services.ConvertPixel

class CastAdapter(
    private val castList: ArrayList<Cast>,
    private val clickListener: CastAdapterClickListener
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCastBinding.inflate(layoutInflater, parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        // Posisi
        val currentItem = castList[position]

        // Binding
        holder.binding.cast = currentItem

        // Margin
        val context = holder.binding.root.context
        val left = when (position == 0) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 6)
        }
        val right = when (position == itemCount - 1) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 6)
        }
        val layoutParams = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.binding.root.layoutParams = layoutParams
    }

    override fun getItemCount() = castList.size

    inner class CastViewHolder(val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onCastItemClick(adapterPosition)
            }
        }
    }
}