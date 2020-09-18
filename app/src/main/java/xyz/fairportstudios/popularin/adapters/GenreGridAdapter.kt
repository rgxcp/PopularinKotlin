package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_genre_grid.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.GenreGridAdapterClickListener
import xyz.fairportstudios.popularin.models.Genre

class GenreGridAdapter(
    private val context: Context,
    private val genreList: ArrayList<Genre>,
    private val clickListener: GenreGridAdapterClickListener
) : RecyclerView.Adapter<GenreGridAdapter.GenreGridViewHolder>() {
    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreGridViewHolder {
        return GenreGridViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_genre_grid, parent, false))
    }

    override fun onBindViewHolder(holder: GenreGridViewHolder, position: Int) {
        // Posisi
        val currentItem = genreList[position]

        // Isi
        holder.mTextTitle.text = currentItem.title
        holder.mImageBackground.setImageResource(currentItem.background)

        // Margin
        val left = when ((position % 2) == 0) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val top = when (position < 2) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val right = when ((position % 2) == 1) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val bottom = when (position >= (itemCount - 2)) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = genreList.size

    inner class GenreGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mImageBackground: ImageView = itemView.image_rgg_background
        val mTextTitle: TextView = itemView.text_rgg_title

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) clickListener.onGenreItemClick(adapterPosition)
        }
    }
}