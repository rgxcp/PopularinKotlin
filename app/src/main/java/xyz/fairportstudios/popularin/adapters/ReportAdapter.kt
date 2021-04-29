package xyz.fairportstudios.popularin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.fairportstudios.popularin.databinding.ItemReportBinding
import xyz.fairportstudios.popularin.interfaces.ReportAdapterClickListener
import xyz.fairportstudios.popularin.models.Report

class ReportAdapter(
    private val reports: List<Report>,
    private val clickListener: ReportAdapterClickListener
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemReportBinding.inflate(layoutInflater, parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        // Binding
        holder.binding.report = reports[position]
    }

    override fun getItemCount() = reports.size

    inner class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.onReportItemClick(adapterPosition)
            }
        }
    }
}