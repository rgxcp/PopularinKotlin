package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemGenreHorizontalBinding
import xyz.fairportstudios.popularin.interfaces.GenreHorizontalAdapterClickListener
import xyz.fairportstudios.popularin.models.Genre
import xyz.fairportstudios.popularin.services.ConvertPixel

class GenreHorizontalAdapter(
    private val genreList: ArrayList<Genre>,
    private val clickListener: GenreHorizontalAdapterClickListener
) : RecyclerView.Adapter<GenreHorizontalAdapter.GenreHorizontalViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreHorizontalViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemGenreHorizontalBinding.inflate(layoutInflater, parent, false)
        return GenreHorizontalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreHorizontalViewHolder, position: Int) {
        // Posisi
        val currentItem = genreList[position]

        // Binding
        holder.binding.genre = currentItem

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

    override fun getItemCount() = genreList.size

    inner class GenreHorizontalViewHolder(val binding: ItemGenreHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onGenreItemClick(adapterPosition)
            }
        }
    }
}