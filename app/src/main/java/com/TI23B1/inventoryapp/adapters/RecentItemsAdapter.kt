package com.TI23B1.inventoryapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.models.RecentItem

class RecentItemsAdapter(
    private val onItemClick: (RecentItem) -> Unit
) : ListAdapter<RecentItem, RecentItemsAdapter.RecentItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent, parent, false)
        return RecentItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class RecentItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemStock: TextView = itemView.findViewById(R.id.tv_item_stock)
        private val tvItemDate: TextView = itemView.findViewById(R.id.tv_item_date)
        private val tvItemType: TextView = itemView.findViewById(R.id.tv_item_type)
        private val tvItemSupplier: TextView = itemView.findViewById(R.id.tv_item_supplier)

        fun bind(item: RecentItem) {
            tvItemName.text = item.name
            tvItemStock.text = "${item.stock} ${item.unit}"

            // Show date if available, otherwise show "No date"
            tvItemDate.text = item.date ?: "No date"

            // Show type with color coding
            tvItemType.text = when (item.type) {
                "IN" -> "Masuk"
                "OUT" -> "Keluar"
                else -> "Unknown"
            }

            // Set type background color
            when (item.type) {
                "IN" -> {
                    tvItemType.setBackgroundResource(R.drawable.bg_status_in)
                    tvItemType.setTextColor(itemView.context.getColor(android.R.color.white))
                }
                "OUT" -> {
                    tvItemType.setBackgroundResource(R.drawable.bg_status_out)
                    tvItemType.setTextColor(itemView.context.getColor(android.R.color.white))
                }
                else -> {
                    tvItemType.setBackgroundResource(R.drawable.bg_status_default)
                    tvItemType.setTextColor(itemView.context.getColor(android.R.color.black))
                }
            }

            // Show supplier only for incoming items
            if (item.type == "IN" && item.supplier.isNotEmpty()) {
                tvItemSupplier.visibility = View.VISIBLE
                tvItemSupplier.text = "Supplier: ${item.supplier}"
            } else {
                tvItemSupplier.visibility = View.GONE
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RecentItem>() {
        override fun areItemsTheSame(oldItem: RecentItem, newItem: RecentItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentItem, newItem: RecentItem): Boolean {
            return oldItem == newItem
        }
    }
}