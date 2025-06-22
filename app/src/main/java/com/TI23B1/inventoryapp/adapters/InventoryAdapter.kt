package com.TI23B1.inventoryapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // Keep if used in item_recent.xml for btnMoreOptions
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.models.InventoryStock // The type for the list
import com.bumptech.glide.Glide
import com.TI23B1.inventoryapp.data.InventoryRepository // For fetching item details/images
import com.TI23B1.inventoryapp.databinding.ItemRecentBinding // Assuming this is your item layout binding

class InventoryAdapter(
    // --- THIS IS THE CORRECTED CONSTRUCTOR FOR InventoryAdapter ---
    private val inventoryRepository: InventoryRepository, // Only this repository is passed here
    private val onItemClick: (InventoryStock) -> Unit,
    private val onMoreOptionsClick: ((InventoryStock, MenuItem) -> Unit)? = null
) : ListAdapter<InventoryStock, InventoryAdapter.InventoryItemViewHolder>(InventoryDiffCallback()) { // ListAdapter is of type InventoryStock

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {
        val binding = ItemRecentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Pass the binding and the inventoryRepository to the ViewHolder
        return InventoryItemViewHolder(binding, inventoryRepository)
    }

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        val item = getItem(position) // getItem() returns InventoryStock
        holder.bind(item, onItemClick, onMoreOptionsClick)
    }

    class InventoryItemViewHolder(
        private val binding: ItemRecentBinding,
        // The ViewHolder needs the InventoryRepository to fetch image URLs
        private val inventoryRepository: InventoryRepository
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryStock, onItemClick: (InventoryStock) -> Unit, onMoreOptionsClick: ((InventoryStock, MenuItem) -> Unit)?) {
            // 'item' here is an InventoryStock object, which has 'id' and 'name'
            Log.d("InventoryDebug", "Binding item: $item")
            Log.d("InventoryDebug", "InventoryStock name: '${item.name}' (ID: ${item.id})")

            binding.tvItemName.text = item.name
            binding.tvItemStock.text = "Stok Tersedia: ${item.stokTersedia}"

            // Status display logic
            binding.tvItemType.visibility = View.VISIBLE
            if (item.stokTersedia < 20) {
                binding.tvItemType.text = "Low Stock"
                binding.tvItemType.setBackgroundResource(R.drawable.bg_status_out)
            } else {
                binding.tvItemType.text = "Available"
                binding.tvItemType.setBackgroundResource(R.drawable.bg_status_in)
            }
            binding.tvItemType.setTextColor(itemView.context.getColor(android.R.color.white))

            // Fetch and load image from InventoryRepository using item.name (from InventoryStock)
            inventoryRepository.getInventoryItemByNamaBarang(item.name) { inventoryItem ->
                if (inventoryItem != null && !inventoryItem.image_url.isNullOrEmpty()) {
                    Log.d("InventoryDebug", "Found InventoryItem for '${item.name}': ID=${inventoryItem.id}, ImageURL='${inventoryItem.image_url}'")
                    Glide.with(binding.ivItemImage.context)
                        .load(inventoryItem.image_url) // Correct: use the instance property
                        .placeholder(R.drawable.ic_placeholder_foreground)
                        .into(binding.ivItemImage)
                } else {
                    Log.w("InventoryDebug", "No InventoryItem or empty imageUrl found for: ${item.name}. Showing placeholder.")
                    binding.ivItemImage.setImageResource(R.drawable.ic_placeholder_foreground)
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }

            if (onMoreOptionsClick != null) {
                binding.btnMoreOptions.visibility = View.VISIBLE
                binding.btnMoreOptions.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        inflate(R.menu.item_options_menu)
                        setOnMenuItemClickListener { menuItem ->
                            onMoreOptionsClick.invoke(item, menuItem)
                            true
                        }
                    }.show()
                }
            } else {
                binding.btnMoreOptions.visibility = View.GONE
            }
        }
    }
}

class InventoryDiffCallback : DiffUtil.ItemCallback<InventoryStock>() { // DiffUtil for InventoryStock
    override fun areItemsTheSame(oldItem: InventoryStock, newItem: InventoryStock): Boolean {
        return oldItem.id == newItem.id // Compare by ID from InventoryStock
    }

    override fun areContentsTheSame(oldItem: InventoryStock, newItem: InventoryStock): Boolean {
        return oldItem == newItem
    }
}