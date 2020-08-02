package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_film_grid.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.statics.TMDbAPI

class FilmGridAdapter(
    private val context: Context,
    private val filmList: ArrayList<Film>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<FilmGridAdapter.FilmGridViewHolder>() {
    interface OnClickListener {
        fun onFilmGridItemClick(position: Int)

        fun onFilmGridItemLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmGridViewHolder {
        return FilmGridViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_film_grid, parent, false))
    }

    override fun onBindViewHolder(holder: FilmGridViewHolder, position: Int) {
        // Posisi
        val currentItem = filmList[position]

        // Parsing
        val poster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.posterPath}"

        // Isi
        Glide.with(context).load(poster).into(holder.imagePoster)
    }

    override fun getItemCount(): Int {
        return filmList.size
    }

    inner class FilmGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val imagePoster: ImageView = itemView.image_rfg_poster

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) {
                onClickListener.onFilmGridItemClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == itemView) {
                onClickListener.onFilmGridItemLongClick(adapterPosition)
            }
            return true
        }
    }
}