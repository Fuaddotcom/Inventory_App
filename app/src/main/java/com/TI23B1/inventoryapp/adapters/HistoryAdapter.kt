// com/TI23B1/inventoryapp/adapters/HistoryAdapter.kt
package com.TI23B1.inventoryapp.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // Changed from View to ImageButton for clarity
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.models.HistoryListItem
import com.TI23B1.inventoryapp.models.RecentItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onItemClick: (RecentItem) -> Unit,
    private val onMoreOptionsClick: (RecentItem, MenuItem) -> Unit
) : ListAdapter<HistoryListItem, RecyclerView.ViewHolder>(HistoryDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
        private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryListItem.DateHeader -> VIEW_TYPE_HEADER
            is HistoryListItem.HistoryItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_recent, parent, false)
                HistoryItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> {
                val header = getItem(position) as HistoryListItem.DateHeader
                holder.bind(header.date)
            }
            is HistoryItemViewHolder -> {
                val historyItem = getItem(position) as HistoryListItem.HistoryItem
                holder.bind(historyItem.recentItem, onItemClick, onMoreOptionsClick)
            }
        }
    }

    // ViewHolder for Date Header (item_date_header.xml)
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateHeader: TextView = itemView.findViewById(R.id.tv_date_header)

        fun bind(date: String) {
            tvDateHeader.text = date
        }
    }

    // ViewHolder for History Item (item_recent.xml)
    class HistoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemType: TextView = itemView.findViewById(R.id.tv_item_type) // This is your type TextView
        private val tvItemStock: TextView = itemView.findViewById(R.id.tv_item_stock) // This is your stock TextView
        private val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btn_more_options) // This is your ImageButton for options

        fun bind(item: RecentItem, onItemClick: (RecentItem) -> Unit, onMoreOptionsClick: (RecentItem, MenuItem) -> Unit) {
            tvItemName.text = item.name
            // For stock, you're displaying "quantity unit", so we'll combine them here
            tvItemStock.text = "${item.stock} ${item.unit}"
            tvItemType.text = if (item.type == "IN") "Masuk" else "Keluar" // Display "Masuk" or "Keluar"

            // Set color for IN/OUT type using your existing drawables if available
            // You have bg_status_in and presumably bg_status_out
            when (item.type) {
                "IN" -> tvItemType.setBackgroundResource(R.drawable.bg_status_in)
                "OUT" -> tvItemType.setBackgroundResource(R.drawable.bg_status_out) // Assuming you have bg_status_out
                else -> tvItemType.setBackgroundResource(android.R.color.darker_gray) // Fallback
            }
            // Ensure text color is set if your drawables don't define it
            tvItemType.setTextColor(itemView.context.getColor(android.R.color.white))


            itemView.setOnClickListener { onItemClick(item) }

            btnMoreOptions.setOnClickListener { view -> // Use btnMoreOptions
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.item_options_menu) // Use your existing menu
                    setOnMenuItemClickListener { menuItem ->
                        onMoreOptionsClick(item, menuItem)
                        true
                    }
                }.show()
            }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryListItem>() {
    override fun areItemsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
        return when {
            oldItem is HistoryListItem.DateHeader && newItem is HistoryListItem.DateHeader ->
                oldItem.date == newItem.date
            oldItem is HistoryListItem.HistoryItem && newItem is HistoryListItem.HistoryItem ->
                // Using cargoId as the stable ID for HistoryItem
                oldItem.recentItem.cargoId == newItem.recentItem.cargoId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
        return oldItem == newItem // Data classes automatically provide equals() based on content
    }
}