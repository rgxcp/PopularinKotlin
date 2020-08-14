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
import xyz.fairportstudios.popularin.models.Genre

class GenreHorizontalAdapter(
    private val context: Context,
    private val genreList: ArrayList<Genre>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<GenreHorizontalAdapter.GenreHorizontalViewHolder>() {
    interface OnClickListener {
        fun onGenreItemClick(position: Int)
    }

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
        holder.textTitle.text = currentItem.title
        holder.imageBackground.setImageResource(currentItem.background)

        // Margin
        var left = getDensity(6)
        var right = getDensity(6)
        val isEdgeLeft = position == 0
        val isEdgeRight = position == itemCount - 1
        if (isEdgeLeft) {
            left = getDensity(16)
        }
        if (isEdgeRight) {
            right = getDensity(16)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return genreList.size
    }

    inner class GenreHorizontalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageBackground: ImageView = itemView.image_rgh_background
        val textTitle: TextView = itemView.text_rgh_title

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) {
                onClickListener.onGenreItemClick(adapterPosition)
            }
        }
    }
}