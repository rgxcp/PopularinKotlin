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
import xyz.fairportstudios.popularin.interfaces.CrewAdapterClickListener
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.statics.TMDbAPI

class CrewAdapter(
    private val context: Context,
    private val crewList: ArrayList<Crew>,
    private val clickListener: CrewAdapterClickListener
) : RecyclerView.Adapter<CrewAdapter.CrewViewHolder>() {
    private fun getDensity(px: Int): Int {
        val dp = px * context.resources.displayMetrics.density
        return dp.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        return CrewViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_credit, parent, false))
    }

    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {
        // Posisi
        val currentItem = crewList[position]

        // Parsing
        val profile = "${TMDbAPI.BASE_SMALL_IMAGE_URL}${currentItem.profilePath}"

        // Isi
        holder.mTextName.text = currentItem.name
        holder.mTextAs.text = currentItem.job
        Glide.with(context).load(profile).into(holder.mImageProfile)

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

    override fun getItemCount() = crewList.size

    inner class CrewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mImageProfile: ImageView = itemView.image_rcr_profile
        val mTextName: TextView = itemView.text_rcr_name
        val mTextAs: TextView = itemView.text_rcr_as

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == itemView) clickListener.onCrewItemClick(adapterPosition)
        }
    }
}