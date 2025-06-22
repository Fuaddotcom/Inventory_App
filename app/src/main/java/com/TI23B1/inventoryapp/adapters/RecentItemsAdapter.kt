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
import com.TI23B1.inventoryapp.models.RecentItem
import com.bumptech.glide.Glide // Import Glide

class RecentItemsAdapter(
    // Add InventoryRepository to the constructor
    private val inventoryRepository: InventoryRepository,
    private val onMoreOptionsClick: (RecentItem, MenuItem) -> Unit
) : ListAdapter<RecentItem, RecentItemsAdapter.RecentItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent, parent, false)
        // Pass inventoryRepository to RecentItemViewHolder
        return RecentItemViewHolder(view, inventoryRepository)
    }

    override fun onBindViewHolder(holder: RecentItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class RecentItemViewHolder(itemView: View, private val inventoryRepository: InventoryRepository) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemStock: TextView = itemView.findViewById(R.id.tv_item_stock)
        private val tvItemType: TextView = itemView.findViewById(R.id.tv_item_type)
        private val btnMoreOptions: ImageButton = itemView.findViewById(R.id.btn_more_options)
        private val ivItemImage: ImageView = itemView.findViewById(R.id.iv_item_image) // Initialize ImageView

        fun bind(item: RecentItem) {
            tvItemName.text = item.name
            tvItemStock.text = "${item.stock} ${item.unit}"

            tvItemType.text = when (item.type) {
                "IN" -> "Masuk"
                "OUT" -> "Keluar"
                else -> "Unknown"
            }

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

            // --- GLIDE IMAGE LOADING LOGIC ---
            inventoryRepository.getInventoryItemByNamaBarang(item.name) { inventoryItem ->
                if (inventoryItem != null && !inventoryItem.image_url.isNullOrEmpty()) {
                    Log.d("RecentItemsAdapter", "Found InventoryItem for '${item.name}': ImageURL='${inventoryItem.image_url}'")
                    Glide.with(ivItemImage.context)
                        .load(inventoryItem.image_url)
                        .placeholder(R.drawable.ic_placeholder_foreground)
                        .error(R.drawable.ic_placeholder_foreground) // Show placeholder on error too
                        .into(ivItemImage)
                } else {
                    Log.w("RecentItemsAdapter", "No InventoryItem or empty imageUrl found for: ${item.name}. Showing placeholder.")
                    ivItemImage.setImageResource(R.drawable.ic_placeholder_foreground)
                }
            }
            // --- END GLIDE IMAGE LOADING LOGIC ---

            btnMoreOptions.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.item_options_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    onMoreOptionsClick(item, menuItem)
                    true
                }
                popupMenu.show()
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