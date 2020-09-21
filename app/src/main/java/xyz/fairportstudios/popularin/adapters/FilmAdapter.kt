package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemFilmBinding
import xyz.fairportstudios.popularin.interfaces.FilmAdapterClickListener
import xyz.fairportstudios.popularin.models.Film

class FilmAdapter(
    private val filmList: ArrayList<Film>,
    private val clickListener: FilmAdapterClickListener
) : RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFilmBinding.inflate(layoutInflater, parent, false)
        return FilmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        // Posisi
        val currentItem = filmList[position]

        // Binding
        holder.binding.film = currentItem
    }

    override fun getItemCount() = filmList.size

    inner class FilmViewHolder(val binding: ItemFilmBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onFilmItemClick(adapterPosition)
            }
            binding.poster.setOnClickListener {
                clickListener.onFilmPosterClick(adapterPosition)
            }
            binding.poster.setOnLongClickListener {
                clickListener.onFilmPosterLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }
}