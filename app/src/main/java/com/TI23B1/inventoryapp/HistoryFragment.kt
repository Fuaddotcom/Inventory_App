package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.adapters.HistoryAdapter
import com.TI23B1.inventoryapp.models.HistoryListItem
import com.TI23B1.inventoryapp.models.RecentItem
import com.TI23B1.inventoryapp.CargoIn
import com.TI23B1.inventoryapp.CargoOut
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var etSearchItemName: TextInputEditText
    private lateinit var rgItemTypeFilter: RadioGroup
    private lateinit var rbFilterAll: RadioButton
    private lateinit var rbFilterIn: RadioButton
    private lateinit var rbFilterOut: RadioButton
    private lateinit var rvHistoryItems: RecyclerView

    private var pendingFirebaseFetches: Int = 0
    private lateinit var btnResetFilter: Button

    private lateinit var historyItemsAdapter: HistoryAdapter
    private var masterHistoryList: MutableList<RecentItem> = mutableListOf()
    private var currentFilteredAndHeaderList: MutableList<HistoryListItem> = mutableListOf()

    private val database = FirebaseDatabase.getInstance()
    private val barangMasukRef = database.getReference("barang_masuk")
    private val barangKeluarRef = database.getReference("barang_keluar")

    private val TAG = "HistoryFragment"
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupListeners() // Ensure this is called
        loadAllHistoryItems()
    }

    private fun setupViews(view: View) {
        etSearchItemName = view.findViewById(R.id.et_search_item_name)
        rgItemTypeFilter = view.findViewById(R.id.rg_item_type_filter)
        rbFilterAll = view.findViewById(R.id.rb_filter_all)
        rbFilterIn = view.findViewById(R.id.rb_filter_in)
        rbFilterOut = view.findViewById(R.id.rb_filter_out)
        rvHistoryItems = view.findViewById(R.id.rv_history_items)
        btnResetFilter = view.findViewById(R.id.btn_reset_filter)
    }

    private fun setupRecyclerView() {
        historyItemsAdapter = HistoryAdapter(
            onItemClick = { item: RecentItem ->
                Toast.makeText(context, "History item clicked: ${item.name}", Toast.LENGTH_SHORT).show()
            },
            onMoreOptionsClick = { item: RecentItem, menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_details -> {
                        Toast.makeText(context, "Details for: ${item.name}", Toast.LENGTH_SHORT).show()
                    }
                    R.id.action_delete -> {
                        Toast.makeText(context, "Delete: ${item.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        rvHistoryItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyItemsAdapter
        }
    }

    private fun setupListeners() {
        etSearchItemName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFiltersAndAddHeaders()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        rgItemTypeFilter.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, "RadioGroup checked change detected. Checked ID: $checkedId")
            applyFiltersAndAddHeaders()
        }

        btnResetFilter.setOnClickListener {
            Log.d(TAG, "Reset Filter button clicked.")
            resetFilters()
        }
    }

    private fun resetFilters() {
        etSearchItemName.setText("") // Clear the search text
        rbFilterAll.isChecked = true // Select the "All" radio button
        // Calling applyFiltersAndAddHeaders() is handled by the listeners above,
        // as clearing text and checking a radio button will trigger them.
        // However, for explicit control, you can call it directly:
        applyFiltersAndAddHeaders()
        Toast.makeText(context, "Filters reset!", Toast.LENGTH_SHORT).show()
    }

    private fun loadAllHistoryItems() {
        masterHistoryList.clear() // Clear master list at the very beginning of a full load
        currentFilteredAndHeaderList.clear() // Also clear the displayed list
        historyItemsAdapter.submitList(emptyList()) // Clear adapter immediately to show nothing while loading
        Log.d(TAG, "Starting to load all history items from Firebase...")

        // Reset fetch counter
        pendingFirebaseFetches = 2 // We have two fetches (barang_masuk and barang_keluar)

        // Fetch barang_masuk items
        barangMasukRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "barang_masuk snapshot exists: ${snapshot.exists()}, children: ${snapshot.childrenCount}")
                snapshot.children.forEach { dataSnapshot ->
                    val cargoIn = dataSnapshot.getValue(CargoIn::class.java)
                    cargoIn?.let {
                        val recentItem = RecentItem.fromCargoIn(it)
                        masterHistoryList.add(recentItem)
                        Log.d(TAG, "Added IN item to master list: ${recentItem.name} (${recentItem.timestamp})")
                    } ?: run {
                        Log.e(TAG, "Failed to parse CargoIn for key: ${dataSnapshot.key}, value: ${dataSnapshot.value}")
                    }
                }
                Log.d(TAG, "Finished processing barang_masuk. Master list size: ${masterHistoryList.size}")
                onFirebaseFetchComplete() // Call completion handler
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read barang_masuk history: ${error.message}", error.toException())
                Toast.makeText(context, "Failed to load incoming history.", Toast.LENGTH_SHORT).show()
                onFirebaseFetchComplete() // Call completion handler even on error
            }
        })

        // Fetch barang_keluar items
        barangKeluarRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "barang_keluar snapshot exists: ${snapshot.exists()}, children: ${snapshot.childrenCount}")
                snapshot.children.forEach { dataSnapshot ->
                    val cargoOut = dataSnapshot.getValue(CargoOut::class.java)
                    cargoOut?.let {
                        val recentItem = RecentItem.fromCargoOut(it)
                        masterHistoryList.add(recentItem)
                        Log.d(TAG, "Added OUT item to master list: ${recentItem.name} (${recentItem.timestamp})")
                    } ?: run {
                        Log.e(TAG, "Failed to parse CargoOut for key: ${dataSnapshot.key}, value: ${dataSnapshot.value}")
                    }
                }
                Log.d(TAG, "Finished processing barang_keluar. Master list size: ${masterHistoryList.size}")
                onFirebaseFetchComplete() // Call completion handler
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read barang_keluar history: ${error.message}", error.toException())
                Toast.makeText(context, "Failed to load outgoing history.", Toast.LENGTH_SHORT).show()
                onFirebaseFetchComplete() // Call completion handler even on error
            }
        })
    }

    // New helper to ensure filters are applied only after ALL initial data is loaded
    private fun onFirebaseFetchComplete() {
        pendingFirebaseFetches--
        if (pendingFirebaseFetches <= 0) {
            Log.d(TAG, "All Firebase fetches complete. Final master list size: ${masterHistoryList.size}")
            applyFiltersAndAddHeaders() // Now apply filters to the fully loaded master list
        }
    }

    private fun applyFiltersAndAddHeaders() {
        val searchTerm = etSearchItemName.text.toString().trim()
        val selectedFilterType = when (rgItemTypeFilter.checkedRadioButtonId) {
            R.id.rb_filter_in -> {
                Log.d(TAG, "Selected filter: IN")
                "IN"
            }
            R.id.rb_filter_out -> {
                Log.d(TAG, "Selected filter: OUT")
                "OUT"
            }
            R.id.rb_filter_all -> {
                Log.d(TAG, "Selected filter: ALL")
                "ALL"
            }
            else -> {
                Log.w(TAG, "Unexpected checked radio button ID: ${rgItemTypeFilter.checkedRadioButtonId}, defaulting to ALL")
                "ALL"
            }
        }

        Log.d(TAG, "Applying filters: SearchTerm='${searchTerm}', Type='${selectedFilterType}'. Master list size: ${masterHistoryList.size}")

        val filteredItems = masterHistoryList.filter { item ->
            val matchesName = if (searchTerm.isBlank()) {
                true
            } else {
                item.name.contains(searchTerm, ignoreCase = true)
            }

            val matchesType = when (selectedFilterType) {
                "ALL" -> true
                "IN" -> {
                    val isIncoming = item.type == "IN"
                    Log.d(TAG, "Item ${item.name} type: ${item.type}, Matches IN: $isIncoming")
                    isIncoming
                }
                "OUT" -> {
                    val isOutgoing = item.type == "OUT"
                    Log.d(TAG, "Item ${item.name} type: ${item.type}, Matches OUT: $isOutgoing")
                    isOutgoing
                }
                else -> true
            }
            matchesName && matchesType
        }.sortedByDescending { it.timestamp }

        // Clear the list that goes to the adapter BEFORE populating it
        currentFilteredAndHeaderList.clear()
        var lastDate: String? = null

        if (filteredItems.isEmpty()) {
            Log.d(TAG, "Filtered items list is empty. No items to display.")
        }

        filteredItems.forEach { item ->
            val itemDate = displayDateFormat.format(Date(item.timestamp))
            if (itemDate != lastDate) {
                currentFilteredAndHeaderList.add(HistoryListItem.DateHeader(itemDate))
                lastDate = itemDate
                Log.d(TAG, "Added date header: $itemDate")
            }
            currentFilteredAndHeaderList.add(HistoryListItem.HistoryItem(item))
            Log.d(TAG, "Added item to displayed list: ${item.name} (${item.type})")
        }

        historyItemsAdapter.submitList(currentFilteredAndHeaderList.toList()) // Convert to immutable list for submitList
        Log.d(TAG, "Filtered list with headers submitted. Displaying ${currentFilteredAndHeaderList.size} items (including headers).")
    }
}