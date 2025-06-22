package com.TI23B1.inventoryapp // Make sure this matches your package

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.TI23B1.inventoryapp.R

class DetailBarangActivity : AppCompatActivity() {

    override fun onCreate(saved_instance_state: Bundle?) {
        super.onCreate(saved_instance_state)
        setContentView(R.layout.activity_detail_barang) // Your activity's layout with FrameLayout

        // Check if the fragment needs to be added (prevents recreation on config changes)
        if (saved_instance_state == null) {
            // Determine what kind of detail to show based on intent extras or other logic
            // For example, if this activity is always for "Detail Barang"
            val item_name = "Axioo NoteBook Hype 1"
            val kode = "G DQ18492"
            val supplier = "PT. Jovan Julian"
            val tanggal_beli = "4 Februari 2025"
            val jumlah_stok = "241"
            val show_supplier = true
            val show_jumlah_stok = true

            // Create the fragment instance using the newInstance factory method
            val fragment = ItemDetailFragment.newInstance(
                item_name, kode, supplier, tanggal_beli, jumlah_stok, show_supplier, show_jumlah_stok
            )

            // Add the fragment to the container
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
}