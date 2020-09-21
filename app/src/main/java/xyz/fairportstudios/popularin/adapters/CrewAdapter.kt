package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemCrewBinding
import xyz.fairportstudios.popularin.interfaces.CrewAdapterClickListener
import xyz.fairportstudios.popularin.models.Crew
import xyz.fairportstudios.popularin.services.ConvertPixel

class CrewAdapter(
    private val crewList: ArrayList<Crew>,
    private val clickListener: CrewAdapterClickListener
) : RecyclerView.Adapter<CrewAdapter.CrewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCrewBinding.inflate(layoutInflater, parent, false)
        return CrewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {
        // Posisi
        val currentItem = crewList[position]

        // Binding
        holder.binding.crew = currentItem

        // Margin
        val context = holder.binding.root.context
        val left = when (position == 0) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 6)
        }
        val right = when (position == itemCount - 1) {
            true -> ConvertPixel.getDensity(context, 16)
            false -> ConvertPixel.getDensity(context, 6)
        }
        val layoutParams = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = left
        layoutParams.marginEnd = right
        holder.binding.root.layoutParams = layoutParams
    }

    override fun getItemCount() = crewList.size

    inner class CrewViewHolder(val binding: ItemCrewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onCrewItemClick(adapterPosition)
            }
        }
    }
}