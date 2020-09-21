package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemGenreGridBinding
import xyz.fairportstudios.popularin.interfaces.GenreGridAdapterClickListener
import xyz.fairportstudios.popularin.models.Genre
import xyz.fairportstudios.popularin.services.ConvertPixel

class GenreGridAdapter(
    private val genreList: ArrayList<Genre>,
    private val clickListener: GenreGridAdapterClickListener
) : RecyclerView.Adapter<GenreGridAdapter.GenreGridViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreGridViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemGenreGridBinding.inflate(layoutInflater, parent, false)
        return GenreGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreGridViewHolder, position: Int) {
        // Posisi
        val currentItem = genreList[position]

        // Binding
        holder.binding.genre = currentItem

        // Margin
        val context = holder.binding.root.context
        val left = when ((position % 2) == 0) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 4)
        }
        val top = when (position < 2) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 4)
        }
        val right = when ((position % 2) == 1) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 4)
        }
        val bottom = when (position >= (itemCount - 2)) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 4)
        }
        val layoutParams = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        holder.binding.root.layoutParams = layoutParams
    }

    override fun getItemCount() = genreList.size

    inner class GenreGridViewHolder(val binding: ItemGenreGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onGenreItemClick(adapterPosition)
            }
        }
    }
}