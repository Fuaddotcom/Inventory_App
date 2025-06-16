// com/TI23B1/inventoryapp/MainViewPagerAdapter.kt
package com.TI23B1.inventoryapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.TI23B1.inventoryapp.fragments.HistoryFragment
import com.TI23B1.inventoryapp.fragments.HomeFragment
// Assuming you have these fragments:
// import com.TI23B1.inventoryapp.fragments.InventoryFragment
// import com.TI23B1.inventoryapp.fragments.ProfileFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // Define your fragments in the order they should appear in the ViewPager
    // This order should match the order of items in your bottom_navigation_menu.xml
    private val fragments: List<Fragment> = listOf(
        HomeFragment(),        // Corresponds to R.id.nav_home
        HistoryFragment(),     // Corresponds to R.id.nav_history
        // InventoryFragment(), // Assuming R.id.nav_inventory
        // ProfileFragment()    // Assuming R.id.nav_profile
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}