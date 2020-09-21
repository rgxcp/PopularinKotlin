package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemFilmGridBinding
import xyz.fairportstudios.popularin.interfaces.FilmGridAdapterClickListener
import xyz.fairportstudios.popularin.models.Film

class FilmGridAdapter(
    private val filmList: ArrayList<Film>,
    private val clickListener: FilmGridAdapterClickListener
) : RecyclerView.Adapter<FilmGridAdapter.FilmGridViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmGridViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFilmGridBinding.inflate(layoutInflater, parent, false)
        return FilmGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmGridViewHolder, position: Int) {
        // Posisi
        val currentItem = filmList[position]

        // Binding
        holder.binding.film = currentItem
    }

    override fun getItemCount() = filmList.size

    inner class FilmGridViewHolder(val binding: ItemFilmGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onFilmGridItemClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                clickListener.onFilmGridItemLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }
}