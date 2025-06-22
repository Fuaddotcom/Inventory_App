package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.adapters.RecentItemsAdapter
import com.TI23B1.inventoryapp.models.RecentItem
import com.TI23B1.inventoryapp.UserControl
import com.TI23B1.inventoryapp.data.CargoInRepository
import com.TI23B1.inventoryapp.data.CargoOutRepository
import com.TI23B1.inventoryapp.data.InventoryRepository
import com.TI23B1.inventoryapp.data.InventoryStockRepository
import com.TI23B1.inventoryapp.dialogs.AddInventoryDialog
import com.TI23B1.inventoryapp.data.HistoryRepository
import com.TI23B1.inventoryapp.models.CargoIn
import com.TI23B1.inventoryapp.models.CargoOut
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ValueEventListener
import java.util.*


class HomeFragment : Fragment(), AddInventoryDialog.OnSaveListener {

    private lateinit var tvGreeting: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBarangMasukCount: TextView
    private lateinit var tvBarangKeluarCount: TextView
    private lateinit var rvRecentItems: RecyclerView
    private lateinit var ivProfile: ImageView
    private lateinit var cardBarangMasuk: CardView
    private lateinit var cardBarangKeluar: CardView

    private lateinit var fabAdd: FloatingActionButton

    private val inventoryRepository = InventoryRepository()
    private val cargoInRepository = CargoInRepository()
    private val cargoOutRepository = CargoOutRepository()
    private val historyRepository = HistoryRepository()
    private val inventoryStockRepository = InventoryStockRepository()

    private lateinit var recentItemsAdapter: RecentItemsAdapter

    private var userName: String? = null
    private var userEmail: String? = null
    private var userPhotoUrl: String? = null

    private var barangMasukCountListener: ValueEventListener? = null
    private var barangKeluarCountListener: ValueEventListener? = null

