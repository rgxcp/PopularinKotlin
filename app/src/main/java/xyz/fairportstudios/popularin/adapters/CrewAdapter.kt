package xyz.fairportstudios.popularin.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_credit.view.*
import xyz.fairportstudios.popularin.R
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.statics.TMDbAPI

class CrewAdapter(
    private val context: Context,
    private val crewList: ArrayList<Crew>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<CrewAdapter.CrewViewHolder>() {
    interface OnClickListener {
        fun onCrewItemClick(position: Int)
    }

    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        return CrewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_credit, parent, false), onClickListener)
    }

    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {
        // Posisi
        val currentItem = crewList[position]

        // Parsing
        val profile = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.profilePath}"

        // Isi
        holder.textName.text = currentItem.name
        holder.textAs.text = currentItem.job
        Glide.with(context).load(profile).into(holder.imageProfile)

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
        return crewList.size
    }

    class CrewViewHolder(itemView: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageProfile: ImageView = itemView.image_rcr_profile
        val textName: TextView = itemView.text_rcr_name
        val textAs: TextView = itemView.text_rcr_as
        private val mOnClickListener = onClickListener

        override fun onClick(v: View?) {
            if (v == itemView) {
                mOnClickListener.onCrewItemClick(adapterPosition)
            }
        }
    }
}