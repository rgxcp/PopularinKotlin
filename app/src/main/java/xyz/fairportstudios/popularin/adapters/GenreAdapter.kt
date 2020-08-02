package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_genre.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Genre

class GenreAdapter(
    private val context: Context,
    private val genreList: ArrayList<Genre>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {
    interface OnClickListener {
        fun onGenreItemClick(position: Int)
    }

    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        return GenreViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_genre, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        // Posisi
        val currentItem = genreList[position]

        // Isi
        holder.textTitle.text = currentItem.title
        holder.imageBackground.setImageResource(currentItem.background)

        // Margin
        var left = getDensity(4)
        var top = getDensity(4)
        var right = getDensity(4)
        var bottom = getDensity(4)
        val isEdgeLeft = (position % 2) == 0
        val isEdgeTop = position < 2
        val isEdgeRight = (position % 2) == 1
        val isEdgeBottom = position >= (itemCount - 2)
        if (isEdgeLeft) {
            left = getDensity(16)
        }
        if (isEdgeTop) {
            top = getDensity(16)
        }
        if (isEdgeRight) {
            right = getDensity(16)
        }
        if (isEdgeBottom) {
            bottom = getDensity(16)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return genreList.size
    }

    class GenreViewHolder(itemView: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageBackground: ImageView = itemView.image_rg_background
        val textTitle: TextView = itemView.text_rg_title
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            if (v == itemView) {
                mOnClickListener.onGenreItemClick(adapterPosition)
            }
        }
    }
}