    private lateinit var userControl: UserControl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userName = it.getString("user_name")
            userEmail = it.getString("user_email")
            userPhotoUrl = it.getString("user_photo_url")
        }

        setupViews(view)
        setupRecyclerView()

        userControl = UserControl()

        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        barangMasukCountListener?.let { historyRepository.removeBarangMasukCountListener(it) }
        barangKeluarCountListener?.let { historyRepository.removeBarangKeluarCountListener(it) }
        Log.d("HomeFragment", "Firebase listeners removed in onDestroyView.")
    }

    private fun setupViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvUsername = view.findViewById(R.id.tv_username)
        tvBarangMasukCount = view.findViewById(R.id.tv_barang_masuk_count)
        tvBarangKeluarCount = view.findViewById(R.id.tv_barang_keluar_count)
        rvRecentItems = view.findViewById(R.id.rv_recent_items)
        ivProfile = view.findViewById(R.id.iv_profile)

        cardBarangMasuk = view.findViewById(R.id.card_barang_masuk)
        cardBarangKeluar = view.findViewById(R.id.card_barang_keluar)

        fabAdd = activity?.findViewById(R.id.fab_add) ?: throw IllegalStateException(
            "Floating Action Button with ID fab_add not found in activity layout. " +
                    "Ensure it's in your activity_main.xml or equivalent host activity layout."
        )

        fabAdd.setOnClickListener {
            Log.d("HomeFragment", "FAB Add clicked. Showing AddInventoryDialog.")
            showAddInventoryDialog()
        }
    }

    private fun setupRecyclerView() {
        recentItemsAdapter = RecentItemsAdapter(
            inventoryRepository = inventoryRepository,
            onMoreOptionsClick = { item: RecentItem, menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_details -> {
                        Toast.makeText(context, "Details for: ${item.name}", Toast.LENGTH_SHORT).show()
                        Log.d("HomeFragment", "Details action clicked for: ${item.name}")
                    }
                    R.id.action_delete -> {
                        Toast.makeText(context, "Delete: ${item.name}", Toast.LENGTH_SHORT).show()
                        Log.d("HomeFragment", "Delete action clicked for: ${item.name}")
                    }
                }
            }
        )

        rvRecentItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentItemsAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
        }
    }

    private fun loadData() {
        tvGreeting.text = getTimeBasedGreeting()

        val firebaseUser = userControl.getCurrentUser()

        if (firebaseUser != null) {
            userControl.readUserData(firebaseUser.uid) { userFromDb ->
                if (userFromDb != null) {
                    userName = userFromDb.username
                    tvUsername.text = userName
                    Log.d("HomeFragment", "User data loaded: Username=${userName}")
                } else {
                    tvUsername.text = "Pengguna Tidak Ditemukan"
                    Log.w("HomeFragment", "User data not found in DB for UID: ${firebaseUser.uid}")
                }
            }
        } else {
            tvUsername.text = "Silakan Masuk"
            Log.d("HomeFragment", "No Firebase user currently logged in.")
        }

        updateDashboardCounts()
        loadRecentItemsFromFirebase()
    }

    private fun updateDashboardCounts() {
        barangMasukCountListener = historyRepository.listenToBarangMasukCount(object : HistoryRepository.CountUpdateListener {
            override fun onCountUpdated(count: Int) {
                tvBarangMasukCount.text = count.toString()
                Log.d("HomeFragment", "Barang Masuk Count Updated: $count")
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error updating Barang Masuk count: $errorMessage")
            }
        })

        barangKeluarCountListener = historyRepository.listenToBarangKeluarCount(object : HistoryRepository.CountUpdateListener {
            override fun onCountUpdated(count: Int) {
                tvBarangKeluarCount.text = count.toString()
                Log.d("HomeFragment", "Barang Keluar Count Updated: $count")
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error updating Barang Keluar count: $errorMessage")
            }
        })
    }

    private fun loadRecentItemsFromFirebase() {
        historyRepository.fetchRecentItems(object : HistoryRepository.RecentItemsFetchListener {
            override fun onRecentItemsFetched(items: List<RecentItem>) {
                recentItemsAdapter.submitList(items)
                Log.d("HomeFragment", "Fetched ${items.size} recent items.")
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(context, "Error loading recent items: $errorMessage", Toast.LENGTH_SHORT).show()
                recentItemsAdapter.submitList(emptyList())
                Log.e("HomeFragment", "Error fetching recent items: ${errorMessage}")
            }
        })
    }

    private fun getTimeBasedGreeting(): String {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "Selamat Pagi,"
            in 12..14 -> "Selamat Siang,"
            in 15..18 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
    }

    private fun showAddInventoryDialog() {
        val dialog = AddInventoryDialog.newInstance(
            inventoryRepository,
            inventoryStockRepository
        )
        dialog.setOnSaveListener(this)
        dialog.show(childFragmentManager, "AddInventoryDialog")
        Log.d("HomeFragment", "AddInventoryDialog shown from FAB.")
    }

    override fun onSaveCargoIn(cargoIn: CargoIn) {
        Log.d("HomeFragment", "onSaveCargoIn triggered for item: ${cargoIn.namaBarang}, qty: ${cargoIn.quantity}")
        cargoInRepository.addCargoIn(cargoIn) { saveSuccess, saveErrorMessage ->
            if (saveSuccess) {
                Toast.makeText(context, "Cargo In saved and stock updated!", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "Cargo In record saved successfully. Stock update handled by repository.")
                loadData() // Refresh counts and recent items for HomeFragment
                // NEW: Notify HistoryFragment to refresh
                (parentFragmentManager.findFragmentByTag("HistoryFragmentTag") as? HistoryFragment)?.refreshData()
                Log.d("HomeFragment", "Attempted to notify HistoryFragment to refresh.")
            } else {
                Toast.makeText(context, "Error saving Cargo In record: ${saveErrorMessage}", Toast.LENGTH_LONG).show()
                Log.e("HomeFragment", "Cargo In record save FAILED: ${saveErrorMessage}")
            }
        }
    }

    override fun onSaveCargoOut(cargoOut: CargoOut) {
        Log.d("HomeFragment", "onSaveCargoOut triggered for item: ${cargoOut.namaBarang}, qty: ${cargoOut.quantity}")
        cargoOutRepository.addCargoOut(cargoOut) { saveSuccess, saveErrorMessage ->
            if (saveSuccess) {
                Toast.makeText(context, "Cargo Out saved and stock updated!", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "Cargo Out record saved successfully. Stock update handled by repository.")
                loadData() // Refresh counts and recent items for HomeFragment
                // NEW: Notify HistoryFragment to refresh
                (parentFragmentManager.findFragmentByTag("HistoryFragmentTag") as? HistoryFragment)?.refreshData()
                Log.d("HomeFragment", "Attempted to notify HistoryFragment to refresh.")
                onCargoOutSavedAndReadyForPrint(cargoOut)
            } else {
                Toast.makeText(context, "Error saving Cargo Out record: ${saveErrorMessage}", Toast.LENGTH_LONG).show()
                Log.e("HomeFragment", "Cargo Out record save FAILED: ${saveErrorMessage}")
            }
        }
    }

    override fun onCargoOutSavedAndReadyForPrint(cargoOut: CargoOut) {
        Toast.makeText(context, "Cargo Out for ${cargoOut.namaBarang} is ready for printing. SJ: ${cargoOut.nomorSuratJalan}", Toast.LENGTH_LONG).show()
        Log.d("HomeFragment", "Cargo Out ready for print callback triggered for: ${cargoOut.namaBarang}")
    }
}