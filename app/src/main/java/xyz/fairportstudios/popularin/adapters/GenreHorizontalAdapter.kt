package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_genre_horizontal.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.GenreHorizontalAdapterClickListener
import xyz.fairportstudios.popularin.models.Genre

class GenreHorizontalAdapter(
    private val context: Context,
    private val genreList: ArrayList<Genre>,
    private val clickListener: GenreHorizontalAdapterClickListener
) : RecyclerView.Adapter<GenreHorizontalAdapter.GenreHorizontalViewHolder>() {
    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreHorizontalViewHolder {
        return GenreHorizontalViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_genre_horizontal, parent, false))
    }

    override fun onBindViewHolder(holder: GenreHorizontalViewHolder, position: Int) {
        // Posisi
        val currentItem = genreList[position]

        // Isi
        holder.mTextTitle.text = currentItem.title
        holder.mImageBackground.setImageResource(currentItem.background)

        // Margin
        val left = when (position == 0) {
            true -> getDensity(16)
            false -> getDensity(6)
        }
        val right = when (position == itemCount - 1) {
            true -> getDensity(16)
            false -> getDensity(6)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = genreList.size

    inner class GenreHorizontalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mImageBackground: ImageView = itemView.image_rgh_background
        val mTextTitle: TextView = itemView.text_rgh_title

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) clickListener.onGenreItemClick(adapterPosition)
        }
    }
}