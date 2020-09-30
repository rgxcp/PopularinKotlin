package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemPointBinding
import xyz.fairportstudios.popularin.interfaces.PointAdapterClickListener
import xyz.fairportstudios.popularin.models.Point

class PointAdapter(
    private val pointList: ArrayList<Point>,
    private val clickListener: PointAdapterClickListener
) : RecyclerView.Adapter<PointAdapter.PointViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPointBinding.inflate(layoutInflater, parent, false)
        return PointViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        // Posisi
        val currentItem = pointList[position]

        // Binding
        holder.binding.point = currentItem
    }

    override fun getItemCount() = pointList.size

    inner class PointViewHolder(val binding: ItemPointBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onPointItemClick(adapterPosition)
            }
        }
    }
}