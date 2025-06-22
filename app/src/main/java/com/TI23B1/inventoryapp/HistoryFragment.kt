package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.adapters.HistoryAdapter
import com.TI23B1.inventoryapp.data.CargoInRepository
import com.TI23B1.inventoryapp.data.CargoOutRepository
import com.TI23B1.inventoryapp.models.HistoryListItem
import com.TI23B1.inventoryapp.models.RecentItem
import com.TI23B1.inventoryapp.data.HistoryRepository
import com.TI23B1.inventoryapp.data.InventoryRepository
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var etSearchItemName: TextInputEditText
    private lateinit var rgItemTypeFilter: RadioGroup
    private lateinit var rbFilterAll: RadioButton
    private lateinit var rbFilterIn: RadioButton
    private lateinit var rbFilterOut: RadioButton
    private lateinit var rvHistoryItems: RecyclerView
    private lateinit var btnResetFilter: Button

    private lateinit var historyItemsAdapter: HistoryAdapter
    private var masterHistoryList: MutableList<RecentItem> = mutableListOf()
    private var currentFilteredAndHeaderList: MutableList<HistoryListItem> = mutableListOf()

    private val historyRepository = HistoryRepository()
    private val inventoryRepository = InventoryRepository()

    private val TAG = "HistoryFragment"

    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

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
        setupListeners()
        loadAllHistoryItems()
    }

    // NEW: Public method to trigger data refresh
    fun refreshData() {
        Log.d(TAG, "refreshData called. Reloading all history items.")
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
            inventoryRepository = inventoryRepository,
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
                        // If you want to enable delete, you'll need a delete method in HistoryRepository
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
        etSearchItemName.setText("")
        rbFilterAll.isChecked = true
        applyFiltersAndAddHeaders()
        Toast.makeText(context, "Filters reset!", Toast.LENGTH_SHORT).show()
    }

    private fun loadAllHistoryItems() {
        masterHistoryList.clear()
        currentFilteredAndHeaderList.clear()
        historyItemsAdapter.submitList(emptyList())
        Log.d(TAG, "Starting to load all history items using HistoryRepository...")

        historyRepository.fetchRecentItems(object : HistoryRepository.RecentItemsFetchListener {
            override fun onRecentItemsFetched(items: List<RecentItem>) {
                Log.d(TAG, "Recent items fetched from repository. Count: ${items.size}")
                masterHistoryList.clear()
                masterHistoryList.addAll(items)
                applyFiltersAndAddHeaders()
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG, "Error fetching history items: $errorMessage")
                Toast.makeText(context, "Error loading history: $errorMessage", Toast.LENGTH_LONG).show()
            }
        })
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
                "IN" -> item.type == "IN"
                "OUT" -> item.type == "OUT"
                else -> true
            }
            matchesName && matchesType
        }.sortedByDescending { it.timestamp }

        currentFilteredAndHeaderList.clear()
        var lastDate: String? = null

        if (filteredItems.isEmpty()) {
            Log.d(TAG, "Filtered items list is empty. No items to display.")
        }

        filteredItems.forEach { item ->
            val itemDate = displayDateFormat.format(Date(item.timestamp ?: 0L))

            if (itemDate != lastDate) {
                currentFilteredAndHeaderList.add(HistoryListItem.DateHeader(itemDate))
                lastDate = itemDate
                Log.d(TAG, "Added date header: $itemDate")
            }
            currentFilteredAndHeaderList.add(HistoryListItem.HistoryItem(item))
            Log.d(TAG, "Added item to displayed list: ${item.name} (${item.type})")
        }

        historyItemsAdapter.submitList(currentFilteredAndHeaderList.toList())
        Log.d(TAG, "Filtered list with headers submitted. Displaying ${currentFilteredAndHeaderList.size} items (including headers).")
    }

}