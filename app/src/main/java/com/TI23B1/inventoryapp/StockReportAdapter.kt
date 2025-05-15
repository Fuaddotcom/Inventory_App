package com.TI23B1.inventoryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Define constants for item types
private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_CURRENT_STOCK = 1
private const val VIEW_TYPE_HISTORY = 2

class StockReportAdapter(private val reportItems: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ViewHolder for Header (e.g., "Current Stock", "History")
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.textViewHeader)
    }

    // ViewHolder for Current Stock (reusing our previous layout)
    class CurrentStockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaBarangTextView: TextView = itemView.findViewById(R.id.textViewNamaBarang)
        val namaSupplierTextView: TextView = itemView.findViewById(R.id.textViewNamaSupplier)
        val quantityTextView: TextView = itemView.findViewById(R.id.textViewQuantity)
    }

    // ViewHolder for Stock Movement History
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaBarangTextView: TextView = itemView.findViewById(R.id.textViewNamaBarangHistory)
        val tanggalTextView: TextView = itemView.findViewById(R.id.textViewTanggalHistory)
        val jenisPergerakanTextView: TextView = itemView.findViewById(R.id.textViewJenisHistory)
        val quantityTextView: TextView = itemView.findViewById(R.id.textViewQuantityHistory)
    }

    override fun getItemViewType(position: Int): Int {
        return when (reportItems[position]) {
            is String -> VIEW_TYPE_HEADER
            is ItemSummary -> VIEW_TYPE_CURRENT_STOCK
            is StockMovement -> VIEW_TYPE_HISTORY
            else -> throw IllegalArgumentException("Unknown item type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val itemView = inflater.inflate(R.layout.item_header_layout, parent, false) // Create item_header_layout.xml
                HeaderViewHolder(itemView)
            }
            VIEW_TYPE_CURRENT_STOCK -> {
                val itemView = inflater.inflate(R.layout.item_summary_layout, parent, false) // Reusing existing layout
                CurrentStockViewHolder(itemView)
            }
            VIEW_TYPE_HISTORY -> {
                val itemView = inflater.inflate(R.layout.item_history_layout, parent, false)   // Create item_history_layout.xml
                HistoryViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = reportItems[position]
        when (holder) {
            is HeaderViewHolder -> {
                holder.headerTextView.text = item as String
            }
            is CurrentStockViewHolder -> {
                val currentStockItem = item as ItemSummary
                holder.namaBarangTextView.text = currentStockItem.namaBarang
                holder.namaSupplierTextView.text = currentStockItem.namaSupplier
                holder.quantityTextView.text = currentStockItem.totalQuantity.toString()
            }
            is HistoryViewHolder -> {
                val historyItem = item as StockMovement
                holder.namaBarangTextView.text = historyItem.namaBarang
                holder.tanggalTextView.text = historyItem.tanggal
                holder.jenisPergerakanTextView.text = historyItem.jenisPergerakan
                holder.quantityTextView.text = historyItem.quantity.toString()
            }
        }
    }

    override fun getItemCount() = reportItems.size
}