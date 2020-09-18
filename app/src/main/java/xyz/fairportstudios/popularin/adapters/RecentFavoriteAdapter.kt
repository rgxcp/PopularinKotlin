package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_recent_favorite.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.interfaces.RecentFavoriteAdapterClickListener
import xyz.fairportstudios.popularin.models.RecentFavorite
import xyz.fairportstudios.popularin.statics.TMDbAPI

class RecentFavoriteAdapter(
    private val context: Context,
    private val recentFavoriteList: ArrayList<RecentFavorite>,
    private val clickListener: RecentFavoriteAdapterClickListener
) : RecyclerView.Adapter<RecentFavoriteAdapter.RecentFavoriteViewHolder>() {
    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentFavoriteViewHolder {
        return RecentFavoriteViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_recent_favorite, parent, false))
    }

    override fun onBindViewHolder(holder: RecentFavoriteViewHolder, position: Int) {
        // Posisi
        val currentItem = recentFavoriteList[position]

        // Parsing
        val poster = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.poster}"

        // Isi
        Glide.with(context).load(poster).into(holder.mImagePoster)

        // Margin
        val left = when (position == 0) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val right = when (position == itemCount - 1) {
            true -> getDensity(16)
            false -> getDensity(4)
        }
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = recentFavoriteList.size

    inner class RecentFavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val mImagePoster: ImageView = itemView.image_rrf_poster

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) clickListener.onRecentFavoriteItemClick(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == itemView) clickListener.onRecentFavoriteItemLongClick(adapterPosition)
            return true
        }
    }
}