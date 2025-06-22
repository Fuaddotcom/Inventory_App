package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.adapters.InventoryAdapter
import com.TI23B1.inventoryapp.data.InventoryRepository // Make sure this is imported
import com.TI23B1.inventoryapp.data.InventoryStockRepository
import com.TI23B1.inventoryapp.models.InventoryStock

class InventoryListFragment : Fragment() {

    private lateinit var recycler_view_inventory: RecyclerView
    private lateinit var inventory_adapter: InventoryAdapter
    private val inventory_stock_repository = InventoryStockRepository()
    private val inventory_repository = InventoryRepository() // This is now correctly instantiated
    private var realtime_listener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        saved_instance_state: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory_list, container, false)

        recycler_view_inventory = view.findViewById(R.id.recycler_view_inventory)
        recycler_view_inventory.layoutManager = LinearLayoutManager(context)

        // --- FIX FOR LINE 45: Pass only `inventory_repository` and the click listeners ---
        inventory_adapter = InventoryAdapter(
            inventoryRepository = inventory_repository, // Pass the correct repository instance
            onItemClick = { item ->
                Toast.makeText(context, "Clicked on stock item: ${item.name} (ID: ${item.id})", Toast.LENGTH_SHORT).show()
            },
            onMoreOptionsClick = { item, menuItem ->
                when (menuItem.itemId) {
                    R.id.action_details -> {
                        Toast.makeText(context, "Details for stock item: ${item.name}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_delete -> {
                        Toast.makeText(context, "Delete stock item: ${item.name}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        )
        // --- END FIX ---

        recycler_view_inventory.adapter = inventory_adapter

        return view
    }

    override fun onViewCreated(view: View, saved_instance_state: Bundle?) {
        super.onViewCreated(view, saved_instance_state)
        fetchInventoryItems()
    }

    private fun fetchInventoryItems() {
        realtime_listener = inventory_stock_repository.getAllStockItemsRealtime { stockList ->
            inventory_adapter.submitList(stockList)
            Log.d("InventoryListFragment", "Inventory stock items updated: ${stockList.size}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realtime_listener?.let {
            inventory_stock_repository.removeStockRealtimeListener(it)
        }
    }
}