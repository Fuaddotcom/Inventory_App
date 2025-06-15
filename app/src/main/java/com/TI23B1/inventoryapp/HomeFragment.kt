package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.adapters.RecentItemsAdapter
import com.TI23B1.inventoryapp.models.RecentItem

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBarangMasukCount: TextView
    private lateinit var tvBarangKeluarCount: TextView
    private lateinit var cardBarangMasuk: CardView
    private lateinit var cardBarangKeluar: CardView
    private lateinit var rvRecentItems: RecyclerView
    private lateinit var ivProfile: ImageView

    private lateinit var recentItemsAdapter: RecentItemsAdapter

    // User data
    private var userName: String? = null
    private var userEmail: String? = null
    private var userPhotoUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user data from arguments
        arguments?.let {
            userName = it.getString("user_name")
            userEmail = it.getString("user_email")
            userPhotoUrl = it.getString("user_photo_url")
        }

        setupViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadData()
    }

    private fun setupViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvUsername = view.findViewById(R.id.tv_username)
        tvBarangMasukCount = view.findViewById(R.id.tv_barang_masuk_count)
        tvBarangKeluarCount = view.findViewById(R.id.tv_barang_keluar_count)
        cardBarangMasuk = view.findViewById(R.id.card_barang_masuk)
        cardBarangKeluar = view.findViewById(R.id.card_barang_keluar)
        rvRecentItems = view.findViewById(R.id.rv_recent_items)
        ivProfile = view.findViewById(R.id.iv_profile)
    }

    private fun setupRecyclerView() {
        recentItemsAdapter = RecentItemsAdapter { item ->
            onRecentItemClick(item)
        }

        rvRecentItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentItemsAdapter
            // Important: Disable nested scrolling since we're inside a ScrollView
            isNestedScrollingEnabled = false
            // Optional: Set fixed size for better performance
            setHasFixedSize(false) // Set to false since items might have different heights
        }
    }

    private fun setupClickListeners() {
        cardBarangMasuk.setOnClickListener {
            // Navigate to incoming goods screen
            // findNavController().navigate(R.id.action_to_incoming_goods)
        }

        cardBarangKeluar.setOnClickListener {
            // Navigate to outgoing goods screen
            // findNavController().navigate(R.id.action_to_outgoing_goods)
        }
    }

    private fun loadData() {
        // Set dynamic greeting based on time
        tvGreeting.text = getTimeBasedGreeting()

        // Set username from arguments or default
        tvUsername.text = userName ?: "Yovan Julian"

        // In real app, load these counts from Firebase/Database
        tvBarangMasukCount.text = "287"
        tvBarangKeluarCount.text = "110"

        // Load recent items - limit to 5-10 items for better performance in ScrollView
        val recentItems = listOf(
            RecentItem(
                id = 1,
                name = "Axioo NoteBook Hype 1",
                stock = 15,
                date = "Today",
                type = "IN",
                supplier = "PT. Axioo Indonesia",
                unit = "pcs",
                cargoId = "CRG001"
            ),
            RecentItem(
                id = 2,
                name = "Dell Monitor 24 inch",
                stock = 8,
                date = "6 Februari 2025",
                type = "OUT",
                supplier = "",
                unit = "pcs",
                cargoId = "CRG002"
            ),
            RecentItem(
                id = 3,
                name = "Logitech Mouse Wireless",
                stock = 25,
                date = "5 Februari 2025",
                type = "IN",
                supplier = "PT. Logitech",
                unit = "pcs",
                cargoId = "CRG003"
            ),
            RecentItem(
                id = 4,
                name = "HP Printer LaserJet",
                stock = 3,
                date = "4 Februari 2025",
                type = "OUT",
                supplier = "",
                unit = "unit",
                cargoId = "CRG004"
            )
        )

        recentItemsAdapter.submitList(recentItems)
    }

    private fun onRecentItemClick(item: RecentItem) {
        // Handle recent item click
        // You can navigate to item details based on the type or cargoId
        when (item.type) {
            "IN" -> {
                // Navigate to incoming goods detail
                // findNavController().navigate(
                //     R.id.action_home_to_incoming_detail,
                //     bundleOf("cargoId" to item.cargoId)
                // )
            }
            "OUT" -> {
                // Navigate to outgoing goods detail
                // findNavController().navigate(
                //     R.id.action_home_to_outgoing_detail,
                //     bundleOf("cargoId" to item.cargoId)
                // )
            }
        }
    }

    // Helper function to update counts from Firebase
    private fun updateDashboardCounts() {
        // This would be called to update the dashboard counts
        // Example implementation:

        // Count incoming goods
        // firebaseDatabase.reference.child("cargo_in")
        //     .addListenerForSingleValueEvent(object : ValueEventListener {
        //         override fun onDataChange(snapshot: DataSnapshot) {
        //             val count = snapshot.childrenCount.toString()
        //             tvBarangMasukCount.text = count
        //         }
        //         override fun onCancelled(error: DatabaseError) {}
        //     })

        // Count outgoing goods
        // firebaseDatabase.reference.child("cargo_out")
        //     .addListenerForSingleValueEvent(object : ValueEventListener {
        //         override fun onDataChange(snapshot: DataSnapshot) {
        //             val count = snapshot.childrenCount.toString()
        //             tvBarangKeluarCount.text = count
        //         }
        //         override fun onCancelled(error: DatabaseError) {}
        //     })
    }

    // Helper function to load recent items from Firebase
    private fun loadRecentItemsFromFirebase() {
        // Load recent items from Firebase, limit to recent 5-10 items
        // to keep the ScrollView performant

        // firebaseDatabase.reference.child("cargo_in")
        //     .orderByChild("tanggalMasuk")
        //     .limitToLast(5)
        //     .addValueEventListener(object : ValueEventListener {
        //         override fun onDataChange(snapshot: DataSnapshot) {
        //             val recentItems = mutableListOf<RecentItem>()
        //
        //             // Add incoming items
        //             for (childSnapshot in snapshot.children) {
        //                 val cargoIn = CargoIn.fromSnapshot(childSnapshot)
        //                 cargoIn?.let {
        //                     recentItems.add(RecentItem.fromCargoIn(it))
        //                 }
        //             }
        //
        //             // You can also load outgoing items and merge them
        //             // Then sort by date and take the most recent ones
        //
        //             recentItemsAdapter.submitList(recentItems.sortedByDescending { it.date })
        //         }
        //
        //         override fun onCancelled(error: DatabaseError) {
        //             // Handle error
        //         }
        //     })
    }

    /**
     * Returns a time-based greeting in Indonesian
     * Morning (05:00-11:59): Selamat Pagi
     * Afternoon (12:00-14:59): Selamat Siang
     * Evening (15:00-18:59): Selamat Sore
     * Night (19:00-04:59): Selamat Malam
     */
    private fun getTimeBasedGreeting(): String {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        return when (currentHour) {
            in 5..11 -> "Selamat Pagi,"
            in 12..14 -> "Selamat Siang,"
            in 15..18 -> "Selamat Sore,"
            else -> "Selamat Malam," // 19-04 (night/late night)
        }
    }

    /**
     * Alternative version with English greetings
     * Uncomment this if you prefer English greetings
     */
    /*
    private fun getTimeBasedGreeting(): String {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        return when (currentHour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night," // 21-04 (night/late night)
        }
    }
    */
}