package com.TI23B1.inventoryapp // Make sure this matches your package

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast // Added for example Toast
import com.TI23B1.inventoryapp.R

class ItemDetailFragment : Fragment() {

    // Define constants for Bundle keys (Kotlin way: companion object, typically UPPER_SNAKE_CASE for const val)
    companion object {
        const val ARG_ITEM_NAME = "item_name"
        const val ARG_KODE = "kode"
        const val ARG_SUPPLIER = "supplier"
        const val ARG_TANGGAL_BELI = "tanggal_beli"
        const val ARG_JUMLAH_STOK = "jumlah_stok"
        const val ARG_SHOW_SUPPLIER = "show_supplier"
        const val ARG_SHOW_JUMLAH_STOK = "show_jumlah_stok"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param item_name Parameter 1.
         * @param kode Parameter 2.
         * @return A new instance of fragment ItemDetailFragment.
         */
        fun newInstance(
            item_name: String,
            kode: String,
            supplier: String?, // Supplier can be null
            tanggal_beli: String,
            jumlah_stok: String,
            show_supplier: Boolean,
            show_jumlah_stok: Boolean
        ) = ItemDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ITEM_NAME, item_name)
                putString(ARG_KODE, kode)
                putString(ARG_SUPPLIER, supplier)
                putString(ARG_TANGGAL_BELI, tanggal_beli)
                putString(ARG_JUMLAH_STOK, jumlah_stok)
                putBoolean(ARG_SHOW_SUPPLIER, show_supplier)
                putBoolean(ARG_SHOW_JUMLAH_STOK, show_jumlah_stok)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        saved_instance_state: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_detail, container, false)
    }

    override fun onViewCreated(view: View, saved_instance_state: Bundle?) {
        super.onViewCreated(view, saved_instance_state)

        // Get references to your TextViews and Layouts using findViewById
        val product_name_text_view: TextView = view.findViewById(R.id.productNameTextView)
        val back_icon: ImageView = view.findViewById(R.id.backIcon)
        val edit_icon: ImageView = view.findViewById(R.id.editIcon)

        val kode_value_text_view: TextView = view.findViewById(R.id.kodeValueTextView)
        val supplier_value_text_view: TextView = view.findViewById(R.id.supplierValueTextView)
        val tanggal_beli_value_text_view: TextView = view.findViewById(R.id.tanggalBeliValueTextView)
        val jumlah_stok_value_text_view: TextView = view.findViewById(R.id.jumlahStokValueTextView)

        val supplier_row: LinearLayout = view.findViewById(R.id.supplierRow)
        val jumlah_stok_row: LinearLayout = view.findViewById(R.id.jumlahStokRow)


        // Populate data from arguments
        arguments?.let { args -> // Use safe call operator `?.let` for nullable arguments
            val item_name = args.getString(ARG_ITEM_NAME)
            val kode = args.getString(ARG_KODE)
            val supplier = args.getString(ARG_SUPPLIER)
            val tanggal_beli = args.getString(ARG_TANGGAL_BELI)
            val jumlah_stok = args.getString(ARG_JUMLAH_STOK)
            val show_supplier = args.getBoolean(ARG_SHOW_SUPPLIER, true) // Default true
            val show_jumlah_stok = args.getBoolean(ARG_SHOW_JUMLAH_STOK, true) // Default true

            product_name_text_view.text = item_name
            kode_value_text_view.text = kode
            tanggal_beli_value_text_view.text = tanggal_beli

            // Set supplier data and visibility
            if (show_supplier) {
                supplier_row.visibility = View.VISIBLE
                supplier_value_text_view.text = supplier
            } else {
                supplier_row.visibility = View.GONE // Hide the entire row
            }

            // Set quantity data and visibility
            if (show_jumlah_stok) {
                jumlah_stok_row.visibility = View.VISIBLE
                jumlah_stok_value_text_view.text = jumlah_stok
            } else {
                jumlah_stok_row.visibility = View.GONE // Hide the entire row
            }
        }

        // Handle button/icon clicks
        back_icon.setOnClickListener {
            activity?.onBackPressed()
        }

        edit_icon.setOnClickListener {
            Toast.makeText(context, "Edit clicked!", Toast.LENGTH_SHORT).show()
            // Handle edit action here, e.g., launch an edit activity/fragment
        }
    }
}