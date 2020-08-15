package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_film.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Film
import xyz.fairportstudios.popularin.services.ConvertGenre
import xyz.fairportstudios.popularin.services.ParseDate
import xyz.fairportstudios.popularin.statics.TMDbAPI

class FilmAdapter(
    private val context: Context,
    private val filmList: ArrayList<Film>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<FilmAdapter.FilmViewHolder>() {
    interface OnClickListener {
        fun onFilmItemClick(position: Int)
        fun onFilmPosterClick(position: Int)
        fun onFilmPosterLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        return FilmViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_film, parent, false))
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        // Posisi
        val currentItem = filmList[position]

        // Parsing
        val genre = ConvertGenre.getGenreForHumans(currentItem.genreID)
        val releaseDate = ParseDate.getDateForHumans(currentItem.releaseDate)
        val poster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.posterPath}"

        // Isi
        holder.mTextTitle.text = currentItem.originalTitle
        holder.mTextGenre.text = genre
        holder.mTextReleaseDate.text = releaseDate
        Glide.with(context).load(poster).into(holder.mImagePoster)
    }

    override fun getItemCount() = filmList.size

    inner class FilmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val mImagePoster: ImageView = itemView.image_rf_poster
        val mTextTitle: TextView = itemView.text_rf_title
        val mTextGenre: TextView = itemView.text_rf_genre
        val mTextReleaseDate: TextView = itemView.text_rf_release_date

        init {
            itemView.setOnClickListener(this)
            mImagePoster.setOnClickListener(this)
            mImagePoster.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v) {
                itemView -> onClickListener.onFilmItemClick(adapterPosition)
                mImagePoster -> onClickListener.onFilmPosterClick(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == mImagePoster) onClickListener.onFilmPosterLongClick(adapterPosition)
            return true
        }
    }
}