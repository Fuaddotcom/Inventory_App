package com.TI23B1.inventoryapp.adapters

import android.util.Log // Add for logging
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView // Import ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.data.InventoryRepository // Import InventoryRepository
import com.TI23B1.inventoryapp.models.HistoryListItem
import com.TI23B1.inventoryapp.models.RecentItem
import com.bumptech.glide.Glide // Import Glide
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    // Add InventoryRepository to the constructor
    private val inventoryRepository: InventoryRepository,
    private val onItemClick: (RecentItem) -> Unit,
    private val onMoreOptionsClick: (RecentItem, MenuItem) -> Unit
) : ListAdapter<HistoryListItem, RecyclerView.ViewHolder>(HistoryDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
        private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US) // Corrected "yyyy"
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
                // Pass inventoryRepository to HistoryItemViewHolder
                HistoryItemViewHolder(view, inventoryRepository)
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
    class HistoryItemViewHolder(itemView: View, private val inventoryRepository: InventoryRepository) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemType: TextView = itemView.findViewById(R.id.tv_item_type)
        private val tvItemStock: TextView = itemView.findViewById(R.id.tv_item_stock)
        private val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btn_more_options)
        private val ivItemImage: ImageView = itemView.findViewById(R.id.iv_item_image) // Initialize ImageView

        fun bind(item: RecentItem, onItemClick: (RecentItem) -> Unit, onMoreOptionsClick: (RecentItem, MenuItem) -> Unit) {
            tvItemName.text = item.name
            tvItemStock.text = "${item.stock} ${item.unit}"
            tvItemType.text = if (item.type == "IN") "Masuk" else "Keluar"

            when (item.type) {
                "IN" -> tvItemType.setBackgroundResource(R.drawable.bg_status_in)
                "OUT" -> tvItemType.setBackgroundResource(R.drawable.bg_status_out)
                else -> tvItemType.setBackgroundResource(android.R.color.darker_gray)
            }
            tvItemType.setTextColor(itemView.context.getColor(android.R.color.white))

            // --- GLIDE IMAGE LOADING LOGIC ---
            inventoryRepository.getInventoryItemByNamaBarang(item.name) { inventoryItem ->
                if (inventoryItem != null && !inventoryItem.image_url.isNullOrEmpty()) {
                    Log.d("HistoryAdapter", "Found InventoryItem for '${item.name}': ImageURL='${inventoryItem.image_url}'")
                    Glide.with(ivItemImage.context)
                        .load(inventoryItem.image_url)
                        .placeholder(R.drawable.ic_placeholder_foreground)
                        .error(R.drawable.ic_placeholder_foreground) // Show placeholder on error too
                        .into(ivItemImage)
                } else {
                    Log.w("HistoryAdapter", "No InventoryItem or empty imageUrl found for: ${item.name}. Showing placeholder.")
                    ivItemImage.setImageResource(R.drawable.ic_placeholder_foreground)
                }
            }
            // --- END GLIDE IMAGE LOADING LOGIC ---

            itemView.setOnClickListener { onItemClick(item) }

            btnMoreOptions.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.item_options_menu)
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
                oldItem.recentItem.cargoId == newItem.recentItem.cargoId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
        return oldItem == newItem
    }
}